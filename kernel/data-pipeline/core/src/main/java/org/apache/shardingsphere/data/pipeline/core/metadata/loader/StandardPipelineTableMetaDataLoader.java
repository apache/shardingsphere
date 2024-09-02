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
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.DataConsistencyCheckUtils;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineIndexMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.caseinsensitive.CaseInsensitiveIdentifier;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Standard pipeline table meta data loader.
 */
@RequiredArgsConstructor
@Slf4j
public final class StandardPipelineTableMetaDataLoader implements PipelineTableMetaDataLoader {
    
    private final PipelineDataSource dataSource;
    
    private final Map<CaseInsensitiveIdentifier, PipelineTableMetaData> tableMetaDataMap = new ConcurrentHashMap<>();
    
    @Override
    public PipelineTableMetaData getTableMetaData(final String schemaName, final String tableName) {
        PipelineTableMetaData result = tableMetaDataMap.get(new CaseInsensitiveIdentifier(tableName));
        if (null != result) {
            return result;
        }
        try {
            loadTableMetaData(schemaName, tableName);
        } catch (final SQLException ex) {
            throw new PipelineInternalException(String.format("Load meta data for schema '%s' and table '%s' failed", schemaName, tableName), ex);
        }
        result = tableMetaDataMap.get(new CaseInsensitiveIdentifier(tableName));
        if (null == result) {
            log.warn("Can not load meta data for table '{}'", tableName);
        }
        return result;
    }
    
    private void loadTableMetaData(final String schemaName, final String tableNamePattern) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(dataSource.getDatabaseType()).getDialectDatabaseMetaData();
            Map<CaseInsensitiveIdentifier, PipelineTableMetaData> tableMetaDataMap = loadTableMetaData0(connection, dialectDatabaseMetaData.isSchemaAvailable() ? schemaName : null, tableNamePattern);
            this.tableMetaDataMap.putAll(tableMetaDataMap);
        }
    }
    
    private Map<CaseInsensitiveIdentifier, PipelineTableMetaData> loadTableMetaData0(final Connection connection, final String schemaName, final String tableNamePattern) throws SQLException {
        Collection<String> tableNames = new LinkedList<>();
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), schemaName, tableNamePattern, null)) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                tableNames.add(tableName);
            }
        }
        Map<CaseInsensitiveIdentifier, PipelineTableMetaData> result = new LinkedHashMap<>(tableNames.size(), 1F);
        for (String each : tableNames) {
            Collection<CaseInsensitiveIdentifier> primaryKeys = loadPrimaryKeys(connection, schemaName, each);
            Map<CaseInsensitiveIdentifier, Collection<CaseInsensitiveIdentifier>> uniqueKeys = loadUniqueIndexesOfTable(connection, schemaName, each);
            Map<CaseInsensitiveIdentifier, PipelineColumnMetaData> columnMetaDataMap = new LinkedHashMap<>();
            try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), schemaName, each, "%")) {
                while (resultSet.next()) {
                    int ordinalPosition = resultSet.getInt("ORDINAL_POSITION");
                    CaseInsensitiveIdentifier columnName = new CaseInsensitiveIdentifier(resultSet.getString("COLUMN_NAME"));
                    if (columnMetaDataMap.containsKey(columnName)) {
                        continue;
                    }
                    int dataType = resultSet.getInt("DATA_TYPE");
                    String dataTypeName = resultSet.getString("TYPE_NAME");
                    boolean primaryKey = primaryKeys.contains(columnName);
                    boolean isNullable = "YES".equals(resultSet.getString("IS_NULLABLE"));
                    boolean isUniqueKey = uniqueKeys.values().stream().anyMatch(names -> names.contains(columnName));
                    PipelineColumnMetaData columnMetaData = new PipelineColumnMetaData(ordinalPosition, columnName.toString(), dataType, dataTypeName, isNullable, primaryKey, isUniqueKey);
                    columnMetaDataMap.put(columnName, columnMetaData);
                }
            }
            Collection<PipelineIndexMetaData> uniqueIndexMetaData = uniqueKeys.entrySet().stream()
                    .map(entry -> new PipelineIndexMetaData(entry.getKey(), entry.getValue().stream().map(columnMetaDataMap::get).collect(Collectors.toList()),
                            DataConsistencyCheckUtils.compareLists(primaryKeys, entry.getValue())))
                    .collect(Collectors.toList());
            result.put(new CaseInsensitiveIdentifier(each), new PipelineTableMetaData(each, columnMetaDataMap, uniqueIndexMetaData));
        }
        return result;
    }
    
    private Map<CaseInsensitiveIdentifier, Collection<CaseInsensitiveIdentifier>> loadUniqueIndexesOfTable(final Connection connection,
                                                                                                           final String schemaName, final String tableName) throws SQLException {
        Map<String, SortedMap<Short, CaseInsensitiveIdentifier>> orderedColumnsOfIndexes = new LinkedHashMap<>();
        // Set approximate=true to avoid Oracle driver 19 run `analyze table`
        try (ResultSet resultSet = connection.getMetaData().getIndexInfo(connection.getCatalog(), schemaName, tableName, true, true)) {
            while (resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME");
                if (null == indexName) {
                    continue;
                }
                orderedColumnsOfIndexes.computeIfAbsent(indexName, unused -> new TreeMap<>()).put(
                        resultSet.getShort("ORDINAL_POSITION"), new CaseInsensitiveIdentifier(resultSet.getString("COLUMN_NAME")));
            }
        }
        Map<CaseInsensitiveIdentifier, Collection<CaseInsensitiveIdentifier>> result = new LinkedHashMap<>();
        for (Entry<String, SortedMap<Short, CaseInsensitiveIdentifier>> entry : orderedColumnsOfIndexes.entrySet()) {
            Collection<CaseInsensitiveIdentifier> columnNames = result.computeIfAbsent(new CaseInsensitiveIdentifier(entry.getKey()), unused -> new LinkedList<>());
            columnNames.addAll(entry.getValue().values());
        }
        return result;
    }
    
    private Collection<CaseInsensitiveIdentifier> loadPrimaryKeys(final Connection connection, final String schemaName, final String tableName) throws SQLException {
        SortedMap<Short, CaseInsensitiveIdentifier> result = new TreeMap<>();
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(connection.getCatalog(), schemaName, tableName)) {
            while (resultSet.next()) {
                result.put(resultSet.getShort("KEY_SEQ"), new CaseInsensitiveIdentifier(resultSet.getString("COLUMN_NAME")));
            }
        }
        return result.values();
    }
}
