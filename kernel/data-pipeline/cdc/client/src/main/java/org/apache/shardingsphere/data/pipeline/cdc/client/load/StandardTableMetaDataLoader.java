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

package org.apache.shardingsphere.data.pipeline.cdc.client.load;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Standard table meta data loader.
 */
@RequiredArgsConstructor
@Slf4j
public final class StandardTableMetaDataLoader {
    
    private final Connection connection;
    
    private final Map<String, StandardTableMetaData> tableMetaDataMap = new ConcurrentHashMap<>();
    
    /**
     * Get table meta data.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return table meta data
     */
    public StandardTableMetaData getTableMetaData(final String schemaName, final String tableName) {
        StandardTableMetaData result = tableMetaDataMap.get(tableName);
        if (null != result) {
            return result;
        }
        try {
            loadTableMetaData(schemaName, tableName);
        } catch (final SQLException ex) {
            throw new RuntimeException(String.format("Load meta data for schema '%s' and table '%s' failed", schemaName, tableName), ex);
        }
        result = tableMetaDataMap.get(tableName);
        if (null == result) {
            log.warn("getTableMetaData, can not load meta data for table '{}'", tableName);
        }
        return result;
    }
    
    private void loadTableMetaData(final String schemaName, final String tableNamePattern) throws SQLException {
        Map<String, StandardTableMetaData> tableMetaDataMap = loadTableMetaData0(connection, schemaName, tableNamePattern);
        this.tableMetaDataMap.putAll(tableMetaDataMap);
    }
    
    private Map<String, StandardTableMetaData> loadTableMetaData0(final Connection connection, final String schemaName, final String tableNamePattern) throws SQLException {
        Collection<String> tableNames = new LinkedList<>();
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), schemaName, tableNamePattern, null)) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                tableNames.add(tableName);
            }
        }
        Map<String, StandardTableMetaData> result = new LinkedHashMap<>();
        for (String each : tableNames) {
            Set<String> primaryKeys = loadPrimaryKeys(connection, schemaName, each);
            Map<String, Collection<String>> uniqueKeys = loadUniqueIndexesOfTable(connection, schemaName, each);
            Map<String, StandardColumnMetaData> columnMetaDataMap = new LinkedHashMap<>();
            try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), schemaName, each, "%")) {
                while (resultSet.next()) {
                    int ordinalPosition = resultSet.getInt("ORDINAL_POSITION");
                    String columnName = resultSet.getString("COLUMN_NAME");
                    if (columnMetaDataMap.containsKey(columnName)) {
                        continue;
                    }
                    int dataType = resultSet.getInt("DATA_TYPE");
                    String dataTypeName = resultSet.getString("TYPE_NAME");
                    boolean primaryKey = primaryKeys.contains(columnName);
                    boolean isNullable = "YES".equals(resultSet.getString("IS_NULLABLE"));
                    boolean isUniqueKey = primaryKey || uniqueKeys.values().stream().anyMatch(names -> names.contains(columnName));
                    StandardColumnMetaData columnMetaData = new StandardColumnMetaData(ordinalPosition, columnName, dataType, dataTypeName, isNullable, primaryKey, isUniqueKey);
                    columnMetaDataMap.put(columnName, columnMetaData);
                }
            }
            result.put(each, new StandardTableMetaData(each, columnMetaDataMap));
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
        for (Entry<String, SortedMap<Short, String>> entry : orderedColumnsOfIndexes.entrySet()) {
            Collection<String> columnNames = result.computeIfAbsent(entry.getKey(), unused -> new LinkedList<>());
            columnNames.addAll(entry.getValue().values());
        }
        return result;
    }
    
    private Set<String> loadPrimaryKeys(final Connection connection, final String schemaName, final String tableName) throws SQLException {
        Set<String> result = new LinkedHashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(connection.getCatalog(), schemaName, tableName)) {
            while (resultSet.next()) {
                result.add(resultSet.getString("COLUMN_NAME"));
            }
        }
        return result;
    }
}
