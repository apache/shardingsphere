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
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.metadata.nodepath.ShadowRuleNodePathProvider;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.table.YamlShadowTableConfigurationSwapper;

/**
 * Shadow table changed processor.
 */
public final class ShadowTableChangedProcessor implements RuleItemConfigurationChangedProcessor<ShadowRuleConfiguration, ShadowTableConfiguration> {
    
    @Override
    public ShadowTableConfiguration swapRuleItemConfiguration(final AlterRuleItemEvent event, final String yamlContent) {
        return new YamlShadowTableConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlShadowTableConfiguration.class));
    }
    
    @Override
    public ShadowRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(ShadowRule.class).map(optional -> (ShadowRuleConfiguration) optional.getConfiguration()).orElseGet(ShadowRuleConfiguration::new);
    }
    
    @Override
    public void changeRuleItemConfiguration(final AlterRuleItemEvent event, final ShadowRuleConfiguration currentRuleConfig, final ShadowTableConfiguration toBeChangedItemConfig) {
        currentRuleConfig.getTables().put(((AlterNamedRuleItemEvent) event).getItemName(), toBeChangedItemConfig);
    }
    
    @Override
    public void dropRuleItemConfiguration(final DropRuleItemEvent event, final ShadowRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getTables().remove(((DropNamedRuleItemEvent) event).getItemName());
    }
    
    @Override
    public String getType() {
        return ShadowRuleNodePathProvider.RULE_TYPE + "." + ShadowRuleNodePathProvider.TABLES;
    }
}
