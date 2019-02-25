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

import org.apache.shardingsphere.api.config.encryptor.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.encrypt.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.YamlSwapper;

/**
 * Encrypt rule configuration yaml swapper.
 *
 * @author panjuan
 */
public final class EncryptTableRuleConfigurationYamlSwapper implements YamlSwapper<YamlEncryptTableRuleConfiguration, EncryptTableRuleConfiguration> {
    
    @Override
    public YamlEncryptTableRuleConfiguration swap(final EncryptTableRuleConfiguration encryptTableRuleConfiguration) {
        YamlEncryptTableRuleConfiguration result = new YamlEncryptTableRuleConfiguration();
        result.setTable(encryptTableRuleConfiguration.getTable());
        result.setEncryptor(new EncryptorConfigurationYamlSwapper().swap(encryptTableRuleConfiguration.getEncryptorConfig()));
        return result;
    }
    
    @Override
    public EncryptTableRuleConfiguration swap(final YamlEncryptTableRuleConfiguration yamlEncryptTableRuleConfiguration) {
        EncryptTableRuleConfiguration result = new EncryptTableRuleConfiguration();
        result.setTable(yamlEncryptTableRuleConfiguration.getTable());
        result.setEncryptorConfig(new EncryptorConfigurationYamlSwapper().swap(yamlEncryptTableRuleConfiguration.getEncryptor()));
        return result;
    }
}
