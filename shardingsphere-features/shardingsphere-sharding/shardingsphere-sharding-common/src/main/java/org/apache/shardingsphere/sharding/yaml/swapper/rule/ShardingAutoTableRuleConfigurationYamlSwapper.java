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
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.KeyGenerateStrategyConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.ShardingStrategyConfigurationYamlSwapper;

/**
 * Sharding auto table rule configuration YAML swapper.
 */
public final class ShardingAutoTableRuleConfigurationYamlSwapper implements YamlSwapper<YamlShardingAutoTableRuleConfiguration, ShardingAutoTableRuleConfiguration> {
    
    private final ShardingStrategyConfigurationYamlSwapper shardingStrategyConfigurationYamlSwapper = new ShardingStrategyConfigurationYamlSwapper();
    
    private final KeyGenerateStrategyConfigurationYamlSwapper keyGenerateStrategyConfigurationYamlSwapper = new KeyGenerateStrategyConfigurationYamlSwapper();
    
    @Override
    public YamlShardingAutoTableRuleConfiguration swap(final ShardingAutoTableRuleConfiguration data) {
        YamlShardingAutoTableRuleConfiguration result = new YamlShardingAutoTableRuleConfiguration();
        result.setLogicTable(data.getLogicTable());
        result.setActualDataSources(data.getActualDataSources());
        if (null != data.getShardingStrategy()) {
            result.setShardingStrategy(shardingStrategyConfigurationYamlSwapper.swap(data.getShardingStrategy()));
        }
        if (null != data.getKeyGenerateStrategy()) {
            result.setKeyGenerateStrategy(keyGenerateStrategyConfigurationYamlSwapper.swap(data.getKeyGenerateStrategy()));
        }
        return result;
    }
    
    @Override
    public ShardingAutoTableRuleConfiguration swap(final YamlShardingAutoTableRuleConfiguration yamlConfiguration) {
        Preconditions.checkNotNull(yamlConfiguration.getLogicTable(), "Logic table cannot be null.");
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration(yamlConfiguration.getLogicTable(), yamlConfiguration.getActualDataSources());
        if (null != yamlConfiguration.getShardingStrategy()) {
            result.setShardingStrategy(shardingStrategyConfigurationYamlSwapper.swap(yamlConfiguration.getShardingStrategy()));
        }
        if (null != yamlConfiguration.getKeyGenerateStrategy()) {
            result.setKeyGenerateStrategy(keyGenerateStrategyConfigurationYamlSwapper.swap(yamlConfiguration.getKeyGenerateStrategy()));
        }
        return result;
    }
}
