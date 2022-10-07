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

package org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml;

import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * YAML pipeline data source configuration swapper.
 */
public final class YamlPipelineDataSourceConfigurationSwapper implements YamlConfigurationSwapper<YamlPipelineDataSourceConfiguration, PipelineDataSourceConfiguration> {
    
    @Override
    public YamlPipelineDataSourceConfiguration swapToYamlConfiguration(final PipelineDataSourceConfiguration data) {
        if (null == data) {
            return null;
        }
        YamlPipelineDataSourceConfiguration result = new YamlPipelineDataSourceConfiguration();
        result.setType(data.getType());
        result.setParameter(data.getParameter());
        return result;
    }
    
    @Override
    public PipelineDataSourceConfiguration swapToObject(final YamlPipelineDataSourceConfiguration yamlConfig) {
        return null == yamlConfig ? null : PipelineDataSourceConfigurationFactory.newInstance(yamlConfig.getType(), yamlConfig.getParameter());
    }
}
