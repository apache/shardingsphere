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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.masterslave.algorithm.config.AlgorithmProvidedMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.apache.shardingsphere.masterslave.constant.MasterSlaveOrder;
import org.apache.shardingsphere.masterslave.yaml.config.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.yaml.config.rule.YamlMasterSlaveDataSourceRuleConfiguration;

/**
 * Master-slave rule configuration YAML swapper.
 */
public final class MasterSlaveRuleAlgorithmProviderConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlMasterSlaveRuleConfiguration, AlgorithmProvidedMasterSlaveRuleConfiguration> {
    
    @Override
    public YamlMasterSlaveRuleConfiguration swapToYamlConfiguration(final AlgorithmProvidedMasterSlaveRuleConfiguration data) {
        YamlMasterSlaveRuleConfiguration result = new YamlMasterSlaveRuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(
                Collectors.toMap(MasterSlaveDataSourceRuleConfiguration::getName, this::swapToYamlConfiguration, (a, b) -> b, LinkedHashMap::new)));
        if (null != data.getLoadBalanceAlgorithms()) {
            data.getLoadBalanceAlgorithms().forEach((key, value) -> result.getLoadBalancers().put(key, YamlShardingSphereAlgorithmConfiguration.buildByTypedSPI(value)));
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
    public AlgorithmProvidedMasterSlaveRuleConfiguration swapToObject(final YamlMasterSlaveRuleConfiguration yamlConfig) {
        Collection<MasterSlaveDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        for (Entry<String, YamlMasterSlaveDataSourceRuleConfiguration> entry : yamlConfig.getDataSources().entrySet()) {
            dataSources.add(swapToObject(entry.getKey(), entry.getValue()));
        }
        AlgorithmProvidedMasterSlaveRuleConfiguration ruleConfiguration = new AlgorithmProvidedMasterSlaveRuleConfiguration();
        ruleConfiguration.setDataSources(dataSources);
        return ruleConfiguration;
    }
    
    private MasterSlaveDataSourceRuleConfiguration swapToObject(final String name, final YamlMasterSlaveDataSourceRuleConfiguration yamlDataSourceRuleConfiguration) {
        return new MasterSlaveDataSourceRuleConfiguration(name, 
                yamlDataSourceRuleConfiguration.getMasterDataSourceName(), yamlDataSourceRuleConfiguration.getSlaveDataSourceNames(), yamlDataSourceRuleConfiguration.getLoadBalancerName());
    }
    
    @Override
    public Class<AlgorithmProvidedMasterSlaveRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedMasterSlaveRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "MASTER_SLAVE";
    }
    
    @Override
    public int getOrder() {
        return MasterSlaveOrder.ALGORITHM_PROVIDER_MASTER_SLAVE_ORDER;
    }
}
