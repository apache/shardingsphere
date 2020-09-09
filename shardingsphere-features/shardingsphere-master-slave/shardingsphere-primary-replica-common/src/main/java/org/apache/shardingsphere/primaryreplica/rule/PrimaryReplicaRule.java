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

package org.apache.shardingsphere.primaryreplica.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.rule.DataSourceRoutedRule;
import org.apache.shardingsphere.infra.rule.StatusContainedRule;
import org.apache.shardingsphere.infra.rule.event.RuleChangedEvent;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.primaryreplica.api.config.PrimaryReplicaRuleConfiguration;
import org.apache.shardingsphere.primaryreplica.api.config.rule.PrimaryReplicaDataSourceRuleConfiguration;
import org.apache.shardingsphere.primaryreplica.spi.PrimaryReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.primaryreplica.algorithm.config.AlgorithmProvidedPrimaryReplicaRuleConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Primary-replica rule.
 */
public final class PrimaryReplicaRule implements DataSourceRoutedRule, StatusContainedRule {
    
    static {
        ShardingSphereServiceLoader.register(PrimaryReplicaLoadBalanceAlgorithm.class);
    }
    
    private final Map<String, PrimaryReplicaLoadBalanceAlgorithm> loadBalancers = new LinkedHashMap<>();
    
    private final Map<String, PrimaryReplicaDataSourceRule> dataSourceRules;
    
    public PrimaryReplicaRule(final PrimaryReplicaRuleConfiguration configuration) {
        Preconditions.checkArgument(!configuration.getDataSources().isEmpty(), "Primary-replica data source rules can not be empty.");
        configuration.getLoadBalancers().forEach((key, value) -> loadBalancers.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, PrimaryReplicaLoadBalanceAlgorithm.class)));
        dataSourceRules = new HashMap<>(configuration.getDataSources().size(), 1);
        for (PrimaryReplicaDataSourceRuleConfiguration each : configuration.getDataSources()) {
            // TODO check if can not find load balancer should throw exception.
            PrimaryReplicaLoadBalanceAlgorithm loadBalanceAlgorithm = Strings.isNullOrEmpty(each.getLoadBalancerName()) || !loadBalancers.containsKey(each.getLoadBalancerName())
                    ? TypedSPIRegistry.getRegisteredService(PrimaryReplicaLoadBalanceAlgorithm.class) : loadBalancers.get(each.getLoadBalancerName());
            dataSourceRules.put(each.getName(), new PrimaryReplicaDataSourceRule(each, loadBalanceAlgorithm));
        }
    }
    
    public PrimaryReplicaRule(final AlgorithmProvidedPrimaryReplicaRuleConfiguration configuration) {
        Preconditions.checkArgument(!configuration.getDataSources().isEmpty(), "Primary-replica data source rules can not be empty.");
        loadBalancers.putAll(configuration.getLoadBalanceAlgorithms());
        dataSourceRules = new HashMap<>(configuration.getDataSources().size(), 1);
        for (PrimaryReplicaDataSourceRuleConfiguration each : configuration.getDataSources()) {
            // TODO check if can not find load balancer should throw exception.
            PrimaryReplicaLoadBalanceAlgorithm loadBalanceAlgorithm = Strings.isNullOrEmpty(each.getLoadBalancerName()) || !loadBalancers.containsKey(each.getLoadBalancerName())
                    ? TypedSPIRegistry.getRegisteredService(PrimaryReplicaLoadBalanceAlgorithm.class) : loadBalancers.get(each.getLoadBalancerName());
            dataSourceRules.put(each.getName(), new PrimaryReplicaDataSourceRule(each, loadBalanceAlgorithm));
        }
    }
    
    /**
     * Get single data source rule.
     *
     * @return primary-replica data source rule
     */
    public PrimaryReplicaDataSourceRule getSingleDataSourceRule() {
        return dataSourceRules.values().iterator().next();
    }
    
    /**
     * Find data source rule.
     * 
     * @param dataSourceName data source name
     * @return primary-replica data source rule
     */
    public Optional<PrimaryReplicaDataSourceRule> findDataSourceRule(final String dataSourceName) {
        return Optional.ofNullable(dataSourceRules.get(dataSourceName));
    }
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>();
        for (Entry<String, PrimaryReplicaDataSourceRule> entry : dataSourceRules.entrySet()) {
            result.putAll(entry.getValue().getDataSourceMapper());
        }
        return result;
    }
    
    @Override
    public void updateRuleStatus(final RuleChangedEvent event) {
        if (event instanceof DataSourceNameDisabledEvent) {
            for (Entry<String, PrimaryReplicaDataSourceRule> entry : dataSourceRules.entrySet()) {
                entry.getValue().updateDisabledDataSourceNames(((DataSourceNameDisabledEvent) event).getDataSourceName(), ((DataSourceNameDisabledEvent) event).isDisabled());
            }
        }
    }
}
