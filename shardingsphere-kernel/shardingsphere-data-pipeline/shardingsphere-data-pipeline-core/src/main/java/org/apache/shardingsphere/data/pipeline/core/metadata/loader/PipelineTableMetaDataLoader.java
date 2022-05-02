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

package org.apache.shardingsphere.data.pipeline.core.metadata.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.metadata.TableName;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pipeline table metadata loader.
 */
@RequiredArgsConstructor
@Slf4j
// TODO PipelineTableMetaDataLoader SPI
public final class PipelineTableMetaDataLoader {
    
    // TODO it doesn't support ShardingSphereDataSource
    private final PipelineDataSourceWrapper dataSource;
    
    private final Map<TableName, PipelineTableMetaData> tableMetaDataMap = new ConcurrentHashMap<>();
    
    /**
     * Load table metadata.
     *
     * @param schemaName schema name
     * @param tableNamePattern table name pattern
     * @throws SQLException if loading failure
     */
    public void loadTableMetaData(final String schemaName, final String tableNamePattern) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            long startMillis = System.currentTimeMillis();
            Map<TableName, PipelineTableMetaData> tableMetaDataMap = loadTableMetaData0(connection, schemaName, tableNamePattern);
            log.info("loadTableMetaData, schemaName={}, tableNamePattern={}, result={}, cost time={} ms", schemaName, tableNamePattern, tableMetaDataMap, System.currentTimeMillis() - startMillis);
            this.tableMetaDataMap.putAll(tableMetaDataMap);
        }
    }
    
    private Map<TableName, PipelineTableMetaData> loadTableMetaData0(final Connection connection, final String schemaName, final String tableNamePattern) throws SQLException {
        Map<String, Map<String, PipelineColumnMetaData>> tablePipelineColumnMetaDataMap = new LinkedHashMap<>();
        try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), schemaName, tableNamePattern, "%")) {
            while (resultSet.next()) {
                int ordinalPosition = resultSet.getInt("ORDINAL_POSITION");
                String tableName = resultSet.getString("TABLE_NAME");
                Map<String, PipelineColumnMetaData> columnMetaDataMap = tablePipelineColumnMetaDataMap.computeIfAbsent(tableName, k -> new LinkedHashMap<>());
                String columnName = resultSet.getString("COLUMN_NAME");
                if (columnMetaDataMap.containsKey(columnName)) {
                    continue;
                }
                int dataType = resultSet.getInt("DATA_TYPE");
                String dataTypeName = resultSet.getString("TYPE_NAME");
                Set<String> primaryKeys;
                try {
                    primaryKeys = loadPrimaryKeys(connection, schemaName, tableName);
                } catch (final SQLException ex) {
                    log.error("loadPrimaryKeys failed, tableName={}", tableName);
                    throw ex;
                }
                boolean primaryKey = primaryKeys.contains(columnName);
                PipelineColumnMetaData columnMetaData = new PipelineColumnMetaData(ordinalPosition, columnName, dataType, dataTypeName, primaryKey);
                columnMetaDataMap.put(columnName, columnMetaData);
            }
        }
        Map<TableName, PipelineTableMetaData> result = new LinkedHashMap<>();
        for (Entry<String, Map<String, PipelineColumnMetaData>> entry : tablePipelineColumnMetaDataMap.entrySet()) {
            result.put(new TableName(entry.getKey()), new PipelineTableMetaData(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    private Set<String> loadPrimaryKeys(final Connection connection, final String schemaName, final String tableName) throws SQLException {
        Set<String> result = new LinkedHashSet<>();
        // TODO order primary keys
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(connection.getCatalog(), schemaName, tableName)) {
            while (resultSet.next()) {
                result.add(resultSet.getString("COLUMN_NAME"));
            }
        }
        return result;
    }
    
    /**
     * Get table metadata, load if it does not exist.
     *
     * @param schemaName schema name. nullable
     * @param tableName dedicated table name, not table name pattern
     * @return table metadata
     */
    public PipelineTableMetaData getTableMetaData(final String schemaName, final String tableName) {
        PipelineTableMetaData result = tableMetaDataMap.get(new TableName(tableName));
        if (null != result) {
            return result;
        }
        try {
            loadTableMetaData(schemaName, tableName);
        } catch (final SQLException ex) {
            throw new RuntimeException(String.format("Load metadata for table '%s' failed", tableName), ex);
        }
        result = tableMetaDataMap.get(new TableName(tableName));
        if (null == result) {
            log.warn("getTableMetaData, can not load metadata for table '{}'", tableName);
        }
        return result;
    }
}
