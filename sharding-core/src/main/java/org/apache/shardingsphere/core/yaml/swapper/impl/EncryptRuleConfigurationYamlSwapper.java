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

package org.apache.shardingsphere.core.yaml.swapper.impl;

import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.encrypt.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.encrypt.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.YamlSwapper;

import java.util.Map.Entry;

/**
 * Encrypt rule configuration yaml swapper.
 *
 * @author panjuan
 */
public final class EncryptRuleConfigurationYamlSwapper implements YamlSwapper<YamlEncryptRuleConfiguration, EncryptRuleConfiguration> {
    
    @Override
    public YamlEncryptRuleConfiguration swap(final EncryptRuleConfiguration encryptRuleConfiguration) {
        YamlEncryptRuleConfiguration result = new YamlEncryptRuleConfiguration();
        result.setDefaultEncryptor(new EncryptorConfigurationYamlSwapper().swap(encryptRuleConfiguration.getDefaultEncryptorConfig()));
        for (EncryptTableRuleConfiguration each : encryptRuleConfiguration.getTableRuleConfigs()) {
            result.getTables().put(each.getTable(), new EncryptTableRuleConfigurationYamlSwapper().swap(each));
        }
        return result;
    }
    
    @Override
    public EncryptRuleConfiguration swap(final YamlEncryptRuleConfiguration yamlEncryptRuleConfiguration) {
        EncryptRuleConfiguration result = new EncryptRuleConfiguration();
        result.setDefaultEncryptorConfig(new EncryptorConfigurationYamlSwapper().swap(yamlEncryptRuleConfiguration.getDefaultEncryptor()));
        for (Entry<String, YamlEncryptTableRuleConfiguration> entry : yamlEncryptRuleConfiguration.getTables().entrySet()) {
            YamlEncryptTableRuleConfiguration tableRuleConfig = entry.getValue();
            tableRuleConfig.setTable(entry.getKey());
            result.getTableRuleConfigs().add(new EncryptTableRuleConfigurationYamlSwapper().swap(tableRuleConfig));
        }
        return result;
    }
}
