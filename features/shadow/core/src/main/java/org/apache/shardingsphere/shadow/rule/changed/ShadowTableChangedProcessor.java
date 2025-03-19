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

package org.apache.shardingsphere.shadow.rule.changed;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.table.YamlShadowTableConfigurationSwapper;

/**
 * Shadow table changed processor.
 */
public final class ShadowTableChangedProcessor implements RuleItemConfigurationChangedProcessor<ShadowRuleConfiguration, ShadowTableConfiguration> {
    
    @Override
    public ShadowTableConfiguration swapRuleItemConfiguration(final String itemName, final String yamlContent) {
        return new YamlShadowTableConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlShadowTableConfiguration.class));
    }
    
    @Override
    public ShadowRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(ShadowRule.class).map(ShadowRule::getConfiguration).orElseGet(ShadowRuleConfiguration::new);
    }
    
    @Override
    public void changeRuleItemConfiguration(final String itemName, final ShadowRuleConfiguration currentRuleConfig, final ShadowTableConfiguration toBeChangedItemConfig) {
        currentRuleConfig.getTables().put(itemName, toBeChangedItemConfig);
    }
    
    @Override
    public void dropRuleItemConfiguration(final String itemName, final ShadowRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getTables().remove(itemName);
    }
    
    @Override
    public RuleChangedItemType getType() {
        return new RuleChangedItemType("shadow", "tables");
    }
}
