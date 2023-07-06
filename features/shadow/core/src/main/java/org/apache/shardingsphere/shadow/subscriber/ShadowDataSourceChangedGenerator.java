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

package org.apache.shardingsphere.shadow.subscriber;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.subsciber.RuleItemConfigurationChangedGenerator;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.event.datasource.AlterShadowDataSourceEvent;
import org.apache.shardingsphere.shadow.event.datasource.DropShadowDataSourceEvent;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;

import java.util.Collection;
import java.util.Collections;

/**
 * Shadow data source changed generator.
 */
public final class ShadowDataSourceChangedGenerator implements RuleItemConfigurationChangedGenerator<ShadowRuleConfiguration, ShadowDataSourceConfiguration> {
    
    @Override
    public ShadowDataSourceConfiguration swapRuleItemConfigurationFromEvent(final AlterRuleItemEvent event, final String yamlContent) {
        YamlShadowDataSourceConfiguration yamlConfig = YamlEngine.unmarshal(yamlContent, YamlShadowDataSourceConfiguration.class);
        return new ShadowDataSourceConfiguration(((AlterNamedRuleItemEvent) event).getItemName(), yamlConfig.getProductionDataSourceName(), yamlConfig.getShadowDataSourceName());
    }
    
    @Override
    public ShadowRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(ShadowRule.class).map(optional -> (ShadowRuleConfiguration) optional.getConfiguration()).orElseGet(ShadowRuleConfiguration::new);
    }
    
    @Override
    public void changeRuleItemConfiguration(final AlterRuleItemEvent event, final ShadowRuleConfiguration currentRuleConfig, final ShadowDataSourceConfiguration toBeChangedItemConfig) {
        // TODO refactor DistSQL to only persist config
        currentRuleConfig.getDataSources().removeIf(each -> each.getName().equals(toBeChangedItemConfig.getName()));
        currentRuleConfig.getDataSources().add(toBeChangedItemConfig);
    }
    
    @Override
    public void dropRuleItemConfiguration(final DropRuleItemEvent event, final ShadowRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getDataSources().removeIf(each -> each.getName().equals(((DropNamedRuleItemEvent) event).getItemName()));
    }
    
    @Override
    public String getType() {
        return AlterShadowDataSourceEvent.class.getName();
    }
    
    @Override
    public Collection<String> getTypeAliases() {
        return Collections.singleton(DropShadowDataSourceEvent.class.getName());
    }
}
