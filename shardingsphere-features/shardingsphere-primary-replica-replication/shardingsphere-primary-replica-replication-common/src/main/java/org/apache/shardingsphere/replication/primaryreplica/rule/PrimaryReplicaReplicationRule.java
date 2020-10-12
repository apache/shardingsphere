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

package org.apache.shardingsphere.replication.primaryreplica.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.rule.DataSourceRoutedRule;
import org.apache.shardingsphere.infra.rule.StatusContainedRule;
import org.apache.shardingsphere.infra.rule.event.RuleChangedEvent;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.replication.primaryreplica.api.config.PrimaryReplicaReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.api.config.rule.PrimaryReplicaReplicationDataSourceRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.spi.ReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.replication.primaryreplica.algorithm.config.AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Primary-replica replication rule.
 */
public final class PrimaryReplicaReplicationRule implements DataSourceRoutedRule, StatusContainedRule {
    
    static {
        ShardingSphereServiceLoader.register(ReplicaLoadBalanceAlgorithm.class);
    }
    
    private final Map<String, ReplicaLoadBalanceAlgorithm> loadBalancers = new LinkedHashMap<>();
    
    private final Map<String, PrimaryReplicaReplicationDataSourceRule> dataSourceRules;
    
    public PrimaryReplicaReplicationRule(final PrimaryReplicaReplicationRuleConfiguration config) {
        Preconditions.checkArgument(!config.getDataSources().isEmpty(), "Primary-replica replication data source rules can not be empty.");
        config.getLoadBalancers().forEach((key, value) -> loadBalancers.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, ReplicaLoadBalanceAlgorithm.class)));
        dataSourceRules = new HashMap<>(config.getDataSources().size(), 1);
        for (PrimaryReplicaReplicationDataSourceRuleConfiguration each : config.getDataSources()) {
            // TODO check if can not find load balancer should throw exception.
            ReplicaLoadBalanceAlgorithm loadBalanceAlgorithm = Strings.isNullOrEmpty(each.getLoadBalancerName()) || !loadBalancers.containsKey(each.getLoadBalancerName())
                    ? TypedSPIRegistry.getRegisteredService(ReplicaLoadBalanceAlgorithm.class) : loadBalancers.get(each.getLoadBalancerName());
            dataSourceRules.put(each.getName(), new PrimaryReplicaReplicationDataSourceRule(each, loadBalanceAlgorithm));
        }
    }
    
    public PrimaryReplicaReplicationRule(final AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration config) {
        Preconditions.checkArgument(!config.getDataSources().isEmpty(), "Primary-replica replication data source rules can not be empty.");
        loadBalancers.putAll(config.getLoadBalanceAlgorithms());
        dataSourceRules = new HashMap<>(config.getDataSources().size(), 1);
        for (PrimaryReplicaReplicationDataSourceRuleConfiguration each : config.getDataSources()) {
            // TODO check if can not find load balancer should throw exception.
            ReplicaLoadBalanceAlgorithm loadBalanceAlgorithm = Strings.isNullOrEmpty(each.getLoadBalancerName()) || !loadBalancers.containsKey(each.getLoadBalancerName())
                    ? TypedSPIRegistry.getRegisteredService(ReplicaLoadBalanceAlgorithm.class) : loadBalancers.get(each.getLoadBalancerName());
            dataSourceRules.put(each.getName(), new PrimaryReplicaReplicationDataSourceRule(each, loadBalanceAlgorithm));
        }
    }
    
    /**
     * Get single data source rule.
     *
     * @return Primary-replica replication data source rule
     */
    public PrimaryReplicaReplicationDataSourceRule getSingleDataSourceRule() {
        return dataSourceRules.values().iterator().next();
    }
    
    /**
     * Find data source rule.
     * 
     * @param dataSourceName data source name
     * @return Primary-replica replication data source rule
     */
    public Optional<PrimaryReplicaReplicationDataSourceRule> findDataSourceRule(final String dataSourceName) {
        return Optional.ofNullable(dataSourceRules.get(dataSourceName));
    }
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>();
        for (Entry<String, PrimaryReplicaReplicationDataSourceRule> entry : dataSourceRules.entrySet()) {
            result.putAll(entry.getValue().getDataSourceMapper());
        }
        return result;
    }
    
    @Override
    public void updateRuleStatus(final RuleChangedEvent event) {
        if (event instanceof DataSourceNameDisabledEvent) {
            for (Entry<String, PrimaryReplicaReplicationDataSourceRule> entry : dataSourceRules.entrySet()) {
                entry.getValue().updateDisabledDataSourceNames(((DataSourceNameDisabledEvent) event).getDataSourceName(), ((DataSourceNameDisabledEvent) event).isDisabled());
            }
        }
    }
}
