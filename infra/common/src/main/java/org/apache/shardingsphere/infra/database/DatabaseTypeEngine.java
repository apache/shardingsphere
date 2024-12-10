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
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Optional<DatabaseType> configuredDatabaseType = findConfiguredDatabaseType(props);
        if (configuredDatabaseType.isPresent()) {
            return configuredDatabaseType.get();
        }
        Collection<DataSource> dataSources = getDataSources(databaseConfig).values();
        return dataSources.isEmpty() ? getDefaultStorageType() : getStorageType(dataSources.iterator().next());
    }
    
    /**
     * Get protocol type.
     *
     * @param databaseConfigs database configurations
     * @param props configuration properties
     * @return protocol type
     */
    public static DatabaseType getProtocolType(final Map<String, ? extends DatabaseConfiguration> databaseConfigs, final ConfigurationProperties props) {
        Optional<DatabaseType> configuredDatabaseType = findConfiguredDatabaseType(props);
        if (configuredDatabaseType.isPresent()) {
            return configuredDatabaseType.get();
        }
        Map<String, DataSource> dataSources = getDataSources(databaseConfigs);
        return dataSources.isEmpty() ? getDefaultStorageType() : getStorageType(dataSources.values().iterator().next());
    }
    
    private static Optional<DatabaseType> findConfiguredDatabaseType(final ConfigurationProperties props) {
        DatabaseType configuredDatabaseType = props.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE);
        return null == configuredDatabaseType ? Optional.empty() : Optional.of(configuredDatabaseType.getTrunkDatabaseType().orElse(configuredDatabaseType));
    }
    
    private static Map<String, DataSource> getDataSources(final Map<String, ? extends DatabaseConfiguration> databaseConfigs) {
        Map<String, DataSource> result = new LinkedHashMap<>();
        for (Entry<String, ? extends DatabaseConfiguration> entry : databaseConfigs.entrySet()) {
            result.putAll(getDataSources(entry.getValue()));
        }
        return result;
    }
    
    private static Map<String, DataSource> getDataSources(final DatabaseConfiguration databaseConfig) {
        return databaseConfig.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    /**
     * Get storage type.
     * Similar to <a href="https://github.com/apache/hive/pull/5554">apache/hive#5554</a>,
     * apache/hive 4.0.1's `org.apache.hive.jdbc.HiveDatabaseMetaData` does not implement {@link DatabaseMetaData#getURL()}.
     * So use {@link java.sql.Wrapper#isWrapperFor(Class)} to try fuzzy matching.
     *
     * @param dataSource data source
     * @return storage type
     * @throws SQLWrapperException SQL wrapper exception
     * @throws RuntimeException Runtime exception
     */
    public static DatabaseType getStorageType(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseTypeFactory.get(connection.getMetaData().getURL());
        } catch (final SQLFeatureNotSupportedException sqlFeatureNotSupportedException) {
            try (Connection connection = dataSource.getConnection()) {
                Class<?> hiveConnectionClass = Class.forName("org.apache.hive.jdbc.HiveConnection");
                if (connection.isWrapperFor(hiveConnectionClass)) {
                    Object hiveConnection = connection.unwrap(hiveConnectionClass);
                    String connectedUrl = (String) hiveConnectionClass.getMethod("getConnectedUrl").invoke(hiveConnection);
                    return DatabaseTypeFactory.get(connectedUrl);
                }
                throw new SQLWrapperException(sqlFeatureNotSupportedException);
            } catch (final SQLException ex) {
                throw new SQLWrapperException(ex);
            } catch (final ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                throw new SQLWrapperException(new SQLException(ex));
            }
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
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
