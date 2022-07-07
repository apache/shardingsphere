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

package org.apache.shardingsphere.sharding.data.pipeline;

import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredDetector;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rulealtered.OnRuleAlteredActionConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationYamlSwapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sharding rule altered detector.
 */
public final class ShardingRuleAlteredDetector implements RuleAlteredDetector {
    
    private static final ShardingRuleConfigurationYamlSwapper SHARDING_RULE_CONFIG_YAML_SWAPPER = new ShardingRuleConfigurationYamlSwapper();
    
    private static final OnRuleAlteredActionConfigurationYamlSwapper RULE_ALTERED_ACTION_CONFIG_YAML_SWAPPER = new OnRuleAlteredActionConfigurationYamlSwapper();
    
    @Override
    public String getYamlRuleConfigClassName() {
        return YamlShardingRuleConfiguration.class.getName();
    }
    
    @Override
    public String getRuleConfigClassName() {
        return ShardingRuleConfiguration.class.getName();
    }
    
    @Override
    public List<String> findRuleAlteredLogicTables(final YamlRuleConfiguration sourceRuleConfig, final YamlRuleConfiguration targetRuleConfig,
                                                   final Map<String, Map<String, Object>> sourceDataSources, final Map<String, Map<String, Object>> targetDataSources) {
        if ((null == sourceRuleConfig) ^ (null == targetRuleConfig)) {
            YamlRuleConfiguration ruleConfig = null != sourceRuleConfig ? sourceRuleConfig : targetRuleConfig;
            return extractAllLogicTables((YamlShardingRuleConfiguration) ruleConfig);
        }
        if (null == sourceRuleConfig) {
            return Collections.emptyList();
        }
        if (isShardingRulesTheSame((YamlShardingRuleConfiguration) sourceRuleConfig, (YamlShardingRuleConfiguration) targetRuleConfig)) {
            return Collections.emptyList();
        }
        ShardingRuleConfiguration sourceShardingConfig = SHARDING_RULE_CONFIG_YAML_SWAPPER.swapToObject((YamlShardingRuleConfiguration) sourceRuleConfig);
        ShardingRuleConfiguration targetShardingConfig = SHARDING_RULE_CONFIG_YAML_SWAPPER.swapToObject((YamlShardingRuleConfiguration) targetRuleConfig);
        // TODO InstanceContext should not null
        ShardingRule sourceShardingRule = new ShardingRule(sourceShardingConfig, sourceDataSources.keySet(), null);
        ShardingRule targetShardingRule = new ShardingRule(targetShardingConfig, targetDataSources.keySet(), null);
        return extractRuleAlteredLogicTables(sourceShardingRule, targetShardingRule);
    }
    
    private List<String> extractAllLogicTables(final YamlShardingRuleConfiguration shardingRuleConfig) {
        List<String> result = new ArrayList<>();
        result.addAll(shardingRuleConfig.getTables().keySet());
        result.addAll(shardingRuleConfig.getAutoTables().keySet());
        // TODO handle broadcastTables
        return result;
    }
    
    private boolean isShardingRulesTheSame(final YamlShardingRuleConfiguration sourceShardingConfig, final YamlShardingRuleConfiguration targetShardingConfig) {
        for (Entry<String, YamlTableRuleConfiguration> entry : sourceShardingConfig.getTables().entrySet()) {
            entry.getValue().setLogicTable(null);
        }
        for (Entry<String, YamlTableRuleConfiguration> entry : targetShardingConfig.getTables().entrySet()) {
            entry.getValue().setLogicTable(null);
        }
        for (Entry<String, YamlShardingAutoTableRuleConfiguration> entry : sourceShardingConfig.getAutoTables().entrySet()) {
            entry.getValue().setLogicTable(null);
        }
        for (Entry<String, YamlShardingAutoTableRuleConfiguration> entry : targetShardingConfig.getAutoTables().entrySet()) {
            entry.getValue().setLogicTable(null);
        }
        String sourceShardingConfigYaml = YamlEngine.marshal(sourceShardingConfig);
        String targetShardingConfigYaml = YamlEngine.marshal(targetShardingConfig);
        return sourceShardingConfigYaml.equals(targetShardingConfigYaml);
    }
    
    private List<String> extractRuleAlteredLogicTables(final ShardingRule sourceShardingRule, final ShardingRule targetShardingRule) {
        List<String> result = new ArrayList<>();
        for (Entry<String, TableRule> entry : sourceShardingRule.getTableRules().entrySet()) {
            TableRule targetTableRule = targetShardingRule.getTableRule(entry.getKey());
            if (isNeedReShardingForLogicTable(entry.getValue(), targetTableRule)) {
                result.add(entry.getKey());
            }
        }
        // TODO handle broadcast tables
        return result;
    }
    
    private boolean isNeedReShardingForLogicTable(final TableRule sourceTableRule, final TableRule targetTableRule) {
        List<DataNode> sourceActualDataNodes = sourceTableRule.getActualDataNodes();
        List<DataNode> targetActualDataNodes = targetTableRule.getActualDataNodes();
        if (sourceActualDataNodes.size() == targetActualDataNodes.size() && sourceActualDataNodes.equals(targetActualDataNodes)) {
            return false;
        }
        if (hasCommonDataSourceNames(sourceActualDataNodes, targetActualDataNodes)) {
            throw new RuntimeException("Scale on source dataSources is not supported for now");
        }
        return true;
    }
    
    private boolean hasCommonDataSourceNames(final List<DataNode> sourceActualDataNodes, final List<DataNode> targetActualDataNodes) {
        Set<String> sourceDataSourceNames = sourceActualDataNodes.stream().map(each -> each.getDataSourceName().toLowerCase()).collect(Collectors.toSet());
        Set<String> targetDataSourceNames = targetActualDataNodes.stream().map(each -> each.getDataSourceName().toLowerCase()).collect(Collectors.toSet());
        for (String each : sourceDataSourceNames) {
            if (targetDataSourceNames.contains(each)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Optional<OnRuleAlteredActionConfiguration> getOnRuleAlteredActionConfig(final RuleConfiguration ruleConfig) {
        if (null == ruleConfig) {
            return Optional.empty();
        }
        ShardingRuleConfiguration shardingRuleConfig = (ShardingRuleConfiguration) ruleConfig;
        String scalingName = shardingRuleConfig.getScalingName();
        if (null == scalingName) {
            return Optional.empty();
        }
        OnRuleAlteredActionConfiguration result = shardingRuleConfig.getScaling().get(scalingName);
        if (null == result) {
            YamlOnRuleAlteredActionConfiguration yamlConfig = new YamlOnRuleAlteredActionConfiguration();
            result = RULE_ALTERED_ACTION_CONFIG_YAML_SWAPPER.swapToObject(yamlConfig);
        }
        return Optional.of(result);
    }
}
