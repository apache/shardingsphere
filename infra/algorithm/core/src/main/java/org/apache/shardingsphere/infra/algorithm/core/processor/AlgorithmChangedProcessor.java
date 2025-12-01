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

package org.apache.shardingsphere.infra.algorithm.core.processor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;

import java.util.Map;

/**
 * Algorithm changed processor.
 * 
 * @param <T> type of rule configuration
 */
@RequiredArgsConstructor
public abstract class AlgorithmChangedProcessor<T extends RuleConfiguration> implements RuleItemConfigurationChangedProcessor<T, AlgorithmConfiguration> {
    
    private final Class<? extends ShardingSphereRule> ruleClass;
    
    @Override
    public final AlgorithmConfiguration swapRuleItemConfiguration(final String itemName, final String yamlContent) {
        return new YamlAlgorithmConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlAlgorithmConfiguration.class));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final T findRuleConfiguration(final ShardingSphereDatabase database) {
        return (T) database.getRuleMetaData().findSingleRule(ruleClass).map(ShardingSphereRule::getConfiguration).orElseGet(this::createEmptyRuleConfiguration);
    }
    
    @Override
    public final void changeRuleItemConfiguration(final String itemName, final T currentRuleConfig, final AlgorithmConfiguration toBeChangedItemConfig) {
        getAlgorithmConfigurations(currentRuleConfig).put(itemName, toBeChangedItemConfig);
    }
    
    @Override
    public final void dropRuleItemConfiguration(final String itemName, final T currentRuleConfig) {
        getAlgorithmConfigurations(currentRuleConfig).remove(itemName);
    }
    
    protected abstract T createEmptyRuleConfiguration();
    
    protected abstract Map<String, AlgorithmConfiguration> getAlgorithmConfigurations(T currentRuleConfig);
}
