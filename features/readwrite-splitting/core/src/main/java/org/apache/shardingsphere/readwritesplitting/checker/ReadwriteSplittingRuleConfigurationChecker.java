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
import org.apache.shardingsphere.infra.algorithm.loadbalancer.core.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingDataSourceType;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.exception.DataSourceNameNotExistedException;
import org.apache.shardingsphere.readwritesplitting.exception.DuplicateDataSourceException;
import org.apache.shardingsphere.readwritesplitting.exception.MissingRequiredDataSourceNameException;
import org.apache.shardingsphere.readwritesplitting.exception.MissingRequiredReadDataSourceNamesException;
import org.apache.shardingsphere.readwritesplitting.exception.MissingRequiredWriteDataSourceNameException;

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
public final class ReadwriteSplittingRuleConfigurationChecker implements RuleConfigurationChecker<ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final ReadwriteSplittingRuleConfiguration ruleConfig, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        checkDataSources(databaseName, ruleConfig.getDataSources(), dataSourceMap, builtRules);
        checkLoadBalancer(databaseName, ruleConfig);
    }
    
    private void checkDataSources(final String databaseName, final Collection<ReadwriteSplittingDataSourceRuleConfiguration> configs,
                                  final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        Collection<String> writeDataSourceNames = new HashSet<>();
        Collection<String> readDataSourceNames = new HashSet<>();
        for (ReadwriteSplittingDataSourceRuleConfiguration each : configs) {
            ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(each.getName()), () -> new MissingRequiredDataSourceNameException(databaseName));
            checkDataSources(databaseName, dataSourceMap, each, writeDataSourceNames, readDataSourceNames, builtRules);
        }
    }
    
    private void checkDataSources(final String databaseName, final Map<String, DataSource> dataSourceMap, final ReadwriteSplittingDataSourceRuleConfiguration config,
                                  final Collection<String> writeDataSourceNames, final Collection<String> readDataSourceNames, final Collection<ShardingSphereRule> builtRules) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(config.getWriteDataSourceName()), () -> new MissingRequiredWriteDataSourceNameException(databaseName));
        ShardingSpherePreconditions.checkState(!config.getReadDataSourceNames().isEmpty(), () -> new MissingRequiredReadDataSourceNamesException(databaseName));
        checkWriteDataSourceNames(databaseName, dataSourceMap, writeDataSourceNames, config, builtRules);
        for (String each : config.getReadDataSourceNames()) {
            checkReadeDataSourceNames(databaseName, dataSourceMap, readDataSourceNames, each, builtRules);
        }
    }
    
    private void checkWriteDataSourceNames(final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<String> writeDataSourceNames,
                                           final ReadwriteSplittingDataSourceRuleConfiguration config, final Collection<ShardingSphereRule> builtRules) {
        for (String each : InlineExpressionParserFactory.newInstance(config.getWriteDataSourceName()).splitAndEvaluate()) {
            ShardingSpherePreconditions.checkState(dataSourceMap.containsKey(each) || containsInOtherRules(each, builtRules),
                    () -> new DataSourceNameNotExistedException(ReadwriteSplittingDataSourceType.WRITE, each, databaseName));
            ShardingSpherePreconditions.checkState(writeDataSourceNames.add(each), () -> new DuplicateDataSourceException(ReadwriteSplittingDataSourceType.WRITE, each, databaseName));
        }
    }
    
    private boolean containsInOtherRules(final String datasourceName, final Collection<ShardingSphereRule> builtRules) {
        return builtRules.stream().map(each -> each.getAttributes().findAttribute(DataSourceMapperRuleAttribute.class))
                .anyMatch(optional -> optional.isPresent() && optional.get().getDataSourceMapper().containsKey(datasourceName));
    }
    
    private void checkReadeDataSourceNames(final String databaseName, final Map<String, DataSource> dataSourceMap,
                                           final Collection<String> readDataSourceNames, final String readDataSourceName, final Collection<ShardingSphereRule> builtRules) {
        for (String each : InlineExpressionParserFactory.newInstance(readDataSourceName).splitAndEvaluate()) {
            ShardingSpherePreconditions.checkState(dataSourceMap.containsKey(each) || containsInOtherRules(each, builtRules),
                    () -> new DataSourceNameNotExistedException(ReadwriteSplittingDataSourceType.READ, each, databaseName));
            ShardingSpherePreconditions.checkState(readDataSourceNames.add(each), () -> new DuplicateDataSourceException(ReadwriteSplittingDataSourceType.READ, each, databaseName));
        }
    }
    
    private void checkLoadBalancer(final String databaseName, final ReadwriteSplittingRuleConfiguration ruleConfig) {
        Map<String, LoadBalanceAlgorithm> loadBalancers = ruleConfig.getLoadBalancers().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> TypedSPILoader.getService(LoadBalanceAlgorithm.class, entry.getValue().getType(), entry.getValue().getProps())));
        for (ReadwriteSplittingDataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
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
        for (ReadwriteSplittingDataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
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
