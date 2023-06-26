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

package org.apache.shardingsphere.data.pipeline.core.metadata;

import com.google.common.base.Strings;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.job.type.JobType;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
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
    public Map<String, DataSourceProperties> load(final PipelineContextKey contextKey, final JobType jobType) {
        String dataSourcesProps = PipelineAPIFactory.getGovernanceRepositoryAPI(contextKey).getMetaDataDataSources(jobType);
        if (Strings.isNullOrEmpty(dataSourcesProps)) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, Object>> yamlDataSources = YamlEngine.unmarshal(dataSourcesProps, Map.class);
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(yamlDataSources.size(), 1F);
        yamlDataSources.forEach((key, value) -> result.put(key, swapper.swapToDataSourceProperties(value)));
        return result;
    }
    
    @Override
    public void persist(final PipelineContextKey contextKey, final JobType jobType, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        Map<String, Map<String, Object>> dataSourceMap = new LinkedHashMap<>(dataSourcePropsMap.size(), 1F);
        for (Entry<String, DataSourceProperties> entry : dataSourcePropsMap.entrySet()) {
            dataSourceMap.put(entry.getKey(), swapper.swapToMap(entry.getValue()));
        }
        PipelineAPIFactory.getGovernanceRepositoryAPI(contextKey).persistMetaDataDataSources(jobType, YamlEngine.marshal(dataSourceMap));
    }
}
