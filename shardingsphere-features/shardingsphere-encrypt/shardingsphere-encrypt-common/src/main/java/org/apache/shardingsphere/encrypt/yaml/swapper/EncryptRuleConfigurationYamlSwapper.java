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
import org.apache.shardingsphere.encrypt.api.config.strategy.impl.SPIEncryptStrategyConfiguration;
import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptStrategyConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * Encrypt rule configuration YAML swapper.
 */
public final class EncryptRuleConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlEncryptRuleConfiguration, EncryptRuleConfiguration> {
    
    private final EncryptStrategyConfigurationYamlSwapper encryptStrategyConfigurationYamlSwapper = new EncryptStrategyConfigurationYamlSwapper();
    
    private final EncryptTableRuleConfigurationYamlSwapper encryptTableRuleConfigurationYamlSwapper = new EncryptTableRuleConfigurationYamlSwapper();
    
    @Override
    public YamlEncryptRuleConfiguration swap(final EncryptRuleConfiguration data) {
        YamlEncryptRuleConfiguration result = new YamlEncryptRuleConfiguration();
        data.getEncryptStrategies().forEach(each -> result.getEncryptStrategies().put(each.getName(), encryptStrategyConfigurationYamlSwapper.swap((SPIEncryptStrategyConfiguration) each)));
        data.getTables().forEach(each -> result.getTables().put(each.getName(), encryptTableRuleConfigurationYamlSwapper.swap(each)));
        return result;
    }
    
    @Override
    public EncryptRuleConfiguration swap(final YamlEncryptRuleConfiguration yamlConfiguration) {
        return new EncryptRuleConfiguration(swapEncryptStrategy(yamlConfiguration), swapTables(yamlConfiguration));
    }
    
    private Collection<SPIEncryptStrategyConfiguration> swapEncryptStrategy(final YamlEncryptRuleConfiguration yamlConfiguration) {
        Collection<SPIEncryptStrategyConfiguration> result = new LinkedList<>();
        for (Entry<String, YamlEncryptStrategyConfiguration> entry : yamlConfiguration.getEncryptStrategies().entrySet()) {
            YamlEncryptStrategyConfiguration yamlEncryptStrategyConfiguration = entry.getValue();
            yamlEncryptStrategyConfiguration.setName(entry.getKey());
            result.add(encryptStrategyConfigurationYamlSwapper.swap(yamlEncryptStrategyConfiguration));
        }
        return result;
    }
    
    private Collection<EncryptTableRuleConfiguration> swapTables(final YamlEncryptRuleConfiguration yamlConfiguration) {
        Collection<EncryptTableRuleConfiguration> result = new LinkedList<>();
        for (Entry<String, YamlEncryptTableRuleConfiguration> entry : yamlConfiguration.getTables().entrySet()) {
            YamlEncryptTableRuleConfiguration yamlEncryptTableRuleConfiguration = entry.getValue();
            yamlEncryptTableRuleConfiguration.setName(entry.getKey());
            result.add(encryptTableRuleConfigurationYamlSwapper.swap(yamlEncryptTableRuleConfiguration));
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
