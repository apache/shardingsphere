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

package org.apache.shardingsphere.infra.database.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Database type engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseTypeEngine {
    
    private static final String DEFAULT_DATABASE_TYPE = "MySQL";
    
    /**
     * Get protocol type.
     * 
     * @param databaseName database name
     * @param databaseConfig database configuration
     * @param props props
     * @return protocol type
     */
    public static DatabaseType getProtocolType(final String databaseName, final DatabaseConfiguration databaseConfig, final ConfigurationProperties props) {
        return findConfiguredDatabaseType(props).orElseGet(() -> getStorageType(DataSourceStateManager.getInstance().getEnabledDataSources(databaseName, databaseConfig)));
    }
    
    /**
     * Get protocol type.
     *
     * @param databaseConfigs database configurations
     * @param props props
     * @return protocol type
     */
    public static DatabaseType getProtocolType(final Map<String, ? extends DatabaseConfiguration> databaseConfigs, final ConfigurationProperties props) {
        Optional<DatabaseType> configuredDatabaseType = findConfiguredDatabaseType(props);
        return configuredDatabaseType.orElseGet(() -> getStorageType(getEnabledDataSources(databaseConfigs).values()));
    }
    
    private static Map<String, DataSource> getEnabledDataSources(final Map<String, ? extends DatabaseConfiguration> databaseConfigs) {
        Map<String, DataSource> result = new LinkedHashMap<>();
        for (Entry<String, ? extends DatabaseConfiguration> entry : databaseConfigs.entrySet()) {
            result.putAll(DataSourceStateManager.getInstance().getEnabledDataSourceMap(entry.getKey(), entry.getValue().getDataSources()));
        }
        return result;
    }
    
    /**
     * Get storage types.
     *
     * @param databaseName database name
     * @param databaseConfig database configuration
     * @return storage types
     */
    public static Map<String, DatabaseType> getStorageTypes(final String databaseName, final DatabaseConfiguration databaseConfig) {
        Map<String, DatabaseType> result = new LinkedHashMap<>(databaseConfig.getDataSources().size(), 1F);
        Map<String, DataSource> enabledDataSources = DataSourceStateManager.getInstance().getEnabledDataSourceMap(databaseName, databaseConfig.getDataSources());
        for (Entry<String, DataSource> entry : enabledDataSources.entrySet()) {
            result.put(entry.getKey(), getStorageType(entry.getValue()));
        }
        return result;
    }
    
    /**
     * Get storage types.
     *
     * @param dataSources data sources
     * @return storage types
     */
    public static Map<String, DatabaseType> getStorageTypes(final Map<String, DataSource> dataSources) {
        Map<String, DatabaseType> result = new LinkedHashMap<>(dataSources.size(), 1F);
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            result.put(entry.getKey(), getStorageType(entry.getValue()));
        }
        return result;
    }
    
    /**
     * Get database type.
     *
     * @param url database URL
     * @return database type
     */
    public static DatabaseType getDatabaseType(final String url) {
        return ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class).stream().filter(each -> matchURLs(url, each))
                .max(Comparator.comparing(each -> getMatchedUrlPrefixLength(each, url))).orElseGet(() -> TypedSPILoader.getService(DatabaseType.class, "SQL92"));
    }
    
    /**
     * Get storage type.
     *
     * @param dataSources data sources
     * @return storage type
     */
    public static DatabaseType getStorageType(final Collection<DataSource> dataSources) {
        return dataSources.isEmpty() ? TypedSPILoader.getService(DatabaseType.class, DEFAULT_DATABASE_TYPE) : getStorageType(dataSources.iterator().next());
    }
    
    private static DatabaseType getStorageType(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return getDatabaseType(connection.getMetaData().getURL());
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
    
    private static Optional<DatabaseType> findConfiguredDatabaseType(final ConfigurationProperties props) {
        String configuredDatabaseType = props.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE);
        return configuredDatabaseType.isEmpty() ? Optional.empty() : Optional.of(DatabaseTypeEngine.getTrunkDatabaseType(configuredDatabaseType));
    }
    
    private static boolean matchURLs(final String url, final DatabaseType databaseType) {
        return databaseType.getJdbcUrlPrefixes().stream().anyMatch(url::startsWith);
    }
    
    private static Integer getMatchedUrlPrefixLength(final DatabaseType databaseType, final String url) {
        return databaseType.getJdbcUrlPrefixes().stream().filter(url::startsWith).findFirst().map(String::length).orElse(0);
    }
    
    /**
     * Get trunk database type.
     *
     * @param name database name 
     * @return trunk database type
     */
    public static DatabaseType getTrunkDatabaseType(final String name) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, name);
        return databaseType instanceof BranchDatabaseType ? ((BranchDatabaseType) databaseType).getTrunkDatabaseType() : databaseType;
    }
    
    /**
     * Get name of trunk database type.
     *
     * @param databaseType database type
     * @return name of trunk database type
     */
    public static String getTrunkDatabaseTypeName(final DatabaseType databaseType) {
        return databaseType instanceof BranchDatabaseType ? ((BranchDatabaseType) databaseType).getTrunkDatabaseType().getType() : databaseType.getType();
    }
    
    /**
     * Get default schema name.
     * 
     * @param protocolType protocol type
     * @param databaseName database name
     * @return default schema name
     */
    public static String getDefaultSchemaName(final DatabaseType protocolType, final String databaseName) {
        return protocolType instanceof SchemaSupportedDatabaseType ? ((SchemaSupportedDatabaseType) protocolType).getDefaultSchema() : databaseName.toLowerCase();
    }
    
    /**
     * Get default schema name.
     *
     * @param protocolType protocol type
     * @return default schema name
     */
    public static Optional<String> getDefaultSchemaName(final DatabaseType protocolType) {
        return protocolType instanceof SchemaSupportedDatabaseType ? Optional.of(((SchemaSupportedDatabaseType) protocolType).getDefaultSchema()) : Optional.empty();
    }
}
