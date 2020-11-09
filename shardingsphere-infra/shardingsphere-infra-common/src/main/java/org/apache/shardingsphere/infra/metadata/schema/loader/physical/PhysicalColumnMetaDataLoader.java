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

package org.apache.shardingsphere.infra.metadata.schema.loader.physical;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.loader.physical.jdbc.handler.DatabaseMetaDataDialectHandler;
import org.apache.shardingsphere.infra.metadata.schema.loader.physical.jdbc.handler.DatabaseMetaDataDialectHandlerFactory;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Physical column meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PhysicalColumnMetaDataLoader {
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    
    private static final String DATA_TYPE = "DATA_TYPE";
    
    private static final String TYPE_NAME = "TYPE_NAME";
    
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
        List<String> columnTypeNames = new ArrayList<>();
        List<Boolean> isPrimaryKeys = new ArrayList<>();
        List<Boolean> isCaseSensitives = new ArrayList<>();
        try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), tableNamePattern, "%")) {
            while (resultSet.next()) {
                String tableName = resultSet.getString(TABLE_NAME);
                if (Objects.equals(tableNamePattern, tableName)) {
                    String columnName = resultSet.getString(COLUMN_NAME);
                    columnTypes.add(resultSet.getInt(DATA_TYPE));
                    columnTypeNames.add(resultSet.getString(TYPE_NAME));
                    isPrimaryKeys.add(primaryKeys.contains(columnName));
                    columnNames.add(columnName);
                }
            }
        }
        try (ResultSet resultSet = connection.createStatement().executeQuery(generateEmptyResultSQL(tableNamePattern, databaseType))) {
            for (String each : columnNames) {
                isCaseSensitives.add(resultSet.getMetaData().isCaseSensitive(resultSet.findColumn(each)));
            }
        }
        for (int i = 0; i < columnNames.size(); i++) {
            // TODO load auto generated from database meta data
            result.add(new ColumnMetaData(columnNames.get(i), columnTypes.get(i), columnTypeNames.get(i), isPrimaryKeys.get(i), false, isCaseSensitives.get(i)));
        }
        return result;
    }
    
    private static String generateEmptyResultSQL(final String table, final DatabaseType databaseType) {
        QuoteCharacter quoteCharacter = DatabaseMetaDataDialectHandlerFactory.findHandler(databaseType).map(DatabaseMetaDataDialectHandler::getQuoteCharacter).orElse(QuoteCharacter.NONE);
        return String.format("SELECT * FROM %s WHERE 1 != 1", quoteCharacter.wrap(table));
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
