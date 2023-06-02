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

package org.apache.shardingsphere.encrypt.yaml.swapper.rule;

import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * YAML encrypt column item rule configuration swapper.
 */
public final class YamlEncryptColumnItemRuleConfigurationSwapper implements YamlConfigurationSwapper<YamlEncryptColumnItemRuleConfiguration, EncryptColumnItemRuleConfiguration> {
    
    @Override
    public YamlEncryptColumnItemRuleConfiguration swapToYamlConfiguration(final EncryptColumnItemRuleConfiguration data) {
        YamlEncryptColumnItemRuleConfiguration result = new YamlEncryptColumnItemRuleConfiguration();
        result.setName(data.getName());
        result.setEncryptorName(data.getEncryptorName());
        return result;
    }
    
    @Override
    public EncryptColumnItemRuleConfiguration swapToObject(final YamlEncryptColumnItemRuleConfiguration yamlConfig) {
        return new EncryptColumnItemRuleConfiguration(yamlConfig.getName(), yamlConfig.getEncryptorName());
    }
}
