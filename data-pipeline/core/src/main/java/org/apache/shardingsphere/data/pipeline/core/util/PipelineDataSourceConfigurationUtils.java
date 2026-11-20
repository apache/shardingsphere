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

package org.apache.shardingsphere.data.pipeline.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for pipeline data source configuration.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
@Slf4j
public final class PipelineDataSourceConfigurationUtils {
    
    private static final String MAX_POOL_SIZE = "maxPoolSize";
    
    /**
     * Transform the given pipeline data source properties.
     *
     * @param jobId job id
     * @param pipelineDataSourceConfig the pipeline data source configuration to transform
     * @param storageUnits current storage units
     * @return transformed pipeline data source configuration
     */
    public static PipelineDataSourceConfiguration transformPipelineDataSourceConfiguration(final String jobId, final PipelineDataSourceConfiguration pipelineDataSourceConfig,
                                                                                           final Map<String, StorageUnit> storageUnits) {
        if (null == storageUnits || storageUnits.isEmpty()) {
            return pipelineDataSourceConfig;
        }
        if (pipelineDataSourceConfig instanceof ShardingSpherePipelineDataSourceConfiguration) {
            return transformPipelineDataSourceConfiguration(jobId, (ShardingSpherePipelineDataSourceConfiguration) pipelineDataSourceConfig, storageUnits);
        }
        return pipelineDataSourceConfig;
    }
    
    /**
     * Transform the given pipeline data source properties.
     *
     * @param jobId job id
     * @param dataSourceName data source name
     * @param pipelineDataSourceConfig the pipeline data source configuration to transform
     * @param storageUnits current storage units
     * @return transformed pipeline data source configuration
     */
    public static PipelineDataSourceConfiguration transformPipelineDataSourceConfiguration(final String jobId, final String dataSourceName,
                                                                                           final PipelineDataSourceConfiguration pipelineDataSourceConfig,
                                                                                           final Map<String, StorageUnit> storageUnits) {
        if (null == storageUnits || storageUnits.isEmpty()) {
            return pipelineDataSourceConfig;
        }
        if (pipelineDataSourceConfig instanceof StandardPipelineDataSourceConfiguration) {
            StorageUnit storageUnit = storageUnits.get(dataSourceName);
            return null == storageUnit ? pipelineDataSourceConfig
                    : transformStandardPipelineDataSourceConfiguration(jobId, dataSourceName, (StandardPipelineDataSourceConfiguration) pipelineDataSourceConfig, storageUnit);
        }
        return transformPipelineDataSourceConfiguration(jobId, pipelineDataSourceConfig, storageUnits);
    }
    
    /**
     * Transform the given pipeline data source properties.
     *
     * @param jobId job id
     * @param dataSourceName data source name
     * @param pipelineDataSourceConfig the pipeline data source configuration to transform
     * @param dataSourcePoolProps current data source pool properties
     * @return transformed pipeline data source configuration
     */
    public static PipelineDataSourceConfiguration transformPipelineDataSourceConfiguration(final String jobId, final String dataSourceName,
                                                                                           final PipelineDataSourceConfiguration pipelineDataSourceConfig,
                                                                                           final DataSourcePoolProperties dataSourcePoolProps) {
        return pipelineDataSourceConfig instanceof StandardPipelineDataSourceConfiguration
                ? transformStandardPipelineDataSourceConfiguration(jobId, dataSourceName, (StandardPipelineDataSourceConfiguration) pipelineDataSourceConfig, dataSourcePoolProps)
                : pipelineDataSourceConfig;
    }
    
    /**
     * Transform the given ShardingSphere pipeline data source properties.
     *
     * @param jobId job id
     * @param pipelineDataSourceConfig the pipeline data source configuration to transform
     * @param storageUnits current storage units
     * @return transformed pipeline data source configuration
     */
    public static ShardingSpherePipelineDataSourceConfiguration transformPipelineDataSourceConfiguration(final String jobId,
                                                                                                         final ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig,
                                                                                                         final Map<String, StorageUnit> storageUnits) {
        if (null == storageUnits || storageUnits.isEmpty()) {
            return pipelineDataSourceConfig;
        }
        Map<String, Map<String, Object>> savedDataSources = YamlEngine.unmarshal(pipelineDataSourceConfig.getParameter(), YamlRootConfiguration.class, true).getDataSources();
        for (Entry<String, DataSourcePoolProperties> entry : pipelineDataSourceConfig.getDataSourcePoolPropertiesMap().entrySet()) {
            StorageUnit storageUnit = storageUnits.get(entry.getKey());
            if (null == storageUnit) {
                continue;
            }
            Map<String, Object> savedProps = savedDataSources.get(entry.getKey());
            DataSourcePoolProperties livePoolProps = storageUnit.getDataSourcePoolProperties();
            if (!Objects.equals(savedProps.getOrDefault("url", savedProps.get("jdbcUrl")), livePoolProps.getConnectionPropertySynonyms().getStandardProperties().get("url"))) {
                continue;
            }
            DataSourcePoolProperties jobPoolProps = entry.getValue();
            getChangedMaxPoolSize(jobPoolProps, livePoolProps).ifPresent(maxPoolSize -> {
                Map<String, Object> props = jobPoolProps.getAllLocalProperties();
                updateMaxPoolSize(jobId, entry.getKey(), jobPoolProps, props, maxPoolSize);
                entry.setValue(new DataSourcePoolProperties(jobPoolProps.getPoolClassName(), props));
            });
        }
        return pipelineDataSourceConfig;
    }
    
    private static Optional<Object> getChangedMaxPoolSize(final DataSourcePoolProperties jobPoolProps, final DataSourcePoolProperties currentPoolProps) {
        Optional<Object> maxPoolSize = Optional.ofNullable(currentPoolProps.getPoolPropertySynonyms().getStandardProperties().get(MAX_POOL_SIZE));
        if (!maxPoolSize.isPresent()) {
            maxPoolSize = TypedSPILoader.findService(DataSourcePoolMetaData.class, currentPoolProps.getPoolClassName())
                    .map(optional -> optional.getDefaultProperties().get(optional.getPropertySynonyms().getOrDefault(MAX_POOL_SIZE, MAX_POOL_SIZE)));
        }
        Object oldMaxPoolSize = jobPoolProps.getPoolPropertySynonyms().getStandardProperties().get(MAX_POOL_SIZE);
        return maxPoolSize.filter(size -> !String.valueOf(size).equals(String.valueOf(oldMaxPoolSize)));
    }
    
    private static void updateMaxPoolSize(final String jobId, final String dataSourceName, final DataSourcePoolProperties jobPoolProps, final Map<String, Object> props, final Object maxPoolSize) {
        log.info("Transform maxPoolSize from '{}' to '{}' for {} data source: {}", jobPoolProps.getPoolPropertySynonyms().getStandardProperties().get(MAX_POOL_SIZE), maxPoolSize, jobId,
                dataSourceName);
        props.put(MAX_POOL_SIZE, maxPoolSize);
    }
    
    private static PipelineDataSourceConfiguration transformStandardPipelineDataSourceConfiguration(final String jobId, final String dataSourceName,
                                                                                                    final StandardPipelineDataSourceConfiguration pipelineDataSourceConfig,
                                                                                                    final StorageUnit storageUnit) {
        return transformStandardPipelineDataSourceConfiguration(jobId, dataSourceName, pipelineDataSourceConfig, storageUnit.getDataSourcePoolProperties());
    }
    
    @SuppressWarnings("unchecked")
    private static PipelineDataSourceConfiguration transformStandardPipelineDataSourceConfiguration(final String jobId, final String dataSourceName,
                                                                                                    final StandardPipelineDataSourceConfiguration pipelineDataSourceConfig,
                                                                                                    final DataSourcePoolProperties dataSourcePoolProps) {
        DataSourcePoolProperties jobPoolProps = (DataSourcePoolProperties) pipelineDataSourceConfig.getDataSourceConfiguration();
        Optional<Object> maxPoolSize = getChangedMaxPoolSize(jobPoolProps, dataSourcePoolProps);
        if (!maxPoolSize.isPresent()) {
            return pipelineDataSourceConfig;
        }
        Map<String, Object> props = YamlEngine.unmarshal(pipelineDataSourceConfig.getParameter(), Map.class);
        if (!Objects.equals(props.getOrDefault("jdbcUrl", props.get("url")), dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties().get("url"))) {
            return pipelineDataSourceConfig;
        }
        updateMaxPoolSize(jobId, dataSourceName, jobPoolProps, props, maxPoolSize.get());
        props.remove("maximumPoolSize");
        return new StandardPipelineDataSourceConfiguration(props);
    }
}
