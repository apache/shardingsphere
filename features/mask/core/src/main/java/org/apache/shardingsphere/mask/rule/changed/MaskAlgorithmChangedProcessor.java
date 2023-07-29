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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.metadata.nodepath.MaskRuleNodePathProvider;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mode.spi.RuleItemConfigurationChangedProcessor;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Mask algorithm changed processor.
 */
public final class MaskAlgorithmChangedProcessor implements RuleItemConfigurationChangedProcessor<MaskRuleConfiguration, AlgorithmConfiguration> {
    
    @Override
    public AlgorithmConfiguration swapRuleItemConfiguration(final AlterRuleItemEvent event, final String yamlContent) {
        return new YamlAlgorithmConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlAlgorithmConfiguration.class));
    }
    
    @Override
    public MaskRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(MaskRule.class)
                .map(maskRule -> getConfiguration((MaskRuleConfiguration) maskRule.getConfiguration())).orElseGet(() -> new MaskRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>()));
    }
    
    private MaskRuleConfiguration getConfiguration(final MaskRuleConfiguration config) {
        return null == config.getMaskAlgorithms() ? new MaskRuleConfiguration(config.getTables(), new LinkedHashMap<>()) : config;
    }
    
    @Override
    public void changeRuleItemConfiguration(final AlterRuleItemEvent event, final MaskRuleConfiguration currentRuleConfig, final AlgorithmConfiguration toBeChangedItemConfig) {
        currentRuleConfig.getMaskAlgorithms().put(((AlterNamedRuleItemEvent) event).getItemName(), toBeChangedItemConfig);
    }
    
    @Override
    public void dropRuleItemConfiguration(final DropRuleItemEvent event, final MaskRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getMaskAlgorithms().remove(((DropNamedRuleItemEvent) event).getItemName());
    }
    
    @Override
    public String getType() {
        return MaskRuleNodePathProvider.RULE_TYPE + "." + MaskRuleNodePathProvider.MASK_ALGORITHMS;
    }
}
