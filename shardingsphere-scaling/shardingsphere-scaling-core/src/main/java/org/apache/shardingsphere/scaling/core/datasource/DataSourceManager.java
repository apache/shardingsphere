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

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.scaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data source manager.
 */
@NoArgsConstructor
public final class DataSourceManager implements AutoCloseable {
    
    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();

    @Getter
    private final ConcurrentHashMap<DataSourceConfiguration, HikariDataSource> cachedDataSources = new ConcurrentHashMap<>();

    @Getter
    private final ConcurrentHashMap<DataSourceConfiguration, HikariDataSource> sourceDatasources = new ConcurrentHashMap<>();

    public DataSourceManager(final List<SyncConfiguration> syncConfigurations) {
        createDatasources(syncConfigurations);
    }
    
    private void createDatasources(final List<SyncConfiguration> syncConfigurations) {
        createSourceDatasources(syncConfigurations);
        createTargetDatasources(syncConfigurations.iterator().next().getImporterConfiguration().getDataSourceConfiguration());
    }
    
    private void createSourceDatasources(final List<SyncConfiguration> syncConfigurations) {
        for (SyncConfiguration syncConfiguration : syncConfigurations) {
            DataSourceConfiguration dataSourceConfiguration = syncConfiguration.getDumperConfiguration().getDataSourceConfiguration();
            HikariDataSource hikariDataSource = (HikariDataSource) dataSourceFactory.newInstance(dataSourceConfiguration);
            cachedDataSources.put(dataSourceConfiguration, hikariDataSource);
            sourceDatasources.put(dataSourceConfiguration, hikariDataSource);
        }
    }
    
    private void createTargetDatasources(final DataSourceConfiguration dataSourceConfiguration) {
        cachedDataSources.put(dataSourceConfiguration, (HikariDataSource) dataSourceFactory.newInstance(dataSourceConfiguration));
    }
    
    /**
     * Get data source by {@code DataSourceConfiguration}.
     *
     * @param dataSourceConfiguration data source configuration
     * @return data source
     */
    public DataSource getDataSource(final DataSourceConfiguration dataSourceConfiguration) {
        if (cachedDataSources.containsKey(dataSourceConfiguration)) {
            return cachedDataSources.get(dataSourceConfiguration);
        }
        synchronized (cachedDataSources) {
            if (cachedDataSources.containsKey(dataSourceConfiguration)) {
                return cachedDataSources.get(dataSourceConfiguration);
            }
            HikariDataSource result = (HikariDataSource) dataSourceFactory.newInstance(dataSourceConfiguration);
            cachedDataSources.put(dataSourceConfiguration, result);
            return result;
        }
    }
    
    /**
     * Close, close cached data source.
     */
    @Override
    public void close() {
        for (HikariDataSource each : cachedDataSources.values()) {
            if (!each.isClosed()) {
                each.close();
            }
        }
        cachedDataSources.clear();
        sourceDatasources.clear();
    }
}
