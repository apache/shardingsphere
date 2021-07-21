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

package org.apache.shardingsphere.authority.yaml.swapper;

import org.apache.shardingsphere.authority.api.config.PasswordEncryptRuleConfiguration;
import org.apache.shardingsphere.authority.constant.PasswordEncryptOrder;
import org.apache.shardingsphere.authority.yaml.config.YamlPasswordEncryptRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.ShardingSphereAlgorithmConfigurationYamlSwapper;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * PasswordEncrypt rule configuration YAML swapper.
 */
public final class PasswordEncryptRuleConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlPasswordEncryptRuleConfiguration, PasswordEncryptRuleConfiguration> {
    
    private final ShardingSphereAlgorithmConfigurationYamlSwapper algorithmSwapper = new ShardingSphereAlgorithmConfigurationYamlSwapper();
    
    @Override
    public YamlPasswordEncryptRuleConfiguration swapToYamlConfiguration(final PasswordEncryptRuleConfiguration data) {
        YamlPasswordEncryptRuleConfiguration result = new YamlPasswordEncryptRuleConfiguration();
        result.setProps(data.getProps());
        result.setEncryptors(
                data.getEncryptors().entrySet()
                .stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> algorithmSwapper.swapToYamlConfiguration(entry.getValue()))
                )
        );
        return result;
    }
    
    @Override
    public PasswordEncryptRuleConfiguration swapToObject(final YamlPasswordEncryptRuleConfiguration yamlConfig) {
        Map<String, ShardingSphereAlgorithmConfiguration> encryptors = yamlConfig.getEncryptors().entrySet()
                .stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> algorithmSwapper.swapToObject(entry.getValue()))
                );
        Properties props = yamlConfig.getProps();
        return new PasswordEncryptRuleConfiguration(props, encryptors);
    }
    
    @Override
    public Class<PasswordEncryptRuleConfiguration> getTypeClass() {
        return PasswordEncryptRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "PASSWORD_ENCRYPT";
    }
    
    @Override
    public int getOrder() {
        return PasswordEncryptOrder.ORDER;
    }
}
