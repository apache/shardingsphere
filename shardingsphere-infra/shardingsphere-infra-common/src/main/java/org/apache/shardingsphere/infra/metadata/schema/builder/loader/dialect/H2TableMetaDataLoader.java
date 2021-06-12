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

package org.apache.shardingsphere.infra.metadata.schema.builder.loader.dialect;

import org.apache.shardingsphere.infra.metadata.schema.builder.loader.DataTypeLoader;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.builder.util.IndexMetaDataUtil;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Table meta data loader for H2.
 */
public final class H2TableMetaDataLoader implements DialectTableMetaDataLoader {

    private static final String TABLE_META_DATA_SQL = "SELECT TABLE_CATALOG, TABLE_NAME, COLUMN_NAME, DATA_TYPE, TYPE_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=?";

    private static final String TABLE_META_DATA_SQL_WITH_EXISTED_TABLES = TABLE_META_DATA_SQL + " AND TABLE_NAME NOT IN (%s)";

    private static final String INDEX_META_DATA_SQL = "SELECT TABLE_CATALOG, TABLE_NAME, INDEX_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.INDEXES"
            + " WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? AND TABLE_NAME IN (%s)";

    private static final String PRIMARY_KEY_META_DATA_SQL = "SELECT TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? AND PRIMARY_KEY = TRUE";

    private static final String PRIMARY_KEY_META_DATA_SQL_WITH_EXISTED_TABLES = PRIMARY_KEY_META_DATA_SQL + " AND TABLE_NAME NOT IN (%s)";

    private static final String GENERATED_INFO_SQL = "SELECT C.TABLE_NAME TABLE_NAME, C.COLUMN_NAME COLUMN_NAME, COALESCE(S.IS_GENERATED, FALSE) IS_GENERATED FROM INFORMATION_SCHEMA.COLUMNS C"
            + " RIGHT JOIN INFORMATION_SCHEMA.SEQUENCES S ON C.SEQUENCE_NAME=S.SEQUENCE_NAME WHERE C.TABLE_CATALOG=? AND C.TABLE_SCHEMA=?";

    private static final String GENERATED_INFO_SQL_WITH_EXISTED_TABLES = GENERATED_INFO_SQL + " AND TABLE_NAME NOT IN (%s)";

    @Override
    public Map<String, TableMetaData> load(final DataSource dataSource, final Collection<String> existedTables) throws SQLException {
        return loadTableMetaDataMap(dataSource, existedTables);
    }

    private Map<String, TableMetaData> loadTableMetaDataMap(final DataSource dataSource, final Collection<String> existedTables) throws SQLException {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            Map<String, Collection<ColumnMetaData>> columnMetaDataMap = loadColumnMetaDataMap(connection, existedTables);
            Map<String, Collection<IndexMetaData>> indexMetaDataMap = columnMetaDataMap.isEmpty() ? Collections.emptyMap() : loadIndexMetaData(connection, columnMetaDataMap.keySet());
            for (Map.Entry<String, Collection<ColumnMetaData>> entry : columnMetaDataMap.entrySet()) {
                result.put(entry.getKey(), new TableMetaData(entry.getValue(), indexMetaDataMap.getOrDefault(entry.getKey(), Collections.emptyList())));
            }
        }
        return result;
    }

    private Map<String, Collection<ColumnMetaData>> loadColumnMetaDataMap(final Connection connection, final Collection<String> existedTables) throws SQLException {
        Map<String, Collection<ColumnMetaData>> result = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(getTableMetaDataSQL(existedTables))) {
            Map<String, Integer> dataTypes = DataTypeLoader.load(connection.getMetaData());
            Map<String, Collection<String>> tablePrimaryKeys = loadTablePrimaryKeys(connection, existedTables);
            Map<String, Map<String, Boolean>> tableGenerated = loadTableGenerated(connection, existedTables);
            preparedStatement.setString(1, connection.getCatalog());
            preparedStatement.setString(2, "PUBLIC");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    ColumnMetaData columnMetaData = loadColumnMetaData(dataTypes, resultSet, tablePrimaryKeys.getOrDefault(tableName, Collections.emptyList()),
                            tableGenerated.getOrDefault(tableName, new HashMap<>()));
                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new LinkedList<>());
                    }
                    result.get(tableName).add(columnMetaData);
                }
            }
        }
        return result;
    }

    private ColumnMetaData loadColumnMetaData(final Map<String, Integer> dataTypeMap, final ResultSet resultSet, final Collection<String> primaryKeys,
                                              final Map<String, Boolean> tableGenerated) throws SQLException {
        String columnName = resultSet.getString("COLUMN_NAME");
        String typeName = resultSet.getString("TYPE_NAME");
        boolean primaryKey = primaryKeys.contains(columnName);
        // tableGenerated.getOrDefault(columnName, Boolean.FALSE);
        boolean generated = false;
        // H2 database case sensitive is always true
        return new ColumnMetaData(columnName, dataTypeMap.get(typeName), primaryKey, generated, true);
    }

    private String getTableMetaDataSQL(final Collection<String> existedTables) {
        return existedTables.isEmpty() ? TABLE_META_DATA_SQL
                : String.format(TABLE_META_DATA_SQL_WITH_EXISTED_TABLES, existedTables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
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
                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new LinkedList<>());
                    }
                    result.get(tableName).add(new IndexMetaData(IndexMetaDataUtil.getLogicIndexName(indexName, tableName)));
                }
            }
        }
        return result;
    }

    private String getIndexMetaDataSQL(final Collection<String> tableNames) {
        return String.format(INDEX_META_DATA_SQL, tableNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }

    @Override
    public String getDatabaseType() {
        return "H2";
    }

    private String getPrimaryKeyMetaDataSQL(final Collection<String> existedTables) {
        return existedTables.isEmpty() ? PRIMARY_KEY_META_DATA_SQL
                : String.format(PRIMARY_KEY_META_DATA_SQL_WITH_EXISTED_TABLES, existedTables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }

    private Map<String, Collection<String>> loadTablePrimaryKeys(final Connection connection, final Collection<String> tableNames) throws SQLException {
        Map<String, Collection<String>> result = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(getPrimaryKeyMetaDataSQL(tableNames))) {
            preparedStatement.setString(1, connection.getCatalog());
            preparedStatement.setString(2, "PUBLIC");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String columnName = resultSet.getString("COLUMN_NAME");
                    String tableName = resultSet.getString("TABLE_NAME");
                    result.computeIfAbsent(tableName, k -> new LinkedList<>()).add(columnName);
                }
            }
        }
        return result;
    }

    private String getGeneratedInfoSQL(final Collection<String> existedTables) {
        return existedTables.isEmpty() ? GENERATED_INFO_SQL
                : String.format(GENERATED_INFO_SQL_WITH_EXISTED_TABLES, existedTables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
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
                    result.computeIfAbsent(tableName, k -> new HashMap<>()).put(columnName, generated);
                }
            }
        }
        return result;
    }
}
