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
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Table meta data loader for MySQL.
 */
public final class MySQLTableMetaDataLoader implements DialectTableMetaDataLoader {
    
    private static final String TABLE_META_DATA_SQL = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_KEY, EXTRA, COLLATION_NAME FROM information_schema.columns WHERE TABLE_SCHEMA=?";
    
    private static final String TABLE_META_DATA_SQL_WITH_EXISTED_TABLES = TABLE_META_DATA_SQL + " AND TABLE_NAME NOT IN (%s)";
    
    private static final String INDEX_META_DATA_SQL = "SELECT TABLE_NAME, INDEX_NAME FROM information_schema.statistics WHERE TABLE_SCHEMA=? and TABLE_NAME IN (%s)";
    
    @Override
    public Map<String, TableMetaData> load(final DataSource dataSource, final Collection<String> existedTables) throws SQLException {
        return loadTableMetaDataMap(dataSource, existedTables);
    }
    
    private Map<String, TableMetaData> loadTableMetaDataMap(final DataSource dataSource, final Collection<String> existedTables) throws SQLException {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        Map<String, Collection<ColumnMetaData>> columnMetaDataMap = loadColumnMetaDataMap(dataSource, existedTables);
        Map<String, Collection<IndexMetaData>> indexMetaDataMap = columnMetaDataMap.isEmpty() ? Collections.emptyMap() : loadIndexMetaData(dataSource, columnMetaDataMap.keySet());
        for (Entry<String, Collection<ColumnMetaData>> entry : columnMetaDataMap.entrySet()) {
            result.put(entry.getKey(), new TableMetaData(entry.getValue(), indexMetaDataMap.getOrDefault(entry.getKey(), Collections.emptyList())));
        }
        return result;
    }
    
    private Map<String, Collection<ColumnMetaData>> loadColumnMetaDataMap(final DataSource dataSource, final Collection<String> existedTables) throws SQLException {
        Map<String, Collection<ColumnMetaData>> result = new HashMap<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(getTableMetaDataSQL(existedTables))) {
            Map<String, Integer> dataTypes = DataTypeLoader.load(connection.getMetaData());
            preparedStatement.setString(1, connection.getCatalog());
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
        boolean primaryKey = "PRI".equals(resultSet.getString("COLUMN_KEY"));
        boolean generated = "auto_increment".equals(resultSet.getString("EXTRA"));
        String collationName = resultSet.getString("COLLATION_NAME");
        boolean caseSensitive = null != collationName && !collationName.endsWith("_ci");
        return new ColumnMetaData(columnName, dataTypeMap.get(dataType), primaryKey, generated, caseSensitive);
    }

    private String getTableMetaDataSQL(final Collection<String> existedTables) {
        return existedTables.isEmpty() ? TABLE_META_DATA_SQL
                : String.format(TABLE_META_DATA_SQL_WITH_EXISTED_TABLES, existedTables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private Map<String, Collection<IndexMetaData>> loadIndexMetaData(final DataSource dataSource, final Collection<String> tableNames) throws SQLException {
        Map<String, Collection<IndexMetaData>> result = new HashMap<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(getIndexMetaDataSQL(tableNames))) {
            preparedStatement.setString(1, connection.getCatalog());
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
