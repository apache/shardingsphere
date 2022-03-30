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

package org.apache.shardingsphere.readwritesplitting.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.rule.event.DataSourceStatusChangedEvent;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.rule.identifier.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StatusContainedRule;
import org.apache.shardingsphere.readwritesplitting.algorithm.DynamicReadwriteSplittingType;
import org.apache.shardingsphere.readwritesplitting.algorithm.config.AlgorithmProvidedReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.spi.ReadwriteSplittingType;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.required.RequiredSPIRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Readwrite-splitting rule.
 */
public final class ReadwriteSplittingRule implements SchemaRule, DataSourceContainedRule, StatusContainedRule, ExportableRule {
    
    static {
        ShardingSphereServiceLoader.register(ReplicaLoadBalanceAlgorithm.class);
        ShardingSphereServiceLoader.register(ReadwriteSplittingType.class);
    }
    
    private static final String DYNAMIC = "DYNAMIC";
    
    private static final String STATIC = "STATIC";
    
    private final Map<String, ReplicaLoadBalanceAlgorithm> loadBalancers = new LinkedHashMap<>();
    
    private final Map<String, ReadwriteSplittingDataSourceRule> dataSourceRules;
    
    public ReadwriteSplittingRule(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        Preconditions.checkArgument(!ruleConfig.getDataSources().isEmpty(), "Replica query data source rules can not be empty.");
        ruleConfig.getLoadBalancers().forEach((key, value) -> loadBalancers.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, ReplicaLoadBalanceAlgorithm.class)));
        dataSourceRules = new HashMap<>(ruleConfig.getDataSources().size(), 1);
        for (ReadwriteSplittingDataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
            // TODO check if can not find load balancer should throw exception.
            ReplicaLoadBalanceAlgorithm loadBalanceAlgorithm = Strings.isNullOrEmpty(each.getLoadBalancerName()) || !loadBalancers.containsKey(each.getLoadBalancerName())
                    ? RequiredSPIRegistry.getRegisteredService(ReplicaLoadBalanceAlgorithm.class) : loadBalancers.get(each.getLoadBalancerName());
            dataSourceRules.put(each.getName(), new ReadwriteSplittingDataSourceRule(each, loadBalanceAlgorithm));
        }
    }
    
    public ReadwriteSplittingRule(final AlgorithmProvidedReadwriteSplittingRuleConfiguration ruleConfig) {
        Preconditions.checkArgument(!ruleConfig.getDataSources().isEmpty(), "Replica query data source rules can not be empty.");
        loadBalancers.putAll(ruleConfig.getLoadBalanceAlgorithms());
        dataSourceRules = new HashMap<>(ruleConfig.getDataSources().size(), 1);
        for (ReadwriteSplittingDataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
            // TODO check if can not find load balancer should throw exception.
            ReplicaLoadBalanceAlgorithm loadBalanceAlgorithm = Strings.isNullOrEmpty(each.getLoadBalancerName()) || !loadBalancers.containsKey(each.getLoadBalancerName())
                    ? RequiredSPIRegistry.getRegisteredService(ReplicaLoadBalanceAlgorithm.class) : loadBalancers.get(each.getLoadBalancerName());
            dataSourceRules.put(each.getName(), new ReadwriteSplittingDataSourceRule(each, loadBalanceAlgorithm));
        }
    }
    
    /**
     * Get single data source rule.
     *
     * @return replica query data source rule
     */
    public ReadwriteSplittingDataSourceRule getSingleDataSourceRule() {
        return dataSourceRules.values().iterator().next();
    }
    
    /**
     * Find data source rule.
     *
     * @param dataSourceName data source name
     * @return replica query data source rule
     */
    public Optional<ReadwriteSplittingDataSourceRule> findDataSourceRule(final String dataSourceName) {
        return Optional.ofNullable(dataSourceRules.get(dataSourceName));
    }
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>();
        for (Entry<String, ReadwriteSplittingDataSourceRule> entry : dataSourceRules.entrySet()) {
            result.putAll(entry.getValue().getDataSourceMapper());
        }
        return result;
    }
    
    @Override
    public void updateStatus(final DataSourceStatusChangedEvent event) {
        if (event instanceof DataSourceNameDisabledEvent) {
            for (Entry<String, ReadwriteSplittingDataSourceRule> entry : dataSourceRules.entrySet()) {
                entry.getValue().updateDisabledDataSourceNames(((DataSourceNameDisabledEvent) event).getQualifiedSchema().getDataSourceName(),
                        ((DataSourceNameDisabledEvent) event).isDisabled());
            }
        }
    }
    
    @Override
    public Map<String, Supplier<Object>> getExportedMethods() {
        Map<String, Supplier<Object>> result = new HashMap<>(4, 1);
        result.put(ExportableConstants.EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE, () -> exportDataSource(false, DYNAMIC));
        result.put(ExportableConstants.EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE_NAME, this::exportAutoAwareDataSourceNames);
        result.put(ExportableConstants.EXPORTABLE_KEY_ENABLED_DATA_SOURCE, () -> exportDataSource(true, STATIC));
        result.put(ExportableConstants.EXPORTABLE_KEY_DATA_SOURCE, () -> exportDataSource(false, DYNAMIC, STATIC));
        return result;
    }
    
    private Map<String, Map<String, String>> exportDataSource(final boolean removeDisabled, final String... readwriteSplittingTypes) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>(dataSourceRules.size(), 1);
        dataSourceRules.forEach((name, dataSourceRule) -> {
            if (Arrays.asList(readwriteSplittingTypes).contains(dataSourceRule.getReadwriteSplittingType().getType())) {
                Map<String, String> dataSources = dataSourceRule.getDataSources(removeDisabled);
                if (!dataSources.isEmpty()) {
                    result.put(dataSourceRule.getName(), dataSources);
                }
            }
        });
        return result;
    }
    
    private Collection<String> exportAutoAwareDataSourceNames() {
        Collection<String> result = new ArrayList<>();
        for (ReadwriteSplittingDataSourceRule each : dataSourceRules.values()) {
            if (each.getReadwriteSplittingType() instanceof DynamicReadwriteSplittingType) {
                result.add(((DynamicReadwriteSplittingType) each.getReadwriteSplittingType()).getAutoAwareDataSourceName());
            }
        }
        return result;
    }
    
    @Override
    public String getType() {
        return ReadwriteSplittingRule.class.getSimpleName();
    }
}
