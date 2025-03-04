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

package org.apache.shardingsphere.broadcast.rule.changed;

import org.apache.shardingsphere.broadcast.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;

import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * Broadcast table changed processor.
 */
public final class BroadcastTableChangedProcessor implements RuleItemConfigurationChangedProcessor<BroadcastRuleConfiguration, BroadcastRuleConfiguration> {
    
    @SuppressWarnings("unchecked")
    @Override
    public BroadcastRuleConfiguration swapRuleItemConfiguration(final String itemName, final String yamlContent) {
        return new BroadcastRuleConfiguration(YamlEngine.unmarshal(yamlContent, LinkedHashSet.class));
    }
    
    @Override
    public BroadcastRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(BroadcastRule.class).map(BroadcastRule::getConfiguration).orElseGet(() -> new BroadcastRuleConfiguration(new LinkedList<>()));
    }
    
    @Override
    public void changeRuleItemConfiguration(final String itemName, final BroadcastRuleConfiguration currentRuleConfig, final BroadcastRuleConfiguration toBeChangedItemConfig) {
        currentRuleConfig.getTables().clear();
        currentRuleConfig.getTables().addAll(toBeChangedItemConfig.getTables());
    }
    
    @Override
    public void dropRuleItemConfiguration(final String itemName, final BroadcastRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getTables().clear();
    }
    
    @Override
    public RuleChangedItemType getType() {
        return new RuleChangedItemType("broadcast", "tables");
    }
}
