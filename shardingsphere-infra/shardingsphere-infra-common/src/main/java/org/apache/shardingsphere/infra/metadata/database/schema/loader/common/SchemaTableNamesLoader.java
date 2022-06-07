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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.adapter.MetaDataLoaderConnectionAdapter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Schema table names loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaTableNamesLoader {
    
    private static final String TABLE_TYPE = "TABLE";
    
    private static final String VIEW_TYPE = "VIEW";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private static final String TABLE_SCHEME = "TABLE_SCHEM";
    
    /**
     * Load schema table names.
     *
     * @param databaseName database name
     * @param databaseType database type
     * @param dataSource data source
     * @return loaded schema table names
     * @throws SQLException SQL exception
     */
    public static Map<String, Collection<String>> loadSchemaTableNames(final String databaseName, final DatabaseType databaseType, final DataSource dataSource) throws SQLException {
        try (MetaDataLoaderConnectionAdapter connectionAdapter = new MetaDataLoaderConnectionAdapter(databaseType, dataSource.getConnection())) {
            Collection<String> schemaNames = loadSchemaNames(connectionAdapter, databaseType);
            return loadSchemaTableNames(connectionAdapter, databaseName, databaseType, schemaNames);
        }
    }
    
    private static Map<String, Collection<String>> loadSchemaTableNames(final Connection connection, final String databaseName,
                                                                        final DatabaseType databaseType, final Collection<String> schemaNames) throws SQLException {
        Map<String, Collection<String>> result = new HashMap<>(schemaNames.size(), 1);
        for (String each : schemaNames) {
            String schemaName = databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType ? each : databaseName;
            result.put(schemaName, loadSchemaTableNames(connection, each));
        }
        return result;
    }
    
    private static Collection<String> loadSchemaTableNames(final Connection connection, final String schemaName) throws SQLException {
        Collection<String> result = new LinkedList<>();
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), schemaName, null, new String[]{TABLE_TYPE, VIEW_TYPE})) {
            while (resultSet.next()) {
                String table = resultSet.getString(TABLE_NAME);
                if (!isSystemTable(table)) {
                    result.add(table);
                }
            }
        }
        return result;
    }
    
    private static Collection<String> loadSchemaNames(final Connection connection, final DatabaseType databaseType) throws SQLException {
        if (!(databaseType instanceof PostgreSQLDatabaseType) && !(databaseType instanceof OpenGaussDatabaseType)) {
            return Collections.singletonList(connection.getSchema());
        }
        Collection<String> result = new LinkedList<>();
        try (ResultSet resultSet = connection.getMetaData().getSchemas()) {
            while (resultSet.next()) {
                String schema = resultSet.getString(TABLE_SCHEME);
                if (!databaseType.getSystemSchemas().contains(schema)) {
                    result.add(schema);
                }
            }
        }
        return result.isEmpty() ? Collections.singletonList(connection.getSchema()) : result;
    }
    
    private static boolean isSystemTable(final String table) {
        return table.contains("$") || table.contains("/");
    }
}
