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

package org.apache.shardingsphere.encrypt.yaml.swapper;

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.rule.EncryptTableRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.ShardingSphereAlgorithmConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rulealtered.OnRuleAlteredActionConfigurationYamlSwapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Encrypt rule configuration YAML swapper.
 */
public final class EncryptRuleConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlEncryptRuleConfiguration, EncryptRuleConfiguration> {
    
    private final EncryptTableRuleConfigurationYamlSwapper tableYamlSwapper = new EncryptTableRuleConfigurationYamlSwapper();
    
    private final ShardingSphereAlgorithmConfigurationYamlSwapper algorithmSwapper = new ShardingSphereAlgorithmConfigurationYamlSwapper();
    
    private final OnRuleAlteredActionConfigurationYamlSwapper onRuleAlteredActionYamlSwapper = new OnRuleAlteredActionConfigurationYamlSwapper();
    
    @Override
    public YamlEncryptRuleConfiguration swapToYamlConfiguration(final EncryptRuleConfiguration data) {
        YamlEncryptRuleConfiguration result = new YamlEncryptRuleConfiguration();
        data.getTables().forEach(each -> result.getTables().put(each.getName(), tableYamlSwapper.swapToYamlConfiguration(each)));
        data.getEncryptors().forEach((key, value) -> result.getEncryptors().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
        result.setQueryWithCipherColumn(data.isQueryWithCipherColumn());
        result.setDataConverterName(data.getDataConverterName());
        data.getDataConverters().forEach((key, value) -> result.getDataConverters().put(key, onRuleAlteredActionYamlSwapper.swapToYamlConfiguration(value)));
        return result;
    }
    
    @Override
    public EncryptRuleConfiguration swapToObject(final YamlEncryptRuleConfiguration yamlConfig) {
        Map<String, OnRuleAlteredActionConfiguration> dataConverters = new LinkedHashMap<>();
        yamlConfig.getDataConverters().forEach((key, value) -> dataConverters.put(key, onRuleAlteredActionYamlSwapper.swapToObject(value)));
        return new EncryptRuleConfiguration(swapTables(yamlConfig), swapEncryptAlgorithm(yamlConfig), yamlConfig.isQueryWithCipherColumn(),
                yamlConfig.getDataConverterName(), dataConverters);
    }
    
    private Collection<EncryptTableRuleConfiguration> swapTables(final YamlEncryptRuleConfiguration yamlConfig) {
        Collection<EncryptTableRuleConfiguration> result = new LinkedList<>();
        for (Entry<String, YamlEncryptTableRuleConfiguration> entry : yamlConfig.getTables().entrySet()) {
            YamlEncryptTableRuleConfiguration yamlEncryptTableRuleConfig = entry.getValue();
            yamlEncryptTableRuleConfig.setName(entry.getKey());
            result.add(tableYamlSwapper.swapToObject(yamlEncryptTableRuleConfig));
        }
        return result;
    }
    
    private Map<String, ShardingSphereAlgorithmConfiguration> swapEncryptAlgorithm(final YamlEncryptRuleConfiguration yamlConfig) {
        Map<String, ShardingSphereAlgorithmConfiguration> result = new LinkedHashMap<>(yamlConfig.getEncryptors().size(), 1);
        for (Entry<String, YamlShardingSphereAlgorithmConfiguration> entry : yamlConfig.getEncryptors().entrySet()) {
            result.put(entry.getKey(), algorithmSwapper.swapToObject(entry.getValue()));
        }
        return result;
    }
    
    @Override
    public Class<EncryptRuleConfiguration> getTypeClass() {
        return EncryptRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "ENCRYPT";
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.ORDER;
    }
}
