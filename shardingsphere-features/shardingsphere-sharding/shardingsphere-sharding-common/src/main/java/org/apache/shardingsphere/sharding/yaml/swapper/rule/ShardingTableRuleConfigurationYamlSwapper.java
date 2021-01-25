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

package org.apache.shardingsphere.sharding.yaml.swapper.rule;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.KeyGenerateStrategyConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.ShardingStrategyConfigurationYamlSwapper;

/**
 * Sharding table rule configuration YAML swapper.
 */
public final class ShardingTableRuleConfigurationYamlSwapper implements YamlSwapper<YamlTableRuleConfiguration, ShardingTableRuleConfiguration> {
    
    private final ShardingStrategyConfigurationYamlSwapper shardingStrategyYamlSwapper = new ShardingStrategyConfigurationYamlSwapper();
    
    private final KeyGenerateStrategyConfigurationYamlSwapper keyGenerateStrategyYamlSwapper = new KeyGenerateStrategyConfigurationYamlSwapper();
    
    @Override
    public YamlTableRuleConfiguration swapToYamlConfiguration(final ShardingTableRuleConfiguration data) {
        YamlTableRuleConfiguration result = new YamlTableRuleConfiguration();
        result.setLogicTable(data.getLogicTable());
        result.setActualDataNodes(data.getActualDataNodes());
        if (null != data.getDatabaseShardingStrategy()) {
            result.setDatabaseStrategy(shardingStrategyYamlSwapper.swapToYamlConfiguration(data.getDatabaseShardingStrategy()));
        }
        if (null != data.getTableShardingStrategy()) {
            result.setTableStrategy(shardingStrategyYamlSwapper.swapToYamlConfiguration(data.getTableShardingStrategy()));
        }
        if (null != data.getKeyGenerateStrategy()) {
            result.setKeyGenerateStrategy(keyGenerateStrategyYamlSwapper.swapToYamlConfiguration(data.getKeyGenerateStrategy()));
        }
        return result;
    }
    
    @Override
    public ShardingTableRuleConfiguration swapToObject(final YamlTableRuleConfiguration yamlConfig) {
        Preconditions.checkNotNull(yamlConfig.getLogicTable(), "Logic table cannot be null.");
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration(yamlConfig.getLogicTable(), yamlConfig.getActualDataNodes());
        if (null != yamlConfig.getDatabaseStrategy()) {
            result.setDatabaseShardingStrategy(shardingStrategyYamlSwapper.swapToObject(yamlConfig.getDatabaseStrategy()));
        }
        if (null != yamlConfig.getTableStrategy()) {
            result.setTableShardingStrategy(shardingStrategyYamlSwapper.swapToObject(yamlConfig.getTableStrategy()));
        }
        if (null != yamlConfig.getKeyGenerateStrategy()) {
            result.setKeyGenerateStrategy(keyGenerateStrategyYamlSwapper.swapToObject(yamlConfig.getKeyGenerateStrategy()));
        }
        return result;
    }
}
