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

import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptColumnRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;

/**
 * Encrypt column rule configuration YAML swapper.
 */
public final class EncryptColumnRuleConfigurationYamlSwapper implements YamlSwapper<YamlEncryptColumnRuleConfiguration, EncryptColumnRuleConfiguration> {
    
    @Override
    public YamlEncryptColumnRuleConfiguration swapToYamlConfiguration(final EncryptColumnRuleConfiguration data) {
        YamlEncryptColumnRuleConfiguration result = new YamlEncryptColumnRuleConfiguration();
        result.setPlainColumn(data.getPlainColumn());
        result.setCipherColumn(data.getCipherColumn());
        result.setAssistedQueryColumn(data.getAssistedQueryColumn());
        result.setEncryptorName(data.getEncryptorName());
        return result;
    }
    
    @Override
    public EncryptColumnRuleConfiguration swapToObject(final YamlEncryptColumnRuleConfiguration yamlConfig) {
        return new EncryptColumnRuleConfiguration(
                yamlConfig.getLogicColumn(), yamlConfig.getCipherColumn(), yamlConfig.getAssistedQueryColumn(), yamlConfig.getPlainColumn(), yamlConfig.getEncryptorName());
    }
}
