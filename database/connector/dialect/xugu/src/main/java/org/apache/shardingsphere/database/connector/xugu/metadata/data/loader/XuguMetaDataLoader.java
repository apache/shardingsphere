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

package org.apache.shardingsphere.database.connector.xugu.metadata.data.loader;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderConnection;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.datatype.DataTypeRegistry;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Meta data loader for xugu.
 */
public final class XuguMetaDataLoader implements DialectMetaDataLoader {

    private static final String TABLE_META_DATA_SQL_NO_ORDER =
            "SELECT s.schema_name, t.TABLE_NAME, c.COL_NAME, c.NOT_NULL, c.TYPE_NAME, c.COL_NO, c.IS_HIDE ,c.IS_SERIAL, c.COLLATOR FROM ALL_COLUMNS AS c "
                    + "join ALL_TABLES AS t on t.table_id = c.table_id "
                    + "JOIN ALL_SCHEMAS AS s on t.schema_id = s.schema_id "
                    + "WHERE s.schema_name = ?";

    private static final String VIEW_META_DATA_SQL_NO_ORDER =
            "SELECT s.schema_name, v.VIEW_NAME, c.COL_NAME, null as NOT_NULL, c.TYPE_NAME, c.COL_NO, null as IS_HIDE ,null as IS_SERIAL, null as COLLATOR FROM ALL_VIEW_COLUMNS AS c "
                    + "join ALL_VIEWS AS v on v.VIEW_ID = c.VIEW_ID "
                    + "JOIN ALL_SCHEMAS AS s on v.schema_id = s.schema_id "
                    + "WHERE s.schema_name = ?";

    private static final String ORDER_BY_COLUMN_ID = " ORDER BY COL_NO";

    private static final String TABLE_META_DATA_SQL = TABLE_META_DATA_SQL_NO_ORDER + ORDER_BY_COLUMN_ID;

    private static final String VIEW_META_DATA_SQL = VIEW_META_DATA_SQL_NO_ORDER + ORDER_BY_COLUMN_ID;

    private static final String TABLE_META_DATA_SQL_IN_TABLES = TABLE_META_DATA_SQL_NO_ORDER + " AND t.TABLE_NAME IN (%s)" + ORDER_BY_COLUMN_ID;

    private static final String VIEW_META_DATA_SQL_IN_VIEWS = VIEW_META_DATA_SQL_NO_ORDER + " AND v.VIEW_NAME IN (%s)" + ORDER_BY_COLUMN_ID;

    private static final String VIEW_NAME_SQL = "SELECT v.VIEW_NAME FROM ALL_VIEWS AS v JOIN ALL_SCHEMAS as s on v.schema_id = s.schema_id WHERE s.schema_name = ? AND v.VIEW_NAME IN (%s)";

    private static final String INDEX_META_DATA_SQL = "SELECT s.schema_name,t.TABLE_NAME,i.INDEX_NAME,IS_UNIQUE FROM ALL_INDEXES AS i "
            + "JOIN ALL_TABLES AS t ON i.table_id = t.table_id "
            + "JOIN ALL_SCHEMAS AS s on t.schema_id = s.schema_id "
            + "WHERE s.schema_name = ? AND t.TABLE_NAME IN (%s)";

    private static final String PRIMARY_KEY_META_DATA_SQL = "SELECT s.schema_name, t.table_name, cs.define FROM ALL_CONSTRAINTS AS cs "
            + "JOIN all_tables AS t ON cs.table_id = t.table_id "
            + "JOIN ALL_SCHEMAS AS s ON t.schema_id = s.schema_id "
            + "WHERE cs.CONS_TYPE = 'p' AND s.schema_name = '%s'";

    private static final String PRIMARY_KEY_META_DATA_SQL_IN_TABLES = PRIMARY_KEY_META_DATA_SQL + " AND t.TABLE_NAME IN (%s)";

    private static final String INDEX_COLUMN_META_DATA_SQL = "SELECT i.KEYS FROM ALL_INDEXES AS i "
            + "JOIN ALL_TABLES AS t ON i.table_id = t.table_id "
            + "JOIN ALL_SCHEMAS AS s on t.schema_id = s.schema_id "
            + "WHERE s.schema_name = ? AND t.TABLE_NAME = ? AND i.INDEX_NAME = ?";

    private static final int MAX_EXPRESSION_SIZE = 1000;

    @Override
    public Collection<SchemaMetaData> load(final MetaDataLoaderMaterial material) throws SQLException {
        Collection<TableMetaData> tableMetaDataList = new LinkedList<>();
        try (Connection connection = new MetaDataLoaderConnection(TypedSPILoader.getService(DatabaseType.class, "XuGu"), material.getDataSource().getConnection())) {
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
        try (PreparedStatement preparedStatement = connection.prepareStatement(getViewNameSQL(tables))) {
            preparedStatement.setString(1, schema);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSet.getString("VIEW_NAME"));
                }
            }
        }
        return result;
    }

    private String getViewNameSQL(final Collection<String> tableNames) {
        return String.format(VIEW_NAME_SQL, tableNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }

    private Map<String, Collection<ColumnMetaData>> loadColumnMetaDataMap(final Connection connection, final Collection<String> tables, final String schema) throws SQLException {
        Map<String, Collection<ColumnMetaData>> result = new HashMap<>(tables.size(), 1F);
        try (PreparedStatement preparedStatement = connection.prepareStatement(getTableMetaDataSQL(tables))) {
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
        try (PreparedStatement preparedStatement = connection.prepareStatement(getViewMetaDataSQL(tables))) {
            Map<String, Collection<String>> tablePrimaryKeys = loadTablePrimaryKeys(connection, tables);
            preparedStatement.setString(1, schema);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("VIEW_NAME");
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
        String columnName = resultSet.getString("COL_NAME");
        String dataType = getOriginalDataType(resultSet.getString("TYPE_NAME"));
        boolean primaryKey = primaryKeys.contains(columnName);
        boolean generated = resultSet.getBoolean("IS_SERIAL");
        String collation = resultSet.getString("COLLATOR");
        // XuguDB-v12 COLLATOR 暂未使用
        boolean caseSensitive = null != collation && collation.endsWith("_CS");
        boolean isVisible = !resultSet.getBoolean("IS_HIDE");
        boolean nullable = !resultSet.getBoolean("NOT_NULL");
        return new ColumnMetaData(columnName, DataTypeRegistry.getDataType(getDatabaseType(), dataType).orElse(Types.OTHER), primaryKey, generated, caseSensitive, isVisible, false, nullable);
    }

    private String getOriginalDataType(final String dataType) {
        int index = dataType.indexOf('(');
        if (index > 0) {
            return dataType.substring(0, index);
        }
        return dataType;
    }

    private String getTableMetaDataSQL(final Collection<String> tables) throws SQLException {
        return tables.isEmpty() ? TABLE_META_DATA_SQL
                : String.format(TABLE_META_DATA_SQL_IN_TABLES, tables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }

    private String getViewMetaDataSQL(final Collection<String> tables) throws SQLException {
        return tables.isEmpty() ? VIEW_META_DATA_SQL
                : String.format(VIEW_META_DATA_SQL_IN_VIEWS, tables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }

    private Map<String, Collection<IndexMetaData>> loadIndexMetaData(final Connection connection, final Collection<String> tableNames, final String schema) throws SQLException {
        Map<String, Collection<IndexMetaData>> result = new HashMap<>(tableNames.size(), 1F);
        try (PreparedStatement preparedStatement = connection.prepareStatement(getIndexMetaDataSQL(tableNames))) {
            preparedStatement.setString(1, schema);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String indexName = resultSet.getString("INDEX_NAME");
                    String tableName = resultSet.getString("TABLE_NAME");
                    boolean isUnique = resultSet.getBoolean("IS_UNIQUE");
                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new LinkedList<>());
                    }
                    IndexMetaData indexMetaData = new IndexMetaData(indexName, loadIndexColumnNames(connection, tableName, indexName));
                    indexMetaData.setUnique(isUnique);
                    result.get(tableName).add(indexMetaData);
                }
            }
        }
        return result;
    }

    private List<String> loadIndexColumnNames(final Connection connection, final String tableName, final String indexName) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(INDEX_COLUMN_META_DATA_SQL)) {
            preparedStatement.setString(1, connection.getSchema());
            preparedStatement.setString(2, tableName);
            preparedStatement.setString(3, indexName);
            List<String> result = new LinkedList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String keys = resultSet.getString("KEYS");
                Arrays.stream(keys.split(","))
                        .map(String::trim)
                        .map(s -> s.replaceAll("^\"|\"$", ""))
                        .filter(s -> !s.isEmpty())
                        .forEach(result::add);
            }
            return result;
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
                    String columnName = resultSet.getString("define");
                    String tableName = resultSet.getString("table_name");
                    Collection<String> collection = result.computeIfAbsent(tableName, key -> new LinkedList<>());
                    Arrays.stream(columnName.split(","))
                            .map(String::trim)
                            .map(s -> s.replaceAll("^\"|\"$", ""))
                            .filter(s -> !s.isEmpty())
                            .forEach(collection::add);
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
        return "XuGu";
    }
}
