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

package org.apache.shardingsphere.database.connector.h2.metadata.data.loader;

import org.apache.shardingsphere.database.connector.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.datatype.DataTypeRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Meta data loader for H2.
 */
public final class H2MetaDataLoader implements DialectMetaDataLoader {
    
    private static final String TABLE_META_DATA_NO_ORDER = "SELECT TABLE_CATALOG, TABLE_NAME, COLUMN_NAME, DATA_TYPE, ORDINAL_POSITION, COALESCE(IS_VISIBLE, FALSE) IS_VISIBLE, IS_NULLABLE"
            + " FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=?";
    
    private static final String ORDER_BY_ORDINAL_POSITION = " ORDER BY ORDINAL_POSITION";
    
    private static final String TABLE_META_DATA_SQL = TABLE_META_DATA_NO_ORDER + ORDER_BY_ORDINAL_POSITION;
    
    private static final String TABLE_META_DATA_SQL_IN_TABLES = TABLE_META_DATA_NO_ORDER + " AND UPPER(TABLE_NAME) IN (%s)" + ORDER_BY_ORDINAL_POSITION;
    
    private static final String INDEX_META_DATA_SQL = "SELECT TABLE_CATALOG, TABLE_NAME, INDEX_NAME, INDEX_TYPE_NAME FROM INFORMATION_SCHEMA.INDEXES"
            + " WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? AND UPPER(TABLE_NAME) IN (%s)";
    
    private static final String PRIMARY_KEY_META_DATA_SQL = "SELECT TABLE_NAME, INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=?"
            + " AND INDEX_TYPE_NAME = 'PRIMARY KEY'";
    
    private static final String PRIMARY_KEY_META_DATA_SQL_IN_TABLES = PRIMARY_KEY_META_DATA_SQL + " AND UPPER(TABLE_NAME) IN (%s)";
    
    private static final String GENERATED_INFO_SQL = "SELECT C.TABLE_NAME TABLE_NAME, C.COLUMN_NAME COLUMN_NAME, COALESCE(I.IS_GENERATED, FALSE) IS_GENERATED FROM INFORMATION_SCHEMA.COLUMNS C"
            + " RIGHT JOIN INFORMATION_SCHEMA.INDEXES I ON C.TABLE_NAME=I.TABLE_NAME WHERE C.TABLE_CATALOG=? AND C.TABLE_SCHEMA=?";
    
    private static final String GENERATED_INFO_SQL_IN_TABLES = GENERATED_INFO_SQL + " AND UPPER(C.TABLE_NAME) IN (%s)";
    
    @Override
    public Collection<SchemaMetaData> load(final MetaDataLoaderMaterial material) throws SQLException {
        Collection<TableMetaData> tableMetaDataList = new LinkedList<>();
        try (Connection connection = material.getDataSource().getConnection()) {
            Map<String, Collection<ColumnMetaData>> columnMetaDataMap = loadColumnMetaDataMap(connection, material.getActualTableNames());
            Map<String, Collection<IndexMetaData>> indexMetaDataMap = columnMetaDataMap.isEmpty() ? Collections.emptyMap() : loadIndexMetaData(connection, columnMetaDataMap.keySet());
            for (Entry<String, Collection<ColumnMetaData>> entry : columnMetaDataMap.entrySet()) {
                Collection<IndexMetaData> indexMetaDataList = indexMetaDataMap.getOrDefault(entry.getKey(), Collections.emptyList());
                tableMetaDataList.add(new TableMetaData(entry.getKey(), entry.getValue(), indexMetaDataList, Collections.emptyList()));
            }
        }
        return Collections.singleton(new SchemaMetaData(material.getDefaultSchemaName(), tableMetaDataList));
    }
    
    private Map<String, Collection<ColumnMetaData>> loadColumnMetaDataMap(final Connection connection, final Collection<String> tables) throws SQLException {
        Map<String, Collection<ColumnMetaData>> result = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(getTableMetaDataSQL(tables))) {
            Map<String, Collection<String>> tablePrimaryKeys = loadTablePrimaryKeys(connection, tables);
            Map<String, Map<String, Boolean>> tableGenerated = loadTableGenerated(connection, tables);
            preparedStatement.setString(1, connection.getCatalog());
            preparedStatement.setString(2, "PUBLIC");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    ColumnMetaData columnMetaData =
                            loadColumnMetaData(resultSet, tablePrimaryKeys.getOrDefault(tableName, Collections.emptyList()), tableGenerated.getOrDefault(tableName, new HashMap<>()));
                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new LinkedList<>());
                    }
                    result.get(tableName).add(columnMetaData);
                }
            }
        }
        return result;
    }
    
    private ColumnMetaData loadColumnMetaData(final ResultSet resultSet, final Collection<String> primaryKeys, final Map<String, Boolean> tableGenerated) throws SQLException {
        String columnName = resultSet.getString("COLUMN_NAME");
        String dataType = resultSet.getString("DATA_TYPE");
        boolean primaryKey = primaryKeys.contains(columnName);
        boolean generated = tableGenerated.getOrDefault(columnName, Boolean.FALSE);
        boolean isVisible = resultSet.getBoolean("IS_VISIBLE");
        boolean isNullable = "YES".equals(resultSet.getString("IS_NULLABLE"));
        return new ColumnMetaData(columnName, DataTypeRegistry.getDataType(getDatabaseType(), dataType).orElse(Types.OTHER), primaryKey, generated, "", false, isVisible, false, isNullable);
    }
    
    private String getTableMetaDataSQL(final Collection<String> tables) {
        return tables.isEmpty() ? TABLE_META_DATA_SQL
                : String.format(TABLE_META_DATA_SQL_IN_TABLES, tables.stream().map(each -> String.format("'%s'", each).toUpperCase()).collect(Collectors.joining(",")));
    }
    
    private Map<String, Collection<IndexMetaData>> loadIndexMetaData(final Connection connection, final Collection<String> tableNames) throws SQLException {
        Map<String, Collection<IndexMetaData>> result = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(getIndexMetaDataSQL(tableNames))) {
            preparedStatement.setString(1, connection.getCatalog());
            preparedStatement.setString(2, "PUBLIC");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String indexName = resultSet.getString("INDEX_NAME");
                    String tableName = resultSet.getString("TABLE_NAME");
                    boolean uniqueIndex = "UNIQUE INDEX".equals(resultSet.getString("INDEX_TYPE_NAME"));
                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new LinkedList<>());
                    }
                    IndexMetaData indexMetaData = new IndexMetaData(indexName);
                    indexMetaData.setUnique(uniqueIndex);
                    result.get(tableName).add(indexMetaData);
                    
                }
            }
        }
        return result;
    }
    
    private String getIndexMetaDataSQL(final Collection<String> tableNames) {
        return String.format(INDEX_META_DATA_SQL, tableNames.stream().map(each -> String.format("'%s'", each).toUpperCase()).collect(Collectors.joining(",")));
    }
    
    private String getPrimaryKeyMetaDataSQL(final Collection<String> tables) {
        return tables.isEmpty() ? PRIMARY_KEY_META_DATA_SQL
                : String.format(PRIMARY_KEY_META_DATA_SQL_IN_TABLES, tables.stream().map(each -> String.format("'%s'", each).toUpperCase()).collect(Collectors.joining(",")));
    }
    
    private Map<String, Collection<String>> loadTablePrimaryKeys(final Connection connection, final Collection<String> tableNames) throws SQLException {
        Map<String, Collection<String>> result = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(getPrimaryKeyMetaDataSQL(tableNames))) {
            preparedStatement.setString(1, connection.getCatalog());
            preparedStatement.setString(2, "PUBLIC");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String indexName = resultSet.getString("INDEX_NAME");
                    String tableName = resultSet.getString("TABLE_NAME");
                    result.computeIfAbsent(tableName, key -> new LinkedList<>()).add(indexName);
                }
            }
        }
        return result;
    }
    
    private String getGeneratedInfoSQL(final Collection<String> tables) {
        return tables.isEmpty() ? GENERATED_INFO_SQL
                : String.format(GENERATED_INFO_SQL_IN_TABLES, tables.stream().map(each -> String.format("'%s'", each).toUpperCase()).collect(Collectors.joining(",")));
    }
    
    private Map<String, Map<String, Boolean>> loadTableGenerated(final Connection connection, final Collection<String> tableNames) throws SQLException {
        Map<String, Map<String, Boolean>> result = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(getGeneratedInfoSQL(tableNames))) {
            preparedStatement.setString(1, connection.getCatalog());
            preparedStatement.setString(2, "PUBLIC");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String columnName = resultSet.getString("COLUMN_NAME");
                    String tableName = resultSet.getString("TABLE_NAME");
                    boolean generated = resultSet.getBoolean("IS_GENERATED");
                    result.computeIfAbsent(tableName, key -> new HashMap<>()).put(columnName, generated);
                }
            }
        }
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "H2";
    }
}
