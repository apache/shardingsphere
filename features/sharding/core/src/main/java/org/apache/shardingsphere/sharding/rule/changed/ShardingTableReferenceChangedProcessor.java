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
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.mode.spi.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.metadata.nodepath.ShardingRuleNodePathProvider;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.swapper.rule.YamlShardingTableReferenceRuleConfigurationConverter;

/**
 * Sharding table reference changed processor.
 */
public final class ShardingTableReferenceChangedProcessor implements RuleItemConfigurationChangedProcessor<ShardingRuleConfiguration, ShardingTableReferenceRuleConfiguration> {
    
    @Override
    public ShardingTableReferenceRuleConfiguration swapRuleItemConfiguration(final AlterRuleItemEvent event, final String yamlContent) {
        return YamlShardingTableReferenceRuleConfigurationConverter.convertToObject(yamlContent);
    }
    
    @Override
    public ShardingRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(ShardingRule.class).map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElseGet(ShardingRuleConfiguration::new);
    }
    
    @Override
    public void changeRuleItemConfiguration(final AlterRuleItemEvent event, final ShardingRuleConfiguration currentRuleConfig, final ShardingTableReferenceRuleConfiguration toBeChangedItemConfig) {
        currentRuleConfig.getBindingTableGroups().removeIf(each -> each.getName().equals(((AlterNamedRuleItemEvent) event).getItemName()));
        currentRuleConfig.getBindingTableGroups().add(toBeChangedItemConfig);
    }
    
    @Override
    public void dropRuleItemConfiguration(final DropRuleItemEvent event, final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getBindingTableGroups().removeIf(each -> each.getName().equals(((DropNamedRuleItemEvent) event).getItemName()));
    }
    
    @Override
    public String getType() {
        return ShardingRuleNodePathProvider.RULE_TYPE + "." + ShardingRuleNodePathProvider.BINDING_TABLES;
    }
}
