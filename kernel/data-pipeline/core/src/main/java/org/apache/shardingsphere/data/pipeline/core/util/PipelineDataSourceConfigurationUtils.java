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
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;

import java.util.Map;
import java.util.Map.Entry;

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
     */
    public static void transformPipelineDataSourceConfiguration(final String jobId, final ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig) {
        YamlRootConfiguration rootConfig = pipelineDataSourceConfig.getRootConfig();
        ShardingSphereDatabase database;
        try {
            database = PipelineContextManager.getProxyContext().getDatabase(rootConfig.getDatabaseName());
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ignored) {
            // CHECKSTYLE:ON
            return;
        }
        Map<String, StorageUnit> storageUnitMap = database.getResourceMetaData().getStorageUnits();
        for (Entry<String, Map<String, Object>> entry : rootConfig.getDataSources().entrySet()) {
            StorageUnit storageUnit = storageUnitMap.get(entry.getKey());
            if (null == storageUnit) {
                continue;
            }
            Map<String, Object> jobDataSourceProps = entry.getValue();
            Map<String, Object> storageUnitStandardProps = storageUnit.getDataSourcePoolProperties().getPoolPropertySynonyms().getStandardProperties();
            if (storageUnitStandardProps.containsKey("maxPoolSize")) {
                log.info("Transform maxPoolSize from '{}' to '{}' for {} data source: {}",
                        jobDataSourceProps.get("maxPoolSize"), storageUnitStandardProps.get("maxPoolSize"), jobId, entry.getKey());
                jobDataSourceProps.put("maxPoolSize", storageUnitStandardProps.get("maxPoolSize"));
            }
            if (storageUnitStandardProps.containsKey("maximumPoolSize")) {
                log.info("Transform maximumPoolSize from '{}' to '{}' for {} data source: {}",
                        jobDataSourceProps.get("maximumPoolSize"), storageUnitStandardProps.get("maximumPoolSize"), jobId, entry.getKey());
                jobDataSourceProps.put("maximumPoolSize", storageUnitStandardProps.get("maximumPoolSize"));
            }
        }
    }
}
