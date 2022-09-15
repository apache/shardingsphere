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

package org.apache.shardingsphere.infra.datasource.state;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.datasource.state.exception.DataSourceStateException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data source state manager.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceStateManager {
    
    private static final DataSourceStateManager INSTANCE = new DataSourceStateManager();
    
    private final Map<String, DataSourceState> dataSourceStates = new ConcurrentHashMap<>();
    
    private volatile boolean force;
    
    private volatile boolean initialized;
    
    /**
     * Get data source state manager.
     *
     * @return data source state manager
     */
    public static DataSourceStateManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Set data source states when bootstrap.
     *
     * @param databaseName database name
     * @param dataSources data sources
     * @param storageDataSourceStates storage node data source state
     * @param force whether to force start
     */
    public void initStates(final String databaseName, final Map<String, DataSource> dataSources, final Map<String, DataSourceState> storageDataSourceStates, final boolean force) {
        this.force = force;
        dataSources.forEach((key, value) -> {
            initState(databaseName, storageDataSourceStates, key, value);
        });
        initialized = true;
    }
    
    private void initState(final String databaseName, final Map<String, DataSourceState> storageDataSourceStates, final String actualDataSourceName, final DataSource dataSource) {
        DataSourceState storageState = storageDataSourceStates.get(getCacheKey(databaseName, actualDataSourceName));
        if (DataSourceState.DISABLED == storageState) {
            dataSourceStates.put(getCacheKey(databaseName, actualDataSourceName), storageState);
        } else {
            checkState(databaseName, actualDataSourceName, dataSource);
        }
    }
    
    private void checkState(final String databaseName, final String actualDataSourceName, final DataSource dataSource) {
        try (Connection ignored = dataSource.getConnection()) {
            dataSourceStates.put(getCacheKey(databaseName, actualDataSourceName), DataSourceState.ENABLED);
        } catch (final SQLException ex) {
            if (this.force) {
                log.error("Data source state unavailable, ignored with the -f parameter.", ex);
            } else {
                throw new DataSourceStateException("DataSourceState", 1, "Data source state unavailable.", ex);
            }
        }
    }
    
    /**
     * Get enabled data sources.
     *
     * @param databaseName database name
     * @param databaseConfig database config
     * @return enabled data sources
     */
    public Collection<DataSource> getEnabledDataSources(final String databaseName, final DatabaseConfiguration databaseConfig) {
        return databaseConfig.getDataSources().isEmpty() ? Collections.emptyList() : getEnabledDataSourceMap(databaseName, databaseConfig.getDataSources()).values();
    }
    
    /**
     * Get enabled data source map.
     *
     * @param databaseName database name
     * @param dataSources data sources
     * @return enabled data source map
     */
    public Map<String, DataSource> getEnabledDataSourceMap(final String databaseName, final Map<String, DataSource> dataSources) {
        if (dataSources.isEmpty() || !initialized) {
            return dataSources;
        }
        Map<String, DataSource> result = new LinkedHashMap<>(dataSources.size(), 1);
        dataSources.forEach((key, value) -> {
            DataSourceState dataSourceState = dataSourceStates.get(getCacheKey(databaseName, key));
            boolean isValidDataSource = force ? DataSourceState.ENABLED == dataSourceState : DataSourceState.DISABLED != dataSourceState;
            if (isValidDataSource) {
                result.put(key, value);
            }
        });
        return result;
    }
    
    private String getCacheKey(final String databaseName, final String dataSourceName) {
        return databaseName + "." + dataSourceName;
    }
}
