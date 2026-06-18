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

import lombok.Getter;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.loadbalancer.spi.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingDataSourceType;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.exception.ReadwriteSplittingRuleExceptionIdentifier;
import org.apache.shardingsphere.readwritesplitting.exception.actual.InvalidReadwriteSplittingActualDataSourceInlineExpressionException;
import org.apache.shardingsphere.readwritesplitting.rule.attribute.ReadwriteSplittingDataSourceMapperRuleAttribute;
import org.apache.shardingsphere.readwritesplitting.rule.attribute.ReadwriteSplittingExportableRuleAttribute;
import org.apache.shardingsphere.readwritesplitting.rule.attribute.ReadwriteSplittingStaticDataSourceRuleAttribute;
import org.apache.shardingsphere.readwritesplitting.rule.attribute.ReadwriteSplittingStorageConnectorReusableRuleAttribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Readwrite-splitting rule.
 */
public final class ReadwriteSplittingRule implements DatabaseRule {
    
    @Getter
    private final ReadwriteSplittingRuleConfiguration configuration;
    
    private final Map<String, LoadBalanceAlgorithm> loadBalancers;
    
    @Getter
    private final Map<String, ReadwriteSplittingDataSourceGroupRule> dataSourceRuleGroups;
    
    @Getter
    private final RuleAttributes attributes;
    
    public ReadwriteSplittingRule(final String databaseName, final ReadwriteSplittingRuleConfiguration ruleConfig, final ComputeNodeInstanceContext computeNodeInstanceContext) {
        configuration = ruleConfig;
        loadBalancers = createLoadBalancers(ruleConfig);
        dataSourceRuleGroups = createDataSourceGroupRules(databaseName, ruleConfig);
        attributes = new RuleAttributes(
                new ReadwriteSplittingDataSourceMapperRuleAttribute(dataSourceRuleGroups.values()),
                new ReadwriteSplittingStaticDataSourceRuleAttribute(databaseName, dataSourceRuleGroups, computeNodeInstanceContext),
                new ReadwriteSplittingExportableRuleAttribute(dataSourceRuleGroups),
                new ReadwriteSplittingStorageConnectorReusableRuleAttribute());
    }
    
    private Map<String, LoadBalanceAlgorithm> createLoadBalancers(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        Map<String, LoadBalanceAlgorithm> result = new HashMap<>(ruleConfig.getDataSourceGroups().size(), 1F);
        for (ReadwriteSplittingDataSourceGroupRuleConfiguration each : ruleConfig.getDataSourceGroups()) {
            if (ruleConfig.getLoadBalancers().containsKey(each.getLoadBalancerName())) {
                AlgorithmConfiguration algorithmConfig = ruleConfig.getLoadBalancers().get(each.getLoadBalancerName());
                result.put(each.getName() + "." + each.getLoadBalancerName(), TypedSPILoader.getService(LoadBalanceAlgorithm.class, algorithmConfig.getType(), algorithmConfig.getProps()));
            }
        }
        return result;
    }
    
    private Map<String, ReadwriteSplittingDataSourceGroupRule> createDataSourceGroupRules(final String databaseName, final ReadwriteSplittingRuleConfiguration ruleConfig) {
        Map<String, ReadwriteSplittingDataSourceGroupRule> result = new HashMap<>(ruleConfig.getDataSourceGroups().size(), 1F);
        for (ReadwriteSplittingDataSourceGroupRuleConfiguration each : ruleConfig.getDataSourceGroups()) {
            result.putAll(createDataSourceGroupRules(databaseName, each));
        }
        return result;
    }
    
    private Map<String, ReadwriteSplittingDataSourceGroupRule> createDataSourceGroupRules(final String databaseName, final ReadwriteSplittingDataSourceGroupRuleConfiguration config) {
        LoadBalanceAlgorithm loadBalanceAlgorithm = loadBalancers.getOrDefault(config.getName() + "." + config.getLoadBalancerName(), TypedSPILoader.getService(LoadBalanceAlgorithm.class, null));
        return createStaticDataSourceGroupRules(databaseName, config, loadBalanceAlgorithm);
    }
    
    private Map<String, ReadwriteSplittingDataSourceGroupRule> createStaticDataSourceGroupRules(final String databaseName, final ReadwriteSplittingDataSourceGroupRuleConfiguration config,
                                                                                                final LoadBalanceAlgorithm loadBalanceAlgorithm) {
        List<String> inlineLogicDataSourceNames = InlineExpressionParserFactory.newInstance(config.getName()).splitAndEvaluate();
        List<String> inlineWriteDataSourceNames = InlineExpressionParserFactory.newInstance(config.getWriteDataSourceName()).splitAndEvaluate();
        List<List<String>> inlineReadDataSourceNames = config.getReadDataSourceNames().stream()
                .map(each -> InlineExpressionParserFactory.newInstance(each).splitAndEvaluate()).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(inlineWriteDataSourceNames.size() == inlineLogicDataSourceNames.size(),
                () -> new InvalidReadwriteSplittingActualDataSourceInlineExpressionException(
                        ReadwriteSplittingDataSourceType.WRITE, new ReadwriteSplittingRuleExceptionIdentifier(databaseName, config.getName())));
        inlineReadDataSourceNames.forEach(each -> ShardingSpherePreconditions.checkState(each.size() == inlineLogicDataSourceNames.size(),
                () -> new InvalidReadwriteSplittingActualDataSourceInlineExpressionException(
                        ReadwriteSplittingDataSourceType.READ, new ReadwriteSplittingRuleExceptionIdentifier(databaseName, config.getName()))));
        Map<String, ReadwriteSplittingDataSourceGroupRule> result = new HashMap<>(inlineLogicDataSourceNames.size(), 1F);
        for (int i = 0; i < inlineLogicDataSourceNames.size(); i++) {
            ReadwriteSplittingDataSourceGroupRuleConfiguration staticConfig = createStaticDataSourceGroupRuleConfiguration(
                    config, i, inlineLogicDataSourceNames, inlineWriteDataSourceNames, inlineReadDataSourceNames);
            result.put(inlineLogicDataSourceNames.get(i), new ReadwriteSplittingDataSourceGroupRule(staticConfig, config.getTransactionalReadQueryStrategy(), loadBalanceAlgorithm));
        }
        return result;
    }
    
    private ReadwriteSplittingDataSourceGroupRuleConfiguration createStaticDataSourceGroupRuleConfiguration(final ReadwriteSplittingDataSourceGroupRuleConfiguration config, final int index,
                                                                                                            final List<String> logicDataSourceNames, final List<String> writeDatasourceNames,
                                                                                                            final List<List<String>> readDatasourceNames) {
        List<String> readDataSourceNames = readDatasourceNames.stream().map(each -> each.get(index)).collect(Collectors.toList());
        return new ReadwriteSplittingDataSourceGroupRuleConfiguration(logicDataSourceNames.get(index), writeDatasourceNames.get(index), readDataSourceNames, config.getLoadBalancerName());
    }
    
    /**
     * Get single data source group rule.
     *
     * @return readwrite-splitting data source group rule
     */
    public ReadwriteSplittingDataSourceGroupRule getSingleDataSourceGroupRule() {
        return dataSourceRuleGroups.values().iterator().next();
    }
    
    /**
     * Find data source group rule.
     *
     * @param dataSourceName data source name
     * @return readwrite-splitting data source group rule
     */
    public Optional<ReadwriteSplittingDataSourceGroupRule> findDataSourceGroupRule(final String dataSourceName) {
        return Optional.ofNullable(dataSourceRuleGroups.get(dataSourceName));
    }
    
    @Override
    public int getOrder() {
        return ReadwriteSplittingOrder.ORDER;
    }
}
