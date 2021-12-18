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

package org.apache.shardingsphere.sharding.schedule;

import org.apache.shardingsphere.data.pipeline.spi.rulealtered.JobRuleAlteredDetector;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rulealtered.OnRuleAlteredActionConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;

import java.util.Map.Entry;
import java.util.Optional;

/**
 * Job sharding rule altered detector.
 */
public final class JobShardingRuleAlteredDetector implements JobRuleAlteredDetector {
    
    private static final OnRuleAlteredActionConfigurationYamlSwapper ON_RULE_ALTERED_ACTION_YAML_SWAPPER = new OnRuleAlteredActionConfigurationYamlSwapper();
    
    @Override
    public boolean isRuleAltered(final YamlRuleConfiguration sourceRuleConfig, final YamlRuleConfiguration targetRuleConfig) {
        if ((null == sourceRuleConfig) ^ (null == targetRuleConfig)) {
            return true;
        }
        if (null == sourceRuleConfig) {
            return false;
        }
        return isShardingRulesTheSame((YamlShardingRuleConfiguration) sourceRuleConfig, (YamlShardingRuleConfiguration) targetRuleConfig);
    }
    
    // TODO more accurate comparison
    private boolean isShardingRulesTheSame(final YamlShardingRuleConfiguration sourceShardingConfig, final YamlShardingRuleConfiguration targetShardingConfig) {
        for (Entry<String, YamlTableRuleConfiguration> entry : sourceShardingConfig.getTables().entrySet()) {
            entry.getValue().setLogicTable(null);
        }
        for (Entry<String, YamlTableRuleConfiguration> entry : targetShardingConfig.getTables().entrySet()) {
            entry.getValue().setLogicTable(null);
        }
        String sourceShardingConfigYaml = YamlEngine.marshal(sourceShardingConfig);
        String targetShardingConfigYaml = YamlEngine.marshal(targetShardingConfig);
        return sourceShardingConfigYaml.equals(targetShardingConfigYaml);
    }
    
    @Override
    public Optional<OnRuleAlteredActionConfiguration> getOnRuleAlteredActionConfig(final YamlRuleConfiguration sourceRuleConfig) {
        if (null == sourceRuleConfig) {
            return Optional.empty();
        }
        YamlShardingRuleConfiguration shardingRuleConfig = (YamlShardingRuleConfiguration) sourceRuleConfig;
        String scalingName = shardingRuleConfig.getScalingName();
        if (null == scalingName) {
            return Optional.empty();
        }
        YamlOnRuleAlteredActionConfiguration result = shardingRuleConfig.getScaling().get(scalingName);
        if (null == result) {
            return Optional.empty();
        }
        return Optional.of(ON_RULE_ALTERED_ACTION_YAML_SWAPPER.swapToObject(result));
    }
    
    @Override
    public String getType() {
        return YamlShardingRuleConfiguration.class.getName();
    }
}
