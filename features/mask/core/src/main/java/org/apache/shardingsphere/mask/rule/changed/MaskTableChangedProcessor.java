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

package org.apache.shardingsphere.mask.rule.changed;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.metadata.nodepath.MaskRuleNodePathProvider;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.swapper.rule.YamlMaskTableRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.spi.RuleItemConfigurationChangedProcessor;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Mask table changed processor.
 */
public final class MaskTableChangedProcessor implements RuleItemConfigurationChangedProcessor<MaskRuleConfiguration, MaskTableRuleConfiguration> {
    
    @Override
    public MaskTableRuleConfiguration swapRuleItemConfiguration(final AlterRuleItemEvent event, final String yamlContent) {
        return new YamlMaskTableRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlMaskTableRuleConfiguration.class));
    }
    
    @Override
    public MaskRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(MaskRule.class)
                .map(optional -> getConfiguration((MaskRuleConfiguration) optional.getConfiguration())).orElseGet(() -> new MaskRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>()));
    }
    
    private MaskRuleConfiguration getConfiguration(final MaskRuleConfiguration config) {
        return null == config.getTables() ? new MaskRuleConfiguration(new LinkedList<>(), config.getMaskAlgorithms()) : config;
    }
    
    @Override
    public void changeRuleItemConfiguration(final AlterRuleItemEvent event, final MaskRuleConfiguration currentRuleConfig, final MaskTableRuleConfiguration toBeChangedItemConfig) {
        // TODO refactor DistSQL to only persist config
        currentRuleConfig.getTables().removeIf(each -> each.getName().equals(toBeChangedItemConfig.getName()));
        currentRuleConfig.getTables().add(toBeChangedItemConfig);
    }
    
    @Override
    public void dropRuleItemConfiguration(final DropRuleItemEvent event, final MaskRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getTables().removeIf(each -> each.getName().equals(((DropNamedRuleItemEvent) event).getItemName()));
    }
    
    @Override
    public String getType() {
        return MaskRuleNodePathProvider.RULE_TYPE + "." + MaskRuleNodePathProvider.TABLES;
    }
}
