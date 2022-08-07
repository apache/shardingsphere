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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DynamicDataSourceContainedRule;
import org.apache.shardingsphere.infra.util.expr.InlineExpressionParser;
import org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.TransactionWeightReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.WeightReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract readwrite-splitting rule configuration checker.
 * 
 * @param <T> type of rule configuration
 */
public abstract class AbstractReadwriteSplittingRuleConfigurationChecker<T extends RuleConfiguration> implements RuleConfigurationChecker<T> {
    
    @Override
    public final void check(final String databaseName, final T config, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) {
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> configs = getDataSources(config);
        Preconditions.checkArgument(!configs.isEmpty(), "Replica query data source rules can not be empty.");
        checkDataSources(databaseName, configs, dataSourceMap, rules);
        checkLoadBalancerDataSourceName(databaseName, configs, getLoadBalancer(config), rules);
    }
    
    private void checkDataSources(final String databaseName, final Collection<ReadwriteSplittingDataSourceRuleConfiguration> configs,
                                  final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) {
        Collection<String> writeDataSourceNames = new HashSet<>();
        Collection<String> readDataSourceNames = new HashSet<>();
        for (ReadwriteSplittingDataSourceRuleConfiguration each : configs) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(each.getName()), "Name is required.");
            Preconditions.checkState(null != each.getStaticStrategy() || null != each.getDynamicStrategy(),
                    "No available readwrite-splitting rule configuration in database `%s`.", databaseName);
            Optional.ofNullable(each.getStaticStrategy()).ifPresent(optional -> checkStaticStrategy(databaseName, dataSourceMap, writeDataSourceNames, readDataSourceNames, optional));
            Optional.ofNullable(each.getDynamicStrategy()).ifPresent(optional -> checkDynamicStrategy(rules, optional));
        }
    }
    
    private void checkStaticStrategy(final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<String> writeDataSourceNames,
                                     final Collection<String> readDataSourceNames, final StaticReadwriteSplittingStrategyConfiguration strategyConfig) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(strategyConfig.getWriteDataSourceName()), "Write data source name is required.");
        Preconditions.checkArgument(!strategyConfig.getReadDataSourceNames().isEmpty(), "Read data source names are required.");
        Collection<String> inlineWriteNames = new InlineExpressionParser(strategyConfig.getWriteDataSourceName()).splitAndEvaluate();
        inlineWriteNames.forEach(each -> Preconditions.checkState(null != dataSourceMap.get(each), "Write data source name `%s` not in database `%s`.", each, databaseName));
        inlineWriteNames.forEach(each -> Preconditions.checkState(writeDataSourceNames.add(each), "Can not config duplicate write dataSource `%s` in database `%s`.", each, databaseName));
        for (String readName : readDataSourceNames) {
            Collection<String> inlineReadNames = new InlineExpressionParser(readName).splitAndEvaluate();
            inlineReadNames.forEach(each -> Preconditions.checkState(null != dataSourceMap.get(each), "Read data source name `%s` not in database `%s`.", each, databaseName));
            inlineReadNames.forEach(each -> Preconditions.checkState(readDataSourceNames.add(each), "Can not config duplicate write dataSource `%s` in database `%s`.", each, databaseName));
        }
    }
    
    private void checkDynamicStrategy(final Collection<ShardingSphereRule> rules, final DynamicReadwriteSplittingStrategyConfiguration dynamicStrategy) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dynamicStrategy.getAutoAwareDataSourceName()), "Auto aware data source name is required.");
        Optional<ShardingSphereRule> dynamicDataSourceStrategy = rules.stream().filter(each -> each instanceof DynamicDataSourceContainedRule).findFirst();
        Preconditions.checkArgument(dynamicDataSourceStrategy.isPresent(), "Dynamic data source strategy is required.");
    }
    
    private void checkLoadBalancerDataSourceName(final String databaseName, final Collection<ReadwriteSplittingDataSourceRuleConfiguration> configs,
                                                 final Map<String, ReadQueryLoadBalanceAlgorithm> loadBalancers, final Collection<ShardingSphereRule> rules) {
        for (ReadwriteSplittingDataSourceRuleConfiguration each : configs) {
            if (Strings.isNullOrEmpty(each.getLoadBalancerName())) {
                continue;
            }
            ReadQueryLoadBalanceAlgorithm loadBalancer = loadBalancers.get(each.getLoadBalancerName());
            Preconditions.checkState(null != loadBalancer, "Not found load balance type in database `%s`.", databaseName);
            if (loadBalancer instanceof WeightReadQueryLoadBalanceAlgorithm || loadBalancer instanceof TransactionWeightReadQueryLoadBalanceAlgorithm) {
                Preconditions.checkState(!loadBalancer.getProps().isEmpty(), "Readwrite-splitting data source weight config are required in database `%s`.", databaseName);
                List<String> dataSourceNames = getDataSourceNames(each, rules);
                loadBalancer.getProps().keySet().forEach(dataSourceName -> Preconditions.checkState(dataSourceNames.contains(dataSourceName),
                        "Load Balancer datasource name config does not match datasource in database `%s`.", databaseName));
            }
        }
    }
    
    private List<String> getDataSourceNames(final ReadwriteSplittingDataSourceRuleConfiguration config, final Collection<ShardingSphereRule> rules) {
        if (null != config.getStaticStrategy()) {
            return config.getStaticStrategy().getReadDataSourceNames();
        }
        Optional<ShardingSphereRule> dynamicDataSourceStrategy = rules.stream().filter(each -> each instanceof DynamicDataSourceContainedRule).findFirst();
        DynamicDataSourceContainedRule dynamicDataSourceRule = (DynamicDataSourceContainedRule) dynamicDataSourceStrategy.get();
        List<String> result = new ArrayList<>(dynamicDataSourceRule.getReplicaDataSourceNames(config.getDynamicStrategy().getAutoAwareDataSourceName()));
        result.add(dynamicDataSourceRule.getPrimaryDataSourceName(config.getDynamicStrategy().getAutoAwareDataSourceName()));
        return result;
    }
    
    protected abstract Collection<ReadwriteSplittingDataSourceRuleConfiguration> getDataSources(T config);
    
    protected abstract Map<String, ReadQueryLoadBalanceAlgorithm> getLoadBalancer(T config);
}
