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
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.InputConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.OutputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration.YamlInputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration.YamlOutputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.ShardingSphereAlgorithmConfigurationYamlSwapper;

/**
 * On rule altered action configuration YAML swapper.
 */
public final class OnRuleAlteredActionConfigurationYamlSwapper implements YamlConfigurationSwapper<YamlOnRuleAlteredActionConfiguration, OnRuleAlteredActionConfiguration> {
    
    private static final ShardingSphereAlgorithmConfigurationYamlSwapper ALGORITHM_CONFIG_YAML_SWAPPER = new ShardingSphereAlgorithmConfigurationYamlSwapper();
    
    private static final InputConfigurationSwapper INPUT_CONFIG_SWAPPER = new InputConfigurationSwapper();
    
    private static final OutputConfigurationSwapper OUTPUT_CONFIG_SWAPPER = new OutputConfigurationSwapper();
    
    @Override
    public YamlOnRuleAlteredActionConfiguration swapToYamlConfiguration(final OnRuleAlteredActionConfiguration data) {
        if (null == data) {
            return null;
        }
        YamlOnRuleAlteredActionConfiguration result = new YamlOnRuleAlteredActionConfiguration();
        result.setInput(INPUT_CONFIG_SWAPPER.swapToYamlConfiguration(data.getInput()));
        result.setOutput(OUTPUT_CONFIG_SWAPPER.swapToYamlConfiguration(data.getOutput()));
        result.setStreamChannel(ALGORITHM_CONFIG_YAML_SWAPPER.swapToYamlConfiguration(data.getStreamChannel()));
        result.setCompletionDetector(ALGORITHM_CONFIG_YAML_SWAPPER.swapToYamlConfiguration(data.getCompletionDetector()));
        result.setDataConsistencyChecker(ALGORITHM_CONFIG_YAML_SWAPPER.swapToYamlConfiguration(data.getDataConsistencyCalculator()));
        return result;
    }
    
    @Override
    public OnRuleAlteredActionConfiguration swapToObject(final YamlOnRuleAlteredActionConfiguration yamlConfig) {
        if (null == yamlConfig) {
            return null;
        }
        return new OnRuleAlteredActionConfiguration(
                INPUT_CONFIG_SWAPPER.swapToObject(yamlConfig.getInput()),
                OUTPUT_CONFIG_SWAPPER.swapToObject(yamlConfig.getOutput()),
                ALGORITHM_CONFIG_YAML_SWAPPER.swapToObject(yamlConfig.getStreamChannel()),
                ALGORITHM_CONFIG_YAML_SWAPPER.swapToObject(yamlConfig.getCompletionDetector()),
                ALGORITHM_CONFIG_YAML_SWAPPER.swapToObject(yamlConfig.getDataConsistencyChecker()));
    }
    
    public static class InputConfigurationSwapper implements YamlConfigurationSwapper<YamlInputConfiguration, InputConfiguration> {
        
        @Override
        public YamlInputConfiguration swapToYamlConfiguration(final InputConfiguration data) {
            if (null == data) {
                return null;
            }
            YamlInputConfiguration result = new YamlInputConfiguration();
            result.setWorkerThread(data.getWorkerThread());
            result.setBatchSize(data.getBatchSize());
            result.setShardingSize(data.getShardingSize());
            result.setRateLimiter(ALGORITHM_CONFIG_YAML_SWAPPER.swapToYamlConfiguration(data.getRateLimiter()));
            return result;
        }
        
        @Override
        public InputConfiguration swapToObject(final YamlInputConfiguration yamlConfig) {
            if (null == yamlConfig) {
                return null;
            }
            return new InputConfiguration(yamlConfig.getWorkerThread(), yamlConfig.getBatchSize(), yamlConfig.getShardingSize(),
                    ALGORITHM_CONFIG_YAML_SWAPPER.swapToObject(yamlConfig.getRateLimiter()));
        }
    }
    
    public static class OutputConfigurationSwapper implements YamlConfigurationSwapper<YamlOutputConfiguration, OutputConfiguration> {
        
        @Override
        public YamlOutputConfiguration swapToYamlConfiguration(final OutputConfiguration data) {
            if (null == data) {
                return null;
            }
            YamlOutputConfiguration result = new YamlOutputConfiguration();
            result.setWorkerThread(data.getWorkerThread());
            result.setBatchSize(data.getBatchSize());
            result.setRateLimiter(ALGORITHM_CONFIG_YAML_SWAPPER.swapToYamlConfiguration(data.getRateLimiter()));
            return result;
        }
        
        @Override
        public OutputConfiguration swapToObject(final YamlOutputConfiguration yamlConfig) {
            if (null == yamlConfig) {
                return null;
            }
            return new OutputConfiguration(yamlConfig.getWorkerThread(), yamlConfig.getBatchSize(), ALGORITHM_CONFIG_YAML_SWAPPER.swapToObject(yamlConfig.getRateLimiter()));
        }
    }
}
