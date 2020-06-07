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

import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.strategy.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.masterslave.api.config.strategy.impl.SPILoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.masterslave.constant.MasterSlaveOrder;
import org.apache.shardingsphere.masterslave.yaml.config.YamlMasterSlaveDataSourceRuleConfiguration;
import org.apache.shardingsphere.masterslave.yaml.config.YamlMasterSlaveLoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.masterslave.yaml.config.YamlMasterSlaveRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Master-slave rule configuration YAML swapper.
 */
public final class MasterSlaveRuleConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlMasterSlaveRuleConfiguration, MasterSlaveRuleConfiguration> {
    
    @Override
    public YamlMasterSlaveRuleConfiguration swap(final MasterSlaveRuleConfiguration data) {
        YamlMasterSlaveRuleConfiguration result = new YamlMasterSlaveRuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(Collectors.toMap(MasterSlaveDataSourceRuleConfiguration::getName, this::swap, (a, b) -> b, LinkedHashMap::new)));
        return result;
    }
    
    private YamlMasterSlaveDataSourceRuleConfiguration swap(final MasterSlaveDataSourceRuleConfiguration dataSourceRuleConfiguration) {
        YamlMasterSlaveDataSourceRuleConfiguration result = new YamlMasterSlaveDataSourceRuleConfiguration();
        result.setName(dataSourceRuleConfiguration.getName());
        result.setMasterDataSourceName(dataSourceRuleConfiguration.getMasterDataSourceName());
        result.setSlaveDataSourceNames(dataSourceRuleConfiguration.getSlaveDataSourceNames());
        result.setLoadBalanceStrategyName(dataSourceRuleConfiguration.getLoadBalanceStrategyName());
        return result;
    }
    
    @Override
    public MasterSlaveRuleConfiguration swap(final YamlMasterSlaveRuleConfiguration yamlConfiguration) {
        Collection<LoadBalanceStrategyConfiguration> loadBalanceStrategies = new LinkedList<>();
        for (Entry<String, YamlMasterSlaveLoadBalanceStrategyConfiguration> entry : yamlConfiguration.getLoadBalanceStrategies().entrySet()) {
            loadBalanceStrategies.add(swap(entry.getKey(), entry.getValue()));
        }
        Collection<MasterSlaveDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        for (Entry<String, YamlMasterSlaveDataSourceRuleConfiguration> entry : yamlConfiguration.getDataSources().entrySet()) {
            dataSources.add(swap(entry.getKey(), entry.getValue()));
        }
        return new MasterSlaveRuleConfiguration(loadBalanceStrategies, dataSources);
    }
    
    private LoadBalanceStrategyConfiguration swap(final String name, final YamlMasterSlaveLoadBalanceStrategyConfiguration yamlLoadBalanceStrategyConfiguration) {
        return new SPILoadBalanceStrategyConfiguration(name, yamlLoadBalanceStrategyConfiguration.getType(), yamlLoadBalanceStrategyConfiguration.getProps());
    }
    
    private MasterSlaveDataSourceRuleConfiguration swap(final String name, final YamlMasterSlaveDataSourceRuleConfiguration yamlDataSourceRuleConfiguration) {
        return new MasterSlaveDataSourceRuleConfiguration(name, 
                yamlDataSourceRuleConfiguration.getMasterDataSourceName(), yamlDataSourceRuleConfiguration.getSlaveDataSourceNames(), yamlDataSourceRuleConfiguration.getLoadBalanceStrategyName());
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
