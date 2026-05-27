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
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Utility class for pipeline data source configuration.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
@Slf4j
public final class PipelineDataSourceConfigurationUtils {
    
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
        for (Entry<String, Map<String, Object>> entry : pipelineDataSourceConfig.getRootConfig().getDataSources().entrySet()) {
            StorageUnit storageUnit = storageUnits.get(entry.getKey());
            if (null == storageUnit) {
                continue;
            }
            Map<String, Object> jobDataSourceProps = entry.getValue();
            Map<String, Object> storageUnitStandardProps = storageUnit.getDataSourcePoolProperties().getPoolPropertySynonyms().getStandardProperties();
            logTransformPoolSize(jobId, entry.getKey(), jobDataSourceProps, storageUnitStandardProps);
            transformPoolSize(jobDataSourceProps, storageUnitStandardProps);
        }
        return pipelineDataSourceConfig;
    }
    
    private static PipelineDataSourceConfiguration transformStandardPipelineDataSourceConfiguration(final String jobId, final String dataSourceName,
                                                                                                    final StandardPipelineDataSourceConfiguration pipelineDataSourceConfig,
                                                                                                    final StorageUnit storageUnit) {
        return transformStandardPipelineDataSourceConfiguration(jobId, dataSourceName, pipelineDataSourceConfig, storageUnit.getDataSourcePoolProperties());
    }
    
    private static PipelineDataSourceConfiguration transformStandardPipelineDataSourceConfiguration(final String jobId, final String dataSourceName,
                                                                                                    final StandardPipelineDataSourceConfiguration pipelineDataSourceConfig,
                                                                                                    final DataSourcePoolProperties dataSourcePoolProps) {
        DataSourcePoolProperties jobDataSourcePoolProps = (DataSourcePoolProperties) pipelineDataSourceConfig.getDataSourceConfiguration();
        Map<String, Object> jobStandardProps = jobDataSourcePoolProps.getPoolPropertySynonyms().getStandardProperties();
        Map<String, Object> currentStandardProps = dataSourcePoolProps.getPoolPropertySynonyms().getStandardProperties();
        if (!isPoolSizeChanged(jobStandardProps, currentStandardProps)) {
            return pipelineDataSourceConfig;
        }
        logTransformPoolSize(jobId, dataSourceName, jobStandardProps, currentStandardProps);
        return new StandardPipelineDataSourceConfiguration(new YamlDataSourceConfigurationSwapper().swapToMap(dataSourcePoolProps));
    }
    
    private static boolean isPoolSizeChanged(final Map<String, Object> jobDataSourceProps, final Map<String, Object> storageUnitStandardProps) {
        return isPoolSizeChanged("maxPoolSize", jobDataSourceProps, storageUnitStandardProps)
                || isPoolSizeChanged("maximumPoolSize", jobDataSourceProps, storageUnitStandardProps);
    }
    
    private static boolean isPoolSizeChanged(final String key, final Map<String, Object> jobDataSourceProps, final Map<String, Object> storageUnitStandardProps) {
        return storageUnitStandardProps.containsKey(key) && !Objects.equals(String.valueOf(jobDataSourceProps.get(key)), String.valueOf(storageUnitStandardProps.get(key)));
    }
    
    private static void logTransformPoolSize(final String jobId, final String dataSourceName, final Map<String, Object> jobDataSourceProps, final Map<String, Object> storageUnitStandardProps) {
        if (storageUnitStandardProps.containsKey("maxPoolSize")) {
            log.info("Transform maxPoolSize from '{}' to '{}' for {} data source: {}",
                    jobDataSourceProps.get("maxPoolSize"), storageUnitStandardProps.get("maxPoolSize"), jobId, dataSourceName);
        }
        if (storageUnitStandardProps.containsKey("maximumPoolSize")) {
            log.info("Transform maximumPoolSize from '{}' to '{}' for {} data source: {}",
                    jobDataSourceProps.get("maximumPoolSize"), storageUnitStandardProps.get("maximumPoolSize"), jobId, dataSourceName);
        }
    }
    
    private static void transformPoolSize(final Map<String, Object> jobDataSourceProps, final Map<String, Object> storageUnitStandardProps) {
        if (storageUnitStandardProps.containsKey("maxPoolSize")) {
            jobDataSourceProps.put("maxPoolSize", storageUnitStandardProps.get("maxPoolSize"));
        }
        if (storageUnitStandardProps.containsKey("maximumPoolSize")) {
            jobDataSourceProps.put("maximumPoolSize", storageUnitStandardProps.get("maximumPoolSize"));
        }
    }
}
