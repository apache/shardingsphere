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
import org.apache.shardingsphere.data.pipeline.core.api.PipelineMetaDataPersistService;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Pipeline data source persist service.
 */
public final class PipelineDataSourcePersistService implements PipelineMetaDataPersistService<Map<String, DataSourceProperties>> {
    
    private final YamlDataSourceConfigurationSwapper swapper = new YamlDataSourceConfigurationSwapper();
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, DataSourceProperties> load(final JobType jobType) {
        String dataSourcesProperties = PipelineAPIFactory.getGovernanceRepositoryAPI().getMetaDataDataSources(jobType);
        if (StringUtils.isBlank(dataSourcesProperties)) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, Object>> yamlDataSources = YamlEngine.unmarshal(dataSourcesProperties, Map.class);
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(yamlDataSources.size());
        yamlDataSources.forEach((key, value) -> result.put(key, swapper.swapToDataSourceProperties(value)));
        return result;
    }
    
    @Override
    public void persist(final JobType jobType, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        Map<String, Map<String, Object>> dataSourceMap = new LinkedHashMap<>(dataSourcePropsMap.size());
        for (Entry<String, DataSourceProperties> entry : dataSourcePropsMap.entrySet()) {
            dataSourceMap.put(entry.getKey(), swapper.swapToMap(entry.getValue()));
        }
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistMetaDataDataSources(jobType, YamlEngine.marshal(dataSourceMap));
    }
}
