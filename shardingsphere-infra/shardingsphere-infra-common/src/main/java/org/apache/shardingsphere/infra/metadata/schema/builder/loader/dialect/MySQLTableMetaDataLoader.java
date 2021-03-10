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

import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Table meta data loader for MySQL.
 */
public final class MySQLTableMetaDataLoader implements DialectTableMetaDataLoader {
    
    private static final String TABLE_META_DATA_SQL
            = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_KEY, EXTRA, COLLATION_NAME FROM information_schema.columns WHERE TABLE_SCHEMA=? AND TABLE_NAME NOT IN (%s)";
    
    @Override
    public Map<String, TableMetaData> load(final DataSource dataSource, final Collection<String> existedTables) throws SQLException {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        loadColumnMetaData(dataSource, existedTables, result);
        // TODO load index
        return result;
    }
    
    private void loadColumnMetaData(final DataSource dataSource, final Collection<String> existedTables, final Map<String, TableMetaData> tableMetaDataMap) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(String.format(TABLE_META_DATA_SQL, String.join(",", existedTables)))
        ) {
            Map<String, Integer> dataTypeMap = getDataTypeMap(connection);
            preparedStatement.setString(1, dataSource.getConnection().getSchema());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    ColumnMetaData columnMetaData = loadColumnMetaData(dataTypeMap, resultSet);
                    if (!tableMetaDataMap.containsKey(tableName)) {
                        tableMetaDataMap.put(tableName, new TableMetaData());
                    }
                    tableMetaDataMap.get(tableName).getColumns().put(columnMetaData.getName(), columnMetaData);
                }
            }
        }
    }
    
    private ColumnMetaData loadColumnMetaData(final Map<String, Integer> dataTypeMap, final ResultSet resultSet) throws SQLException {
        String columnName = resultSet.getString("COLUMN_NAME");
        String dataType = resultSet.getString("DATA_TYPE");
        boolean primaryKey = "PRI".equals(resultSet.getString("COLUMN_KEY"));
        boolean generated = "auto_increment".equals(resultSet.getString("EXTRA"));
        boolean caseSensitive = null != resultSet.getString("COLLATION_NAME") && resultSet.getString("COLLATION_NAME").endsWith("_ci");
        return new ColumnMetaData(columnName, dataTypeMap.get(dataType), dataType, primaryKey, generated, caseSensitive);
    }
    
    private Map<String, Integer> getDataTypeMap(final Connection connection) throws SQLException {
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        try (ResultSet resultSet = connection.getMetaData().getTypeInfo()) {
            while (resultSet.next()) {
                result.put(resultSet.getString("TYPE_NAME"), resultSet.getInt("DATA_TYPE"));
            }
        }
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
