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

package org.apache.shardingsphere.data.pipeline.yaml.job;

import org.apache.shardingsphere.data.pipeline.api.config.job.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * YAML consistency check job configuration swapper.
 */
public final class YamlConsistencyCheckJobConfigurationSwapper implements YamlConfigurationSwapper<YamlConsistencyCheckJobConfiguration, ConsistencyCheckJobConfiguration> {
    
    @Override
    public YamlConsistencyCheckJobConfiguration swapToYamlConfiguration(final ConsistencyCheckJobConfiguration data) {
        YamlConsistencyCheckJobConfiguration result = new YamlConsistencyCheckJobConfiguration();
        result.setJobId(data.getJobId());
        result.setParentJobId(data.getParentJobId());
        result.setAlgorithmTypeName(data.getAlgorithmTypeName());
        result.setAlgorithmProps(data.getAlgorithmProps());
        return result;
    }
    
    @Override
    public ConsistencyCheckJobConfiguration swapToObject(final YamlConsistencyCheckJobConfiguration yamlConfig) {
        return new ConsistencyCheckJobConfiguration(yamlConfig.getJobId(), yamlConfig.getParentJobId(), yamlConfig.getAlgorithmTypeName(), yamlConfig.getAlgorithmProps());
    }
    
    /**
     * Swap to job configuration from text.
     *
     * @param jobParam job parameter
     * @return job configuration
     */
    public ConsistencyCheckJobConfiguration swapToObject(final String jobParam) {
        return null == jobParam ? null : swapToObject(YamlEngine.unmarshal(jobParam, YamlConsistencyCheckJobConfiguration.class, true));
    }
}
