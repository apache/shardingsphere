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

package org.apache.shardingsphere.database.connector.core.metadata.data.loader.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;

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
@Slf4j
public final class ColumnMetaDataLoader {
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    
    private static final String DATA_TYPE = "DATA_TYPE";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private static final String IS_NULLABLE = "IS_NULLABLE";
    
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
        List<Boolean> nullableFlags = new ArrayList<>();
        try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), tableNamePattern, "%")) {
            while (resultSet.next()) {
                String tableName = resultSet.getString(TABLE_NAME);
                if (Objects.equals(tableNamePattern, tableName)) {
                    String columnName = resultSet.getString(COLUMN_NAME);
                    columnTypes.add(resultSet.getInt(DATA_TYPE));
                    primaryKeyFlags.add(primaryKeys.contains(columnName));
                    nullableFlags.add("YES".equals(resultSet.getString(IS_NULLABLE)));
                    columnNames.add(columnName);
                }
            }
        }
        String emptyResultSQL = generateEmptyResultSQL(tableNamePattern, columnNames, databaseType);
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(emptyResultSQL)) {
            for (int i = 0; i < columnNames.size(); i++) {
                boolean generated = resultSet.getMetaData().isAutoIncrement(i + 1);
                caseSensitiveFlags.add(resultSet.getMetaData().isCaseSensitive(resultSet.findColumn(columnNames.get(i))));
                result.add(new ColumnMetaData(columnNames.get(i), columnTypes.get(i), primaryKeyFlags.get(i), generated, caseSensitiveFlags.get(i), true, false, nullableFlags.get(i)));
            }
        } catch (final SQLException ex) {
            log.error("Error occurred while loading column meta data, SQL: {}", emptyResultSQL, ex);
            throw ex;
        }
        return result;
    }
    
    private static String generateEmptyResultSQL(final String table, final List<String> columnNames, final DatabaseType databaseType) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
        String wrappedColumnNames = columnNames.stream().map(each -> dialectDatabaseMetaData.getQuoteCharacter().wrap(each)).collect(Collectors.joining(","));
        return String.format("SELECT %s FROM %s WHERE 1 != 1", wrappedColumnNames, dialectDatabaseMetaData.getQuoteCharacter().wrap(table));
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
