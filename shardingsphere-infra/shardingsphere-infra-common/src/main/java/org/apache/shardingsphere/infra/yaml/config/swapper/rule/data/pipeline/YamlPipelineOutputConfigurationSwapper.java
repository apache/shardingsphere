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

package org.apache.shardingsphere.infra.yaml.config.swapper.rule.data.pipeline;

import lombok.Data;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineOutputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.data.pipeline.YamlPipelineOutputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;

/**
 * YAML pipeline output configuration swapper.
 */
@Data
public final class YamlPipelineOutputConfigurationSwapper implements YamlConfigurationSwapper<YamlPipelineOutputConfiguration, PipelineOutputConfiguration> {
    
    private static final YamlAlgorithmConfigurationSwapper ALGORITHM_CONFIG_SWAPPER = new YamlAlgorithmConfigurationSwapper();
    
    @Override
    public YamlPipelineOutputConfiguration swapToYamlConfiguration(final PipelineOutputConfiguration data) {
        if (null == data) {
            return null;
        }
        YamlPipelineOutputConfiguration result = new YamlPipelineOutputConfiguration();
        result.setWorkerThread(data.getWorkerThread());
        result.setBatchSize(data.getBatchSize());
        result.setRateLimiter(ALGORITHM_CONFIG_SWAPPER.swapToYamlConfiguration(data.getRateLimiter()));
        return result;
    }
    
    @Override
    public PipelineOutputConfiguration swapToObject(final YamlPipelineOutputConfiguration yamlConfig) {
        return null == yamlConfig
                ? null
                : new PipelineOutputConfiguration(yamlConfig.getWorkerThread(), yamlConfig.getBatchSize(), ALGORITHM_CONFIG_SWAPPER.swapToObject(yamlConfig.getRateLimiter()));
    }
}
