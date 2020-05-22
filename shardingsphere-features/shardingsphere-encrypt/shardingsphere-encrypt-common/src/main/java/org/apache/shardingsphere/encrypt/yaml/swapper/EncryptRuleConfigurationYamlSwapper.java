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

import com.google.common.collect.Maps;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;

/**
 * Encrypt rule configuration yaml swapper.
 */
public final class EncryptRuleConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlEncryptRuleConfiguration, EncryptRuleConfiguration> {
    
    private final EncryptorRuleConfigurationYamlSwapper encryptorRuleConfigurationYamlSwapper = new EncryptorRuleConfigurationYamlSwapper();
    
    private final EncryptTableRuleConfigurationYamlSwapper encryptTableRuleConfigurationYamlSwapper = new EncryptTableRuleConfigurationYamlSwapper();
    
    @Override
    public YamlEncryptRuleConfiguration swap(final EncryptRuleConfiguration data) {
        YamlEncryptRuleConfiguration result = new YamlEncryptRuleConfiguration();
        result.getEncryptors().putAll(Maps.transformValues(data.getEncryptors(), encryptorRuleConfigurationYamlSwapper::swap));
        result.getTables().putAll(Maps.transformValues(data.getTables(), encryptTableRuleConfigurationYamlSwapper::swap));
        return result;
    }
    
    @Override
    public EncryptRuleConfiguration swap(final YamlEncryptRuleConfiguration yamlConfiguration) {
        return new EncryptRuleConfiguration(Maps.transformValues(yamlConfiguration.getEncryptors(), encryptorRuleConfigurationYamlSwapper::swap), 
                Maps.transformValues(yamlConfiguration.getTables(), encryptTableRuleConfigurationYamlSwapper::swap));
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
        return 20;
    }
}
