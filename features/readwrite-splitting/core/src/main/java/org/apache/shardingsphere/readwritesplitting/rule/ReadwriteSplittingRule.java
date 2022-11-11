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
import lombok.Getter;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.constant.ExportableItemConstants;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.event.DataSourceStatusChangedEvent;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StorageConnectorReusableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.infra.util.expr.InlineExpressionParser;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.metadata.storage.event.StorageNodeDataSourceChangedEvent;
import org.apache.shardingsphere.readwritesplitting.algorithm.config.AlgorithmProvidedReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.factory.ReadQueryLoadBalanceAlgorithmFactory;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.strategy.type.DynamicReadwriteSplittingStrategy;
import org.apache.shardingsphere.readwritesplitting.strategy.type.StaticReadwriteSplittingStrategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Readwrite-splitting rule.
 */
public final class ReadwriteSplittingRule implements DatabaseRule, DataSourceContainedRule, StaticDataSourceContainedRule, ExportableRule, StorageConnectorReusableRule {
    
    @Getter
    private final RuleConfiguration configuration;
    
    private final Map<String, ReadQueryLoadBalanceAlgorithm> loadBalancers = new LinkedHashMap<>();
    
    private final Map<String, ReadwriteSplittingDataSourceRule> dataSourceRules;
    
    public ReadwriteSplittingRule(final ReadwriteSplittingRuleConfiguration ruleConfig, final Collection<ShardingSphereRule> builtRules) {
        configuration = ruleConfig;
        ruleConfig.getDataSources().stream().filter(each -> null != ruleConfig.getLoadBalancers().get(each.getLoadBalancerName()))
                .forEach(each -> loadBalancers.put(each.getName() + "." + each.getLoadBalancerName(),
                        ReadQueryLoadBalanceAlgorithmFactory.newInstance(ruleConfig.getLoadBalancers().get(each.getLoadBalancerName()))));
        dataSourceRules = new HashMap<>(ruleConfig.getDataSources().size(), 1);
        for (ReadwriteSplittingDataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
            dataSourceRules.putAll(createReadwriteSplittingDataSourceRules(each, builtRules));
        }
    }
    
    public ReadwriteSplittingRule(final AlgorithmProvidedReadwriteSplittingRuleConfiguration ruleConfig, final Collection<ShardingSphereRule> builtRules) {
        configuration = ruleConfig;
        ruleConfig.getDataSources().stream().filter(each -> null != ruleConfig.getLoadBalanceAlgorithms().get(each.getLoadBalancerName()))
                .forEach(each -> loadBalancers.put(each.getName() + "." + each.getLoadBalancerName(), ruleConfig.getLoadBalanceAlgorithms().get(each.getLoadBalancerName())));
        dataSourceRules = new HashMap<>(ruleConfig.getDataSources().size(), 1);
        for (ReadwriteSplittingDataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
            dataSourceRules.putAll(createReadwriteSplittingDataSourceRules(each, builtRules));
        }
    }
    
    private Map<String, ReadwriteSplittingDataSourceRule> createReadwriteSplittingDataSourceRules(final ReadwriteSplittingDataSourceRuleConfiguration config,
                                                                                                  final Collection<ShardingSphereRule> builtRules) {
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = loadBalancers.getOrDefault(config.getName() + "_" + config.getLoadBalancerName(), ReadQueryLoadBalanceAlgorithmFactory.newInstance());
        return null == config.getStaticStrategy()
                ? createDynamicReadwriteSplittingDataSourceRules(config, builtRules, loadBalanceAlgorithm)
                : createStaticReadwriteSplittingDataSourceRules(config, builtRules, loadBalanceAlgorithm);
    }
    
    private Map<String, ReadwriteSplittingDataSourceRule> createStaticReadwriteSplittingDataSourceRules(final ReadwriteSplittingDataSourceRuleConfiguration config,
                                                                                                        final Collection<ShardingSphereRule> builtRules,
                                                                                                        final ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm) {
        Map<String, ReadwriteSplittingDataSourceRule> result = new LinkedHashMap<>();
        List<String> inlineReadwriteDataSourceNames = new InlineExpressionParser(config.getName()).splitAndEvaluate();
        List<String> inlineWriteDatasourceNames = new InlineExpressionParser(config.getStaticStrategy().getWriteDataSourceName()).splitAndEvaluate();
        List<List<String>> inlineReadDatasourceNames = config.getStaticStrategy().getReadDataSourceNames().stream()
                .map(each -> new InlineExpressionParser(each).splitAndEvaluate()).collect(Collectors.toList());
        Preconditions.checkArgument(inlineWriteDatasourceNames.size() == inlineReadwriteDataSourceNames.size(), "Inline expression write data source names size error");
        inlineReadDatasourceNames.forEach(each -> Preconditions.checkArgument(each.size() == inlineReadwriteDataSourceNames.size(), "Inline expression read data source names size error"));
        for (int i = 0; i < inlineReadwriteDataSourceNames.size(); i++) {
            ReadwriteSplittingDataSourceRuleConfiguration staticConfig = createStaticDataSourceRuleConfiguration(
                    config, i, inlineReadwriteDataSourceNames, inlineWriteDatasourceNames, inlineReadDatasourceNames);
            result.put(inlineReadwriteDataSourceNames.get(i), new ReadwriteSplittingDataSourceRule(staticConfig, loadBalanceAlgorithm, builtRules));
        }
        return result;
    }
    
    private ReadwriteSplittingDataSourceRuleConfiguration createStaticDataSourceRuleConfiguration(final ReadwriteSplittingDataSourceRuleConfiguration config, final int index,
                                                                                                  final List<String> readwriteDataSourceNames, final List<String> writeDatasourceNames,
                                                                                                  final List<List<String>> readDatasourceNames) {
        List<String> readDataSourceNames = readDatasourceNames.stream().map(each -> each.get(index)).collect(Collectors.toList());
        return new ReadwriteSplittingDataSourceRuleConfiguration(readwriteDataSourceNames.get(index),
                new StaticReadwriteSplittingStrategyConfiguration(writeDatasourceNames.get(index), readDataSourceNames), null, config.getLoadBalancerName());
    }
    
    private Map<String, ReadwriteSplittingDataSourceRule> createDynamicReadwriteSplittingDataSourceRules(final ReadwriteSplittingDataSourceRuleConfiguration config,
                                                                                                         final Collection<ShardingSphereRule> builtRules,
                                                                                                         final ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm) {
        Map<String, ReadwriteSplittingDataSourceRule> result = new LinkedHashMap<>();
        List<String> inlineReadwriteDataSourceNames = new InlineExpressionParser(config.getName()).splitAndEvaluate();
        List<String> inlineAutoAwareDataSourceNames = new InlineExpressionParser(config.getDynamicStrategy().getAutoAwareDataSourceName()).splitAndEvaluate();
        Preconditions.checkArgument(inlineAutoAwareDataSourceNames.size() == inlineReadwriteDataSourceNames.size(), "Inline expression auto aware data source names size error");
        for (int i = 0; i < inlineReadwriteDataSourceNames.size(); i++) {
            ReadwriteSplittingDataSourceRuleConfiguration dynamicConfig = createDynamicDataSourceRuleConfiguration(config, i, inlineReadwriteDataSourceNames, inlineAutoAwareDataSourceNames);
            result.put(inlineReadwriteDataSourceNames.get(i), new ReadwriteSplittingDataSourceRule(dynamicConfig, loadBalanceAlgorithm, builtRules));
        }
        return result;
    }
    
    private ReadwriteSplittingDataSourceRuleConfiguration createDynamicDataSourceRuleConfiguration(final ReadwriteSplittingDataSourceRuleConfiguration config, final int index,
                                                                                                   final List<String> readwriteDataSourceNames, final List<String> autoAwareDataSourceNames) {
        return new ReadwriteSplittingDataSourceRuleConfiguration(readwriteDataSourceNames.get(index), null,
                new DynamicReadwriteSplittingStrategyConfiguration(autoAwareDataSourceNames.get(index), config.getDynamicStrategy().getWriteDataSourceQueryEnabled()), config.getLoadBalancerName());
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
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>();
        for (Entry<String, ReadwriteSplittingDataSourceRule> entry : dataSourceRules.entrySet()) {
            result.put(entry.getValue().getName(), entry.getValue().getReadwriteSplittingStrategy().getAllDataSources());
        }
        return result;
    }
    
    @Override
    public void updateStatus(final DataSourceStatusChangedEvent event) {
        StorageNodeDataSourceChangedEvent dataSourceEvent = (StorageNodeDataSourceChangedEvent) event;
        QualifiedDatabase qualifiedDatabase = dataSourceEvent.getQualifiedDatabase();
        ReadwriteSplittingDataSourceRule dataSourceRule = dataSourceRules.get(qualifiedDatabase.getGroupName());
        Preconditions.checkNotNull(dataSourceRule, "Can not find readwrite-splitting data source rule in database `%s`", qualifiedDatabase.getDatabaseName());
        dataSourceRule.updateDisabledDataSourceNames(dataSourceEvent.getQualifiedDatabase().getDataSourceName(), StorageNodeStatus.isDisable(dataSourceEvent.getDataSource().getStatus()));
    }
    
    @Override
    public Map<String, Object> getExportData() {
        Map<String, Object> result = new HashMap<>(2, 1);
        result.put(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE, exportDynamicDataSources());
        result.put(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE, exportStaticDataSources());
        return result;
    }
    
    private Map<String, Map<String, String>> exportDynamicDataSources() {
        Map<String, Map<String, String>> result = new LinkedHashMap<>(dataSourceRules.size(), 1);
        for (ReadwriteSplittingDataSourceRule each : dataSourceRules.values()) {
            if (each.getReadwriteSplittingStrategy() instanceof DynamicReadwriteSplittingStrategy) {
                Map<String, String> exportedDataSources = new LinkedHashMap<>(2, 1);
                exportedDataSources.put(ExportableItemConstants.AUTO_AWARE_DATA_SOURCE_NAME, ((DynamicReadwriteSplittingStrategy) each.getReadwriteSplittingStrategy()).getAutoAwareDataSourceName());
                exportedDataSources.put(ExportableItemConstants.PRIMARY_DATA_SOURCE_NAME, each.getWriteDataSource());
                exportedDataSources.put(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES, String.join(",", each.getReadwriteSplittingStrategy().getReadDataSources()));
                result.put(each.getName(), exportedDataSources);
            }
        }
        return result;
    }
    
    private Map<String, Map<String, String>> exportStaticDataSources() {
        Map<String, Map<String, String>> result = new LinkedHashMap<>(dataSourceRules.size(), 1);
        for (ReadwriteSplittingDataSourceRule each : dataSourceRules.values()) {
            if (each.getReadwriteSplittingStrategy() instanceof StaticReadwriteSplittingStrategy) {
                Map<String, String> exportedDataSources = new LinkedHashMap<>(2, 1);
                exportedDataSources.put(ExportableItemConstants.PRIMARY_DATA_SOURCE_NAME, each.getWriteDataSource());
                exportedDataSources.put(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES, String.join(",", each.getReadwriteSplittingStrategy().getReadDataSources()));
                result.put(each.getName(), exportedDataSources);
            }
        }
        return result;
    }
    
    @Override
    public String getType() {
        return ReadwriteSplittingRule.class.getSimpleName();
    }
}
