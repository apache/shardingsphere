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

package org.apache.shardingsphere.infra.yaml.config.swapper.rule.rulealtered;

import org.apache.shardingsphere.infra.config.rule.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlOnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.data.pipeline.YamlPipelineWriteConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.data.pipeline.YamlPipelineReadConfigurationSwapper;

/**
 * YAML on rule altered action configuration swapper.
 */
public final class YamlOnRuleAlteredActionConfigurationSwapper implements YamlConfigurationSwapper<YamlOnRuleAlteredActionConfiguration, OnRuleAlteredActionConfiguration> {
    
    private static final YamlAlgorithmConfigurationSwapper ALGORITHM_CONFIG_SWAPPER = new YamlAlgorithmConfigurationSwapper();
    
    private static final YamlPipelineReadConfigurationSwapper READ_CONFIG_SWAPPER = new YamlPipelineReadConfigurationSwapper();
    
    private static final YamlPipelineWriteConfigurationSwapper WRITE_CONFIG_SWAPPER = new YamlPipelineWriteConfigurationSwapper();
    
    @Override
    public YamlOnRuleAlteredActionConfiguration swapToYamlConfiguration(final OnRuleAlteredActionConfiguration data) {
        if (null == data) {
            return null;
        }
        YamlOnRuleAlteredActionConfiguration result = new YamlOnRuleAlteredActionConfiguration();
        result.setInput(READ_CONFIG_SWAPPER.swapToYamlConfiguration(data.getInput()));
        result.setOutput(WRITE_CONFIG_SWAPPER.swapToYamlConfiguration(data.getOutput()));
        result.setStreamChannel(ALGORITHM_CONFIG_SWAPPER.swapToYamlConfiguration(data.getStreamChannel()));
        result.setCompletionDetector(ALGORITHM_CONFIG_SWAPPER.swapToYamlConfiguration(data.getCompletionDetector()));
        result.setDataConsistencyChecker(ALGORITHM_CONFIG_SWAPPER.swapToYamlConfiguration(data.getDataConsistencyCalculator()));
        return result;
    }
    
    @Override
    public OnRuleAlteredActionConfiguration swapToObject(final YamlOnRuleAlteredActionConfiguration yamlConfig) {
        if (null == yamlConfig) {
            return null;
        }
        return new OnRuleAlteredActionConfiguration(
                READ_CONFIG_SWAPPER.swapToObject(yamlConfig.getInput()),
                WRITE_CONFIG_SWAPPER.swapToObject(yamlConfig.getOutput()),
                ALGORITHM_CONFIG_SWAPPER.swapToObject(yamlConfig.getStreamChannel()),
                ALGORITHM_CONFIG_SWAPPER.swapToObject(yamlConfig.getCompletionDetector()),
                ALGORITHM_CONFIG_SWAPPER.swapToObject(yamlConfig.getDataConsistencyChecker()));
    }
}
