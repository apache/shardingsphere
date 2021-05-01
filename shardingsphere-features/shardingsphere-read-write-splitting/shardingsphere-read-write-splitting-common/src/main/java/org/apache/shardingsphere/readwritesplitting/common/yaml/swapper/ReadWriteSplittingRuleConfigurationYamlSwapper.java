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

import org.apache.shardingsphere.readwritesplitting.common.constant.ReadWriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.YamlReadWriteSplittingRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.swapper.algorithm.ShardingSphereAlgorithmConfigurationYamlSwapper;
import org.apache.shardingsphere.readwritesplitting.api.ReadWriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadWriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.common.yaml.config.rule.YamlReadWriteSplittingDataSourceRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Read write splitting rule configuration YAML swapper.
 */
public final class ReadWriteSplittingRuleConfigurationYamlSwapper 
        implements YamlRuleConfigurationSwapper<YamlReadWriteSplittingRuleConfiguration, ReadWriteSplittingRuleConfiguration> {
    
    private final ShardingSphereAlgorithmConfigurationYamlSwapper algorithmSwapper = new ShardingSphereAlgorithmConfigurationYamlSwapper();
    
    @Override
    public YamlReadWriteSplittingRuleConfiguration swapToYamlConfiguration(final ReadWriteSplittingRuleConfiguration data) {
        YamlReadWriteSplittingRuleConfiguration result = new YamlReadWriteSplittingRuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(
                Collectors.toMap(ReadWriteSplittingDataSourceRuleConfiguration::getName, this::swapToYamlConfiguration, (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        if (null != data.getLoadBalancers()) {
            data.getLoadBalancers().forEach((key, value) -> result.getLoadBalancers().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
        }
        return result;
    }
    
    private YamlReadWriteSplittingDataSourceRuleConfiguration swapToYamlConfiguration(final ReadWriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig) {
        YamlReadWriteSplittingDataSourceRuleConfiguration result = new YamlReadWriteSplittingDataSourceRuleConfiguration();
        result.setName(dataSourceRuleConfig.getName());
        result.setAutoAwareDataSourceName(dataSourceRuleConfig.getAutoAwareDataSourceName());
        result.setWriteDataSourceName(dataSourceRuleConfig.getWriteDataSourceName());
        result.setReadDataSourceNames(dataSourceRuleConfig.getReadDataSourceNames());
        result.setLoadBalancerName(dataSourceRuleConfig.getLoadBalancerName());
        return result;
    }
    
    @Override
    public ReadWriteSplittingRuleConfiguration swapToObject(final YamlReadWriteSplittingRuleConfiguration yamlConfig) {
        Collection<ReadWriteSplittingDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        for (Entry<String, YamlReadWriteSplittingDataSourceRuleConfiguration> entry : yamlConfig.getDataSources().entrySet()) {
            dataSources.add(swapToObject(entry.getKey(), entry.getValue()));
        }
        Map<String, ShardingSphereAlgorithmConfiguration> loadBalancerMap = new LinkedHashMap<>(yamlConfig.getLoadBalancers().entrySet().size(), 1);
        if (null != yamlConfig.getLoadBalancers()) {
            yamlConfig.getLoadBalancers().forEach((key, value) -> loadBalancerMap.put(key, algorithmSwapper.swapToObject(value)));
        }
        return new ReadWriteSplittingRuleConfiguration(dataSources, loadBalancerMap);
    }
    
    private ReadWriteSplittingDataSourceRuleConfiguration swapToObject(final String name, final YamlReadWriteSplittingDataSourceRuleConfiguration yamlDataSourceRuleConfig) {
        return new ReadWriteSplittingDataSourceRuleConfiguration(name, yamlDataSourceRuleConfig.getAutoAwareDataSourceName(),
                yamlDataSourceRuleConfig.getWriteDataSourceName(), yamlDataSourceRuleConfig.getReadDataSourceNames(), yamlDataSourceRuleConfig.getLoadBalancerName());
    }
    
    @Override
    public Class<ReadWriteSplittingRuleConfiguration> getTypeClass() {
        return ReadWriteSplittingRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "READ_WRITE_SPLITTING";
    }
    
    @Override
    public int getOrder() {
        return ReadWriteSplittingOrder.ORDER;
    }
}
