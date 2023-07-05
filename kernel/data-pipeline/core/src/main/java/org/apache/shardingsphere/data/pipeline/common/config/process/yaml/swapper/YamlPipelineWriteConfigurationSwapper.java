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

package org.apache.shardingsphere.data.pipeline.common.config.process.yaml.swapper;

import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.yaml.YamlPipelineWriteConfiguration;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;

/**
 * YAML pipeline write configuration swapper.
 */
public final class YamlPipelineWriteConfigurationSwapper implements YamlConfigurationSwapper<YamlPipelineWriteConfiguration, PipelineWriteConfiguration> {
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    @Override
    public YamlPipelineWriteConfiguration swapToYamlConfiguration(final PipelineWriteConfiguration data) {
        if (null == data) {
            return null;
        }
        YamlPipelineWriteConfiguration result = new YamlPipelineWriteConfiguration();
        result.setWorkerThread(data.getWorkerThread());
        result.setBatchSize(data.getBatchSize());
        result.setRateLimiter(algorithmSwapper.swapToYamlConfiguration(data.getRateLimiter()));
        return result;
    }
    
    @Override
    public PipelineWriteConfiguration swapToObject(final YamlPipelineWriteConfiguration yamlConfig) {
        return null == yamlConfig
                ? null
                : new PipelineWriteConfiguration(yamlConfig.getWorkerThread(), yamlConfig.getBatchSize(), algorithmSwapper.swapToObject(yamlConfig.getRateLimiter()));
    }
}
