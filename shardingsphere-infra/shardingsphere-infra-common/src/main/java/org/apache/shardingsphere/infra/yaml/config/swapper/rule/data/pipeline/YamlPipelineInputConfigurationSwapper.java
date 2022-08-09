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
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineInputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.data.pipeline.YamlPipelineInputConfiguration;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;

/**
 * YAML pipeline input configuration swapper.
 */
@Data
public final class YamlPipelineInputConfigurationSwapper implements YamlConfigurationSwapper<YamlPipelineInputConfiguration, PipelineInputConfiguration> {
    
    private static final YamlAlgorithmConfigurationSwapper ALGORITHM_CONFIG_SWAPPER = new YamlAlgorithmConfigurationSwapper();
    
    @Override
    public YamlPipelineInputConfiguration swapToYamlConfiguration(final PipelineInputConfiguration data) {
        if (null == data) {
            return null;
        }
        YamlPipelineInputConfiguration result = new YamlPipelineInputConfiguration();
        result.setWorkerThread(data.getWorkerThread());
        result.setBatchSize(data.getBatchSize());
        result.setShardingSize(data.getShardingSize());
        result.setRateLimiter(ALGORITHM_CONFIG_SWAPPER.swapToYamlConfiguration(data.getRateLimiter()));
        return result;
    }
    
    @Override
    public PipelineInputConfiguration swapToObject(final YamlPipelineInputConfiguration yamlConfig) {
        return null == yamlConfig
                ? null
                : new PipelineInputConfiguration(yamlConfig.getWorkerThread(), yamlConfig.getBatchSize(), yamlConfig.getShardingSize(),
                        ALGORITHM_CONFIG_SWAPPER.swapToObject(yamlConfig.getRateLimiter()));
    }
}
