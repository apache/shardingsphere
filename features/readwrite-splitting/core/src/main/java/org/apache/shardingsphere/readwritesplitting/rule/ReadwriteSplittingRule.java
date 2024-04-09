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
import org.apache.shardingsphere.infra.algorithm.loadbalancer.core.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.exception.rule.InvalidInlineExpressionDataSourceNameException;
import org.apache.shardingsphere.readwritesplitting.rule.attribute.ReadwriteSplittingDataSourceMapperRuleAttribute;
import org.apache.shardingsphere.readwritesplitting.rule.attribute.ReadwriteSplittingExportableRuleAttribute;
import org.apache.shardingsphere.readwritesplitting.rule.attribute.ReadwriteSplittingStaticDataSourceRuleAttribute;
import org.apache.shardingsphere.readwritesplitting.rule.attribute.ReadwriteSplittingStorageConnectorReusableRuleAttribute;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private final Map<String, ReadwriteSplittingDataSourceRule> dataSourceRules;
    
    @Getter
    private final RuleAttributes attributes;
    
    public ReadwriteSplittingRule(final String databaseName, final ReadwriteSplittingRuleConfiguration ruleConfig, final InstanceContext instanceContext) {
        configuration = ruleConfig;
        loadBalancers = createLoadBalancers(ruleConfig);
        dataSourceRules = createDataSourceRules(ruleConfig);
        attributes = new RuleAttributes(
                new ReadwriteSplittingDataSourceMapperRuleAttribute(dataSourceRules.values()), new ReadwriteSplittingStaticDataSourceRuleAttribute(databaseName, dataSourceRules, instanceContext),
                new ReadwriteSplittingExportableRuleAttribute(dataSourceRules), new ReadwriteSplittingStorageConnectorReusableRuleAttribute());
    }
    
    private Map<String, LoadBalanceAlgorithm> createLoadBalancers(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        Map<String, LoadBalanceAlgorithm> result = new LinkedHashMap<>(ruleConfig.getDataSources().size(), 1F);
        for (ReadwriteSplittingDataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
            if (ruleConfig.getLoadBalancers().containsKey(each.getLoadBalancerName())) {
                AlgorithmConfiguration algorithmConfig = ruleConfig.getLoadBalancers().get(each.getLoadBalancerName());
                result.put(each.getName() + "." + each.getLoadBalancerName(), TypedSPILoader.getService(LoadBalanceAlgorithm.class, algorithmConfig.getType(), algorithmConfig.getProps()));
            }
        }
        return result;
    }
    
    private Map<String, ReadwriteSplittingDataSourceRule> createDataSourceRules(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        Map<String, ReadwriteSplittingDataSourceRule> result = new HashMap<>(ruleConfig.getDataSources().size(), 1F);
        for (ReadwriteSplittingDataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
            result.putAll(createDataSourceRules(each));
        }
        return result;
    }
    
    private Map<String, ReadwriteSplittingDataSourceRule> createDataSourceRules(final ReadwriteSplittingDataSourceRuleConfiguration config) {
        LoadBalanceAlgorithm loadBalanceAlgorithm = loadBalancers.getOrDefault(
                config.getName() + "." + config.getLoadBalancerName(), TypedSPILoader.getService(LoadBalanceAlgorithm.class, null));
        return createStaticDataSourceRules(config, loadBalanceAlgorithm);
    }
    
    private Map<String, ReadwriteSplittingDataSourceRule> createStaticDataSourceRules(final ReadwriteSplittingDataSourceRuleConfiguration config,
                                                                                      final LoadBalanceAlgorithm loadBalanceAlgorithm) {
        List<String> inlineReadwriteDataSourceNames = InlineExpressionParserFactory.newInstance(config.getName()).splitAndEvaluate();
        List<String> inlineWriteDatasourceNames = InlineExpressionParserFactory.newInstance(config.getWriteDataSourceName()).splitAndEvaluate();
        List<List<String>> inlineReadDatasourceNames = config.getReadDataSourceNames().stream()
                .map(each -> InlineExpressionParserFactory.newInstance(each).splitAndEvaluate()).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(inlineWriteDatasourceNames.size() == inlineReadwriteDataSourceNames.size(),
                () -> new InvalidInlineExpressionDataSourceNameException("Inline expression write data source names size error."));
        inlineReadDatasourceNames.forEach(each -> ShardingSpherePreconditions.checkState(each.size() == inlineReadwriteDataSourceNames.size(),
                () -> new InvalidInlineExpressionDataSourceNameException("Inline expression read data source names size error.")));
        Map<String, ReadwriteSplittingDataSourceRule> result = new LinkedHashMap<>(inlineReadwriteDataSourceNames.size(), 1F);
        for (int i = 0; i < inlineReadwriteDataSourceNames.size(); i++) {
            ReadwriteSplittingDataSourceRuleConfiguration staticConfig = createStaticDataSourceRuleConfiguration(
                    config, i, inlineReadwriteDataSourceNames, inlineWriteDatasourceNames, inlineReadDatasourceNames);
            result.put(inlineReadwriteDataSourceNames.get(i), new ReadwriteSplittingDataSourceRule(staticConfig, config.getTransactionalReadQueryStrategy(), loadBalanceAlgorithm));
        }
        return result;
    }
    
    private ReadwriteSplittingDataSourceRuleConfiguration createStaticDataSourceRuleConfiguration(final ReadwriteSplittingDataSourceRuleConfiguration config, final int index,
                                                                                                  final List<String> readwriteDataSourceNames, final List<String> writeDatasourceNames,
                                                                                                  final List<List<String>> readDatasourceNames) {
        List<String> readDataSourceNames = readDatasourceNames.stream().map(each -> each.get(index)).collect(Collectors.toList());
        return new ReadwriteSplittingDataSourceRuleConfiguration(readwriteDataSourceNames.get(index), writeDatasourceNames.get(index), readDataSourceNames, config.getLoadBalancerName());
    }
    
    /**
     * Get single data source rule.
     *
     * @return readwrite-splitting data source rule
     */
    public ReadwriteSplittingDataSourceRule getSingleDataSourceRule() {
        return dataSourceRules.values().iterator().next();
    }
    
    /**
     * Find data source rule.
     *
     * @param dataSourceName data source name
     * @return readwrite-splitting data source rule
     */
    public Optional<ReadwriteSplittingDataSourceRule> findDataSourceRule(final String dataSourceName) {
        return Optional.ofNullable(dataSourceRules.get(dataSourceName));
    }
}
