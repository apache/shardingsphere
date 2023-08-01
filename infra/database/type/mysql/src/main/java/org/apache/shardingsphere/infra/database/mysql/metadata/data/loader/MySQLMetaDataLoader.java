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

package org.apache.shardingsphere.infra.database.mysql.metadata.data.loader;

import org.apache.shardingsphere.infra.database.core.GlobalDataSourceRegistry;
import org.apache.shardingsphere.infra.database.core.metadata.database.datatype.DataTypeLoader;
import org.apache.shardingsphere.infra.database.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.TableMetaData;

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
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Meta data loader for MySQL.
 */
public final class MySQLMetaDataLoader implements DialectMetaDataLoader {
    
    private static final String ORDER_BY_ORDINAL_POSITION = " ORDER BY ORDINAL_POSITION";
    
    private static final String TABLE_META_DATA_NO_ORDER =
            "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_KEY, EXTRA, COLLATION_NAME, ORDINAL_POSITION, COLUMN_TYPE FROM information_schema.columns "
                    + "WHERE TABLE_SCHEMA=?";
    
    private static final String TABLE_META_DATA_SQL = TABLE_META_DATA_NO_ORDER + ORDER_BY_ORDINAL_POSITION;
    
    private static final String TABLE_META_DATA_SQL_IN_TABLES = TABLE_META_DATA_NO_ORDER + " AND TABLE_NAME IN (%s)" + ORDER_BY_ORDINAL_POSITION;
    
    private static final String INDEX_META_DATA_SQL = "SELECT TABLE_NAME, INDEX_NAME FROM information_schema.statistics WHERE TABLE_SCHEMA=? and TABLE_NAME IN (%s)";
    
    private static final String CONSTRAINT_META_DATA_SQL = "SELECT CONSTRAINT_NAME, TABLE_NAME, REFERENCED_TABLE_NAME FROM information_schema.KEY_COLUMN_USAGE "
            + "WHERE TABLE_NAME IN (%s) AND REFERENCED_TABLE_SCHEMA IS NOT NULL";
    
    @Override
    public Collection<SchemaMetaData> load(final DataSource dataSource, final Collection<String> tables, final String defaultSchemaName) throws SQLException {
        Collection<TableMetaData> tableMetaDataList = new LinkedList<>();
        Map<String, Collection<ColumnMetaData>> columnMetaDataMap = loadColumnMetaDataMap(dataSource, tables);
        Map<String, Collection<IndexMetaData>> indexMetaDataMap = columnMetaDataMap.isEmpty() ? Collections.emptyMap() : loadIndexMetaData(dataSource, columnMetaDataMap.keySet());
        Map<String, Collection<ConstraintMetaData>> constraintMetaDataMap = columnMetaDataMap.isEmpty() ? Collections.emptyMap() : loadConstraintMetaDataMap(dataSource, columnMetaDataMap.keySet());
        for (Entry<String, Collection<ColumnMetaData>> entry : columnMetaDataMap.entrySet()) {
            Collection<IndexMetaData> indexMetaDataList = indexMetaDataMap.getOrDefault(entry.getKey(), Collections.emptyList());
            Collection<ConstraintMetaData> constraintMetaDataList = constraintMetaDataMap.getOrDefault(entry.getKey(), Collections.emptyList());
            tableMetaDataList.add(new TableMetaData(entry.getKey(), entry.getValue(), indexMetaDataList, constraintMetaDataList));
        }
        return Collections.singletonList(new SchemaMetaData(defaultSchemaName, tableMetaDataList));
    }
    
    private Map<String, Collection<ConstraintMetaData>> loadConstraintMetaDataMap(final DataSource dataSource, final Collection<String> tables) throws SQLException {
        Map<String, Collection<ConstraintMetaData>> result = new LinkedHashMap<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(getConstraintMetaDataSQL(tables))) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String constraintName = resultSet.getString("CONSTRAINT_NAME");
                    String tableName = resultSet.getString("TABLE_NAME");
                    String referencedTableName = resultSet.getString("REFERENCED_TABLE_NAME");
                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new LinkedList<>());
                    }
                    result.get(tableName).add(new ConstraintMetaData(constraintName, referencedTableName));
                }
            }
        }
        return result;
    }
    
    private String getConstraintMetaDataSQL(final Collection<String> tableNames) {
        return String.format(CONSTRAINT_META_DATA_SQL, tableNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private Map<String, Collection<ColumnMetaData>> loadColumnMetaDataMap(final DataSource dataSource, final Collection<String> tables) throws SQLException {
        Map<String, Collection<ColumnMetaData>> result = new HashMap<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(getTableMetaDataSQL(tables))) {
            Map<String, Integer> dataTypes = new DataTypeLoader().load(connection.getMetaData(), getType());
            String databaseName = "".equals(connection.getCatalog()) ? GlobalDataSourceRegistry.getInstance().getCachedDatabaseTables().get(tables.iterator().next()) : connection.getCatalog();
            preparedStatement.setString(1, databaseName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    ColumnMetaData columnMetaData = loadColumnMetaData(dataTypes, resultSet);
                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new LinkedList<>());
                    }
                    result.get(tableName).add(columnMetaData);
                }
            }
        }
        return result;
    }
    
    private ColumnMetaData loadColumnMetaData(final Map<String, Integer> dataTypeMap, final ResultSet resultSet) throws SQLException {
        String columnName = resultSet.getString("COLUMN_NAME");
        String dataType = resultSet.getString("DATA_TYPE");
        boolean primaryKey = "PRI".equalsIgnoreCase(resultSet.getString("COLUMN_KEY"));
        String extra = resultSet.getString("EXTRA");
        boolean generated = "auto_increment".equals(extra);
        String collationName = resultSet.getString("COLLATION_NAME");
        boolean caseSensitive = null != collationName && !collationName.endsWith("_ci");
        boolean visible = !"INVISIBLE".equalsIgnoreCase(extra);
        boolean unsigned = resultSet.getString("COLUMN_TYPE").toUpperCase().contains("UNSIGNED");
        return new ColumnMetaData(columnName, dataTypeMap.get(dataType), primaryKey, generated, caseSensitive, visible, unsigned);
    }
    
    private String getTableMetaDataSQL(final Collection<String> tables) {
        return tables.isEmpty() ? TABLE_META_DATA_SQL : String.format(TABLE_META_DATA_SQL_IN_TABLES, tables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private Map<String, Collection<IndexMetaData>> loadIndexMetaData(final DataSource dataSource, final Collection<String> tableNames) throws SQLException {
        Map<String, Collection<IndexMetaData>> result = new HashMap<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(getIndexMetaDataSQL(tableNames))) {
            String databaseName = "".equals(connection.getCatalog()) ? GlobalDataSourceRegistry.getInstance().getCachedDatabaseTables().get(tableNames.iterator().next()) : connection.getCatalog();
            preparedStatement.setString(1, databaseName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String indexName = resultSet.getString("INDEX_NAME");
                    String tableName = resultSet.getString("TABLE_NAME");
                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new LinkedList<>());
                    }
                    result.get(tableName).add(new IndexMetaData(indexName));
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
        return "MySQL";
    }
}
