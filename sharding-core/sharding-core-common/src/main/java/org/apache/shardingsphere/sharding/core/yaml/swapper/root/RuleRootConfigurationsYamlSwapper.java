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

package org.apache.shardingsphere.sharding.core.yaml.swapper.root;

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.core.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.sharding.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.sharding.core.yaml.config.shadow.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.core.yaml.config.sharding.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.core.yaml.swapper.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.core.yaml.swapper.ShadowRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.core.yaml.swapper.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.config.YamlRuleConfiguration;
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
                result.getRules().add(shardingRuleConfigurationYamlSwapper.swap((ShardingRuleConfiguration) each));
            } else if (each instanceof MasterSlaveRuleConfiguration) {
                result.getRules().add(masterSlaveRuleConfigurationYamlSwapper.swap((MasterSlaveRuleConfiguration) each));
            } else if (each instanceof EncryptRuleConfiguration) {
                result.getRules().add(encryptRuleConfigurationYamlSwapper.swap((EncryptRuleConfiguration) each));
            } else if (each instanceof ShadowRuleConfiguration) {
                result.getRules().add(shadowRuleConfigurationYamlSwapper.swap((ShadowRuleConfiguration) each));
            }
        }
        return result;
    }
    
    @Override
    public Collection<RuleConfiguration> swap(final YamlRootRuleConfigurations configurations) {
        Collection<RuleConfiguration> result = new LinkedList<>();
        for (YamlRuleConfiguration each : configurations.getRules()) {
            if (each instanceof YamlShardingRuleConfiguration) {
                result.add(shardingRuleConfigurationYamlSwapper.swap((YamlShardingRuleConfiguration) each));
            }
            if (each instanceof YamlMasterSlaveRuleConfiguration) {
                result.add(masterSlaveRuleConfigurationYamlSwapper.swap((YamlMasterSlaveRuleConfiguration) each));
            }
            if (each instanceof YamlEncryptRuleConfiguration) {
                result.add(encryptRuleConfigurationYamlSwapper.swap((YamlEncryptRuleConfiguration) each));
            }
            if (each instanceof YamlShadowRuleConfiguration) {
                result.add(shadowRuleConfigurationYamlSwapper.swap((YamlShadowRuleConfiguration) each));
            }
        }
        return result;
    }
}
