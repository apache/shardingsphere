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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.primaryreplica.algorithm.config.AlgorithmProvidedPrimaryReplicaRuleConfiguration;
import org.apache.shardingsphere.primaryreplica.api.config.rule.PrimaryReplicaDataSourceRuleConfiguration;
import org.apache.shardingsphere.primaryreplica.constant.PrimaryReplicaOrder;
import org.apache.shardingsphere.primaryreplica.yaml.config.YamlPrimaryReplicaRuleConfiguration;
import org.apache.shardingsphere.primaryreplica.yaml.config.rule.YamlPrimaryReplicaDataSourceRuleConfiguration;

/**
 * Primary-replica rule configuration YAML swapper.
 */
public final class PrimaryReplicaRuleAlgorithmProviderConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlPrimaryReplicaRuleConfiguration, 
        AlgorithmProvidedPrimaryReplicaRuleConfiguration> {
    
    @Override
    public YamlPrimaryReplicaRuleConfiguration swapToYamlConfiguration(final AlgorithmProvidedPrimaryReplicaRuleConfiguration data) {
        YamlPrimaryReplicaRuleConfiguration result = new YamlPrimaryReplicaRuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(
                Collectors.toMap(PrimaryReplicaDataSourceRuleConfiguration::getName, this::swapToYamlConfiguration, (a, b) -> b, LinkedHashMap::new)));
        if (null != data.getLoadBalanceAlgorithms()) {
            data.getLoadBalanceAlgorithms().forEach((key, value) -> result.getLoadBalancers().put(key, YamlShardingSphereAlgorithmConfiguration.buildByTypedSPI(value)));
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
    public AlgorithmProvidedPrimaryReplicaRuleConfiguration swapToObject(final YamlPrimaryReplicaRuleConfiguration yamlConfig) {
        Collection<PrimaryReplicaDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        for (Entry<String, YamlPrimaryReplicaDataSourceRuleConfiguration> entry : yamlConfig.getDataSources().entrySet()) {
            dataSources.add(swapToObject(entry.getKey(), entry.getValue()));
        }
        AlgorithmProvidedPrimaryReplicaRuleConfiguration ruleConfiguration = new AlgorithmProvidedPrimaryReplicaRuleConfiguration();
        ruleConfiguration.setDataSources(dataSources);
        return ruleConfiguration;
    }
    
    private PrimaryReplicaDataSourceRuleConfiguration swapToObject(final String name, final YamlPrimaryReplicaDataSourceRuleConfiguration yamlDataSourceRuleConfiguration) {
        return new PrimaryReplicaDataSourceRuleConfiguration(name, 
                yamlDataSourceRuleConfiguration.getPrimaryDataSourceName(), yamlDataSourceRuleConfiguration.getReplicaDataSourceNames(), yamlDataSourceRuleConfiguration.getLoadBalancerName());
    }
    
    @Override
    public Class<AlgorithmProvidedPrimaryReplicaRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedPrimaryReplicaRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "PRIMARY_REPLICA";
    }
    
    @Override
    public int getOrder() {
        return PrimaryReplicaOrder.ALGORITHM_PROVIDER_PRIMARY_REPLICA_ORDER;
    }
}
