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
import org.apache.shardingsphere.infra.algorithm.loadbalancer.core.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.exception.checker.DataSourceNameNotExistedException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.DuplicateDataSourceException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.LoadBalancerAlgorithmNotFoundException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.MissingRequiredDataSourceNameException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.MissingRequiredReadDataSourceNamesException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.MissingRequiredWriteDataSourceNameException;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Readwrite-splitting rule configuration checker.
 */
public final class ReadwriteSplittingRuleConfigurationChecker implements RuleConfigurationChecker<ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final ReadwriteSplittingRuleConfiguration config, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> configs = config.getDataSources();
        checkDataSources(databaseName, configs, dataSourceMap, builtRules);
        checkLoadBalancer(databaseName, configs, getLoadBalancer(config));
    }
    
    private void checkDataSources(final String databaseName, final Collection<ReadwriteSplittingDataSourceRuleConfiguration> configs,
                                  final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        Collection<String> addedWriteDataSourceNames = new HashSet<>();
        Collection<String> addedReadDataSourceNames = new HashSet<>();
        for (ReadwriteSplittingDataSourceRuleConfiguration each : configs) {
            ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(each.getName()), () -> new MissingRequiredDataSourceNameException(databaseName));
            checkDataSources(databaseName, dataSourceMap, each, addedWriteDataSourceNames, addedReadDataSourceNames, builtRules);
        }
    }
    
    private void checkDataSources(final String databaseName, final Map<String, DataSource> dataSourceMap,
                                  final ReadwriteSplittingDataSourceRuleConfiguration config, final Collection<String> addedWriteDataSourceNames,
                                  final Collection<String> readDataSourceNames, final Collection<ShardingSphereRule> rules) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(config.getWriteDataSourceName()), () -> new MissingRequiredWriteDataSourceNameException(databaseName));
        ShardingSpherePreconditions.checkState(!config.getReadDataSourceNames().isEmpty(), () -> new MissingRequiredReadDataSourceNamesException(databaseName));
        checkWriteDataSourceNames(databaseName, dataSourceMap, addedWriteDataSourceNames, config, rules);
        for (String each : config.getReadDataSourceNames()) {
            checkReadeDataSourceNames(databaseName, dataSourceMap, readDataSourceNames, each, rules);
        }
    }
    
    private void checkWriteDataSourceNames(final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<String> addedWriteDataSourceNames,
                                           final ReadwriteSplittingDataSourceRuleConfiguration config, final Collection<ShardingSphereRule> rules) {
        for (String each : InlineExpressionParserFactory.newInstance(config.getWriteDataSourceName()).splitAndEvaluate()) {
            ShardingSpherePreconditions.checkState(dataSourceMap.containsKey(each) || containsInOtherRules(each, rules),
                    () -> new DataSourceNameNotExistedException(String.format("Write data source name `%s` not in database `%s`.", each, databaseName)));
            ShardingSpherePreconditions.checkState(addedWriteDataSourceNames.add(each),
                    () -> new DuplicateDataSourceException(String.format("Can not config duplicate write data source `%s` in database `%s`.", each, databaseName)));
        }
    }
    
    private boolean containsInOtherRules(final String datasourceName, final Collection<ShardingSphereRule> rules) {
        for (ShardingSphereRule each : rules) {
            Optional<DataSourceMapperRuleAttribute> ruleAttribute = each.getAttributes().findAttribute(DataSourceMapperRuleAttribute.class);
            if (ruleAttribute.isPresent() && ruleAttribute.get().getDataSourceMapper().containsKey(datasourceName)) {
                return true;
            }
        }
        return false;
    }
    
    private void checkReadeDataSourceNames(final String databaseName, final Map<String, DataSource> dataSourceMap,
                                           final Collection<String> addedReadDataSourceNames, final String readDataSourceName, final Collection<ShardingSphereRule> rules) {
        for (String each : InlineExpressionParserFactory.newInstance(readDataSourceName).splitAndEvaluate()) {
            ShardingSpherePreconditions.checkState(dataSourceMap.containsKey(each) || containsInOtherRules(each, rules),
                    () -> new DataSourceNameNotExistedException(String.format("Read data source name `%s` not in database `%s`.", each, databaseName)));
            ShardingSpherePreconditions.checkState(addedReadDataSourceNames.add(each),
                    () -> new DuplicateDataSourceException(String.format("Can not config duplicate read data source `%s` in database `%s`.", each, databaseName)));
        }
    }
    
    private void checkLoadBalancer(final String databaseName, final Collection<ReadwriteSplittingDataSourceRuleConfiguration> configs, final Map<String, LoadBalanceAlgorithm> loadBalancers) {
        for (ReadwriteSplittingDataSourceRuleConfiguration each : configs) {
            if (Strings.isNullOrEmpty(each.getLoadBalancerName())) {
                continue;
            }
            LoadBalanceAlgorithm loadBalancer = loadBalancers.get(each.getLoadBalancerName());
            ShardingSpherePreconditions.checkNotNull(loadBalancer, () -> new LoadBalancerAlgorithmNotFoundException(databaseName));
            loadBalancer.check(databaseName, each.getReadDataSourceNames());
        }
    }
    
    private Map<String, LoadBalanceAlgorithm> getLoadBalancer(final ReadwriteSplittingRuleConfiguration config) {
        Map<String, LoadBalanceAlgorithm> result = new LinkedHashMap<>(config.getLoadBalancers().size(), 1F);
        config.getLoadBalancers().forEach((key, value) -> result.put(key, TypedSPILoader.getService(LoadBalanceAlgorithm.class, value.getType(), value.getProps())));
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
