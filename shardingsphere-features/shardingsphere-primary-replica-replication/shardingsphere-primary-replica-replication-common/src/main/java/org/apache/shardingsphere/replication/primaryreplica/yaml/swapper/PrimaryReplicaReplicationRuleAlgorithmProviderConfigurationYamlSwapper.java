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

package org.apache.shardingsphere.replication.primaryreplica.yaml.swapper;

import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.replication.primaryreplica.algorithm.config.AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.api.config.rule.PrimaryReplicaReplicationDataSourceRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.constant.PrimaryReplicaReplicationOrder;
import org.apache.shardingsphere.replication.primaryreplica.yaml.config.YamlPrimaryReplicaReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.yaml.config.rule.YamlPrimaryReplicaReplicationDataSourceRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Primary-replica replication rule configuration YAML swapper.
 */
public final class PrimaryReplicaReplicationRuleAlgorithmProviderConfigurationYamlSwapper
        implements YamlRuleConfigurationSwapper<YamlPrimaryReplicaReplicationRuleConfiguration, AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration> {
    
    @Override
    public YamlPrimaryReplicaReplicationRuleConfiguration swapToYamlConfiguration(final AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration data) {
        YamlPrimaryReplicaReplicationRuleConfiguration result = new YamlPrimaryReplicaReplicationRuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(
                Collectors.toMap(PrimaryReplicaReplicationDataSourceRuleConfiguration::getName, this::swapToYamlConfiguration, (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        if (null != data.getLoadBalanceAlgorithms()) {
            data.getLoadBalanceAlgorithms().forEach((key, value) -> result.getLoadBalancers().put(key, YamlShardingSphereAlgorithmConfiguration.buildByTypedSPI(value)));
        }
        return result;
    }
    
    private YamlPrimaryReplicaReplicationDataSourceRuleConfiguration swapToYamlConfiguration(final PrimaryReplicaReplicationDataSourceRuleConfiguration dataSourceRuleConfig) {
        YamlPrimaryReplicaReplicationDataSourceRuleConfiguration result = new YamlPrimaryReplicaReplicationDataSourceRuleConfiguration();
        result.setName(dataSourceRuleConfig.getName());
        result.setPrimaryDataSourceName(dataSourceRuleConfig.getPrimaryDataSourceName());
        result.setReplicaDataSourceNames(dataSourceRuleConfig.getReplicaDataSourceNames());
        result.setLoadBalancerName(dataSourceRuleConfig.getLoadBalancerName());
        return result;
    }
    
    @Override
    public AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration swapToObject(final YamlPrimaryReplicaReplicationRuleConfiguration yamlConfig) {
        Collection<PrimaryReplicaReplicationDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        for (Entry<String, YamlPrimaryReplicaReplicationDataSourceRuleConfiguration> entry : yamlConfig.getDataSources().entrySet()) {
            dataSources.add(swapToObject(entry.getKey(), entry.getValue()));
        }
        AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration ruleConfig = new AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration();
        ruleConfig.setDataSources(dataSources);
        return ruleConfig;
    }
    
    private PrimaryReplicaReplicationDataSourceRuleConfiguration swapToObject(final String name, final YamlPrimaryReplicaReplicationDataSourceRuleConfiguration yamlDataSourceRuleConfig) {
        return new PrimaryReplicaReplicationDataSourceRuleConfiguration(name, 
                yamlDataSourceRuleConfig.getPrimaryDataSourceName(), yamlDataSourceRuleConfig.getReplicaDataSourceNames(), yamlDataSourceRuleConfig.getLoadBalancerName());
    }
    
    @Override
    public Class<AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "PRIMARY_REPLICA_REPLICATION";
    }
    
    @Override
    public int getOrder() {
        return PrimaryReplicaReplicationOrder.ALGORITHM_PROVIDER_ORDER;
    }
}
