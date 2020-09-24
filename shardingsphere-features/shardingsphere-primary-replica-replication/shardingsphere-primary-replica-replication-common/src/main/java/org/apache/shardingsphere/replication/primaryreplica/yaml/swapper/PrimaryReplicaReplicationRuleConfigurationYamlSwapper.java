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

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.swapper.algorithm.ShardingSphereAlgorithmConfigurationYamlSwapper;
import org.apache.shardingsphere.replication.primaryreplica.api.config.PrimaryReplicaReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.api.config.rule.PrimaryReplicaReplicationDataSourceRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.constant.PrimaryReplicaReplicationOrder;
import org.apache.shardingsphere.replication.primaryreplica.yaml.config.YamlPrimaryReplicaReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.yaml.config.rule.YamlPrimaryReplicaReplicationDataSourceRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Primary-replica replication rule configuration YAML swapper.
 */
public final class PrimaryReplicaReplicationRuleConfigurationYamlSwapper 
        implements YamlRuleConfigurationSwapper<YamlPrimaryReplicaReplicationRuleConfiguration, PrimaryReplicaReplicationRuleConfiguration> {
    
    private final ShardingSphereAlgorithmConfigurationYamlSwapper algorithmSwapper = new ShardingSphereAlgorithmConfigurationYamlSwapper();
    
    @Override
    public YamlPrimaryReplicaReplicationRuleConfiguration swapToYamlConfiguration(final PrimaryReplicaReplicationRuleConfiguration data) {
        YamlPrimaryReplicaReplicationRuleConfiguration result = new YamlPrimaryReplicaReplicationRuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(
                Collectors.toMap(PrimaryReplicaReplicationDataSourceRuleConfiguration::getName, this::swapToYamlConfiguration, (a, b) -> b, LinkedHashMap::new)));
        if (null != data.getLoadBalancers()) {
            data.getLoadBalancers().forEach((key, value) -> result.getLoadBalancers().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
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
    public PrimaryReplicaReplicationRuleConfiguration swapToObject(final YamlPrimaryReplicaReplicationRuleConfiguration yamlConfig) {
        Collection<PrimaryReplicaReplicationDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        for (Entry<String, YamlPrimaryReplicaReplicationDataSourceRuleConfiguration> entry : yamlConfig.getDataSources().entrySet()) {
            dataSources.add(swapToObject(entry.getKey(), entry.getValue()));
        }
        Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers = new LinkedHashMap<>(yamlConfig.getLoadBalancers().entrySet().size(), 1);
        if (null != yamlConfig.getLoadBalancers()) {
            yamlConfig.getLoadBalancers().forEach((key, value) -> loadBalancers.put(key, algorithmSwapper.swapToObject(value)));
        }
        return new PrimaryReplicaReplicationRuleConfiguration(dataSources, loadBalancers);
    }
    
    private PrimaryReplicaReplicationDataSourceRuleConfiguration swapToObject(final String name, final YamlPrimaryReplicaReplicationDataSourceRuleConfiguration yamlDataSourceRuleConfig) {
        return new PrimaryReplicaReplicationDataSourceRuleConfiguration(name, 
                yamlDataSourceRuleConfig.getPrimaryDataSourceName(), yamlDataSourceRuleConfig.getReplicaDataSourceNames(), yamlDataSourceRuleConfig.getLoadBalancerName());
    }
    
    @Override
    public Class<PrimaryReplicaReplicationRuleConfiguration> getTypeClass() {
        return PrimaryReplicaReplicationRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "PRIMARY_REPLICA_REPLICATION";
    }
    
    @Override
    public int getOrder() {
        return PrimaryReplicaReplicationOrder.ORDER;
    }
}
