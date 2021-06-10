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

package org.apache.shardingsphere.sharding.yaml.swapper;

import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.swapper.algorithm.ShardingSphereAlgorithmConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.ShardingAutoTableRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.ShardingTableRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.KeyGenerateStrategyConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.ShardingStrategyConfigurationYamlSwapper;

import java.util.Map.Entry;

/**
 * Sharding rule configuration YAML swapper.
 */
public final class ShardingRuleConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlShardingRuleConfiguration, ShardingRuleConfiguration> {
    
    private final ShardingTableRuleConfigurationYamlSwapper tableYamlSwapper = new ShardingTableRuleConfigurationYamlSwapper();
    
    private final ShardingAutoTableRuleConfigurationYamlSwapper autoTableYamlSwapper = new ShardingAutoTableRuleConfigurationYamlSwapper();
    
    private final ShardingStrategyConfigurationYamlSwapper shardingStrategyYamlSwapper = new ShardingStrategyConfigurationYamlSwapper();
    
    private final KeyGenerateStrategyConfigurationYamlSwapper keyGenerateStrategyYamlSwapper = new KeyGenerateStrategyConfigurationYamlSwapper();
    
    private final ShardingSphereAlgorithmConfigurationYamlSwapper algorithmSwapper = new ShardingSphereAlgorithmConfigurationYamlSwapper();
    
    @Override
    public YamlShardingRuleConfiguration swapToYamlConfiguration(final ShardingRuleConfiguration data) {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        data.getTables().forEach(each -> result.getTables().put(each.getLogicTable(), tableYamlSwapper.swapToYamlConfiguration(each)));
        data.getAutoTables().forEach(each -> result.getAutoTables().put(each.getLogicTable(), autoTableYamlSwapper.swapToYamlConfiguration(each)));
        result.getBindingTables().addAll(data.getBindingTableGroups());
        result.getBroadcastTables().addAll(data.getBroadcastTables());
        setYamlDefaultStrategies(data, result);
        setYamlAlgorithms(data, result);
        return result;
    }
    
    private void setYamlDefaultStrategies(final ShardingRuleConfiguration data, final YamlShardingRuleConfiguration yamlConfig) {
        if (null != data.getDefaultDatabaseShardingStrategy()) {
            yamlConfig.setDefaultDatabaseStrategy(shardingStrategyYamlSwapper.swapToYamlConfiguration(data.getDefaultDatabaseShardingStrategy()));
        }
        if (null != data.getDefaultTableShardingStrategy()) {
            yamlConfig.setDefaultTableStrategy(shardingStrategyYamlSwapper.swapToYamlConfiguration(data.getDefaultTableShardingStrategy()));
        }
        if (null != data.getDefaultKeyGenerateStrategy()) {
            yamlConfig.setDefaultKeyGenerateStrategy(keyGenerateStrategyYamlSwapper.swapToYamlConfiguration(data.getDefaultKeyGenerateStrategy()));
        }
    }
    
    private void setYamlAlgorithms(final ShardingRuleConfiguration data, final YamlShardingRuleConfiguration yamlConfig) {
        if (null != data.getShardingAlgorithms()) {
            data.getShardingAlgorithms().forEach((key, value) -> yamlConfig.getShardingAlgorithms().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
        }
        if (null != data.getKeyGenerators()) {
            data.getKeyGenerators().forEach((key, value) -> yamlConfig.getKeyGenerators().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
        }
    }
    
    @Override
    public ShardingRuleConfiguration swapToObject(final YamlShardingRuleConfiguration yamlConfig) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (Entry<String, YamlTableRuleConfiguration> entry : yamlConfig.getTables().entrySet()) {
            YamlTableRuleConfiguration tableRuleConfig = entry.getValue();
            tableRuleConfig.setLogicTable(entry.getKey());
            result.getTables().add(tableYamlSwapper.swapToObject(tableRuleConfig));
        }
        for (Entry<String, YamlShardingAutoTableRuleConfiguration> entry : yamlConfig.getAutoTables().entrySet()) {
            YamlShardingAutoTableRuleConfiguration tableRuleConfig = entry.getValue();
            tableRuleConfig.setLogicTable(entry.getKey());
            result.getAutoTables().add(autoTableYamlSwapper.swapToObject(tableRuleConfig));
        }
        result.getBindingTableGroups().addAll(yamlConfig.getBindingTables());
        result.getBroadcastTables().addAll(yamlConfig.getBroadcastTables());
        setDefaultStrategies(yamlConfig, result);
        setAlgorithms(yamlConfig, result);
        return result;
    }
    
    private void setDefaultStrategies(final YamlShardingRuleConfiguration yamlConfig, final ShardingRuleConfiguration ruleConfig) {
        if (null != yamlConfig.getDefaultDatabaseStrategy()) {
            ruleConfig.setDefaultDatabaseShardingStrategy(shardingStrategyYamlSwapper.swapToObject(yamlConfig.getDefaultDatabaseStrategy()));
        }
        if (null != yamlConfig.getDefaultTableStrategy()) {
            ruleConfig.setDefaultTableShardingStrategy(shardingStrategyYamlSwapper.swapToObject(yamlConfig.getDefaultTableStrategy()));
        }
        if (null != yamlConfig.getDefaultKeyGenerateStrategy()) {
            ruleConfig.setDefaultKeyGenerateStrategy(keyGenerateStrategyYamlSwapper.swapToObject(yamlConfig.getDefaultKeyGenerateStrategy()));
        }
    }
    
    private void setAlgorithms(final YamlShardingRuleConfiguration yamlConfig, final ShardingRuleConfiguration ruleConfig) {
        if (null != yamlConfig.getShardingAlgorithms()) {
            yamlConfig.getShardingAlgorithms().forEach((key, value) -> ruleConfig.getShardingAlgorithms().put(key, algorithmSwapper.swapToObject(value)));
        }
        if (null != yamlConfig.getKeyGenerators()) {
            yamlConfig.getKeyGenerators().forEach((key, value) -> ruleConfig.getKeyGenerators().put(key, algorithmSwapper.swapToObject(value)));
        }
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getTypeClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "SHARDING";
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
}
