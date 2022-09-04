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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.datasource.state.exception.DataSourceStateException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data source state manager.
 */
@Slf4j
public final class DataSourceStateManager {
    
    private static final DataSourceStateManager INSTANCE = new DataSourceStateManager();
    
    private final Map<String, DataSourceState> dataSourceNameStates = new ConcurrentHashMap<>();
    
    private final Map<DataSource, DataSourceState> dataSourceStates = new ConcurrentHashMap<>();
    
    private volatile boolean started;
    
    private volatile boolean force;
    
    /**
     * Get data source state manager.
     *
     * @return data source state manager
     */
    public static DataSourceStateManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Mark has been started.
     */
    public void started() {
        started = true;
        dataSourceNameStates.clear();
        dataSourceStates.clear();
    }
    
    /**
     * Set data source states when bootstrap.
     *
     * @param databaseName database name
     * @param dataSources data sources
     * @param storageDataSourceStates storage node data source status
     */
    public void initStates(final String databaseName, final Map<String, DataSource> dataSources, final Map<String, DataSourceState> storageDataSourceStates) {
        dataSources.forEach((key, value) -> {
            DataSourceState storageState = storageDataSourceStates.get(getCacheKey(databaseName, key));
            if (DataSourceState.DISABLED == storageState) {
                dataSourceNameStates.put(getCacheKey(databaseName, key), storageState);
                dataSourceStates.put(value, storageState);
            } else {
                try (Connection ignored = value.getConnection()) {
                    log.info("Data source connection is successful.");
                } catch (final SQLException ex) {
                    throw new DataSourceStateException("DataSourceState", 1, "Data source status unavailable.", ex);
                }
                dataSourceNameStates.put(getCacheKey(databaseName, key), DataSourceState.ENABLED);
                dataSourceStates.put(value, DataSourceState.ENABLED);
            }
        });
    }
    
    /**
     * Filter disabled data sources.
     *
     * @param databaseConfigs database config
     * @return data sources in a non-disabled state
     */
    public Collection<DataSource> getNonDisabledDataSources(final Map<String, ? extends DatabaseConfiguration> databaseConfigs) {
        String databaseName = "";
        Map<String, DataSource> dataSourceMap = Collections.emptyMap();
        for (Entry<String, ? extends DatabaseConfiguration> entry : databaseConfigs.entrySet()) {
            DatabaseConfiguration value = entry.getValue();
            if (!value.getDataSources().isEmpty()) {
                databaseName = entry.getKey();
                dataSourceMap = value.getDataSources();
                break;
            }
        }
        return filterDisabledDataSources(databaseName, dataSourceMap).values();
    }
    
    /**
     * Filter disabled data sources.
     *
     * @param databaseName database name
     * @param dataSources data sources
     * @return data sources in a non-disabled state
     */
    public Map<String, DataSource> filterDisabledDataSources(final String databaseName, final Map<String, DataSource> dataSources) {
        if (started || force) {
            return dataSources;
        }
        Map<String, DataSource> result = new LinkedHashMap<>(dataSources.size(), 1);
        Map<String, DataSourceState> disabledDataSources = getDisabledStates(databaseName, dataSources);
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            String key = entry.getKey();
            if (!disabledDataSources.containsKey(getCacheKey(databaseName, key))) {
                result.put(key, dataSources.get(key));
            }
        }
        return result;
    }
    
    /**
     * Filter disabled data sources.
     * 
     * @param dataSources data sources
     * @return data sources in a non-disabled state
     */
    public Map<String, DataSource> filterDisabledDataSources(final Map<String, DataSource> dataSources) {
        if (started || force) {
            return dataSources;
        }
        Map<String, DataSource> result = new LinkedHashMap<>(dataSources.size(), 1);
        Map<DataSource, DataSourceState> disabledDataSources = getDisabledDataSources(dataSources);
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            String key = entry.getKey();
            if (!disabledDataSources.containsKey(entry.getValue())) {
                result.put(key, dataSources.get(key));
            }
        }
        return result;
    }
    
    private Map<String, DataSourceState> getDisabledStates(final String databaseName, final Map<String, DataSource> dataSources) {
        Map<String, DataSourceState> result = new HashMap<>(dataSources.size(), 1);
        dataSources.forEach((key, value) -> {
            DataSourceState state = dataSourceNameStates.get(getCacheKey(databaseName, key));
            if (state == DataSourceState.DISABLED) {
                result.put(getCacheKey(databaseName, key), state);
            }
        });
        return result;
    }
    
    private Map<DataSource, DataSourceState> getDisabledDataSources(final Map<String, DataSource> dataSources) {
        Map<DataSource, DataSourceState> result = new HashMap<>(dataSources.size(), 1);
        dataSources.forEach((key, value) -> {
            DataSourceState state = dataSourceStates.get(value);
            if (state == DataSourceState.DISABLED) {
                result.put(value, state);
            }
        });
        return result;
    }
    
    private String getCacheKey(final String databaseName, final String key) {
        return databaseName + "." + key;
    }
}
