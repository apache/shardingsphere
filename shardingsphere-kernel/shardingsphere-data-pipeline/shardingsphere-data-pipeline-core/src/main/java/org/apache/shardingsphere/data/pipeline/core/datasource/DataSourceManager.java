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

package org.apache.shardingsphere.data.pipeline.core.datasource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfiguration;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data source manager.
 */
@NoArgsConstructor
@Getter
@Slf4j
public final class DataSourceManager implements AutoCloseable {
    
    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();
    
    private final Map<JDBCDataSourceConfiguration, DataSourceWrapper> cachedDataSources = new ConcurrentHashMap<>();
    
    private final Map<JDBCDataSourceConfiguration, DataSourceWrapper> sourceDataSources = new ConcurrentHashMap<>();
    
    private final Map<JDBCDataSourceConfiguration, DataSourceWrapper> targetDataSources = new ConcurrentHashMap<>();
    
    /**
     * Create source data source.
     *
     * @param dataSourceConfig data source configuration
     */
    public void createSourceDataSource(final JDBCDataSourceConfiguration dataSourceConfig) {
        DataSourceWrapper dataSource = dataSourceFactory.newInstance(dataSourceConfig);
        cachedDataSources.put(dataSourceConfig, dataSource);
        sourceDataSources.put(dataSourceConfig, dataSource);
    }
    
    /**
     * Create target data source.
     *
     * @param dataSourceConfig data source configuration
     */
    public void createTargetDataSource(final JDBCDataSourceConfiguration dataSourceConfig) {
        DataSourceWrapper dataSource = dataSourceFactory.newInstance(dataSourceConfig);
        cachedDataSources.put(dataSourceConfig, dataSource);
        targetDataSources.put(dataSourceConfig, dataSource);
    }
    
    /**
     * Get data source.
     *
     * @param dataSourceConfig data source configuration
     * @return data source
     */
    public DataSourceWrapper getDataSource(final JDBCDataSourceConfiguration dataSourceConfig) {
        // TODO re-init if existing dataSource was closed
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
            } catch (final SQLException ex) {
                log.error("An exception occurred while closing the data source", ex);
            }
        }
        cachedDataSources.clear();
        sourceDataSources.clear();
    }
}
