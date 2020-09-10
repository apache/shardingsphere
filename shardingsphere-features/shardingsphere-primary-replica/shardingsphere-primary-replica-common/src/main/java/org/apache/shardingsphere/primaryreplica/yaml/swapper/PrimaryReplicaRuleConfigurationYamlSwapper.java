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

package org.apache.shardingsphere.primaryreplica.yaml.swapper;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.swapper.algorithm.ShardingSphereAlgorithmConfigurationYamlSwapper;
import org.apache.shardingsphere.primaryreplica.api.config.PrimaryReplicaRuleConfiguration;
import org.apache.shardingsphere.primaryreplica.api.config.rule.PrimaryReplicaDataSourceRuleConfiguration;
import org.apache.shardingsphere.primaryreplica.constant.PrimaryReplicaOrder;
import org.apache.shardingsphere.primaryreplica.yaml.config.YamlPrimaryReplicaRuleConfiguration;
import org.apache.shardingsphere.primaryreplica.yaml.config.rule.YamlPrimaryReplicaDataSourceRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Primary-replica rule configuration YAML swapper.
 */
public final class PrimaryReplicaRuleConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlPrimaryReplicaRuleConfiguration, PrimaryReplicaRuleConfiguration> {
    
    private final ShardingSphereAlgorithmConfigurationYamlSwapper algorithmSwapper = new ShardingSphereAlgorithmConfigurationYamlSwapper();
    
    @Override
    public YamlPrimaryReplicaRuleConfiguration swapToYamlConfiguration(final PrimaryReplicaRuleConfiguration data) {
        YamlPrimaryReplicaRuleConfiguration result = new YamlPrimaryReplicaRuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(
                Collectors.toMap(PrimaryReplicaDataSourceRuleConfiguration::getName, this::swapToYamlConfiguration, (a, b) -> b, LinkedHashMap::new)));
        if (null != data.getLoadBalancers()) {
            data.getLoadBalancers().forEach((key, value) -> result.getLoadBalancers().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
        }
        return result;
    }
    
    private YamlPrimaryReplicaDataSourceRuleConfiguration swapToYamlConfiguration(final PrimaryReplicaDataSourceRuleConfiguration dataSourceRuleConfiguration) {
        YamlPrimaryReplicaDataSourceRuleConfiguration result = new YamlPrimaryReplicaDataSourceRuleConfiguration();
        result.setName(dataSourceRuleConfiguration.getName());
        result.setPrimaryDataSourceName(dataSourceRuleConfiguration.getPrimaryDataSourceName());
        result.setReplicaDataSourceNames(dataSourceRuleConfiguration.getReplicaDataSourceNames());
        result.setLoadBalancerName(dataSourceRuleConfiguration.getLoadBalancerName());
        return result;
    }
    
    @Override
    public PrimaryReplicaRuleConfiguration swapToObject(final YamlPrimaryReplicaRuleConfiguration yamlConfig) {
        Collection<PrimaryReplicaDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        for (Entry<String, YamlPrimaryReplicaDataSourceRuleConfiguration> entry : yamlConfig.getDataSources().entrySet()) {
            dataSources.add(swapToObject(entry.getKey(), entry.getValue()));
        }
        Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers = new LinkedHashMap<>(yamlConfig.getLoadBalancers().entrySet().size(), 1);
        if (null != yamlConfig.getLoadBalancers()) {
            yamlConfig.getLoadBalancers().forEach((key, value) -> loadBalancers.put(key, algorithmSwapper.swapToObject(value)));
        }
        return new PrimaryReplicaRuleConfiguration(dataSources, loadBalancers);
    }
    
    private PrimaryReplicaDataSourceRuleConfiguration swapToObject(final String name, final YamlPrimaryReplicaDataSourceRuleConfiguration yamlDataSourceRuleConfiguration) {
        return new PrimaryReplicaDataSourceRuleConfiguration(name, 
                yamlDataSourceRuleConfiguration.getPrimaryDataSourceName(), yamlDataSourceRuleConfiguration.getReplicaDataSourceNames(), yamlDataSourceRuleConfiguration.getLoadBalancerName());
    }
    
    @Override
    public Class<PrimaryReplicaRuleConfiguration> getTypeClass() {
        return PrimaryReplicaRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "PRIMARY_REPLICA";
    }
    
    @Override
    public int getOrder() {
        return PrimaryReplicaOrder.ORDER;
    }
}
