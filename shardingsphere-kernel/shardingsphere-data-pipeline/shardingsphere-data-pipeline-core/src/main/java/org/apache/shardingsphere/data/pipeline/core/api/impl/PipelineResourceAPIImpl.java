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

package org.apache.shardingsphere.data.pipeline.core.api.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineResourceAPI;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Pipeline resource API implementation.
 */
public final class PipelineResourceAPIImpl implements PipelineResourceAPI {
    
    private static final YamlDataSourceConfigurationSwapper DATA_SOURCE_CONFIG_SWAPPER = new YamlDataSourceConfigurationSwapper();
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, DataSourceProperties> getMetaDataDataSource(final JobType jobType) {
        String dataSourcesProperties = PipelineAPIFactory.getGovernanceRepositoryAPI().getMetaDataDataSource(jobType);
        if (StringUtils.isBlank(dataSourcesProperties)) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, Object>> yamlDataSources = YamlEngine.unmarshal(dataSourcesProperties, Map.class);
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(yamlDataSources.size());
        yamlDataSources.forEach((key, value) -> result.put(key, DATA_SOURCE_CONFIG_SWAPPER.swapToDataSourceProperties(value)));
        return result;
    }
    
    @Override
    public void persistMetaDataDataSource(final JobType jobType, final Map<String, DataSourceProperties> dataSourceConfigs) {
        Map<String, Map<String, Object>> dataSourceMap = new LinkedHashMap<>(dataSourceConfigs.size());
        for (Entry<String, DataSourceProperties> entry : dataSourceConfigs.entrySet()) {
            dataSourceMap.put(entry.getKey(), DATA_SOURCE_CONFIG_SWAPPER.swapToMap(entry.getValue()));
        }
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistMetaDataDataSource(jobType, YamlEngine.marshal(dataSourceMap));
    }
}
