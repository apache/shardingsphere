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

import org.apache.shardingsphere.api.config.shadow.ShadowRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.shadow.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.underlying.common.yaml.swapper.YamlSwapper;

/**
 * Shadow rule configuration yaml swapper.
 *
 * @author xiayan
 */
public final class ShadowRuleConfigurationYamlSwapper implements YamlSwapper<YamlShadowRuleConfiguration, ShadowRuleConfiguration> {
    
    private final ShardingRuleConfigurationYamlSwapper shardingRuleConfigurationYamlSwapper = new ShardingRuleConfigurationYamlSwapper();
    
    private final EncryptRuleConfigurationYamlSwapper encryptRuleConfigurationYamlSwapper = new EncryptRuleConfigurationYamlSwapper();
    
    private final MasterSlaveRuleConfigurationYamlSwapper masterSlaveRuleConfigurationYamlSwapper = new MasterSlaveRuleConfigurationYamlSwapper();
    
    @Override
    public YamlShadowRuleConfiguration swap(final ShadowRuleConfiguration data) {
        YamlShadowRuleConfiguration result = new YamlShadowRuleConfiguration();
        result.setColumn(data.getColumn());
        result.setShadowMappings(data.getShadowMappings());
        
        if (data.isEncrypt()) {
            result.setEncryptRule(encryptRuleConfigurationYamlSwapper.swap(data.getEncryptRuleConfig()));
        } else if (data.isMasterSlave()) {
            result.setMasterSlaveRule(masterSlaveRuleConfigurationYamlSwapper.swap(data.getMasterSlaveRuleConfig()));
        } else if (data.isSharding()) {
            result.setShardingRule(shardingRuleConfigurationYamlSwapper.swap(data.getShardingRuleConfig()));
        }
        return result;
    }
    
    @Override
    public ShadowRuleConfiguration swap(final YamlShadowRuleConfiguration yamlConfiguration) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setColumn(yamlConfiguration.getColumn());
        result.setShadowMappings(yamlConfiguration.getShadowMappings());
        
        if (yamlConfiguration.isEncrypt()) {
            result.setEncryptRuleConfig(encryptRuleConfigurationYamlSwapper.swap(yamlConfiguration.getEncryptRule()));
        } else if (yamlConfiguration.isMasterSlave()) {
            result.setMasterSlaveRuleConfig(masterSlaveRuleConfigurationYamlSwapper.swap(yamlConfiguration.getMasterSlaveRule()));
        } else if (yamlConfiguration.isSharding()) {
            result.setShardingRuleConfig(shardingRuleConfigurationYamlSwapper.swap(yamlConfiguration.getShardingRule()));
        }
        return result;
    }
}
