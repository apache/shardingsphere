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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlKeyGenerateStrategyConfigurationSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlShardingStrategyConfigurationSwapper;

/**
 * YAML sharding auto table rule configuration swapper.
 */
@RequiredArgsConstructor
public final class YamlShardingAutoTableRuleConfigurationSwapper implements YamlConfigurationSwapper<YamlShardingAutoTableRuleConfiguration, ShardingAutoTableRuleConfiguration> {
    
    private final YamlShardingStrategyConfigurationSwapper shardingStrategySwapper = new YamlShardingStrategyConfigurationSwapper();
    
    private final YamlKeyGenerateStrategyConfigurationSwapper keyGenerateStrategySwapper = new YamlKeyGenerateStrategyConfigurationSwapper();
    
    @Override
    public YamlShardingAutoTableRuleConfiguration swapToYamlConfiguration(final ShardingAutoTableRuleConfiguration data) {
        YamlShardingAutoTableRuleConfiguration result = new YamlShardingAutoTableRuleConfiguration();
        result.setLogicTable(data.getLogicTable());
        result.setActualDataSources(data.getActualDataSources());
        result.setActualTablePrefix(data.getActualTablePrefix());
        if (null != data.getShardingStrategy()) {
            result.setShardingStrategy(shardingStrategySwapper.swapToYamlConfiguration(data.getShardingStrategy()));
        }
        if (null != data.getKeyGenerateStrategy()) {
            result.setKeyGenerateStrategy(keyGenerateStrategySwapper.swapToYamlConfiguration(data.getKeyGenerateStrategy()));
        }
        return result;
    }
    
    @Override
    public ShardingAutoTableRuleConfiguration swapToObject(final YamlShardingAutoTableRuleConfiguration yamlConfig) {
        Preconditions.checkNotNull(yamlConfig.getLogicTable(), "Logic table cannot be null.");
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration(yamlConfig.getLogicTable(), yamlConfig.getActualDataSources());
        result.setActualTablePrefix(yamlConfig.getActualTablePrefix());
        if (null != yamlConfig.getShardingStrategy()) {
            result.setShardingStrategy(shardingStrategySwapper.swapToObject(yamlConfig.getShardingStrategy()));
        }
        if (null != yamlConfig.getKeyGenerateStrategy()) {
            result.setKeyGenerateStrategy(keyGenerateStrategySwapper.swapToObject(yamlConfig.getKeyGenerateStrategy()));
        }
        return result;
    }
}
