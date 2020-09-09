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

package org.apache.shardingsphere.scaling.core.datasource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data source manager.
 */
@Slf4j
@NoArgsConstructor
public final class DataSourceManager implements AutoCloseable {
    
    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();

    @Getter
    private final Map<DataSourceConfiguration, DataSourceWrapper> cachedDataSources = new ConcurrentHashMap<>();

    @Getter
    private final Map<DataSourceConfiguration, DataSourceWrapper> sourceDatasources = new ConcurrentHashMap<>();

    public DataSourceManager(final List<SyncConfiguration> syncConfigs) {
        createDatasources(syncConfigs);
    }
    
    private void createDatasources(final List<SyncConfiguration> syncConfigs) {
        createSourceDatasources(syncConfigs);
        createTargetDatasources(syncConfigs.iterator().next().getImporterConfiguration().getDataSourceConfiguration());
    }
    
    private void createSourceDatasources(final List<SyncConfiguration> syncConfigs) {
        for (SyncConfiguration syncConfiguration : syncConfigs) {
            DataSourceConfiguration dataSourceConfig = syncConfiguration.getDumperConfiguration().getDataSourceConfiguration();
            DataSourceWrapper hikariDataSource = dataSourceFactory.newInstance(dataSourceConfig);
            cachedDataSources.put(dataSourceConfig, hikariDataSource);
            sourceDatasources.put(dataSourceConfig, hikariDataSource);
        }
    }
    
    private void createTargetDatasources(final DataSourceConfiguration dataSourceConfig) {
        cachedDataSources.put(dataSourceConfig, dataSourceFactory.newInstance(dataSourceConfig));
    }
    
    /**
     * Get data source by {@code DataSourceConfiguration}.
     *
     * @param dataSourceConfig data source configuration
     * @return data source
     */
    public DataSource getDataSource(final DataSourceConfiguration dataSourceConfig) {
        if (cachedDataSources.containsKey(dataSourceConfig)) {
            return cachedDataSources.get(dataSourceConfig);
        }
        synchronized (cachedDataSources) {
            if (cachedDataSources.containsKey(dataSourceConfig)) {
                return cachedDataSources.get(dataSourceConfig);
            }
            DataSourceWrapper result = dataSourceFactory.newInstance(dataSourceConfig);
            cachedDataSources.put(dataSourceConfig, result);
            return result;
        }
    }
    
    /**
     * Close, close cached data source.
     */
    @Override
    public void close() {
        for (DataSourceWrapper each : cachedDataSources.values()) {
            try {
                each.close();
            } catch (IOException e) {
                log.warn("An exception occurred while closing the data source", e);
            }
        }
        cachedDataSources.clear();
        sourceDatasources.clear();
    }
}
