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

package org.apache.shardingsphere.infra.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.jdbcurl.DialectJdbcUrlFetcher;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Database type engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseTypeEngine {
    
    private static final String DEFAULT_DATABASE_TYPE = "MySQL";
    
    /**
     * Get protocol type.
     *
     * @param databaseConfig database configuration
     * @param props configuration properties
     * @return protocol type
     */
    public static DatabaseType getProtocolType(final DatabaseConfiguration databaseConfig, final ConfigurationProperties props) {
        return getDatabaseType(databaseConfig.getStorageUnits(), props);
    }
    
    /**
     * Get protocol type.
     *
     * @param databaseConfigs database configurations
     * @param props configuration properties
     * @return protocol type
     */
    public static DatabaseType getProtocolType(final Map<String, DatabaseConfiguration> databaseConfigs, final ConfigurationProperties props) {
        return getDatabaseTypeFromDatabaseConfigurations(databaseConfigs, props);
    }
    
    /**
     * Get protocol type from storage type.
     *
     * @param storageType storage type
     * @return protocol type
     */
    public static DatabaseType getProtocolType(final DatabaseType storageType) {
        return storageType.getTrunkDatabaseType().orElse(storageType);
    }
    
    private static DatabaseType getDatabaseTypeFromDatabaseConfigurations(final Map<String, DatabaseConfiguration> databaseConfigs, final ConfigurationProperties props) {
        Optional<DatabaseType> configuredDatabaseType = findConfiguredDatabaseType(props);
        if (configuredDatabaseType.isPresent()) {
            return configuredDatabaseType.get();
        }
        for (DatabaseConfiguration each : databaseConfigs.values()) {
            if (!each.getStorageUnits().isEmpty()) {
                return getDatabaseType(each.getStorageUnits(), props);
            }
        }
        return getDefaultStorageType();
    }
    
    private static DatabaseType getDatabaseType(final Map<String, StorageUnit> storageUnits, final ConfigurationProperties props) {
        return findConfiguredDatabaseType(props).orElseGet(() -> storageUnits.isEmpty() ? getDefaultStorageType() : getProtocolType(storageUnits.values().iterator().next().getStorageType()));
    }
    
    private static Optional<DatabaseType> findConfiguredDatabaseType(final ConfigurationProperties props) {
        DatabaseType configuredDatabaseType = props.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE);
        return null == configuredDatabaseType ? Optional.empty() : Optional.of(configuredDatabaseType.getTrunkDatabaseType().orElse(configuredDatabaseType));
    }
    
    /**
     * Get storage type.
     *
     * @param dataSource data source
     * @return storage type
     * @throws SQLWrapperException SQL wrapper exception
     * @throws RuntimeException Runtime exception
     */
    public static DatabaseType getStorageType(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseTypeFactory.get(connection);
        } catch (final SQLFeatureNotSupportedException sqlFeatureNotSupportedException) {
            return findStorageType(dataSource).orElseThrow(() -> new SQLWrapperException(sqlFeatureNotSupportedException));
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
    
    /**
     * Get storage type.
     *
     * @param url storage unit URL
     * @param dataSource data source
     * @return storage type
     * @throws SQLWrapperException SQL wrapper exception
     * @throws RuntimeException Runtime exception
     */
    public static DatabaseType getStorageType(final String url, final DataSource dataSource) {
        DatabaseType result = DatabaseTypeFactory.get(url);
        if (!DatabaseTypeFactory.containsDetectableBranchDatabaseTypes(result)) {
            return result;
        }
        return getActualStorageType(result, dataSource);
    }
    
    /**
     * Get storage type.
     *
     * @param dataSourcePoolProps data source pool properties
     * @return storage type
     * @throws SQLWrapperException SQL wrapper exception
     * @throws RuntimeException Runtime exception
     */
    public static DatabaseType getStorageType(final DataSourcePoolProperties dataSourcePoolProps) {
        String url = getURL(dataSourcePoolProps);
        DatabaseType result = DatabaseTypeFactory.get(url);
        if (!DatabaseTypeFactory.containsDetectableBranchDatabaseTypes(result)) {
            return result;
        }
        try {
            loadDriverClass(dataSourcePoolProps);
            try (Connection connection = DriverManager.getConnection(url, getConnectionProperties(dataSourcePoolProps))) {
                return DatabaseTypeFactory.getActualDatabaseType(result, connection);
            }
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
    
    private static void loadDriverClass(final DataSourcePoolProperties dataSourcePoolProps) throws SQLException {
        Object driverClassName = dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties().get("driverClassName");
        if (null == driverClassName) {
            return;
        }
        try {
            Class.forName(driverClassName.toString());
        } catch (final ReflectiveOperationException ex) {
            throw new SQLException(ex);
        }
    }
    
    private static String getURL(final DataSourcePoolProperties dataSourcePoolProps) {
        return dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties().get("url").toString();
    }
    
    private static Properties getConnectionProperties(final DataSourcePoolProperties dataSourcePoolProps) {
        Map<String, Object> connectionProps = dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties();
        Properties result = new Properties();
        setConnectionProperty(result, connectionProps, "username", "user");
        setConnectionProperty(result, connectionProps, "password", "password");
        for (Entry<String, Object> entry : dataSourcePoolProps.getCustomProperties().getProperties().entrySet()) {
            if (entry.getValue() instanceof Properties) {
                result.putAll((Properties) entry.getValue());
            }
        }
        return result;
    }
    
    private static void setConnectionProperty(final Properties target, final Map<String, Object> props, final String sourceKey, final String targetKey) {
        if (null != props.get(sourceKey)) {
            target.setProperty(targetKey, props.get(sourceKey).toString());
        }
    }
    
    private static DatabaseType getActualStorageType(final DatabaseType databaseType, final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseTypeFactory.getActualDatabaseType(databaseType, connection);
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
    
    private static Optional<DatabaseType> findStorageType(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            for (DialectJdbcUrlFetcher each : ShardingSphereServiceLoader.getServiceInstances(DialectJdbcUrlFetcher.class)) {
                if (connection.isWrapperFor(each.getConnectionClass())) {
                    return Optional.of(DatabaseTypeFactory.get(each.fetch(connection)));
                }
            }
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
        return Optional.empty();
    }
    
    /**
     * Get default storage type.
     *
     * @return default storage type
     */
    public static DatabaseType getDefaultStorageType() {
        return TypedSPILoader.getService(DatabaseType.class, DEFAULT_DATABASE_TYPE);
    }
}
