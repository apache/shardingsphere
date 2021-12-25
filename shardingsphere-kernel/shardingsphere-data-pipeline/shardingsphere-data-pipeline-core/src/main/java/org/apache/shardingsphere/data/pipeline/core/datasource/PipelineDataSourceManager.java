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
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pipeline data source manager.
 */
@NoArgsConstructor
@Getter
@Slf4j
public final class PipelineDataSourceManager implements AutoCloseable {
    
    private final PipelineDataSourceFactory dataSourceFactory = new PipelineDataSourceFactory();
    
    private final Map<PipelineDataSourceConfiguration, PipelineDataSourceWrapper> cachedDataSources = new ConcurrentHashMap<>();
    
    private final Map<PipelineDataSourceConfiguration, PipelineDataSourceWrapper> sourceDataSources = new ConcurrentHashMap<>();
    
    private final Map<PipelineDataSourceConfiguration, PipelineDataSourceWrapper> targetDataSources = new ConcurrentHashMap<>();
    
    /**
     * Create source data source.
     *
     * @param pipelineDataSourceConfig pipeline data source configuration
     */
    public void createSourceDataSource(final PipelineDataSourceConfiguration pipelineDataSourceConfig) {
        PipelineDataSourceWrapper dataSource = dataSourceFactory.newInstance(pipelineDataSourceConfig);
        cachedDataSources.put(pipelineDataSourceConfig, dataSource);
        sourceDataSources.put(pipelineDataSourceConfig, dataSource);
    }
    
    /**
     * Create target data source.
     *
     * @param pipelineDataSourceConfig pipeline data source configuration
     */
    public void createTargetDataSource(final PipelineDataSourceConfiguration pipelineDataSourceConfig) {
        PipelineDataSourceWrapper dataSource = dataSourceFactory.newInstance(pipelineDataSourceConfig);
        cachedDataSources.put(pipelineDataSourceConfig, dataSource);
        targetDataSources.put(pipelineDataSourceConfig, dataSource);
    }
    
    /**
     * Get data source.
     *
     * @param dataSourceConfig data source configuration
     * @return data source
     */
    public PipelineDataSourceWrapper getDataSource(final PipelineDataSourceConfiguration dataSourceConfig) {
        // TODO re-init if existing dataSource was closed
        if (cachedDataSources.containsKey(dataSourceConfig)) {
            return cachedDataSources.get(dataSourceConfig);
        }
        synchronized (cachedDataSources) {
            if (cachedDataSources.containsKey(dataSourceConfig)) {
                return cachedDataSources.get(dataSourceConfig);
            }
            PipelineDataSourceWrapper result = dataSourceFactory.newInstance(dataSourceConfig);
            cachedDataSources.put(dataSourceConfig, result);
            return result;
        }
    }
    
    /**
     * Close, close cached data source.
     */
    @Override
    public void close() {
        for (PipelineDataSourceWrapper each : cachedDataSources.values()) {
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
