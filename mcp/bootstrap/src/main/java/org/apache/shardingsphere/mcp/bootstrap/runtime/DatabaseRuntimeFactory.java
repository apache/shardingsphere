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

import lombok.Getter;
import org.apache.shardingsphere.mcp.execute.ExecuteQueryFacade.DatabaseRuntime;
import org.apache.shardingsphere.mcp.execute.ShardingSphereExecutionAdapter;
import org.apache.shardingsphere.mcp.execute.ShardingSphereExecutionAdapter.ConnectionProvider;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.MetadataCatalog;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
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
        String databaseName = getRequiredProperty(actualProps, DATABASE_NAME_KEY);
        Map<String, DatabaseConnectionConfiguration> result = new LinkedHashMap<>(1, 1F);
        result.put(databaseName, new DatabaseConnectionConfiguration(databaseName, getRequiredProperty(actualProps, "databaseType"),
                getRequiredProperty(actualProps, "jdbcUrl"), getProperty(actualProps, "username"), getProperty(actualProps, "password"),
                getProperty(actualProps, "driverClassName"), getProperty(actualProps, "schemaPattern"),
                getProperty(actualProps, "defaultSchema"), Boolean.parseBoolean(getProperty(actualProps, "supportsCrossSchemaSql", "false")),
                Boolean.parseBoolean(getProperty(actualProps, "supportsExplainAnalyze", "false"))));
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
        return new DatabaseRuntime(executionAdapter, ignored -> refreshMetadata(connectionConfigurations, metadataCatalog, metadataLoader));
    }
    
    private void refreshMetadata(final Map<String, DatabaseConnectionConfiguration> connectionConfigurations,
                                 final MetadataCatalog metadataCatalog, final JdbcMetadataLoader metadataLoader) {
        MetadataCatalog refreshedCatalog = metadataLoader.load(connectionConfigurations);
        metadataCatalog.replaceSnapshot(refreshedCatalog.getDatabaseTypes(), refreshedCatalog.getMetadataObjects(), refreshedCatalog.getRuntimeDatabaseDescriptors());
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
    
    /**
     * JDBC connection configuration for one logical database.
     */
    @Getter
    public static final class DatabaseConnectionConfiguration {
        
        private final String database;
        
        private final String databaseType;
        
        private final String jdbcUrl;
        
        private final String username;
        
        private final String password;
        
        private final String driverClassName;
        
        private final String schemaPattern;
        
        private final String defaultSchema;
        
        private final boolean supportsCrossSchemaSql;
        
        private final boolean supportsExplainAnalyze;
        
        /**
         * Construct one JDBC connection configuration.
         *
         * @param database logical database name
         * @param databaseType database type
         * @param jdbcUrl JDBC URL
         * @param username username
         * @param password password
         * @param driverClassName driver class name
         * @param schemaPattern schema pattern
         * @param defaultSchema default schema
         * @param supportsCrossSchemaSql cross-schema SQL support flag
         * @param supportsExplainAnalyze explain analyze support flag
         */
        public DatabaseConnectionConfiguration(final String database, final String databaseType, final String jdbcUrl, final String username,
                                               final String password, final String driverClassName, final String schemaPattern,
                                               final String defaultSchema, final boolean supportsCrossSchemaSql, final boolean supportsExplainAnalyze) {
            this.database = Objects.requireNonNull(database, "database cannot be null");
            this.databaseType = Objects.requireNonNull(databaseType, "databaseType cannot be null");
            this.jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl cannot be null");
            this.username = Objects.requireNonNull(username, "username cannot be null");
            this.password = Objects.requireNonNull(password, "password cannot be null");
            this.driverClassName = Objects.requireNonNull(driverClassName, "driverClassName cannot be null");
            this.schemaPattern = Objects.requireNonNull(schemaPattern, "schemaPattern cannot be null");
            this.defaultSchema = Objects.requireNonNull(defaultSchema, "defaultSchema cannot be null");
            this.supportsCrossSchemaSql = supportsCrossSchemaSql;
            this.supportsExplainAnalyze = supportsExplainAnalyze;
        }
    }
}
