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
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeTopologyConfiguration;
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
import java.util.Properties;

/**
 * Create production database runtimes from runtime properties.
 */
public final class DatabaseRuntimeFactory {
    
    private static final String DATABASE_NAME_KEY = "databaseName";
    
    private static final String LEGACY_DATABASE_NAMES_KEY = "databaseNames";
    
    private static final String LEGACY_DATABASES_PREFIX = "databases.";
    
    private final JdbcConnectionFactory jdbcConnectionFactory = new JdbcConnectionFactory();
    
    /**
     * Create connection configurations from runtime properties.
     *
     * @param props runtime properties
     * @return connection configurations keyed by logical database
     */
    public Map<String, DatabaseConnectionConfiguration> createConnectionConfigurations(final Properties props) {
        Properties actualProps = Objects.requireNonNull(props, "props cannot be null");
        validateLegacyRuntimeProperties(actualProps);
        return createConnectionConfigurations(new RuntimeTopologyConfiguration(Collections.singletonMap(getRequiredProperty(actualProps, DATABASE_NAME_KEY),
                new RuntimeDatabaseConfiguration(getRequiredProperty(actualProps, "databaseType"), getRequiredProperty(actualProps, "jdbcUrl"),
                        getProperty(actualProps, "username"), getProperty(actualProps, "password"), getProperty(actualProps, "driverClassName"),
                        getProperty(actualProps, "schemaPattern"), getProperty(actualProps, "defaultSchema"),
                        Boolean.parseBoolean(getProperty(actualProps, "supportsCrossSchemaSql", "false")),
                        Boolean.parseBoolean(getProperty(actualProps, "supportsExplainAnalyze", "false"))))));
    }
    
    /**
     * Create connection configurations from runtime topology configuration.
     *
     * @param runtimeTopologyConfiguration runtime topology configuration
     * @return connection configurations keyed by logical database
     * @throws IllegalArgumentException when no runtime database is configured
     */
    public Map<String, DatabaseConnectionConfiguration> createConnectionConfigurations(final RuntimeTopologyConfiguration runtimeTopologyConfiguration) {
        RuntimeTopologyConfiguration actualRuntimeTopologyConfiguration = Objects.requireNonNull(runtimeTopologyConfiguration, "runtimeTopologyConfiguration cannot be null");
        if (!actualRuntimeTopologyConfiguration.isConfigured()) {
            throw new IllegalArgumentException("At least one runtime database must be configured.");
        }
        return createConnectionConfigurations(actualRuntimeTopologyConfiguration.getDatabases());
    }
    
    private Map<String, DatabaseConnectionConfiguration> createConnectionConfigurations(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        Map<String, DatabaseConnectionConfiguration> result = new LinkedHashMap<>(runtimeDatabases.size(), 1F);
        for (Entry<String, RuntimeDatabaseConfiguration> entry : runtimeDatabases.entrySet()) {
            String databaseName = normalizeDatabaseName(entry.getKey());
            if (result.containsKey(databaseName)) {
                throw new IllegalArgumentException(String.format("Runtime logical database `%s` is duplicated.", databaseName));
            }
            result.put(databaseName, createConnectionConfiguration(databaseName, entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
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
        RuntimeDatabaseConfiguration actualRuntimeDatabaseConfiguration = Objects.requireNonNull(runtimeDatabaseConfiguration, "runtimeDatabaseConfiguration cannot be null");
        return new DatabaseConnectionConfiguration(databaseName, getRequiredValue(actualRuntimeDatabaseConfiguration.getDatabaseType(), databaseName, "databaseType"),
                getRequiredValue(actualRuntimeDatabaseConfiguration.getJdbcUrl(), databaseName, "jdbcUrl"), actualRuntimeDatabaseConfiguration.getUsername(),
                actualRuntimeDatabaseConfiguration.getPassword(), actualRuntimeDatabaseConfiguration.getDriverClassName(), actualRuntimeDatabaseConfiguration.getSchemaPattern(),
                actualRuntimeDatabaseConfiguration.getDefaultSchema(), actualRuntimeDatabaseConfiguration.isSupportsCrossSchemaSql(),
                actualRuntimeDatabaseConfiguration.isSupportsExplainAnalyze());
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
    
    private void validateLegacyRuntimeProperties(final Properties props) {
        if (!getProperty(props, LEGACY_DATABASE_NAMES_KEY).isEmpty()) {
            throw new IllegalArgumentException("Runtime property `databaseNames` is no longer supported. "
                    + "Configure a single database with `databaseName`, `databaseType`, and `jdbcUrl`.");
        }
        for (String each : props.stringPropertyNames()) {
            if (each.startsWith(LEGACY_DATABASES_PREFIX)) {
                throw new IllegalArgumentException("Runtime properties with `databases.<name>.*` are no longer supported. "
                        + "Configure a single database with `databaseName`, `databaseType`, and `jdbcUrl`.");
            }
        }
    }
    
    private String getRequiredProperty(final Properties props, final String key) {
        String result = getProperty(props, key);
        if (result.isEmpty()) {
            throw new IllegalArgumentException(String.format("Runtime property `%s` is required.", key));
        }
        return result;
    }
    
    private String getProperty(final Properties props, final String key) {
        return getProperty(props, key, "");
    }
    
    private String getProperty(final Properties props, final String key, final String defaultValue) {
        return Objects.requireNonNull(props.getProperty(key, defaultValue), "propertyValue cannot be null").trim();
    }
    
    private String normalizeDatabaseName(final String databaseName) {
        String result = Objects.requireNonNull(databaseName, "databaseName cannot be null").trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Runtime logical database name cannot be blank.");
        }
        return result;
    }
    
    private String getRequiredValue(final String value, final String databaseName, final String fieldName) {
        String result = Objects.requireNonNull(value, fieldName + " cannot be null").trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException(String.format("Runtime database `%s` property `%s` is required.", databaseName, fieldName));
        }
        return result;
    }
}
