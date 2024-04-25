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

package org.apache.shardingsphere.infra.state.datasource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.state.datasource.exception.UnavailableDataSourceException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Data source state manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class DataSourceStateManager {
    
    private static final DataSourceStateManager INSTANCE = new DataSourceStateManager();
    
    private final Map<String, DataSourceState> dataSourceStates = new ConcurrentHashMap<>();
    
    private volatile boolean forceStart;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
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
     * @param storageUnits storage units
     * @param storageDataSourceStates storage node data source state
     * @param forceStart whether to force start
     */
    public void initStates(final String databaseName, final Map<String, StorageUnit> storageUnits, final Map<String, DataSourceState> storageDataSourceStates, final boolean forceStart) {
        this.forceStart = forceStart;
        if (initialized.compareAndSet(false, true)) {
            storageUnits.forEach((key, value) -> initState(databaseName, storageDataSourceStates, key, value.getDataSource()));
        }
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
            ShardingSpherePreconditions.checkState(forceStart, () -> new UnavailableDataSourceException(actualDataSourceName, ex));
            log.error("Data source unavailable, ignored with the -f parameter.", ex);
        }
    }
    
    /**
     * Get enabled data sources.
     *
     * @param databaseName database name
     * @param databaseConfig database configuration
     * @return enabled data sources
     */
    public Map<String, DataSource> getEnabledDataSources(final String databaseName, final DatabaseConfiguration databaseConfig) {
        Map<String, DataSource> dataSources = databaseConfig.getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        return getEnabledDataSources(databaseName, dataSources);
    }
    
    /**
     * Get enabled data sources.
     *
     * @param databaseName database name
     * @param dataSources data sources
     * @return enabled data sources
     */
    public Map<String, DataSource> getEnabledDataSources(final String databaseName, final Map<String, DataSource> dataSources) {
        if (dataSources.isEmpty() || !initialized.get()) {
            return dataSources;
        }
        Map<String, DataSource> result = filterDisabledDataSources(databaseName, dataSources);
        checkForceConnection(result);
        return result;
    }
    
    private Map<String, DataSource> filterDisabledDataSources(final String databaseName, final Map<String, DataSource> dataSources) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSources.size(), 1F);
        dataSources.forEach((key, value) -> {
            DataSourceState dataSourceState = dataSourceStates.get(getCacheKey(databaseName, key));
            if (DataSourceState.DISABLED != dataSourceState) {
                result.put(key, value);
            }
        });
        return result;
    }
    
    private void checkForceConnection(final Map<String, DataSource> dataSources) {
        if (forceStart) {
            dataSources.entrySet().removeIf(entry -> {
                try (Connection ignored = entry.getValue().getConnection()) {
                    return false;
                } catch (final SQLException ex) {
                    log.error("Data source state unavailable, ignored with the -f parameter.", ex);
                    return true;
                }
            });
        }
    }
    
    /**
     * Update data source state.
     * 
     * @param qualifiedDataSource qualified data source
     * @param dataSourceState data source state
     */
    public void updateState(final QualifiedDataSource qualifiedDataSource, final DataSourceState dataSourceState) {
        dataSourceStates.put(getCacheKey(qualifiedDataSource.getDatabaseName(), qualifiedDataSource.getDataSourceName()), dataSourceState);
    }
    
    private String getCacheKey(final String databaseName, final String dataSourceName) {
        return databaseName + "." + dataSourceName;
    }
}
