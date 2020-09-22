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

package org.apache.shardingsphere.masterslave.yaml.swapper;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.swapper.algorithm.ShardingSphereAlgorithmConfigurationYamlSwapper;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.apache.shardingsphere.masterslave.constant.MasterSlaveOrder;
import org.apache.shardingsphere.masterslave.yaml.config.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.yaml.config.rule.YamlMasterSlaveDataSourceRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Master-slave rule configuration YAML swapper.
 */
public final class MasterSlaveRuleConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlMasterSlaveRuleConfiguration, MasterSlaveRuleConfiguration> {
    
    private final ShardingSphereAlgorithmConfigurationYamlSwapper algorithmSwapper = new ShardingSphereAlgorithmConfigurationYamlSwapper();
    
    @Override
    public YamlMasterSlaveRuleConfiguration swapToYamlConfiguration(final MasterSlaveRuleConfiguration data) {
        YamlMasterSlaveRuleConfiguration result = new YamlMasterSlaveRuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(
                Collectors.toMap(MasterSlaveDataSourceRuleConfiguration::getName, this::swapToYamlConfiguration, (a, b) -> b, LinkedHashMap::new)));
        if (null != data.getLoadBalancers()) {
            data.getLoadBalancers().forEach((key, value) -> result.getLoadBalancers().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
        }
        return result;
    }
    
    private YamlMasterSlaveDataSourceRuleConfiguration swapToYamlConfiguration(final MasterSlaveDataSourceRuleConfiguration dataSourceRuleConfiguration) {
        YamlMasterSlaveDataSourceRuleConfiguration result = new YamlMasterSlaveDataSourceRuleConfiguration();
        result.setName(dataSourceRuleConfiguration.getName());
        result.setMasterDataSourceName(dataSourceRuleConfiguration.getMasterDataSourceName());
        result.setSlaveDataSourceNames(dataSourceRuleConfiguration.getSlaveDataSourceNames());
        result.setLoadBalancerName(dataSourceRuleConfiguration.getLoadBalancerName());
        return result;
    }
    
    @Override
    public MasterSlaveRuleConfiguration swapToObject(final YamlMasterSlaveRuleConfiguration yamlConfig) {
        Collection<MasterSlaveDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        for (Entry<String, YamlMasterSlaveDataSourceRuleConfiguration> entry : yamlConfig.getDataSources().entrySet()) {
            dataSources.add(swapToObject(entry.getKey(), entry.getValue()));
        }
        Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers = new LinkedHashMap<>(yamlConfig.getLoadBalancers().entrySet().size(), 1);
        if (null != yamlConfig.getLoadBalancers()) {
            yamlConfig.getLoadBalancers().forEach((key, value) -> loadBalancers.put(key, algorithmSwapper.swapToObject(value)));
        }
        return new MasterSlaveRuleConfiguration(dataSources, loadBalancers);
    }
    
    private MasterSlaveDataSourceRuleConfiguration swapToObject(final String name, final YamlMasterSlaveDataSourceRuleConfiguration yamlDataSourceRuleConfiguration) {
        return new MasterSlaveDataSourceRuleConfiguration(name, 
                yamlDataSourceRuleConfiguration.getMasterDataSourceName(), yamlDataSourceRuleConfiguration.getSlaveDataSourceNames(), yamlDataSourceRuleConfiguration.getLoadBalancerName());
    }
    
    @Override
    public Class<MasterSlaveRuleConfiguration> getTypeClass() {
        return MasterSlaveRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "MASTER_SLAVE";
    }
    
    @Override
    public int getOrder() {
        return MasterSlaveOrder.ORDER;
    }
}
