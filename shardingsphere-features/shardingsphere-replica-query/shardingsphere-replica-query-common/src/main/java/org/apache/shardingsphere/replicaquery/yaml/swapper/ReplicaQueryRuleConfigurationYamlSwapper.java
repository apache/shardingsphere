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

package org.apache.shardingsphere.replicaquery.yaml.swapper;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.swapper.algorithm.ShardingSphereAlgorithmConfigurationYamlSwapper;
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;
import org.apache.shardingsphere.replicaquery.constant.ReplicaQueryOrder;
import org.apache.shardingsphere.replicaquery.yaml.config.YamlReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.yaml.config.rule.YamlReplicaQueryDataSourceRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Replica query rule configuration YAML swapper.
 */
public final class ReplicaQueryRuleConfigurationYamlSwapper 
        implements YamlRuleConfigurationSwapper<YamlReplicaQueryRuleConfiguration, ReplicaQueryRuleConfiguration> {
    
    private final ShardingSphereAlgorithmConfigurationYamlSwapper algorithmSwapper = new ShardingSphereAlgorithmConfigurationYamlSwapper();
    
    @Override
    public YamlReplicaQueryRuleConfiguration swapToYamlConfiguration(final ReplicaQueryRuleConfiguration data) {
        YamlReplicaQueryRuleConfiguration result = new YamlReplicaQueryRuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(
                Collectors.toMap(ReplicaQueryDataSourceRuleConfiguration::getName, this::swapToYamlConfiguration, (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        if (null != data.getLoadBalancers()) {
            data.getLoadBalancers().forEach((key, value) -> result.getLoadBalancers().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
        }
        return result;
    }
    
    private YamlReplicaQueryDataSourceRuleConfiguration swapToYamlConfiguration(final ReplicaQueryDataSourceRuleConfiguration dataSourceRuleConfig) {
        YamlReplicaQueryDataSourceRuleConfiguration result = new YamlReplicaQueryDataSourceRuleConfiguration();
        result.setName(dataSourceRuleConfig.getName());
        result.setPrimaryDataSourceName(dataSourceRuleConfig.getPrimaryDataSourceName());
        result.setReplicaDataSourceNames(dataSourceRuleConfig.getReplicaDataSourceNames());
        result.setLoadBalancerName(dataSourceRuleConfig.getLoadBalancerName());
        return result;
    }
    
    @Override
    public ReplicaQueryRuleConfiguration swapToObject(final YamlReplicaQueryRuleConfiguration yamlConfig) {
        Collection<ReplicaQueryDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        for (Entry<String, YamlReplicaQueryDataSourceRuleConfiguration> entry : yamlConfig.getDataSources().entrySet()) {
            dataSources.add(swapToObject(entry.getKey(), entry.getValue()));
        }
        Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers = new LinkedHashMap<>(yamlConfig.getLoadBalancers().entrySet().size(), 1);
        if (null != yamlConfig.getLoadBalancers()) {
            yamlConfig.getLoadBalancers().forEach((key, value) -> loadBalancers.put(key, algorithmSwapper.swapToObject(value)));
        }
        return new ReplicaQueryRuleConfiguration(dataSources, loadBalancers);
    }
    
    private ReplicaQueryDataSourceRuleConfiguration swapToObject(final String name, final YamlReplicaQueryDataSourceRuleConfiguration yamlDataSourceRuleConfig) {
        return new ReplicaQueryDataSourceRuleConfiguration(name, 
                yamlDataSourceRuleConfig.getPrimaryDataSourceName(), yamlDataSourceRuleConfig.getReplicaDataSourceNames(), yamlDataSourceRuleConfig.getLoadBalancerName());
    }
    
    @Override
    public Class<ReplicaQueryRuleConfiguration> getTypeClass() {
        return ReplicaQueryRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "REPLICA_QUERY";
    }
    
    @Override
    public int getOrder() {
        return ReplicaQueryOrder.ORDER;
    }
}
