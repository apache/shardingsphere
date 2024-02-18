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

package org.apache.shardingsphere.mask.yaml.swapper;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.constant.MaskOrder;
import org.apache.shardingsphere.mask.yaml.config.YamlMaskRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.swapper.rule.YamlMaskTableRuleConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML mask rule configuration swapper.
 */
public final class YamlMaskRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlMaskRuleConfiguration, MaskRuleConfiguration> {
    
    private final YamlMaskTableRuleConfigurationSwapper tableSwapper = new YamlMaskTableRuleConfigurationSwapper();
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    @Override
    public YamlMaskRuleConfiguration swapToYamlConfiguration(final MaskRuleConfiguration data) {
        YamlMaskRuleConfiguration result = new YamlMaskRuleConfiguration();
        data.getTables().forEach(each -> result.getTables().put(each.getName(), tableSwapper.swapToYamlConfiguration(each)));
        data.getMaskAlgorithms().forEach((key, value) -> result.getMaskAlgorithms().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
        return result;
    }
    
    @Override
    public MaskRuleConfiguration swapToObject(final YamlMaskRuleConfiguration yamlConfig) {
        return new MaskRuleConfiguration(swapTables(yamlConfig), swapMaskAlgorithm(yamlConfig));
    }
    
    private Collection<MaskTableRuleConfiguration> swapTables(final YamlMaskRuleConfiguration yamlConfig) {
        Collection<MaskTableRuleConfiguration> result = new LinkedList<>();
        for (Entry<String, YamlMaskTableRuleConfiguration> entry : yamlConfig.getTables().entrySet()) {
            YamlMaskTableRuleConfiguration yamlMaskTableRuleConfig = entry.getValue();
            yamlMaskTableRuleConfig.setName(entry.getKey());
            result.add(tableSwapper.swapToObject(yamlMaskTableRuleConfig));
        }
        return result;
    }
    
    private Map<String, AlgorithmConfiguration> swapMaskAlgorithm(final YamlMaskRuleConfiguration yamlConfig) {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>(yamlConfig.getMaskAlgorithms().size(), 1F);
        for (Entry<String, YamlAlgorithmConfiguration> entry : yamlConfig.getMaskAlgorithms().entrySet()) {
            result.put(entry.getKey(), algorithmSwapper.swapToObject(entry.getValue()));
        }
        return result;
    }
    
    @Override
    public Class<MaskRuleConfiguration> getTypeClass() {
        return MaskRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "MASK";
    }
    
    @Override
    public int getOrder() {
        return MaskOrder.ORDER;
    }
}
