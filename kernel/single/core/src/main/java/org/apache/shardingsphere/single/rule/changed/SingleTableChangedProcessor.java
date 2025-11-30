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

package org.apache.shardingsphere.single.rule.changed;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.util.LinkedHashSet;

/**
 * Single table changed processor.
 */
public final class SingleTableChangedProcessor implements RuleItemConfigurationChangedProcessor<SingleRuleConfiguration, SingleRuleConfiguration> {
    
    @SuppressWarnings("unchecked")
    @Override
    public SingleRuleConfiguration swapRuleItemConfiguration(final String itemName, final String yamlContent) {
        return new SingleRuleConfiguration(YamlEngine.unmarshal(yamlContent, LinkedHashSet.class), null);
    }
    
    @Override
    public SingleRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().getSingleRule(SingleRule.class).getConfiguration();
    }
    
    @Override
    public void changeRuleItemConfiguration(final String itemName, final SingleRuleConfiguration currentRuleConfig, final SingleRuleConfiguration toBeChangedItemConfig) {
        currentRuleConfig.getTables().clear();
        currentRuleConfig.getTables().addAll(toBeChangedItemConfig.getTables());
    }
    
    @Override
    public void dropRuleItemConfiguration(final String itemName, final SingleRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getTables().clear();
    }
    
    @Override
    public RuleChangedItemType getType() {
        return new RuleChangedItemType("single", "tables");
    }
}
