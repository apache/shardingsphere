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

package org.apache.shardingsphere.masterslave.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.rule.DataSourceRoutedRule;
import org.apache.shardingsphere.infra.rule.StatusContainedRule;
import org.apache.shardingsphere.infra.rule.event.RuleChangedEvent;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.apache.shardingsphere.masterslave.spi.MasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.masterslave.algorithm.config.AlgorithmProvidedMasterSlaveRuleConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Master-slave rule.
 */
public final class MasterSlaveRule implements DataSourceRoutedRule, StatusContainedRule {
    
    static {
        ShardingSphereServiceLoader.register(MasterSlaveLoadBalanceAlgorithm.class);
    }
    
    private final Map<String, MasterSlaveLoadBalanceAlgorithm> loadBalancers = new LinkedHashMap<>();
    
    private final Map<String, MasterSlaveDataSourceRule> dataSourceRules;
    
    public MasterSlaveRule(final MasterSlaveRuleConfiguration configuration) {
        Preconditions.checkArgument(!configuration.getDataSources().isEmpty(), "Master-slave data source rules can not be empty.");
        configuration.getLoadBalancers().forEach((key, value) -> loadBalancers.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, MasterSlaveLoadBalanceAlgorithm.class)));
        dataSourceRules = new HashMap<>(configuration.getDataSources().size(), 1);
        for (MasterSlaveDataSourceRuleConfiguration each : configuration.getDataSources()) {
            // TODO check if can not find load balancer should throw exception.
            MasterSlaveLoadBalanceAlgorithm loadBalanceAlgorithm = Strings.isNullOrEmpty(each.getLoadBalancerName()) || !loadBalancers.containsKey(each.getLoadBalancerName())
                    ? TypedSPIRegistry.getRegisteredService(MasterSlaveLoadBalanceAlgorithm.class) : loadBalancers.get(each.getLoadBalancerName());
            dataSourceRules.put(each.getName(), new MasterSlaveDataSourceRule(each, loadBalanceAlgorithm));
        }
    }
    
    public MasterSlaveRule(final AlgorithmProvidedMasterSlaveRuleConfiguration configuration) {
        Preconditions.checkArgument(!configuration.getDataSources().isEmpty(), "Master-slave data source rules can not be empty.");
        loadBalancers.putAll(configuration.getLoadBalanceAlgorithms());
        dataSourceRules = new HashMap<>(configuration.getDataSources().size(), 1);
        for (MasterSlaveDataSourceRuleConfiguration each : configuration.getDataSources()) {
            // TODO check if can not find load balancer should throw exception.
            MasterSlaveLoadBalanceAlgorithm loadBalanceAlgorithm = Strings.isNullOrEmpty(each.getLoadBalancerName()) || !loadBalancers.containsKey(each.getLoadBalancerName())
                    ? TypedSPIRegistry.getRegisteredService(MasterSlaveLoadBalanceAlgorithm.class) : loadBalancers.get(each.getLoadBalancerName());
            dataSourceRules.put(each.getName(), new MasterSlaveDataSourceRule(each, loadBalanceAlgorithm));
        }
    }
    
    /**
     * Get single data source rule.
     *
     * @return master-slave data source rule
     */
    public MasterSlaveDataSourceRule getSingleDataSourceRule() {
        return dataSourceRules.values().iterator().next();
    }
    
    /**
     * Find data source rule.
     * 
     * @param dataSourceName data source name
     * @return master-slave data source rule
     */
    public Optional<MasterSlaveDataSourceRule> findDataSourceRule(final String dataSourceName) {
        return Optional.ofNullable(dataSourceRules.get(dataSourceName));
    }
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>();
        for (Entry<String, MasterSlaveDataSourceRule> entry : dataSourceRules.entrySet()) {
            result.putAll(entry.getValue().getDataSourceMapper());
        }
        return result;
    }
    
    @Override
    public void updateRuleStatus(final RuleChangedEvent event) {
        if (event instanceof DataSourceNameDisabledEvent) {
            for (Entry<String, MasterSlaveDataSourceRule> entry : dataSourceRules.entrySet()) {
                entry.getValue().updateDisabledDataSourceNames(((DataSourceNameDisabledEvent) event).getDataSourceName(), ((DataSourceNameDisabledEvent) event).isDisabled());
            }
        }
    }
}
