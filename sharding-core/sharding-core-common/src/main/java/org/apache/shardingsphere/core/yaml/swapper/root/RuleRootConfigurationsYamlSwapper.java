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

package org.apache.shardingsphere.core.yaml.swapper.root;

import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.shadow.ShadowRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.core.yaml.swapper.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.ShadowRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.swapper.YamlSwapper;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Rule root configurations YAML swapper.
 */
public final class RuleRootConfigurationsYamlSwapper implements YamlSwapper<YamlRootRuleConfigurations, Collection<RuleConfiguration>> {
    
    private final ShardingRuleConfigurationYamlSwapper shardingRuleConfigurationYamlSwapper = new ShardingRuleConfigurationYamlSwapper();
    
    private final MasterSlaveRuleConfigurationYamlSwapper masterSlaveRuleConfigurationYamlSwapper = new MasterSlaveRuleConfigurationYamlSwapper();
    
    private final EncryptRuleConfigurationYamlSwapper encryptRuleConfigurationYamlSwapper = new EncryptRuleConfigurationYamlSwapper();
    
    private final ShadowRuleConfigurationYamlSwapper shadowRuleConfigurationYamlSwapper = new ShadowRuleConfigurationYamlSwapper();
    
    @Override
    public YamlRootRuleConfigurations swap(final Collection<RuleConfiguration> data) {
        YamlRootRuleConfigurations result = new YamlRootRuleConfigurations();
        for (RuleConfiguration each : data) {
            if (each instanceof ShardingRuleConfiguration) {
                result.setShardingRule(shardingRuleConfigurationYamlSwapper.swap((ShardingRuleConfiguration) each));
            } else if (each instanceof MasterSlaveRuleConfiguration) {
                result.setMasterSlaveRule(masterSlaveRuleConfigurationYamlSwapper.swap((MasterSlaveRuleConfiguration) each));
            } else if (each instanceof EncryptRuleConfiguration) {
                result.setEncryptRule(encryptRuleConfigurationYamlSwapper.swap((EncryptRuleConfiguration) each));
            } else if (each instanceof ShadowRuleConfiguration) {
                result.setShadowRule(shadowRuleConfigurationYamlSwapper.swap((ShadowRuleConfiguration) each));
            }
        }
        return result;
    }
    
    @Override
    public Collection<RuleConfiguration> swap(final YamlRootRuleConfigurations configurations) {
        Collection<RuleConfiguration> result = new LinkedList<>();
        if (null != configurations.getShardingRule()) {
            result.add(shardingRuleConfigurationYamlSwapper.swap(configurations.getShardingRule()));
        }
        if (null != configurations.getMasterSlaveRule()) {
            result.add(masterSlaveRuleConfigurationYamlSwapper.swap(configurations.getMasterSlaveRule()));
        }
        if (null != configurations.getEncryptRule()) {
            result.add(encryptRuleConfigurationYamlSwapper.swap(configurations.getEncryptRule()));
        }
        if (null != configurations.getShadowRule()) {
            result.add(shadowRuleConfigurationYamlSwapper.swap(configurations.getShadowRule()));
        }
        return result;
    }
}
