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

package org.apache.shardingsphere.database.connector.oracle.metadata.data.loader;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderConnection;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.datatype.DataTypeRegistry;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Meta data loader for Oracle.
 */
public final class OracleMetaDataLoader implements DialectMetaDataLoader {
    
    private static final String TABLE_META_DATA_SQL_NO_ORDER =
            "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, NULLABLE, DATA_TYPE, COLUMN_ID, HIDDEN_COLUMN %s FROM ALL_TAB_COLS WHERE OWNER = ?";
    
    private static final String ORDER_BY_COLUMN_ID = " ORDER BY COLUMN_ID";
    
    private static final String TABLE_META_DATA_SQL = TABLE_META_DATA_SQL_NO_ORDER + ORDER_BY_COLUMN_ID;
    
    private static final String TABLE_META_DATA_SQL_IN_TABLES = TABLE_META_DATA_SQL_NO_ORDER + " AND TABLE_NAME IN (%s)" + ORDER_BY_COLUMN_ID;
    
    private static final String VIEW_META_DATA_SQL = "SELECT VIEW_NAME FROM ALL_VIEWS WHERE OWNER = ? AND VIEW_NAME IN (%s)";
    
    private static final String INDEX_META_DATA_SQL = "SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, INDEX_NAME, UNIQUENESS FROM ALL_INDEXES WHERE OWNER = ? AND TABLE_NAME IN (%s)";
    
    private static final String PRIMARY_KEY_META_DATA_SQL = "SELECT A.OWNER AS TABLE_SCHEMA, A.TABLE_NAME AS TABLE_NAME, B.COLUMN_NAME AS COLUMN_NAME FROM ALL_CONSTRAINTS A INNER JOIN"
            + " ALL_CONS_COLUMNS B ON A.CONSTRAINT_NAME = B.CONSTRAINT_NAME WHERE CONSTRAINT_TYPE = 'P' AND A.OWNER = '%s'";
    
    private static final String PRIMARY_KEY_META_DATA_SQL_IN_TABLES = PRIMARY_KEY_META_DATA_SQL + " AND A.TABLE_NAME IN (%s)";
    
    private static final String INDEX_COLUMN_META_DATA_SQL = "SELECT INDEX_NAME, COLUMN_NAME FROM ALL_IND_COLUMNS WHERE INDEX_OWNER = ? AND INDEX_NAME IN (%s)";
    
    private static final int COLLATION_START_MAJOR_VERSION = 12;
    
    private static final int COLLATION_START_MINOR_VERSION = 2;
    
    private static final int IDENTITY_COLUMN_START_MINOR_VERSION = 1;
    
    private static final int MAX_EXPRESSION_SIZE = 1000;
    
    @Override
    public Collection<SchemaMetaData> load(final MetaDataLoaderMaterial material) throws SQLException {
        Collection<TableMetaData> tableMetaDataList = new LinkedList<>();
        try (Connection connection = new MetaDataLoaderConnection(TypedSPILoader.getService(DatabaseType.class, "Oracle"), material.getDataSource().getConnection())) {
            tableMetaDataList.addAll(getTableMetaDataList(connection, connection.getSchema(), material.getActualTableNames()));
        }
        return Collections.singletonList(new SchemaMetaData(material.getDefaultSchemaName(), tableMetaDataList));
    }
    
    private Collection<TableMetaData> getTableMetaDataList(final Connection connection, final String schema, final Collection<String> tableNames) throws SQLException {
        Collection<String> viewNames = new LinkedList<>();
        Map<String, Collection<ColumnMetaData>> columnMetaDataMap = new HashMap<>(tableNames.size(), 1F);
        Map<String, Collection<IndexMetaData>> indexMetaDataMap = new HashMap<>(tableNames.size(), 1F);
        for (List<String> each : Lists.partition(new ArrayList<>(tableNames), MAX_EXPRESSION_SIZE)) {
            viewNames.addAll(loadViewNames(connection, each, schema));
            columnMetaDataMap.putAll(loadColumnMetaDataMap(connection, each, schema));
            indexMetaDataMap.putAll(loadIndexMetaData(connection, each, schema));
        }
        Collection<TableMetaData> result = new LinkedList<>();
        for (Entry<String, Collection<ColumnMetaData>> entry : columnMetaDataMap.entrySet()) {
            result.add(new TableMetaData(entry.getKey(), entry.getValue(), indexMetaDataMap.getOrDefault(entry.getKey(), Collections.emptyList()), Collections.emptyList(),
                    viewNames.contains(entry.getKey()) ? TableType.VIEW : TableType.TABLE));
        }
        return result;
    }
    
    private Collection<String> loadViewNames(final Connection connection, final Collection<String> tables, final String schema) throws SQLException {
        Collection<String> result = new LinkedList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(getViewMetaDataSQL(tables))) {
            preparedStatement.setString(1, schema);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSet.getString(1));
                }
            }
        }
        return result;
    }
    
    private String getViewMetaDataSQL(final Collection<String> tableNames) {
        return String.format(VIEW_META_DATA_SQL, tableNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private Map<String, Collection<ColumnMetaData>> loadColumnMetaDataMap(final Connection connection, final Collection<String> tables, final String schema) throws SQLException {
        Map<String, Collection<ColumnMetaData>> result = new HashMap<>(tables.size(), 1F);
        try (PreparedStatement preparedStatement = connection.prepareStatement(getTableMetaDataSQL(tables, connection.getMetaData()))) {
            Map<String, Collection<String>> tablePrimaryKeys = loadTablePrimaryKeys(connection, tables);
            preparedStatement.setString(1, schema);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    ColumnMetaData columnMetaData = loadColumnMetaData(resultSet, tablePrimaryKeys.getOrDefault(tableName, Collections.emptyList()), connection.getMetaData());
                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new LinkedList<>());
                    }
                    result.get(tableName).add(columnMetaData);
                }
            }
        }
        return result;
    }
    
    private ColumnMetaData loadColumnMetaData(final ResultSet resultSet, final Collection<String> primaryKeys, final DatabaseMetaData databaseMetaData) throws SQLException {
        String columnName = resultSet.getString("COLUMN_NAME");
        String dataType = getOriginalDataType(resultSet.getString("DATA_TYPE"));
        boolean primaryKey = primaryKeys.contains(columnName);
        boolean generated = versionContainsIdentityColumn(databaseMetaData) && "YES".equals(resultSet.getString("IDENTITY_COLUMN"));
        // TODO need to support caseSensitive when version < 12.2.
        String collation = versionContainsCollation(databaseMetaData) ? resultSet.getString("COLLATION") : null;
        boolean caseSensitive = null != collation && collation.endsWith("_CS");
        boolean isVisible = "NO".equals(resultSet.getString("HIDDEN_COLUMN"));
        boolean nullable = "Y".equals(resultSet.getString("NULLABLE"));
        return new ColumnMetaData(columnName, DataTypeRegistry.getDataType(getDatabaseType(), dataType).orElse(Types.OTHER), primaryKey, generated, "", caseSensitive, isVisible, false, nullable);
    }
    
    private String getOriginalDataType(final String dataType) {
        int index = dataType.indexOf('(');
        if (index > 0) {
            return dataType.substring(0, index);
        }
        return dataType;
    }
    
    private String getTableMetaDataSQL(final Collection<String> tables, final DatabaseMetaData databaseMetaData) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder(28);
        if (versionContainsIdentityColumn(databaseMetaData)) {
            stringBuilder.append(", IDENTITY_COLUMN");
        }
        if (versionContainsCollation(databaseMetaData)) {
            stringBuilder.append(", COLLATION");
        }
        String collation = stringBuilder.toString();
        return tables.isEmpty() ? String.format(TABLE_META_DATA_SQL, collation)
                : String.format(TABLE_META_DATA_SQL_IN_TABLES, collation, tables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private boolean versionContainsCollation(final DatabaseMetaData databaseMetaData) throws SQLException {
        return databaseMetaData.getDatabaseMajorVersion() >= COLLATION_START_MAJOR_VERSION && databaseMetaData.getDatabaseMinorVersion() >= COLLATION_START_MINOR_VERSION;
    }
    
    private boolean versionContainsIdentityColumn(final DatabaseMetaData databaseMetaData) throws SQLException {
        return databaseMetaData.getDatabaseMajorVersion() >= COLLATION_START_MAJOR_VERSION && databaseMetaData.getDatabaseMinorVersion() >= IDENTITY_COLUMN_START_MINOR_VERSION;
    }
    
    private Map<String, Collection<IndexMetaData>> loadIndexMetaData(final Connection connection, final Collection<String> tableNames, final String schema) throws SQLException {
        Map<String, Collection<IndexMetaData>> result = new HashMap<>(tableNames.size(), 1F);
        try (PreparedStatement preparedStatement = connection.prepareStatement(getIndexMetaDataSQL(tableNames))) {
            preparedStatement.setString(1, schema);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String indexName = resultSet.getString("INDEX_NAME");
                    String tableName = resultSet.getString("TABLE_NAME");
                    boolean isUnique = "UNIQUE".equals(resultSet.getString("UNIQUENESS"));
                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new LinkedList<>());
                    }
                    IndexMetaData indexMetaData = new IndexMetaData(indexName);
                    indexMetaData.setUnique(isUnique);
                    result.get(tableName).add(indexMetaData);
                }
            }
        }
        loadIndexColumnNames(connection, result);
        return result;
    }
    
    private void loadIndexColumnNames(final Connection connection, final Map<String, Collection<IndexMetaData>> tableIndexMetaDataMap) throws SQLException {
        List<String> quotedIndexNames =
                tableIndexMetaDataMap.values().stream().flatMap(Collection::stream).map(IndexMetaData::getName).map(QuoteCharacter.SINGLE_QUOTE::wrap).collect(Collectors.toList());
        if (!quotedIndexNames.isEmpty()) {
            return;
        }
        Map<String, Collection<String>> indexColumnsMap = new HashMap<>();
        for (List<String> each : Lists.partition(quotedIndexNames, 1000)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(String.format(INDEX_COLUMN_META_DATA_SQL, Joiner.on(",").join(each)))) {
                preparedStatement.setString(1, connection.getSchema());
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    Collection<String> columns = indexColumnsMap.computeIfAbsent(resultSet.getString("INDEX_NAME"), key -> new LinkedList<>());
                    columns.add(resultSet.getString("COLUMN_NAME"));
                }
            }
        }
        for (Entry<String, Collection<IndexMetaData>> entry : tableIndexMetaDataMap.entrySet()) {
            for (IndexMetaData each : entry.getValue()) {
                Optional.ofNullable(indexColumnsMap.get(each.getName())).ifPresent(each::setColumns);
            }
        }
    }
    
    private String getIndexMetaDataSQL(final Collection<String> tableNames) {
        // TODO The table name needs to be in uppercase, otherwise the index cannot be found.
        return String.format(INDEX_META_DATA_SQL, tableNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private Map<String, Collection<String>> loadTablePrimaryKeys(final Connection connection, final Collection<String> tableNames) throws SQLException {
        Map<String, Collection<String>> result = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(getPrimaryKeyMetaDataSQL(connection.getSchema(), tableNames))) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String columnName = resultSet.getString("COLUMN_NAME");
                    String tableName = resultSet.getString("TABLE_NAME");
                    result.computeIfAbsent(tableName, key -> new LinkedList<>()).add(columnName);
                }
            }
        }
        return result;
    }
    
    private String getPrimaryKeyMetaDataSQL(final String schemaName, final Collection<String> tables) {
        return tables.isEmpty() ? String.format(PRIMARY_KEY_META_DATA_SQL, schemaName)
                : String.format(PRIMARY_KEY_META_DATA_SQL_IN_TABLES, schemaName, tables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    @Override
    public String getDatabaseType() {
        return "Oracle";
    }
}
