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

package org.apache.shardingsphere.test.e2e.operation.pipeline.cases.cdc;

import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.ProtobufAnyValueConverter;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record.DataChangeType;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record.MetaData;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.TableColumn;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.SQLBuilderUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public final class DataSourceRecordConsumer implements Consumer<List<Record>> {
    
    private final DataSource dataSource;
    
    private final StandardPipelineTableMetaDataLoader loader;
    
    private final Map<String, PipelineTableMetaData> tableMetaDataMap = new ConcurrentHashMap<>();
    
    public DataSourceRecordConsumer(final DataSource dataSource, final DatabaseType databaseType) {
        this.dataSource = dataSource;
        loader = new StandardPipelineTableMetaDataLoader(new PipelineDataSource(dataSource, databaseType));
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
            Map<String, List<Record>> recordsMap = new HashMap<>(records.size(), 1F);
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
            write(each, connection, records.size() < 5);
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
    
    private void write(final Record ingestedRecord, final Connection connection, final boolean printSQL) throws SQLException {
        String sql = buildSQL(ingestedRecord);
        MetaData metaData = ingestedRecord.getMetaData();
        PipelineTableMetaData tableMetaData = loadTableMetaData(metaData.getSchema(), metaData.getTable());
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int updateCount;
            switch (ingestedRecord.getDataChangeType()) {
                case INSERT:
                    for (int i = 0; i < ingestedRecord.getAfterCount(); i++) {
                        TableColumn tableColumn = ingestedRecord.getAfterList().get(i);
                        preparedStatement.setObject(i + 1, convertValueFromAny(tableMetaData, tableColumn));
                    }
                    updateCount = preparedStatement.executeUpdate();
                    if (1 != updateCount || printSQL) {
                        log.info("Execute insert, update count: {}, sql: {}, values: {}", updateCount, sql,
                                ingestedRecord.getAfterList().stream().map(each -> convertValueFromAny(tableMetaData, each)).collect(Collectors.toList()));
                    }
                    break;
                case UPDATE:
                    for (int i = 0; i < ingestedRecord.getAfterCount(); i++) {
                        TableColumn tableColumn = ingestedRecord.getAfterList().get(i);
                        preparedStatement.setObject(i + 1, convertValueFromAny(tableMetaData, tableColumn));
                    }
                    preparedStatement.setObject(ingestedRecord.getAfterCount() + 1, convertValueFromAny(tableMetaData, getOrderIdTableColumn(ingestedRecord.getAfterList())));
                    updateCount = preparedStatement.executeUpdate();
                    if (1 != updateCount || printSQL) {
                        log.info("Execute update, update count: {}, sql: {}, values: {}", updateCount, sql,
                                ingestedRecord.getAfterList().stream().map(each -> convertValueFromAny(tableMetaData, each)).collect(Collectors.toList()));
                    }
                    break;
                case DELETE:
                    Object orderId = convertValueFromAny(tableMetaData, getOrderIdTableColumn(ingestedRecord.getBeforeList()));
                    preparedStatement.setObject(1, orderId);
                    updateCount = preparedStatement.executeUpdate();
                    if (1 != updateCount || printSQL) {
                        log.info("Execute delete, update count: {}, sql: {}, order_id: {}", updateCount, sql, orderId);
                    }
                    break;
                default:
            }
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
                throw new UnsupportedOperationException("");
        }
    }
    
    private TableColumn getOrderIdTableColumn(final List<TableColumn> tableColumns) {
        return tableColumns.stream().filter(each -> "order_id".equals(each.getName())).findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("No primary key found in the t_order"));
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
