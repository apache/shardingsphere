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

package org.apache.shardingsphere.ha.yaml.swapper;

import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.ha.algorithm.config.AlgorithmProvidedHARuleConfiguration;
import org.apache.shardingsphere.ha.api.config.rule.HADataSourceRuleConfiguration;
import org.apache.shardingsphere.ha.constant.HAOrder;
import org.apache.shardingsphere.ha.yaml.config.YamlHARuleConfiguration;
import org.apache.shardingsphere.ha.yaml.config.rule.YamlHADataSourceRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * HA rule configuration YAML swapper.
 */
public final class HARuleAlgorithmProviderConfigurationYamlSwapper
        implements YamlRuleConfigurationSwapper<YamlHARuleConfiguration, AlgorithmProvidedHARuleConfiguration> {
    
    @Override
    public YamlHARuleConfiguration swapToYamlConfiguration(final AlgorithmProvidedHARuleConfiguration data) {
        YamlHARuleConfiguration result = new YamlHARuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(
                Collectors.toMap(HADataSourceRuleConfiguration::getName, this::swapToYamlConfiguration, (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        if (null != data.getLoadBalanceAlgorithms()) {
            data.getLoadBalanceAlgorithms().forEach((key, value) -> result.getLoadBalancers().put(key, YamlShardingSphereAlgorithmConfiguration.buildByTypedSPI(value)));
        }
        return result;
    }
    
    private YamlHADataSourceRuleConfiguration swapToYamlConfiguration(final HADataSourceRuleConfiguration dataSourceRuleConfig) {
        YamlHADataSourceRuleConfiguration result = new YamlHADataSourceRuleConfiguration();
        result.setName(dataSourceRuleConfig.getName());
        result.setPrimaryDataSourceName(dataSourceRuleConfig.getPrimaryDataSourceName());
        result.setReplicaDataSourceNames(dataSourceRuleConfig.getReplicaDataSourceNames());
        result.setLoadBalancerName(dataSourceRuleConfig.getLoadBalancerName());
        return result;
    }
    
    @Override
    public AlgorithmProvidedHARuleConfiguration swapToObject(final YamlHARuleConfiguration yamlConfig) {
        Collection<HADataSourceRuleConfiguration> dataSources = new LinkedList<>();
        for (Entry<String, YamlHADataSourceRuleConfiguration> entry : yamlConfig.getDataSources().entrySet()) {
            dataSources.add(swapToObject(entry.getKey(), entry.getValue()));
        }
        AlgorithmProvidedHARuleConfiguration ruleConfig = new AlgorithmProvidedHARuleConfiguration();
        ruleConfig.setDataSources(dataSources);
        return ruleConfig;
    }
    
    private HADataSourceRuleConfiguration swapToObject(final String name, final YamlHADataSourceRuleConfiguration yamlDataSourceRuleConfig) {
        return new HADataSourceRuleConfiguration(name,
                yamlDataSourceRuleConfig.getPrimaryDataSourceName(), yamlDataSourceRuleConfig.getReplicaDataSourceNames(), yamlDataSourceRuleConfig.getLoadBalancerName());
    }
    
    @Override
    public Class<AlgorithmProvidedHARuleConfiguration> getTypeClass() {
        return AlgorithmProvidedHARuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "HA";
    }
    
    @Override
    public int getOrder() {
        return HAOrder.ALGORITHM_PROVIDER_HA_ORDER;
    }
}
