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
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlCompatibleEncryptColumnRuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * YAML encrypt column rule configuration swapper.
 *
 * @deprecated Should use new api, compatible api will remove in next version.
 */
@Deprecated
public final class YamlCompatibleEncryptColumnRuleConfigurationSwapper implements YamlConfigurationSwapper<YamlCompatibleEncryptColumnRuleConfiguration, EncryptColumnRuleConfiguration> {
    
    @Override
    public YamlCompatibleEncryptColumnRuleConfiguration swapToYamlConfiguration(final EncryptColumnRuleConfiguration data) {
        YamlCompatibleEncryptColumnRuleConfiguration result = new YamlCompatibleEncryptColumnRuleConfiguration();
        result.setLogicColumn(data.getName());
        result.setCipherColumn(data.getCipher().getName());
        result.setEncryptorName(data.getCipher().getEncryptorName());
        if (data.getLikeQuery().isPresent()) {
            result.setLikeQueryColumn(data.getLikeQuery().get().getName());
            result.setLikeQueryEncryptorName(data.getLikeQuery().get().getEncryptorName());
        }
        if (data.getAssistedQuery().isPresent()) {
            result.setAssistedQueryColumn(data.getAssistedQuery().get().getName());
            result.setAssistedQueryEncryptorName(data.getAssistedQuery().get().getEncryptorName());
        }
        return result;
    }
    
    @Override
    public EncryptColumnRuleConfiguration swapToObject(final YamlCompatibleEncryptColumnRuleConfiguration yamlConfig) {
        EncryptColumnItemRuleConfiguration cipherColumnConfig = new EncryptColumnItemRuleConfiguration(yamlConfig.getCipherColumn(), yamlConfig.getEncryptorName());
        EncryptColumnRuleConfiguration result = new EncryptColumnRuleConfiguration(yamlConfig.getLogicColumn(), cipherColumnConfig);
        if (null != yamlConfig.getAssistedQueryColumn()) {
            EncryptColumnItemRuleConfiguration assistedQueryColumnConfig = new EncryptColumnItemRuleConfiguration(yamlConfig.getAssistedQueryColumn(), yamlConfig.getAssistedQueryEncryptorName());
            result.setAssistedQuery(assistedQueryColumnConfig);
        }
        if (null != yamlConfig.getLikeQueryColumn()) {
            EncryptColumnItemRuleConfiguration likeQueryColumnConfig = new EncryptColumnItemRuleConfiguration(yamlConfig.getLikeQueryColumn(), yamlConfig.getLikeQueryEncryptorName());
            result.setLikeQuery(likeQueryColumnConfig);
        }
        return result;
    }
}
