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

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderConnection;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.SystemDatabase;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * Schema meta data loader.
 */
public final class SchemaMetaDataLoader {
    
    private static final String TABLE_TYPE = "TABLE";
    
    private static final String PARTITIONED_TABLE_TYPE = "PARTITIONED TABLE";
    
    private static final String VIEW_TYPE = "VIEW";
    
    private static final String SYSTEM_TABLE_TYPE = "SYSTEM TABLE";
    
    private static final String SYSTEM_VIEW_TYPE = "SYSTEM VIEW";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private static final String TABLE_SCHEME = "TABLE_SCHEM";
    
    private final DatabaseType databaseType;
    
    private final DialectSchemaOption schemaOption;
    
    public SchemaMetaDataLoader(final DatabaseType databaseType) {
        this.databaseType = databaseType;
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
        schemaOption = dialectDatabaseMetaData.getSchemaOption();
    }
    
    /**
     * Load schema table names.
     *
     * @param databaseName database name
     * @param dataSource data source
     * @param excludedTables excluded tables
     * @return loaded schema table names
     * @throws SQLException SQL exception
     */
    public Map<String, Collection<String>> loadSchemaTableNames(final String databaseName, final DataSource dataSource, final Collection<String> excludedTables) throws SQLException {
        try (MetaDataLoaderConnection connection = new MetaDataLoaderConnection(databaseType, dataSource.getConnection())) {
            Collection<String> schemaNames = loadSchemaNames(connection);
            Map<String, Collection<String>> result = new CaseInsensitiveMap<>(schemaNames.size(), 1F);
            for (String each : schemaNames) {
                String schemaName = schemaOption.getDefaultSchema().isPresent() ? each : databaseName;
                result.put(schemaName, loadTableNames(connection, each, excludedTables));
            }
            return result;
        }
    }
    
    /**
     * Load schema names.
     *
     * @param connection connection
     * @return schema names collection
     * @throws SQLException SQL exception
     */
    public Collection<String> loadSchemaNames(final Connection connection) throws SQLException {
        if (!schemaOption.getDefaultSchema().isPresent()) {
            return Collections.singletonList(connection.getSchema());
        }
        Collection<String> result = new LinkedList<>();
        SystemDatabase systemDatabase = new SystemDatabase(databaseType);
        try (ResultSet resultSet = connection.getMetaData().getSchemas()) {
            while (resultSet.next()) {
                String schema = resultSet.getString(TABLE_SCHEME);
                if (!systemDatabase.getSystemSchemas().contains(schema)) {
                    result.add(schema);
                }
            }
        }
        return result.isEmpty() ? Collections.singletonList(connection.getSchema()) : result;
    }
    
    private Collection<String> loadTableNames(final Connection connection, final String schemaName, final Collection<String> excludedTables) throws SQLException {
        Collection<String> result = new CaseInsensitiveSet<>();
        String[] tableTypes = new String[]{TABLE_TYPE, PARTITIONED_TABLE_TYPE, VIEW_TYPE, SYSTEM_TABLE_TYPE, SYSTEM_VIEW_TYPE};
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), schemaName, null, tableTypes)) {
            while (resultSet.next()) {
                String table = resultSet.getString(TABLE_NAME);
                if (!isSystemTable(table) && !excludedTables.contains(table)) {
                    result.add(table);
                }
            }
        }
        return result;
    }
    
    private boolean isSystemTable(final String table) {
        return table.contains("$") || table.contains("/") || table.contains("##");
    }
}
