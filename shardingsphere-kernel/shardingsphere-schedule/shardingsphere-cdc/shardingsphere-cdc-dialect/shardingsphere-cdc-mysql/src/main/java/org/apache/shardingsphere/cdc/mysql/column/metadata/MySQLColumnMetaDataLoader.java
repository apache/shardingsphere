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

package org.apache.shardingsphere.cdc.mysql.column.metadata;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * MySQL column meta data loader.
 */
@RequiredArgsConstructor
public final class MySQLColumnMetaDataLoader {
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    
    private static final String TYPE_NAME = "TYPE_NAME";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private final Map<String, List<MySQLColumnMetaData>> columnMetaDataMap = new HashMap<>();
    
    private final DataSource dataSource;
    
    /**
     * Load column meta data list.
     *
     * @param tableNamePattern table name pattern
     * @return column meta data list
     */
    public List<MySQLColumnMetaData> load(final String tableNamePattern) {
        if (!columnMetaDataMap.containsKey(tableNamePattern)) {
            try (Connection connection = dataSource.getConnection()) {
                columnMetaDataMap.put(tableNamePattern, load0(connection, tableNamePattern));
            } catch (SQLException ex) {
                throw new RuntimeException(String.format("Load metaData for table %s failed", tableNamePattern), ex);
            }
        }
        return columnMetaDataMap.get(tableNamePattern);
    }
    
    private List<MySQLColumnMetaData> load0(final Connection connection, final String tableNamePattern) throws SQLException {
        List<MySQLColumnMetaData> result = new LinkedList<>();
        Collection<String> primaryKeys = loadPrimaryKeys(connection, tableNamePattern);
        List<String> columnNames = new ArrayList<>();
        List<String> columnTypeNames = new ArrayList<>();
        List<Boolean> isPrimaryKeys = new ArrayList<>();
        try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), tableNamePattern, "%")) {
            while (resultSet.next()) {
                String tableName = resultSet.getString(TABLE_NAME);
                if (Objects.equals(tableNamePattern, tableName)) {
                    String columnName = resultSet.getString(COLUMN_NAME);
                    columnTypeNames.add(resultSet.getString(TYPE_NAME));
                    isPrimaryKeys.add(primaryKeys.contains(columnName));
                    columnNames.add(columnName);
                }
            }
        }
        for (int i = 0; i < columnNames.size(); i++) {
            result.add(new MySQLColumnMetaData(columnNames.get(i), columnTypeNames.get(i), isPrimaryKeys.get(i)));
        }
        return result;
    }
    
    private Collection<String> loadPrimaryKeys(final Connection connection, final String table) throws SQLException {
        Collection<String> result = new HashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(connection.getCatalog(), connection.getSchema(), table)) {
            while (resultSet.next()) {
                result.add(resultSet.getString(COLUMN_NAME));
            }
        }
        return result;
    }
}
