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

package org.apache.shardingsphere.readwritesplitting.common.yaml.swapper;

import org.apache.shardingsphere.readwritesplitting.common.algorithm.config.AlgorithmProvidedReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Readwrite-splitting rule configuration YAML swapper.
 */
public final class ReadwriteSplittingRuleAlgorithmProviderConfigurationYamlSwapper
        implements YamlRuleConfigurationSwapper<YamlReadwriteSplittingRuleConfiguration, AlgorithmProvidedReadwriteSplittingRuleConfiguration> {
    
    @Override
    public YamlReadwriteSplittingRuleConfiguration swapToYamlConfiguration(final AlgorithmProvidedReadwriteSplittingRuleConfiguration data) {
        YamlReadwriteSplittingRuleConfiguration result = new YamlReadwriteSplittingRuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(
                Collectors.toMap(ReadwriteSplittingDataSourceRuleConfiguration::getName, this::swapToYamlConfiguration, (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        if (null != data.getLoadBalanceAlgorithms()) {
            data.getLoadBalanceAlgorithms().forEach((key, value) -> result.getLoadBalancers().put(key, new YamlShardingSphereAlgorithmConfiguration(value.getType(), value.getProps())));
        }
        return result;
    }
    
    private YamlReadwriteSplittingDataSourceRuleConfiguration swapToYamlConfiguration(final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig) {
        YamlReadwriteSplittingDataSourceRuleConfiguration result = new YamlReadwriteSplittingDataSourceRuleConfiguration();
        result.setAutoAwareDataSourceName(dataSourceRuleConfig.getAutoAwareDataSourceName());
        result.setWriteDataSourceName(dataSourceRuleConfig.getWriteDataSourceName());
        result.setReadDataSourceNames(dataSourceRuleConfig.getReadDataSourceNames());
        result.setLoadBalancerName(dataSourceRuleConfig.getLoadBalancerName());
        return result;
    }
    
    @Override
    public AlgorithmProvidedReadwriteSplittingRuleConfiguration swapToObject(final YamlReadwriteSplittingRuleConfiguration yamlConfig) {
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        for (Entry<String, YamlReadwriteSplittingDataSourceRuleConfiguration> entry : yamlConfig.getDataSources().entrySet()) {
            dataSources.add(swapToObject(entry.getKey(), entry.getValue()));
        }
        AlgorithmProvidedReadwriteSplittingRuleConfiguration ruleConfig = new AlgorithmProvidedReadwriteSplittingRuleConfiguration();
        ruleConfig.setDataSources(dataSources);
        return ruleConfig;
    }
    
    private ReadwriteSplittingDataSourceRuleConfiguration swapToObject(final String name, final YamlReadwriteSplittingDataSourceRuleConfiguration yamlDataSourceRuleConfig) {
        return new ReadwriteSplittingDataSourceRuleConfiguration(name, yamlDataSourceRuleConfig.getAutoAwareDataSourceName(),
                yamlDataSourceRuleConfig.getWriteDataSourceName(), yamlDataSourceRuleConfig.getReadDataSourceNames(), yamlDataSourceRuleConfig.getLoadBalancerName());
    }
    
    @Override
    public Class<AlgorithmProvidedReadwriteSplittingRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedReadwriteSplittingRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "READWRITE_SPLITTING";
    }
    
    @Override
    public int getOrder() {
        return ReadwriteSplittingOrder.ALGORITHM_PROVIDER_ORDER;
    }
}
