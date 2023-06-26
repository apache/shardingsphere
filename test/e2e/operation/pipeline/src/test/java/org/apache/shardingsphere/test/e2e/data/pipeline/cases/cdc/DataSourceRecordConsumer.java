/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.e2e.data.pipeline.cases.cdc;

import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.ProtobufAnyValueConverter;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record.DataChangeType;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record.MetaData;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.TableColumn;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.SQLBuilderUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public final class DataSourceRecordConsumer implements Consumer<List<Record>> {
    
    private final DataSource dataSource;
    
    private final Map<String, PipelineTableMetaData> tableMetaDataMap;
    
    private final StandardPipelineTableMetaDataLoader loader;
    
    public DataSourceRecordConsumer(final DataSource dataSource, final DatabaseType databaseType) {
        this.dataSource = dataSource;
        tableMetaDataMap = new ConcurrentHashMap<>();
        loader = new StandardPipelineTableMetaDataLoader(new PipelineDataSourceWrapper(dataSource, databaseType));
    }
    
    @Override
    public void accept(final List<Record> records) {
        log.info("Records count: {}", records.size());
        log.debug("Records: {}", records);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            processRecords(records, connection);
            connection.commit();
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private void processRecords(final List<Record> records, final Connection connection) throws SQLException {
        long insertCount = records.stream().filter(each -> DataChangeType.INSERT == each.getDataChangeType()).count();
        if (insertCount == records.size()) {
            Map<String, List<Record>> recordsMap = new HashMap<>();
            for (Record each : records) {
                String key = buildTableNameWithSchema(each.getMetaData().getTable(), each.getMetaData().getSchema());
                recordsMap.computeIfAbsent(key, ignored -> new LinkedList<>()).add(each);
            }
            for (List<Record> each : recordsMap.values()) {
                batchInsertRecords(each, connection);
            }
            return;
        }
        for (Record each : records) {
            write(each, connection);
        }
    }
    
    private void batchInsertRecords(final List<Record> records, final Connection connection) throws SQLException {
        Record firstRecord = records.get(0);
        MetaData metaData = firstRecord.getMetaData();
        PipelineTableMetaData tableMetaData = loadTableMetaData(metaData.getSchema(), metaData.getTable());
        List<String> columnNames = firstRecord.getAfterList().stream().map(TableColumn::getName).collect(Collectors.toList());
        String tableName = buildTableNameWithSchema(metaData.getSchema(), metaData.getTable());
        String insertSQL = SQLBuilderUtils.buildInsertSQL(columnNames, tableName);
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            for (Record each : records) {
                List<TableColumn> tableColumns = each.getAfterList();
                for (int i = 0; i < tableColumns.size(); i++) {
                    preparedStatement.setObject(i + 1, convertValueFromAny(tableMetaData, tableColumns.get(i)));
                }
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    private void write(final Record ingestedRecord, final Connection connection) throws SQLException {
        String sql = buildSQL(ingestedRecord);
        MetaData metaData = ingestedRecord.getMetaData();
        PipelineTableMetaData tableMetaData = loadTableMetaData(metaData.getSchema(), metaData.getTable());
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            Map<String, TableColumn> afterMap = new LinkedHashMap<>(ingestedRecord.getBeforeList().size(), 1F);
            ingestedRecord.getAfterList().forEach(each -> afterMap.put(each.getName(), each));
            switch (ingestedRecord.getDataChangeType()) {
                case INSERT:
                    for (int i = 0; i < ingestedRecord.getAfterCount(); i++) {
                        TableColumn tableColumn = ingestedRecord.getAfterList().get(i);
                        preparedStatement.setObject(i + 1, convertValueFromAny(tableMetaData, tableColumn));
                    }
                    break;
                case UPDATE:
                    for (int i = 0; i < ingestedRecord.getAfterCount(); i++) {
                        TableColumn tableColumn = ingestedRecord.getAfterList().get(i);
                        preparedStatement.setObject(i + 1, convertValueFromAny(tableMetaData, tableColumn));
                    }
                    preparedStatement.setObject(ingestedRecord.getAfterCount() + 1, convertValueFromAny(tableMetaData, afterMap.get("order_id")));
                    int updateCount = preparedStatement.executeUpdate();
                    if (1 != updateCount) {
                        log.warn("executeUpdate failed, updateCount={}, updateSql={}, updatedColumns={}", updateCount, sql, afterMap.keySet());
                    }
                    break;
                case DELETE:
                    TableColumn orderId = ingestedRecord.getBeforeList().stream().filter(each -> "order_id".equals(each.getName())).findFirst()
                            .orElseThrow(() -> new UnsupportedOperationException("No primary key found in the t_order"));
                    preparedStatement.setObject(1, convertValueFromAny(tableMetaData, orderId));
                    preparedStatement.execute();
                    break;
                default:
            }
            preparedStatement.execute();
        }
    }
    
    private PipelineTableMetaData loadTableMetaData(final String schemaName, final String tableName) {
        PipelineTableMetaData result = tableMetaDataMap.get(buildTableNameWithSchema(schemaName, tableName));
        if (null != result) {
            return result;
        }
        result = loader.getTableMetaData(Strings.emptyToNull(schemaName), tableName);
        tableMetaDataMap.put(buildTableNameWithSchema(schemaName, tableName), result);
        return result;
    }
    
    private String buildTableNameWithSchema(final String schema, final String tableName) {
        return schema.isEmpty() ? tableName : String.join(".", schema, tableName);
    }
    
    private String buildSQL(final Record ingestedRecord) {
        List<String> columnNames = ingestedRecord.getAfterList().stream().map(TableColumn::getName).collect(Collectors.toList());
        MetaData metaData = ingestedRecord.getMetaData();
        String tableName = buildTableNameWithSchema(metaData.getSchema(), metaData.getTable());
        switch (ingestedRecord.getDataChangeType()) {
            case INSERT:
                return SQLBuilderUtils.buildInsertSQL(columnNames, tableName);
            case UPDATE:
                return SQLBuilderUtils.buildUpdateSQL(columnNames, tableName, "?");
            case DELETE:
                return SQLBuilderUtils.buildDeleteSQL(tableName, "order_id");
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    private Object convertValueFromAny(final PipelineTableMetaData tableMetaData, final TableColumn tableColumn) {
        PipelineColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(tableColumn.getName());
        Object result;
        try {
            result = ProtobufAnyValueConverter.convertToObject(tableColumn.getValue());
        } catch (final InvalidProtocolBufferException ex) {
            log.error("invalid protocol message value: {}", tableColumn.getValue());
            throw new RuntimeException(ex);
        }
        if (null == result) {
            return null;
        }
        switch (columnMetaData.getDataType()) {
            case Types.TIME:
                if ("TIME".equalsIgnoreCase(columnMetaData.getDataTypeName())) {
                    // Time.valueOf() will lose nanos
                    return LocalTime.ofNanoOfDay((Long) result);
                }
                return result;
            case Types.DATE:
                if ("DATE".equalsIgnoreCase(columnMetaData.getDataTypeName())) {
                    LocalDate localDate = LocalDate.ofEpochDay((Long) result);
                    return Date.valueOf(localDate);
                }
                return result;
            default:
                return result;
        }
    }
}
