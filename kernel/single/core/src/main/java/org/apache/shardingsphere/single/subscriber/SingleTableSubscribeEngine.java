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

package org.apache.shardingsphere.single.subscriber;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.subsciber.RuleItemChangedSubscribeEngine;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.event.config.AlterSingleTableEvent;
import org.apache.shardingsphere.single.event.config.DropSingleTableEvent;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.yaml.config.pojo.YamlSingleRuleConfiguration;
import org.apache.shardingsphere.single.yaml.config.swapper.YamlSingleRuleConfigurationSwapper;

import java.util.Collection;
import java.util.Collections;

/**
 * Single table subscribe engine.
 */
public final class SingleTableSubscribeEngine implements RuleItemChangedSubscribeEngine<SingleRuleConfiguration, SingleRuleConfiguration> {
    
    @Override
    public SingleRuleConfiguration swapRuleItemConfigurationFromEvent(final AlterRuleItemEvent event, final String yamlContent) {
        return new YamlSingleRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlSingleRuleConfiguration.class));
    }
    
    @Override
    public SingleRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(SingleRule.class).map(SingleRule::getConfiguration).orElseGet(SingleRuleConfiguration::new);
    }
    
    @Override
    public void changeRuleItemConfiguration(final AlterRuleItemEvent event, final SingleRuleConfiguration currentRuleConfig, final SingleRuleConfiguration toBeChangedItemConfig) {
        currentRuleConfig.getTables().clear();
        currentRuleConfig.getTables().addAll(toBeChangedItemConfig.getTables());
        toBeChangedItemConfig.getDefaultDataSource().ifPresent(optional -> currentRuleConfig.setDefaultDataSource(toBeChangedItemConfig.getDefaultDataSource().get()));
    }
    
    @Override
    public void dropRuleItemConfiguration(final DropRuleItemEvent event, final SingleRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getTables().clear();
        currentRuleConfig.setDefaultDataSource(null);
    }
    
    @Override
    public String getType() {
        return AlterSingleTableEvent.class.getName();
    }
    
    @Override
    public Collection<String> getTypeAliases() {
        return Collections.singleton(DropSingleTableEvent.class.getName());
    }
}
