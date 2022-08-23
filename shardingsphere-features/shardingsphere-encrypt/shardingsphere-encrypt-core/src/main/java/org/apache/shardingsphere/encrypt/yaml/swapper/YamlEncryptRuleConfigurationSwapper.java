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
import org.apache.shardingsphere.encrypt.yaml.swapper.rule.YamlEncryptTableRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML encrypt rule configuration swapper.
 */
public final class YamlEncryptRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlEncryptRuleConfiguration, EncryptRuleConfiguration> {
    
    private final YamlEncryptTableRuleConfigurationSwapper tableSwapper = new YamlEncryptTableRuleConfigurationSwapper();
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    @Override
    public YamlEncryptRuleConfiguration swapToYamlConfiguration(final EncryptRuleConfiguration data) {
        YamlEncryptRuleConfiguration result = new YamlEncryptRuleConfiguration();
        data.getTables().forEach(each -> result.getTables().put(each.getName(), tableSwapper.swapToYamlConfiguration(each)));
        data.getEncryptors().forEach((key, value) -> result.getEncryptors().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
        result.setQueryWithCipherColumn(data.isQueryWithCipherColumn());
        result.setDataConverterName(data.getDataConverterName());
        return result;
    }
    
    @Override
    public EncryptRuleConfiguration swapToObject(final YamlEncryptRuleConfiguration yamlConfig) {
        return new EncryptRuleConfiguration(swapTables(yamlConfig), swapEncryptAlgorithm(yamlConfig), yamlConfig.isQueryWithCipherColumn(),
                yamlConfig.getDataConverterName());
    }
    
    private Collection<EncryptTableRuleConfiguration> swapTables(final YamlEncryptRuleConfiguration yamlConfig) {
        Collection<EncryptTableRuleConfiguration> result = new LinkedList<>();
        for (Entry<String, YamlEncryptTableRuleConfiguration> entry : yamlConfig.getTables().entrySet()) {
            YamlEncryptTableRuleConfiguration yamlEncryptTableRuleConfig = entry.getValue();
            yamlEncryptTableRuleConfig.setName(entry.getKey());
            result.add(tableSwapper.swapToObject(yamlEncryptTableRuleConfig));
        }
        return result;
    }
    
    private Map<String, AlgorithmConfiguration> swapEncryptAlgorithm(final YamlEncryptRuleConfiguration yamlConfig) {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>(yamlConfig.getEncryptors().size(), 1);
        for (Entry<String, YamlAlgorithmConfiguration> entry : yamlConfig.getEncryptors().entrySet()) {
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
