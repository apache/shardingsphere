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

package org.apache.shardingsphere.mcp.bootstrap.runtime;

import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.execute.ShardingSphereExecutionAdapter;
import org.apache.shardingsphere.mcp.execute.ShardingSphereExecutionAdapter.ConnectionProvider;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.RuntimeDatabaseDescriptor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Create production database runtimes from runtime databases.
 */
public final class DatabaseRuntimeFactory {
    
    private final JdbcConnectionFactory jdbcConnectionFactory = new JdbcConnectionFactory();
    
    /**
     * Create connection configurations from runtime databases.
     *
     * @param runtimeDatabases runtime databases
     * @return connection configurations keyed by logical database
     * @throws IllegalArgumentException when no runtime database is configured
     */
    public Map<String, DatabaseConnectionConfiguration> createConnectionConfigurations(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        if (runtimeDatabases.isEmpty()) {
            throw new IllegalArgumentException("At least one runtime database must be configured.");
        }
        return buildConnectionConfigurations(runtimeDatabases);
    }
    
    private Map<String, DatabaseConnectionConfiguration> buildConnectionConfigurations(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        Map<String, DatabaseConnectionConfiguration> result = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> entry : runtimeDatabases.entrySet()) {
            String databaseName = validateDatabaseName(entry.getKey());
            if (result.containsKey(databaseName)) {
                throw new IllegalArgumentException(String.format("Runtime logical database `%s` is duplicated.", databaseName));
            }
            result.put(databaseName, createConnectionConfiguration(databaseName, entry.getValue()));
        }
        return result;
    }
    
    /**
     * Create one adapter-backed database runtime.
     *
     * @param connectionConfigurations connection configurations
     * @param metadataCatalog metadata catalog
     * @param metadataLoader metadata loader
     * @return database runtime
     */
    public DatabaseRuntime createDatabaseRuntime(final Map<String, DatabaseConnectionConfiguration> connectionConfigurations,
                                                 final MetadataCatalog metadataCatalog, final JdbcMetadataLoader metadataLoader) {
        Map<String, ConnectionProvider> connectionProviders = new LinkedHashMap<>(connectionConfigurations.size(), 1F);
        for (DatabaseConnectionConfiguration each : connectionConfigurations.values()) {
            connectionProviders.put(each.getDatabase(), () -> jdbcConnectionFactory.openConnection(each));
        }
        ShardingSphereExecutionAdapter executionAdapter = new ShardingSphereExecutionAdapter(connectionProviders);
        return new DatabaseRuntime(executionAdapter, database -> refreshMetadata(database, connectionConfigurations, metadataCatalog, metadataLoader));
    }
    
    private DatabaseConnectionConfiguration createConnectionConfiguration(final String databaseName, final RuntimeDatabaseConfiguration runtimeDatabaseConfiguration) {
        return new DatabaseConnectionConfiguration(databaseName, validateRequiredValue(runtimeDatabaseConfiguration.getDatabaseType(), databaseName, "databaseType"),
                validateRequiredValue(runtimeDatabaseConfiguration.getJdbcUrl(), databaseName, "jdbcUrl"), runtimeDatabaseConfiguration.getUsername(),
                runtimeDatabaseConfiguration.getPassword(), runtimeDatabaseConfiguration.getDriverClassName());
    }
    
    private void refreshMetadata(final String database, final Map<String, DatabaseConnectionConfiguration> connectionConfigurations,
                                 final MetadataCatalog metadataCatalog, final JdbcMetadataLoader metadataLoader) {
        DatabaseConnectionConfiguration connectionConfiguration = Objects.requireNonNull(connectionConfigurations.get(database),
                String.format("Database `%s` is not configured.", database));
        MetadataCatalog refreshedCatalog = metadataLoader.load(Collections.singletonMap(database, connectionConfiguration));
        RuntimeDatabaseDescriptor runtimeDatabaseDescriptor = Objects.requireNonNull(refreshedCatalog.getRuntimeDatabaseDescriptors().get(database),
                "runtimeDatabaseDescriptor cannot be null");
        metadataCatalog.replaceDatabaseSnapshot(database, refreshedCatalog.getDatabaseTypes().get(database), refreshedCatalog.getMetadataObjects(), runtimeDatabaseDescriptor);
    }
    
    private String validateDatabaseName(final String databaseName) {
        if (databaseName.isBlank()) {
            throw new IllegalArgumentException("Runtime logical database name cannot be blank.");
        }
        return databaseName;
    }
    
    private String validateRequiredValue(final String value, final String databaseName, final String fieldName) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(String.format("Runtime database `%s` property `%s` is required.", databaseName, fieldName));
        }
        return value;
    }
}
