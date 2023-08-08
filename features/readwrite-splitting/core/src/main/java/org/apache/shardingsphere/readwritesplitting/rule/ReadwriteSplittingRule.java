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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.rule.event.DataSourceStatusChangedEvent;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StorageConnectorReusableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableItemConstants;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.core.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSourceChangedEvent;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSourceDeletedEvent;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.exception.rule.InvalidInlineExpressionDataSourceNameException;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.group.type.StaticReadwriteSplittingGroup;

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
    
    private final String databaseName;
    
    @Getter
    private final RuleConfiguration configuration;
    
    private final Map<String, ReadQueryLoadBalanceAlgorithm> loadBalancers;
    
    private final Map<String, ReadwriteSplittingDataSourceRule> dataSourceRules;
    
    private final InstanceContext instanceContext;
    
    public ReadwriteSplittingRule(final String databaseName, final ReadwriteSplittingRuleConfiguration ruleConfig, final InstanceContext instanceContext) {
        this.databaseName = databaseName;
        this.instanceContext = instanceContext;
        configuration = ruleConfig;
        loadBalancers = createLoadBalancers(ruleConfig);
        dataSourceRules = createDataSourceRules(ruleConfig);
    }
    
    private Map<String, ReadQueryLoadBalanceAlgorithm> createLoadBalancers(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        Map<String, ReadQueryLoadBalanceAlgorithm> result = new LinkedHashMap<>(ruleConfig.getDataSources().size(), 1F);
        for (ReadwriteSplittingDataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
            if (ruleConfig.getLoadBalancers().containsKey(each.getLoadBalancerName())) {
                AlgorithmConfiguration algorithmConfig = ruleConfig.getLoadBalancers().get(each.getLoadBalancerName());
                result.put(each.getName() + "." + each.getLoadBalancerName(), TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class, algorithmConfig.getType(), algorithmConfig.getProps()));
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
        ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = loadBalancers.getOrDefault(
                config.getName() + "." + config.getLoadBalancerName(), TypedSPILoader.getService(ReadQueryLoadBalanceAlgorithm.class, null));
        return createStaticDataSourceRules(config, loadBalanceAlgorithm);
    }
    
    private Map<String, ReadwriteSplittingDataSourceRule> createStaticDataSourceRules(final ReadwriteSplittingDataSourceRuleConfiguration config,
                                                                                      final ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm) {
        List<String> inlineReadwriteDataSourceNames = InlineExpressionParserFactory.newInstance().splitAndEvaluate(config.getName());
        List<String> inlineWriteDatasourceNames = InlineExpressionParserFactory.newInstance().splitAndEvaluate(config.getWriteDataSourceName());
        List<List<String>> inlineReadDatasourceNames = config.getReadDataSourceNames().stream()
                .map(each -> InlineExpressionParserFactory.newInstance().splitAndEvaluate(each)).collect(Collectors.toList());
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
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>();
        for (Entry<String, ReadwriteSplittingDataSourceRule> entry : dataSourceRules.entrySet()) {
            result.put(entry.getValue().getName(), entry.getValue().getReadwriteSplittingGroup().getAllDataSources());
        }
        return result;
    }
    
    @Override
    public void updateStatus(final DataSourceStatusChangedEvent event) {
        StorageNodeDataSourceChangedEvent dataSourceEvent = (StorageNodeDataSourceChangedEvent) event;
        QualifiedDatabase qualifiedDatabase = dataSourceEvent.getQualifiedDatabase();
        ReadwriteSplittingDataSourceRule dataSourceRule = dataSourceRules.get(qualifiedDatabase.getGroupName());
        Preconditions.checkNotNull(dataSourceRule, "Can not find readwrite-splitting data source rule in database `%s`", qualifiedDatabase.getDatabaseName());
        if (DataSourceState.DISABLED == dataSourceEvent.getDataSource().getStatus()) {
            dataSourceRule.disableDataSource(dataSourceEvent.getQualifiedDatabase().getDataSourceName());
        } else {
            dataSourceRule.enableDataSource(dataSourceEvent.getQualifiedDatabase().getDataSourceName());
        }
    }
    
    @Override
    public void cleanStorageNodeDataSource(final String groupName) {
        Preconditions.checkNotNull(dataSourceRules.get(groupName), String.format("`%s` group name not exist in database `%s`", groupName, databaseName));
        deleteStorageNodeDataSources(dataSourceRules.get(groupName));
    }
    
    private void deleteStorageNodeDataSources(final ReadwriteSplittingDataSourceRule rule) {
        rule.getReadwriteSplittingGroup().getReadDataSources()
                .forEach(each -> instanceContext.getEventBusContext().post(new StorageNodeDataSourceDeletedEvent(new QualifiedDatabase(databaseName, rule.getName(), each))));
    }
    
    @Override
    public void cleanStorageNodeDataSources() {
        for (Entry<String, ReadwriteSplittingDataSourceRule> entry : dataSourceRules.entrySet()) {
            deleteStorageNodeDataSources(entry.getValue());
        }
    }
    
    @Override
    public Map<String, Object> getExportData() {
        Map<String, Object> result = new HashMap<>(2, 1F);
        result.put(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE, exportStaticDataSources());
        return result;
    }
    
    private Map<String, Map<String, String>> exportStaticDataSources() {
        Map<String, Map<String, String>> result = new LinkedHashMap<>(dataSourceRules.size(), 1F);
        for (ReadwriteSplittingDataSourceRule each : dataSourceRules.values()) {
            if (each.getReadwriteSplittingGroup() instanceof StaticReadwriteSplittingGroup) {
                Map<String, String> exportedDataSources = new LinkedHashMap<>(2, 1F);
                exportedDataSources.put(ExportableItemConstants.PRIMARY_DATA_SOURCE_NAME, each.getWriteDataSource());
                exportedDataSources.put(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES, String.join(",", each.getReadwriteSplittingGroup().getReadDataSources()));
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
