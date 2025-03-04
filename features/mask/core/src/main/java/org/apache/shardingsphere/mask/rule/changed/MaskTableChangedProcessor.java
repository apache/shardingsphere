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
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.swapper.rule.YamlMaskTableRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Mask table changed processor.
 */
public final class MaskTableChangedProcessor implements RuleItemConfigurationChangedProcessor<MaskRuleConfiguration, MaskTableRuleConfiguration> {
    
    @Override
    public MaskTableRuleConfiguration swapRuleItemConfiguration(final String itemName, final String yamlContent) {
        return new YamlMaskTableRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlMaskTableRuleConfiguration.class));
    }
    
    @Override
    public MaskRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(MaskRule.class).map(MaskRule::getConfiguration).orElseGet(() -> new MaskRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>()));
    }
    
    @Override
    public void changeRuleItemConfiguration(final String itemName, final MaskRuleConfiguration currentRuleConfig, final MaskTableRuleConfiguration toBeChangedItemConfig) {
        // TODO refactor DistSQL to only persist config
        currentRuleConfig.getTables().removeIf(each -> each.getName().equals(toBeChangedItemConfig.getName()));
        currentRuleConfig.getTables().add(toBeChangedItemConfig);
    }
    
    @Override
    public void dropRuleItemConfiguration(final String itemName, final MaskRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getTables().removeIf(each -> each.getName().equals(itemName));
    }
    
    @Override
    public RuleChangedItemType getType() {
        return new RuleChangedItemType("mask", "tables");
    }
}
