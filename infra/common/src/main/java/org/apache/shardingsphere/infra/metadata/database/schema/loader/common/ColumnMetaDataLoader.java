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

package org.apache.shardingsphere.infra.metadata.database.schema.loader.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Column meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnMetaDataLoader {
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    
    private static final String DATA_TYPE = "DATA_TYPE";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    /**
     * Load column meta data list.
     *
     * @param connection connection
     * @param tableNamePattern table name pattern
     * @param databaseType database type
     * @return column meta data list
     * @throws SQLException SQL exception
     */
    public static Collection<ColumnMetaData> load(final Connection connection, final String tableNamePattern, final DatabaseType databaseType) throws SQLException {
        Collection<ColumnMetaData> result = new LinkedList<>();
        Collection<String> primaryKeys = loadPrimaryKeys(connection, tableNamePattern);
        List<String> columnNames = new ArrayList<>();
        List<Integer> columnTypes = new ArrayList<>();
        List<Boolean> primaryKeyFlags = new ArrayList<>();
        List<Boolean> caseSensitiveFlags = new ArrayList<>();
        try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), tableNamePattern, "%")) {
            while (resultSet.next()) {
                String tableName = resultSet.getString(TABLE_NAME);
                if (Objects.equals(tableNamePattern, tableName)) {
                    String columnName = resultSet.getString(COLUMN_NAME);
                    columnTypes.add(resultSet.getInt(DATA_TYPE));
                    primaryKeyFlags.add(primaryKeys.contains(columnName));
                    columnNames.add(columnName);
                }
            }
        }
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(generateEmptyResultSQL(tableNamePattern, columnNames, databaseType))) {
            for (int i = 0; i < columnNames.size(); i++) {
                boolean generated = resultSet.getMetaData().isAutoIncrement(i + 1);
                caseSensitiveFlags.add(resultSet.getMetaData().isCaseSensitive(resultSet.findColumn(columnNames.get(i))));
                result.add(new ColumnMetaData(columnNames.get(i), columnTypes.get(i), primaryKeyFlags.get(i), generated, caseSensitiveFlags.get(i), true, false));
            }
        }
        return result;
    }
    
    private static String generateEmptyResultSQL(final String table, final List<String> columnNames, final DatabaseType databaseType) {
        String wrappedColumnNames = columnNames.stream().map(each -> databaseType.getQuoteCharacter().wrap(each)).collect(Collectors.joining(","));
        return String.format("SELECT %s FROM %s WHERE 1 != 1", wrappedColumnNames, databaseType.getQuoteCharacter().wrap(table));
    }
    
    private static Collection<String> loadPrimaryKeys(final Connection connection, final String table) throws SQLException {
        Collection<String> result = new HashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(connection.getCatalog(), connection.getSchema(), table)) {
            while (resultSet.next()) {
                result.add(resultSet.getString(COLUMN_NAME));
            }
        }
        return result;
    }
}
