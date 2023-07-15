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
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.WeightReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.exception.algorithm.MissingRequiredReadDatabaseWeightException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.DataSourceNameExistedException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.DuplicateDataSourceException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.InvalidWeightLoadBalancerConfigurationException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.LoadBalancerAlgorithmNotFoundException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.MissingRequiredDataSourceNameException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.MissingRequiredReadDataSourceNamesException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.MissingRequiredWriteDataSourceNameException;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Readwrite-splitting rule configuration checker.
 */
public final class ReadwriteSplittingRuleConfigurationChecker implements RuleConfigurationChecker<ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final ReadwriteSplittingRuleConfiguration config, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> configs = config.getDataSources();
        checkDataSources(databaseName, configs, dataSourceMap, builtRules);
        checkLoadBalancerDataSourceName(databaseName, configs, getLoadBalancer(config));
    }
    
    private void checkDataSources(final String databaseName,
                                  final Collection<ReadwriteSplittingDataSourceRuleConfiguration> configs, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) {
        Collection<String> addedWriteDataSourceNames = new HashSet<>();
        Collection<String> addedReadDataSourceNames = new HashSet<>();
        for (ReadwriteSplittingDataSourceRuleConfiguration each : configs) {
            ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(each.getName()), () -> new MissingRequiredDataSourceNameException(databaseName));
            checkDataSources(databaseName, dataSourceMap, each, addedWriteDataSourceNames, addedReadDataSourceNames, rules);
        }
    }
    
    private void checkDataSources(final String databaseName, final Map<String, DataSource> dataSourceMap,
                                  final ReadwriteSplittingDataSourceRuleConfiguration config, final Collection<String> addedWriteDataSourceNames,
                                  final Collection<String> readDataSourceNames, final Collection<ShardingSphereRule> rules) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(config.getWriteDataSourceName()), () -> new MissingRequiredWriteDataSourceNameException(databaseName));
        ShardingSpherePreconditions.checkState(!config.getReadDataSourceNames().isEmpty(), () -> new MissingRequiredReadDataSourceNamesException(databaseName));
        checkWriteDataSourceNames(databaseName, dataSourceMap, addedWriteDataSourceNames, config, rules);
        for (String each : config.getReadDataSourceNames()) {
            checkReadeDataSourceNames(databaseName, dataSourceMap, readDataSourceNames, each);
        }
    }
    
    private void checkWriteDataSourceNames(final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<String> addedWriteDataSourceNames,
                                           final ReadwriteSplittingDataSourceRuleConfiguration config, final Collection<ShardingSphereRule> rules) {
        for (String each : InlineExpressionParserFactory.newInstance().splitAndEvaluate(config.getWriteDataSourceName())) {
            ShardingSpherePreconditions.checkState(dataSourceMap.containsKey(each) || containsInOtherRules(each, rules),
                    () -> new DataSourceNameExistedException(String.format("Write data source name `%s` not in database `%s`.", each, databaseName)));
            ShardingSpherePreconditions.checkState(addedWriteDataSourceNames.add(each),
                    () -> new DuplicateDataSourceException(String.format("Can not config duplicate write data source `%s` in database `%s`.", each, databaseName)));
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
    
    private void checkReadeDataSourceNames(final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<String> addedReadDataSourceNames, final String readDataSourceName) {
        for (String each : InlineExpressionParserFactory.newInstance().splitAndEvaluate(readDataSourceName)) {
            ShardingSpherePreconditions.checkState(dataSourceMap.containsKey(each),
                    () -> new DataSourceNameExistedException(String.format("Read data source name `%s` not in database `%s`.", each, databaseName)));
            ShardingSpherePreconditions.checkState(addedReadDataSourceNames.add(each),
                    () -> new DuplicateDataSourceException(String.format("Can not config duplicate read data source `%s` in database `%s`.", each, databaseName)));
        }
    }
    
    private void checkLoadBalancerDataSourceName(final String databaseName, final Collection<ReadwriteSplittingDataSourceRuleConfiguration> configs,
                                                 final Map<String, ReadQueryLoadBalanceAlgorithm> loadBalancers) {
        for (ReadwriteSplittingDataSourceRuleConfiguration each : configs) {
            if (Strings.isNullOrEmpty(each.getLoadBalancerName())) {
                continue;
            }
            ReadQueryLoadBalanceAlgorithm loadBalancer = loadBalancers.get(each.getLoadBalancerName());
            ShardingSpherePreconditions.checkNotNull(loadBalancer, () -> new LoadBalancerAlgorithmNotFoundException(databaseName));
            if (loadBalancer instanceof WeightReadQueryLoadBalanceAlgorithm) {
                ShardingSpherePreconditions.checkState(!((WeightReadQueryLoadBalanceAlgorithm) loadBalancer).getDataSourceNames().isEmpty(),
                        () -> new MissingRequiredReadDatabaseWeightException(loadBalancer.getType(), String.format("Read data source weight config are required in database `%s`", databaseName)));
                Collection<String> dataSourceNames = each.getReadDataSourceNames();
                ((WeightReadQueryLoadBalanceAlgorithm) loadBalancer).getDataSourceNames().forEach(dataSourceName -> ShardingSpherePreconditions.checkState(dataSourceNames.contains(dataSourceName),
                        () -> new InvalidWeightLoadBalancerConfigurationException(databaseName)));
            }
        }
    }
    
    private Map<String, ReadQueryLoadBalanceAlgorithm> getLoadBalancer(final ReadwriteSplittingRuleConfiguration config) {
        Map<String, ReadQueryLoadBalanceAlgorithm> result = new LinkedHashMap<>(config.getLoadBalancers().size(), 1F);
        config.getLoadBalancers().forEach((key, value) -> result.put(key, TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class, value.getType(), value.getProps())));
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
