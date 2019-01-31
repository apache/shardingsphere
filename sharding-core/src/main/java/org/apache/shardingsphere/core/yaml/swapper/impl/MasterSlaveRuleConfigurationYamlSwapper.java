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

import com.google.common.base.Strings;
import org.apache.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.exception.ShardingConfigurationException;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.YamlSwapper;

/**
 * Master-slave rule configuration YAML swapper.
 *
 * @author zhangliang
 */
public final class MasterSlaveRuleConfigurationYamlSwapper implements YamlSwapper<YamlMasterSlaveRuleConfiguration, MasterSlaveRuleConfiguration> {
    
    @Override
    public YamlMasterSlaveRuleConfiguration swap(final MasterSlaveRuleConfiguration data) {
        YamlMasterSlaveRuleConfiguration result = new YamlMasterSlaveRuleConfiguration();
        result.setName(data.getName());
        result.setMasterDataSourceName(data.getMasterDataSourceName());
        result.setSlaveDataSourceNames(data.getSlaveDataSourceNames());
        if (null != data.getLoadBalanceAlgorithm()) {
            result.setLoadBalanceAlgorithmClassName(data.getLoadBalanceAlgorithm().getClass().getName());
        }
        return result;
    }
    
    @Override
    public MasterSlaveRuleConfiguration swap(final YamlMasterSlaveRuleConfiguration yamlConfiguration) {
        MasterSlaveLoadBalanceAlgorithm loadBalanceAlgorithm = null;
        if (!Strings.isNullOrEmpty(yamlConfiguration.getLoadBalanceAlgorithmClassName())) {
            loadBalanceAlgorithm = newInstance(yamlConfiguration.getLoadBalanceAlgorithmClassName());
        } else if (null != yamlConfiguration.getLoadBalanceAlgorithmType()) {
            loadBalanceAlgorithm = yamlConfiguration.getLoadBalanceAlgorithmType().getAlgorithm();
        }
        return new MasterSlaveRuleConfiguration(yamlConfiguration.getName(), yamlConfiguration.getMasterDataSourceName(), yamlConfiguration.getSlaveDataSourceNames(), loadBalanceAlgorithm);
    }
    
    private MasterSlaveLoadBalanceAlgorithm newInstance(final String masterSlaveLoadBalanceAlgorithmClassName) {
        try {
            Class<?> result = Class.forName(masterSlaveLoadBalanceAlgorithmClassName);
            if (!MasterSlaveLoadBalanceAlgorithm.class.isAssignableFrom(result)) {
                throw new ShardingConfigurationException("Class %s should be implement %s", masterSlaveLoadBalanceAlgorithmClassName, MasterSlaveLoadBalanceAlgorithm.class.getName());
            }
            return (MasterSlaveLoadBalanceAlgorithm) result.newInstance();
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingConfigurationException("Class %s should have public privilege and no argument constructor", masterSlaveLoadBalanceAlgorithmClassName);
        }
    }
}
