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

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.util.exception.external.sql.SQLWrapperException;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.util.exception.sql.SQLWrapperException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
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
     *
     * @param databaseName database name
     * @param databaseConfig database configuration
     * @param props props
     * @return protocol type
     */
    public static DatabaseType getProtocolType(final String databaseName, final DatabaseConfiguration databaseConfig, final ConfigurationProperties props) {
        return findConfiguredDatabaseType(props).orElseGet(() -> getDatabaseType(DataSourceStateManager.getInstance().getNonDisabledDataSources(databaseName, databaseConfig)));
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
        if (configuredDatabaseType.isPresent()) {
            return configuredDatabaseType.get();
        }
        return getDatabaseType(DataSourceStateManager.getInstance().getNonDisabledDataSources(databaseConfigs));
    }
    
    /**
     * Get storage type.
     *
     * @param databaseConfigs database configs
     * @return storage type
     */
    public static DatabaseType getStorageType(final Map<String, ? extends DatabaseConfiguration> databaseConfigs) {
        return getDatabaseType(DataSourceStateManager.getInstance().getNonDisabledDataSources(databaseConfigs));
    }
    
    /**
     * Get database type.
     *
     * @param url database URL
     * @return database type
     */
    public static DatabaseType getDatabaseType(final String url) {
        return DatabaseTypeFactory.getInstances().stream().filter(each -> matchURLs(url, each)).findAny().orElseGet(() -> DatabaseTypeFactory.getInstance("SQL92"));
    }
    
    /**
     * Get database type.
     * 
     * @param dataSources data sources
     * @return database type
     */
    public static DatabaseType getDatabaseType(final Collection<DataSource> dataSources) {
        DatabaseType result = null;
        for (DataSource each : dataSources) {
            DatabaseType databaseType = getDatabaseType(each);
            Preconditions.checkState(null == result || result == databaseType, "Database type inconsistent with '%s' and '%s'", result, databaseType);
            result = databaseType;
        }
        return null == result ? DatabaseTypeFactory.getInstance(DEFAULT_DATABASE_TYPE) : result;
    }
    
    private static DatabaseType getDatabaseType(final DataSource dataSource) {
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
    
    /**
     * Get trunk database type.
     *
     * @param name database name 
     * @return trunk database type
     */
    public static DatabaseType getTrunkDatabaseType(final String name) {
        DatabaseType databaseType = DatabaseTypeFactory.getInstance(name);
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
     * @param databaseType database type
     * @param databaseName database name
     * @return default schema name
     */
    public static String getDefaultSchemaName(final DatabaseType databaseType, final String databaseName) {
        return databaseType instanceof SchemaSupportedDatabaseType ? ((SchemaSupportedDatabaseType) databaseType).getDefaultSchema() : databaseName.toLowerCase();
    }
}
