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
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.algorithm.KeyGenerateAlgorithmConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.ShardingAutoTableRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.ShardingTableRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.KeyGenerateStrategyConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.ShardingStrategyConfigurationYamlSwapper;

import java.util.Map.Entry;

/**
 * Sharding rule configuration YAML swapper.
 */
public final class ShardingRuleConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlShardingRuleConfiguration, ShardingRuleConfiguration> {
    
    private final ShardingTableRuleConfigurationYamlSwapper tableRuleConfigurationYamlSwapper = new ShardingTableRuleConfigurationYamlSwapper();

    private final ShardingAutoTableRuleConfigurationYamlSwapper autoTableRuleConfigurationYamlSwapper = new ShardingAutoTableRuleConfigurationYamlSwapper();
    
    private final ShardingStrategyConfigurationYamlSwapper shardingStrategyConfigurationYamlSwapper = new ShardingStrategyConfigurationYamlSwapper();
    
    private final KeyGenerateStrategyConfigurationYamlSwapper keyGenerateStrategyConfigurationYamlSwapper = new KeyGenerateStrategyConfigurationYamlSwapper();
    
    private final KeyGenerateAlgorithmConfigurationYamlSwapper keyGenerateAlgorithmConfigurationYamlSwapper = new KeyGenerateAlgorithmConfigurationYamlSwapper();
    
    @Override
    public YamlShardingRuleConfiguration swap(final ShardingRuleConfiguration data) {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        for (ShardingTableRuleConfiguration each : data.getTables()) {
            result.getTables().put(each.getLogicTable(), tableRuleConfigurationYamlSwapper.swap(each));
        }
        for (ShardingAutoTableRuleConfiguration each : data.getAutoTables()) {
            result.getAutoTables().put(each.getLogicTable(), autoTableRuleConfigurationYamlSwapper.swap(each));
        }
        result.getBindingTables().addAll(data.getBindingTableGroups());
        result.getBroadcastTables().addAll(data.getBroadcastTables());
        if (null != data.getDefaultDatabaseShardingStrategy()) {
            result.setDefaultDatabaseStrategy(shardingStrategyConfigurationYamlSwapper.swap(data.getDefaultDatabaseShardingStrategy()));
        }
        if (null != data.getDefaultTableShardingStrategy()) {
            result.setDefaultTableStrategy(shardingStrategyConfigurationYamlSwapper.swap(data.getDefaultTableShardingStrategy()));
        }
        if (null != data.getDefaultKeyGenerateStrategy()) {
            result.setDefaultKeyGenerateStrategy(keyGenerateStrategyConfigurationYamlSwapper.swap(data.getDefaultKeyGenerateStrategy()));
        }
        if (null != data.getKeyGenerators()) {
            data.getKeyGenerators().forEach((key, value) -> result.getKeyGenerators().put(key, keyGenerateAlgorithmConfigurationYamlSwapper.swap(value)));
        }
        return result;
    }
    
    @Override
    public ShardingRuleConfiguration swap(final YamlShardingRuleConfiguration yamlConfiguration) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (Entry<String, YamlTableRuleConfiguration> entry : yamlConfiguration.getTables().entrySet()) {
            YamlTableRuleConfiguration tableRuleConfig = entry.getValue();
            tableRuleConfig.setLogicTable(entry.getKey());
            result.getTables().add(tableRuleConfigurationYamlSwapper.swap(tableRuleConfig));
        }
        for (Entry<String, YamlShardingAutoTableRuleConfiguration> entry : yamlConfiguration.getAutoTables().entrySet()) {
            YamlShardingAutoTableRuleConfiguration tableRuleConfig = entry.getValue();
            tableRuleConfig.setLogicTable(entry.getKey());
            result.getAutoTables().add(autoTableRuleConfigurationYamlSwapper.swap(tableRuleConfig));
        }
        result.getBindingTableGroups().addAll(yamlConfiguration.getBindingTables());
        result.getBroadcastTables().addAll(yamlConfiguration.getBroadcastTables());
        if (null != yamlConfiguration.getDefaultDatabaseStrategy()) {
            result.setDefaultDatabaseShardingStrategy(shardingStrategyConfigurationYamlSwapper.swap(yamlConfiguration.getDefaultDatabaseStrategy()));
        }
        if (null != yamlConfiguration.getDefaultTableStrategy()) {
            result.setDefaultTableShardingStrategy(shardingStrategyConfigurationYamlSwapper.swap(yamlConfiguration.getDefaultTableStrategy()));
        }
        if (null != yamlConfiguration.getDefaultKeyGenerateStrategy()) {
            result.setDefaultKeyGenerateStrategy(keyGenerateStrategyConfigurationYamlSwapper.swap(yamlConfiguration.getDefaultKeyGenerateStrategy()));
        }
        if (null != yamlConfiguration.getKeyGenerators()) {
            yamlConfiguration.getKeyGenerators().forEach((key, value) -> result.getKeyGenerators().put(key, keyGenerateAlgorithmConfigurationYamlSwapper.swap(value)));
        }
        return result;
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
