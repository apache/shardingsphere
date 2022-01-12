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

import lombok.extern.slf4j.Slf4j;
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

/**
 * Pipeline table meta data loader.
 */
@Slf4j
public final class PipelineTableMetaDataLoader {
    
    private final Map<String, PipelineTableMetaData> tableMetaDataMap;
    
    public PipelineTableMetaDataLoader(final Connection connection, final String tableNamePattern) throws SQLException {
        this.tableMetaDataMap = loadTableMetadataMap(connection, tableNamePattern);
    }
    
    private Map<String, PipelineTableMetaData> loadTableMetadataMap(final Connection connection, final String tableNamePattern) throws SQLException {
        Map<String, Map<String, PipelineColumnMetaData>> tablePipelineColumnMetaDataMap = new LinkedHashMap<>();
        // TODO if tableNamePattern is '%', it might return inconsistent result, actual table `t_order_2` may be return
        try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), null, tableNamePattern, "%")) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                Map<String, PipelineColumnMetaData> columnMetaDataMap = tablePipelineColumnMetaDataMap.computeIfAbsent(tableName, k -> new LinkedHashMap<>());
                String columnName = resultSet.getString("COLUMN_NAME");
                if (columnMetaDataMap.containsKey(columnName)) {
                    continue;
                }
                int dataType = resultSet.getInt("DATA_TYPE");
                Set<String> primaryKeys;
                try {
                    primaryKeys = loadPrimaryKeys(connection, tableName);
                } catch (final SQLException ex) {
                    log.error("loadPrimaryKeys failed, tableName={}", tableName);
                    throw ex;
                }
                boolean primaryKey = primaryKeys.contains(columnName);
                PipelineColumnMetaData columnMetaData = new PipelineColumnMetaData(columnName, dataType, primaryKey);
                columnMetaDataMap.put(columnName, columnMetaData);
            }
        }
        Map<String, PipelineTableMetaData> result = new LinkedHashMap<>();
        for (Entry<String, Map<String, PipelineColumnMetaData>> entry : tablePipelineColumnMetaDataMap.entrySet()) {
            result.put(entry.getKey(), new PipelineTableMetaData(entry.getKey(), entry.getValue()));
        }
        log.info("loadTableMetadataMap, result={}", result);
        return result;
    }
    
    private Set<String> loadPrimaryKeys(final Connection connection, final String tableName) throws SQLException {
        Set<String> result = new LinkedHashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(connection.getCatalog(), connection.getSchema(), tableName)) {
            while (resultSet.next()) {
                result.add(resultSet.getString("COLUMN_NAME"));
            }
        }
        return result;
    }
    
    /**
     * Get table metadata.
     *
     * @param tableName table name
     * @return table metadata
     */
    public PipelineTableMetaData getTableMetaData(final String tableName) {
        return tableMetaDataMap.get(tableName);
    }
}
