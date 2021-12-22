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

package org.apache.shardingsphere.infra.yaml.config.swapper.rulealtered;

import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.ShardingSphereAlgorithmConfigurationYamlSwapper;

/**
 * On rule altered action configuration YAML swapper.
 */
public final class OnRuleAlteredActionConfigurationYamlSwapper implements YamlConfigurationSwapper<YamlOnRuleAlteredActionConfiguration, OnRuleAlteredActionConfiguration> {
    
    private static final ShardingSphereAlgorithmConfigurationYamlSwapper ALGORITHM_CONFIG_YAML_SWAPPER = new ShardingSphereAlgorithmConfigurationYamlSwapper();
    
    @Override
    public YamlOnRuleAlteredActionConfiguration swapToYamlConfiguration(final OnRuleAlteredActionConfiguration data) {
        YamlOnRuleAlteredActionConfiguration result = new YamlOnRuleAlteredActionConfiguration();
        result.setBlockQueueSize(data.getBlockQueueSize());
        result.setWorkerThread(data.getWorkerThread());
        result.setReadBatchSize(data.getReadBatchSize());
        result.setRateLimiter(ALGORITHM_CONFIG_YAML_SWAPPER.swapToYamlConfiguration(data.getRateLimiter()));
        result.setCompletionDetector(ALGORITHM_CONFIG_YAML_SWAPPER.swapToYamlConfiguration(data.getCompletionDetector()));
        result.setSourceWritingStopper(ALGORITHM_CONFIG_YAML_SWAPPER.swapToYamlConfiguration(data.getSourceWritingStopper()));
        result.setDataConsistencyChecker(ALGORITHM_CONFIG_YAML_SWAPPER.swapToYamlConfiguration(data.getDataConsistencyChecker()));
        result.setCheckoutLocker(ALGORITHM_CONFIG_YAML_SWAPPER.swapToYamlConfiguration(data.getCheckoutLocker()));
        return result;
    }
    
    @Override
    public OnRuleAlteredActionConfiguration swapToObject(final YamlOnRuleAlteredActionConfiguration yamlConfig) {
        return new OnRuleAlteredActionConfiguration(yamlConfig.getBlockQueueSize(), yamlConfig.getWorkerThread(), yamlConfig.getReadBatchSize(),
                ALGORITHM_CONFIG_YAML_SWAPPER.swapToObject(yamlConfig.getRateLimiter()),
                ALGORITHM_CONFIG_YAML_SWAPPER.swapToObject(yamlConfig.getCompletionDetector()),
                ALGORITHM_CONFIG_YAML_SWAPPER.swapToObject(yamlConfig.getSourceWritingStopper()),
                ALGORITHM_CONFIG_YAML_SWAPPER.swapToObject(yamlConfig.getDataConsistencyChecker()),
                ALGORITHM_CONFIG_YAML_SWAPPER.swapToObject(yamlConfig.getCheckoutLocker()));
    }
}
