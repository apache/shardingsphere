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
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineIndexMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Standard pipeline table metadata loader.
 */
@RequiredArgsConstructor
@Slf4j
public final class StandardPipelineTableMetaDataLoader implements PipelineTableMetaDataLoader {
    
    // It doesn't support ShardingSphereDataSource
    private final PipelineDataSourceWrapper dataSource;
    
    private final Map<TableName, PipelineTableMetaData> tableMetaDataMap = new ConcurrentHashMap<>();
    
    @Override
    public PipelineTableMetaData getTableMetaData(final String schemaName, final String tableName) {
        PipelineTableMetaData result = tableMetaDataMap.get(new TableName(tableName));
        if (null != result) {
            return result;
        }
        try {
            loadTableMetaData(schemaName, tableName);
        } catch (final SQLException ex) {
            throw new RuntimeException(String.format("Load metadata for schema '%s' and table '%s' failed", schemaName, tableName), ex);
        }
        result = tableMetaDataMap.get(new TableName(tableName));
        if (null == result) {
            log.warn("getTableMetaData, can not load metadata for table '{}'", tableName);
        }
        return result;
    }
    
    private void loadTableMetaData(final String schemaName, final String tableNamePattern) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            long startMillis = System.currentTimeMillis();
            String schemaNameFinal = isSchemaAvailable() ? schemaName : null;
            Map<TableName, PipelineTableMetaData> tableMetaDataMap = loadTableMetaData0(connection, schemaNameFinal, tableNamePattern);
            log.info("loadTableMetaData, schemaNameFinal={}, tableNamePattern={}, result={}, cost time={} ms",
                    schemaNameFinal, tableNamePattern, tableMetaDataMap, System.currentTimeMillis() - startMillis);
            this.tableMetaDataMap.putAll(tableMetaDataMap);
        }
    }
    
    private boolean isSchemaAvailable() {
        return DatabaseTypeFactory.getInstance(dataSource.getDatabaseType().getType()).isSchemaAvailable();
    }
    
    private Map<TableName, PipelineTableMetaData> loadTableMetaData0(final Connection connection, final String schemaName, final String tableNamePattern) throws SQLException {
        Map<String, Map<String, PipelineColumnMetaData>> tablePipelineColumnMetaDataMap = new LinkedHashMap<>();
        Map<String, Map<String, Collection<String>>> uniqueKeysMap = new HashMap<>();
        Map<String, Set<String>> primaryKeysMap = new HashMap<>();
        List<String> tableNames = new ArrayList<>();
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), schemaName, tableNamePattern, null)) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                tableNames.add(tableName);
                primaryKeysMap.put(tableName, loadPrimaryKeys(connection, schemaName, tableName));
                uniqueKeysMap.put(tableName, loadUniqueIndexesOfTable(connection, schemaName, tableName));
            }
        }
        for (String each : tableNames) {
            try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), schemaName, each, "%")) {
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
                    Set<String> primaryKeys = primaryKeysMap.getOrDefault(each, Collections.emptySet());
                    boolean primaryKey = primaryKeys.contains(columnName);
                    boolean isNullable = "YES".equals(resultSet.getString("IS_NULLABLE"));
                    Map<String, Collection<String>> uniqueKeys = uniqueKeysMap.getOrDefault(tableName, Collections.emptyMap());
                    boolean isUniqueKey = primaryKey || uniqueKeys.values().stream().anyMatch(names -> names.contains(columnName));
                    PipelineColumnMetaData columnMetaData = new PipelineColumnMetaData(ordinalPosition, columnName, dataType, dataTypeName, isNullable, primaryKey, isUniqueKey);
                    columnMetaDataMap.put(columnName, columnMetaData);
                }
            }
        }
        Map<TableName, PipelineTableMetaData> result = new LinkedHashMap<>();
        for (Entry<String, Map<String, PipelineColumnMetaData>> entry : tablePipelineColumnMetaDataMap.entrySet()) {
            String tableName = entry.getKey();
            Map<String, PipelineColumnMetaData> metaDataMap = tablePipelineColumnMetaDataMap.get(tableName);
            Map<String, Collection<String>> uniqueKeys = uniqueKeysMap.getOrDefault(tableName, Collections.emptyMap());
            Collection<PipelineIndexMetaData> uniqueIndexMetaData = uniqueKeys.entrySet().stream()
                    .map(each -> new PipelineIndexMetaData(each.getKey(), each.getValue().stream().map(metaDataMap::get).collect(Collectors.toList()))).collect(Collectors.toList());
            result.put(new TableName(tableName), new PipelineTableMetaData(tableName, entry.getValue(), uniqueIndexMetaData));
        }
        return result;
    }
    
    private Map<String, Collection<String>> loadUniqueIndexesOfTable(final Connection connection, final String schemaName, final String tableName) throws SQLException {
        Map<String, SortedMap<Short, String>> orderedColumnsOfIndexes = new LinkedHashMap<>();
        try (ResultSet resultSet = connection.getMetaData().getIndexInfo(connection.getCatalog(), schemaName, tableName, true, false)) {
            while (resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME");
                if (null == indexName) {
                    continue;
                }
                orderedColumnsOfIndexes.computeIfAbsent(indexName, unused -> new TreeMap<>()).put(resultSet.getShort("ORDINAL_POSITION"), resultSet.getString("COLUMN_NAME"));
            }
        }
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        for (Entry<String, SortedMap<Short, String>> each : orderedColumnsOfIndexes.entrySet()) {
            Collection<String> columnNames = result.computeIfAbsent(each.getKey(), unused -> new LinkedList<>());
            columnNames.addAll(each.getValue().values());
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
}
