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

package org.apache.shardingsphere.shardingjdbc.spring.boot.rule;

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.masterslave.yaml.swapper.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shadow.yaml.swapper.ShadowRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Spring boot rules configuration YAML swapper.
 */
public final class SpringBootRulesConfigurationYamlSwapper implements YamlSwapper<SpringBootRulesConfigurationProperties, Collection<RuleConfiguration>> {
    
    private final ShardingRuleConfigurationYamlSwapper shardingRuleConfigurationYamlSwapper = new ShardingRuleConfigurationYamlSwapper();
    
    private final MasterSlaveRuleConfigurationYamlSwapper masterSlaveRuleConfigurationYamlSwapper = new MasterSlaveRuleConfigurationYamlSwapper();
    
    private final EncryptRuleConfigurationYamlSwapper encryptRuleConfigurationYamlSwapper = new EncryptRuleConfigurationYamlSwapper();
    
    private final ShadowRuleConfigurationYamlSwapper shadowRuleConfigurationYamlSwapper = new ShadowRuleConfigurationYamlSwapper();
    
    @Override
    public SpringBootRulesConfigurationProperties swap(final Collection<RuleConfiguration> data) {
        SpringBootRulesConfigurationProperties result = new SpringBootRulesConfigurationProperties();
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
    public Collection<RuleConfiguration> swap(final SpringBootRulesConfigurationProperties configurations) {
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
