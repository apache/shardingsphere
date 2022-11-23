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
import org.apache.shardingsphere.infra.datasource.state.exception.UnavailableDataSourceException;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class DataSourceStateManager {
    
    private static final DataSourceStateManager INSTANCE = new DataSourceStateManager();
    
    private final Map<String, DataSourceState> physicalStates = new ConcurrentHashMap<>();
    
    private final Map<String, DataSourceState> logicalStates = new ConcurrentHashMap<>();
    
    private volatile boolean forceStart;
    
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
     * @param forceStart whether to force start
     */
    public void initStates(final String databaseName, final Map<String, DataSource> dataSources, final Map<String, DataSourceState> storageDataSourceStates, final boolean forceStart) {
        this.forceStart = forceStart;
        dataSources.forEach((key, value) -> initState(databaseName, storageDataSourceStates, key, value));
        initialized = true;
    }
    
    private void initState(final String databaseName, final Map<String, DataSourceState> storageDataSourceStates, final String actualDataSourceName, final DataSource dataSource) {
        DataSourceState logicStorageState = storageDataSourceStates.get(getCacheKey(databaseName, actualDataSourceName));
        if (DataSourceState.DISABLED == logicStorageState) {
            setLogicalState(databaseName, actualDataSourceName, DataSourceState.DISABLED);
        } else {
            setLogicalState(databaseName, actualDataSourceName, DataSourceState.ENABLED);
            checkPhysicalState(databaseName, actualDataSourceName, dataSource);
        }
    }
    
    private void setLogicalState(final String databaseName, final String actualDataSourceName, final DataSourceState dataSourceState) {
        logicalStates.put(getCacheKey(databaseName, actualDataSourceName), dataSourceState);
    }
    
    private void checkPhysicalState(final String databaseName, final String actualDataSourceName, final DataSource dataSource) {
        try (Connection ignored = dataSource.getConnection()) {
            setPhysicalState(databaseName, actualDataSourceName, DataSourceState.OK);
        } catch (final SQLException ex) {
            ShardingSpherePreconditions.checkState(forceStart, UnavailableDataSourceException::new);
            setPhysicalState(databaseName, actualDataSourceName, DataSourceState.ERROR);
            log.error("Data source unavailable, ignored with the -f parameter.", ex);
        }
    }
    
    private void setPhysicalState(final String databaseName, final String actualDataSourceName, final DataSourceState dataSourceState) {
        physicalStates.put(getCacheKey(databaseName, actualDataSourceName), dataSourceState);
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
        return filterDataSources(databaseName, dataSources);
    }
    
    private Map<String, DataSource> filterDataSources(final String databaseName, final Map<String, DataSource> dataSources) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSources.size(), 1);
        dataSources.forEach((key, value) -> {
            filterDataSource(databaseName, result, key, value);
        });
        return result;
    }
    
    private void filterDataSource(final String databaseName, final Map<String, DataSource> result, final String key, final DataSource value) {
        if (forceStart) {
            if (DataSourceState.DISABLED != getLogicalState(databaseName, key) && DataSourceState.ERROR != getPhysicalState(databaseName, key)) {
                result.put(key, value);
            }
        } else {
            if (DataSourceState.DISABLED != getLogicalState(databaseName, key)) {
                result.put(key, value);
            }
        }
    }
    
    /**
     * Update physical data source state.
     *
     * @param databaseName database name
     * @param actualDataSourceName actual data source name
     * @param dataSourceState data source state
     */
    public void updatePhysicalState(final String databaseName, final String actualDataSourceName, final DataSourceState dataSourceState) {
        if (!dataSourceState.equals(physicalStates.get(getCacheKey(databaseName, actualDataSourceName)))) {
            setPhysicalState(databaseName, actualDataSourceName, dataSourceState);
        }
    }
    
    /**
     * Update logical data source state.
     *
     * @param databaseName database name
     * @param actualDataSourceName actual data source name
     * @param dataSourceState data source state
     */
    public void updateLogicalState(final String databaseName, final String actualDataSourceName, final DataSourceState dataSourceState) {
        if (!dataSourceState.equals(logicalStates.get(getCacheKey(databaseName, actualDataSourceName)))) {
            setLogicalState(databaseName, actualDataSourceName, dataSourceState);
        }
    }
    
    /**
     * Get physical data source state.
     *
     * @param databaseName database name
     * @param actualDataSourceName actual data source name
     * @return data source state
     */
    public DataSourceState getPhysicalState(final String databaseName, final String actualDataSourceName) {
        return physicalStates.get(getCacheKey(databaseName, actualDataSourceName));
    }
    
    /**
     * Get logical data source state.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source state
     */
    public DataSourceState getLogicalState(final String databaseName, final String dataSourceName) {
        return logicalStates.get(getCacheKey(databaseName, dataSourceName));
    }
    
    private String getCacheKey(final String databaseName, final String dataSourceName) {
        return databaseName + "." + dataSourceName;
    }
}
