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
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
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
import java.util.Collections;
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
        Preconditions.checkArgument(!configs.isEmpty(), "Readwrite-splitting data source rules can not be empty.");
        checkDataSources(databaseName, configs, dataSourceMap, rules);
        checkLoadBalancerDataSourceName(databaseName, configs, getLoadBalancer(config), rules);
    }
    
    private void checkDataSources(final String databaseName,
                                  final Collection<ReadwriteSplittingDataSourceRuleConfiguration> configs, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) {
        Collection<String> addedWriteDataSourceNames = new HashSet<>();
        Collection<String> addedReadDataSourceNames = new HashSet<>();
        for (ReadwriteSplittingDataSourceRuleConfiguration each : configs) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(each.getName()), "Readwrite-splitting data source name is required.");
            Preconditions.checkState(null != each.getStaticStrategy() || null != each.getDynamicStrategy(), "No available readwrite-splitting rule configuration in database `%s`.", databaseName);
            Optional.ofNullable(each.getStaticStrategy()).ifPresent(optional -> checkStaticStrategy(databaseName, dataSourceMap, addedWriteDataSourceNames, addedReadDataSourceNames, optional, rules));
            Optional.ofNullable(each.getDynamicStrategy()).ifPresent(optional -> checkDynamicStrategy(rules, optional));
        }
    }
    
    private void checkStaticStrategy(final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<String> addedWriteDataSourceNames,
                                     final Collection<String> readDataSourceNames, final StaticReadwriteSplittingStrategyConfiguration strategyConfig, final Collection<ShardingSphereRule> rules) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(strategyConfig.getWriteDataSourceName()), "Write data source name is required.");
        Preconditions.checkArgument(!strategyConfig.getReadDataSourceNames().isEmpty(), "Read data source names are required.");
        checkWriteDataSourceNames(databaseName, dataSourceMap, addedWriteDataSourceNames, strategyConfig, rules);
        for (String each : readDataSourceNames) {
            checkReadeDataSourceNames(databaseName, dataSourceMap, readDataSourceNames, each);
        }
    }
    
    private void checkWriteDataSourceNames(final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<String> addedWriteDataSourceNames,
                                           final StaticReadwriteSplittingStrategyConfiguration strategyConfig, final Collection<ShardingSphereRule> rules) {
        for (String each : new InlineExpressionParser(strategyConfig.getWriteDataSourceName()).splitAndEvaluate()) {
            Preconditions.checkState(dataSourceMap.containsKey(each) || containsInOtherRules(each, rules), "Write data source name `%s` not in database `%s`.", each, databaseName);
            Preconditions.checkState(addedWriteDataSourceNames.add(each), "Can not config duplicate write data source `%s` in database `%s`.", each, databaseName);
        }
    }
    
    private boolean containsInOtherRules(final String datasourceName, final Collection<ShardingSphereRule> rules) {
        for (ShardingSphereRule each : rules) {
            if (each instanceof DataSourceContainedRule && ((DataSourceContainedRule) each).getDataSourceMapper().containsKey(datasourceName)) {
                return true;
            }
        }
        return false;
    }
    
    private void checkReadeDataSourceNames(final String databaseName,
                                           final Map<String, DataSource> dataSourceMap, final Collection<String> addedReadDataSourceNames, final String readDataSourceName) {
        for (String each : new InlineExpressionParser(readDataSourceName).splitAndEvaluate()) {
            Preconditions.checkState(dataSourceMap.containsKey(each), "Read data source name `%s` not in database `%s`.", each, databaseName);
            Preconditions.checkState(addedReadDataSourceNames.add(each), "Can not config duplicate read data source `%s` in database `%s`.", each, databaseName);
        }
    }
    
    private void checkDynamicStrategy(final Collection<ShardingSphereRule> rules, final DynamicReadwriteSplittingStrategyConfiguration dynamicStrategy) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dynamicStrategy.getAutoAwareDataSourceName()), "Auto aware data source name is required");
        Optional<ShardingSphereRule> dynamicDataSourceStrategy = rules.stream().filter(each -> each instanceof DynamicDataSourceContainedRule).findFirst();
        Preconditions.checkArgument(dynamicDataSourceStrategy.isPresent(), "Dynamic data source strategy is required");
    }
    
    private void checkLoadBalancerDataSourceName(final String databaseName, final Collection<ReadwriteSplittingDataSourceRuleConfiguration> configs,
                                                 final Map<String, ReadQueryLoadBalanceAlgorithm> loadBalancers, final Collection<ShardingSphereRule> rules) {
        for (ReadwriteSplittingDataSourceRuleConfiguration each : configs) {
            if (Strings.isNullOrEmpty(each.getLoadBalancerName())) {
                continue;
            }
            ReadQueryLoadBalanceAlgorithm loadBalancer = loadBalancers.get(each.getLoadBalancerName());
            Preconditions.checkNotNull(loadBalancer, "Not found load balance type in database `%s`", databaseName);
            if (loadBalancer instanceof WeightReadQueryLoadBalanceAlgorithm || loadBalancer instanceof TransactionWeightReadQueryLoadBalanceAlgorithm) {
                Preconditions.checkState(!loadBalancer.getProps().isEmpty(), "Readwrite-splitting data source weight config are required in database `%s`", databaseName);
                Collection<String> dataSourceNames = getDataSourceNames(each, rules);
                loadBalancer.getProps().stringPropertyNames().forEach(dataSourceName -> Preconditions.checkState(dataSourceNames.contains(dataSourceName),
                        "Load Balancer datasource name config does not match datasource in database `%s`", databaseName));
            }
        }
    }
    
    private List<String> getDataSourceNames(final ReadwriteSplittingDataSourceRuleConfiguration config, final Collection<ShardingSphereRule> rules) {
        if (null != config.getStaticStrategy()) {
            return config.getStaticStrategy().getReadDataSourceNames();
        }
        Optional<ShardingSphereRule> dynamicDataSourceStrategy = rules.stream().filter(each -> each instanceof DynamicDataSourceContainedRule).findFirst();
        if (!dynamicDataSourceStrategy.isPresent()) {
            return Collections.emptyList();
        }
        DynamicDataSourceContainedRule dynamicDataSourceRule = (DynamicDataSourceContainedRule) dynamicDataSourceStrategy.get();
        List<String> result = new ArrayList<>(dynamicDataSourceRule.getReplicaDataSourceNames(config.getDynamicStrategy().getAutoAwareDataSourceName()));
        result.add(dynamicDataSourceRule.getPrimaryDataSourceName(config.getDynamicStrategy().getAutoAwareDataSourceName()));
        return result;
    }
    
    protected abstract Collection<ReadwriteSplittingDataSourceRuleConfiguration> getDataSources(T config);
    
    protected abstract Map<String, ReadQueryLoadBalanceAlgorithm> getLoadBalancer(T config);
}
