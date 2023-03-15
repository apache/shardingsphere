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

package org.apache.shardingsphere.data.pipeline.cdc.client.importer;

import com.google.common.base.Strings;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.client.load.StandardColumnMetaData;
import org.apache.shardingsphere.data.pipeline.cdc.client.load.StandardTableMetaData;
import org.apache.shardingsphere.data.pipeline.cdc.client.load.StandardTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.ImportDataSourceParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.sqlbuilder.SQLBuilder;
import org.apache.shardingsphere.data.pipeline.cdc.client.sqlbuilder.SQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.AnyValueConvert;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record.MetaData;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.TableColumn;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data source importer.
 */
@Slf4j
public final class DataSourceImporter implements Importer {
    
    private final Connection connection;
    
    private final SQLBuilder sqlBuilder;
    
    private final Map<String, StandardTableMetaData> tableMetaDataMap;
    
    private final StandardTableMetaDataLoader loader;
    
    public DataSourceImporter(final String databaseType, final ImportDataSourceParameter dataSourceParam) {
        String jdbcUrl = Optional.ofNullable(dataSourceParam.getJdbcUrl()).orElseThrow(() -> new IllegalArgumentException("jdbcUrl is null"));
        String username = Optional.ofNullable(dataSourceParam.getUsername()).orElseThrow(() -> new IllegalArgumentException("username is null"));
        String password = Optional.ofNullable(dataSourceParam.getPassword()).orElseThrow(() -> new IllegalArgumentException("password is null"));
        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
        sqlBuilder = SQLBuilderFactory.getSQLBuilder(databaseType);
        tableMetaDataMap = new ConcurrentHashMap<>();
        loader = new StandardTableMetaDataLoader(connection);
    }
    
    @Override
    public void write(final Record record) throws SQLException {
        MetaData metaData = record.getMetaData();
        StandardTableMetaData tableMetaData = loadTableMetaData(metaData.getSchema(), metaData.getTableName());
        if (null == tableMetaData) {
            throw new RuntimeException("load table meta data failed");
        }
        List<String> uniqueKeyNames = tableMetaData.getPrimaryKeyColumns().isEmpty() ? tableMetaData.getUniqueKeyColumns() : tableMetaData.getPrimaryKeyColumns();
        Optional<String> sqlOptional = buildSQL(record, uniqueKeyNames);
        if (!sqlOptional.isPresent()) {
            log.error("build sql failed, record {}", record);
            throw new RuntimeException("build sql failed");
        }
        String sql = sqlOptional.get();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            Map<String, TableColumn> afterMap = new LinkedHashMap<>(record.getBeforeList().size(), 1);
            record.getAfterList().forEach(each -> afterMap.put(each.getName(), each));
            Map<String, Any> beforeMap = new LinkedHashMap<>(record.getBeforeList().size(), 1);
            record.getBeforeList().forEach(each -> beforeMap.put(each.getName(), each.getValue()));
            List<String> conditionColumnNames = beforeMap.keySet().containsAll(uniqueKeyNames) ? uniqueKeyNames : new ArrayList<>(beforeMap.keySet());
            switch (record.getDataChangeType()) {
                case INSERT:
                    for (int i = 0; i < record.getAfterCount(); i++) {
                        TableColumn tableColumn = record.getAfterList().get(i);
                        preparedStatement.setObject(i + 1, convertValueFromAny(tableMetaData, tableColumn));
                    }
                    break;
                case UPDATE:
                    for (int i = 0; i < record.getAfterCount(); i++) {
                        TableColumn tableColumn = record.getAfterList().get(i);
                        preparedStatement.setObject(i + 1, convertValueFromAny(tableMetaData, tableColumn));
                    }
                    for (int i = 0; i < conditionColumnNames.size(); i++) {
                        TableColumn tableColumn = record.getAfterList().get(i);
                        preparedStatement.setObject(afterMap.size() + i + 1, convertValueFromAny(tableMetaData, tableColumn));
                    }
                    int updateCount = preparedStatement.executeUpdate();
                    if (1 != updateCount) {
                        log.warn("executeUpdate failed, updateCount={}, updateSql={}, updatedColumns={}, conditionColumns={}", updateCount, sql, afterMap.keySet(), conditionColumnNames);
                    }
                    break;
                case DELETE:
                    for (int i = 0; i < conditionColumnNames.size(); i++) {
                        preparedStatement.setObject(i + 1, convertValueFromAny(tableMetaData, afterMap.get(conditionColumnNames.get(i))));
                    }
                    preparedStatement.execute();
                    break;
                default:
            }
            preparedStatement.execute();
        }
    }
    
    private String buildKey(final String schema, final String tableName) {
        return schema.isEmpty() ? tableName : String.join(".", schema, tableName);
    }
    
    private StandardTableMetaData loadTableMetaData(final String schema, final String tableName) {
        StandardTableMetaData result = tableMetaDataMap.get(buildKey(schema, tableName));
        if (null != result) {
            return result;
        }
        result = loader.getTableMetaData(Strings.emptyToNull(schema), tableName);
        tableMetaDataMap.put(buildKey(schema, tableName), result);
        return result;
    }
    
    private Optional<String> buildSQL(final Record record, final List<String> uniqueKeyNames) {
        switch (record.getDataChangeType()) {
            case INSERT:
                return Optional.ofNullable(sqlBuilder.buildInsertSQL(record, uniqueKeyNames));
            case UPDATE:
                return Optional.ofNullable(sqlBuilder.buildUpdateSQL(record, uniqueKeyNames));
            case DELETE:
                return Optional.ofNullable(sqlBuilder.buildDeleteSQL(record, uniqueKeyNames));
            default:
                return Optional.empty();
        }
    }
    
    private Object convertValueFromAny(final StandardTableMetaData tableMetaData, final TableColumn tableColumn) {
        StandardColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(tableColumn.getName());
        Object object;
        try {
            object = AnyValueConvert.convertToObject(tableColumn.getValue());
        } catch (final InvalidProtocolBufferException ex) {
            log.error("invalid protocol message value: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
        if (null == object) {
            return null;
        }
        switch (columnMetaData.getDataType()) {
            case Types.TIME:
                if ("TIME".equalsIgnoreCase(columnMetaData.getDataTypeName())) {
                    // Time.valueOf() will lose nanos
                    return LocalTime.ofNanoOfDay((Long) object);
                }
                return object;
            case Types.DATE:
                if ("DATE".equalsIgnoreCase(columnMetaData.getDataTypeName())) {
                    LocalDate localDate = LocalDate.ofEpochDay((Long) object);
                    return Date.valueOf(localDate);
                }
                return object;
            default:
                return object;
        }
    }
    
    @Override
    public void close() throws Exception {
        connection.close();
    }
}
