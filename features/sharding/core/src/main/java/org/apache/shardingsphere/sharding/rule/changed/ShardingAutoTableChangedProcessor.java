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

package org.apache.shardingsphere.sharding.rule.changed;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.YamlShardingAutoTableRuleConfigurationSwapper;

/**
 * Sharding auto table changed processor.
 */
public final class ShardingAutoTableChangedProcessor implements RuleItemConfigurationChangedProcessor<ShardingRuleConfiguration, ShardingAutoTableRuleConfiguration> {
    
    @Override
    public ShardingAutoTableRuleConfiguration swapRuleItemConfiguration(final String itemName, final String yamlContent) {
        return new YamlShardingAutoTableRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlShardingAutoTableRuleConfiguration.class));
    }
    
    @Override
    public ShardingRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(ShardingRule.class).map(ShardingRule::getConfiguration).orElseGet(ShardingRuleConfiguration::new);
    }
    
    @Override
    public void changeRuleItemConfiguration(final String itemName, final ShardingRuleConfiguration currentRuleConfig, final ShardingAutoTableRuleConfiguration toBeChangedItemConfig) {
        currentRuleConfig.getAutoTables().removeIf(each -> each.getLogicTable().equals(itemName));
        currentRuleConfig.getAutoTables().add(toBeChangedItemConfig);
    }
    
    @Override
    public void dropRuleItemConfiguration(final String itemName, final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getAutoTables().removeIf(each -> each.getLogicTable().equals(itemName));
    }
    
    @Override
    public RuleChangedItemType getType() {
        return new RuleChangedItemType("sharding", "auto_tables");
    }
}
