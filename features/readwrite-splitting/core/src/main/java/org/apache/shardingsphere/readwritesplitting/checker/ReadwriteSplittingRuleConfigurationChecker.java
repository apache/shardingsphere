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

package org.apache.shardingsphere.readwritesplitting.checker;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.algorithm.loadbalancer.spi.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Readwrite-splitting rule configuration checker.
 */
public final class ReadwriteSplittingRuleConfigurationChecker implements DatabaseRuleConfigurationChecker<ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final ReadwriteSplittingRuleConfiguration ruleConfig, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        checkDataSources(databaseName, ruleConfig.getDataSourceGroups(), dataSourceMap, builtRules);
        checkLoadBalancer(databaseName, ruleConfig);
    }
    
    private void checkDataSources(final String databaseName, final Collection<ReadwriteSplittingDataSourceGroupRuleConfiguration> configs,
                                  final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        Collection<String> builtWriteDataSourceNames = new HashSet<>();
        Collection<String> builtReadDataSourceNames = new HashSet<>();
        configs.forEach(each -> new ReadwriteSplittingDataSourceRuleConfigurationChecker(databaseName, each, dataSourceMap).check(builtWriteDataSourceNames, builtReadDataSourceNames, builtRules));
    }
    
    private void checkLoadBalancer(final String databaseName, final ReadwriteSplittingRuleConfiguration ruleConfig) {
        Map<String, LoadBalanceAlgorithm> loadBalancers = ruleConfig.getLoadBalancers().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> TypedSPILoader.getService(LoadBalanceAlgorithm.class, entry.getValue().getType(), entry.getValue().getProps())));
        for (ReadwriteSplittingDataSourceGroupRuleConfiguration each : ruleConfig.getDataSourceGroups()) {
            if (Strings.isNullOrEmpty(each.getLoadBalancerName())) {
                continue;
            }
            LoadBalanceAlgorithm loadBalancer = loadBalancers.get(each.getLoadBalancerName());
            ShardingSpherePreconditions.checkNotNull(loadBalancer, () -> new UnregisteredAlgorithmException("Load balancer", each.getLoadBalancerName(), new SQLExceptionIdentifier(databaseName)));
            loadBalancer.check(databaseName, each.getReadDataSourceNames());
        }
    }
    
    @Override
    public Collection<String> getRequiredDataSourceNames(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        for (ReadwriteSplittingDataSourceGroupRuleConfiguration each : ruleConfig.getDataSourceGroups()) {
            if (null != each.getWriteDataSourceName()) {
                result.add(each.getWriteDataSourceName());
            }
            if (!each.getReadDataSourceNames().isEmpty()) {
                result.addAll(each.getReadDataSourceNames());
            }
        }
        return result;
    }
    
    @Override
    public int getOrder() {
        return ReadwriteSplittingOrder.ORDER;
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getTypeClass() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
}
