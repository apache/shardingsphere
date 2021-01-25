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

import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.rule.EncryptTableRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * Encrypt rule algorithm provider configuration YAML swapper.
 */
public final class EncryptRuleAlgorithmProviderConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlEncryptRuleConfiguration, AlgorithmProvidedEncryptRuleConfiguration> {
    
    private final EncryptTableRuleConfigurationYamlSwapper tableYamlSwapper = new EncryptTableRuleConfigurationYamlSwapper();
    
    @Override
    public YamlEncryptRuleConfiguration swapToYamlConfiguration(final AlgorithmProvidedEncryptRuleConfiguration data) {
        YamlEncryptRuleConfiguration result = new YamlEncryptRuleConfiguration();
        data.getTables().forEach(each -> result.getTables().put(each.getName(), tableYamlSwapper.swapToYamlConfiguration(each)));
        data.getEncryptors().forEach((key, value) -> result.getEncryptors().put(key, YamlShardingSphereAlgorithmConfiguration.buildByTypedSPI(value)));
        return result;
    }
    
    @Override
    public AlgorithmProvidedEncryptRuleConfiguration swapToObject(final YamlEncryptRuleConfiguration yamlConfig) {
        AlgorithmProvidedEncryptRuleConfiguration result = new AlgorithmProvidedEncryptRuleConfiguration();
        result.setTables(swapTables(yamlConfig));
        return result;
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
    
    @Override
    public Class<AlgorithmProvidedEncryptRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedEncryptRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "ENCRYPT";
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.ORDER + 1;
    }
}
