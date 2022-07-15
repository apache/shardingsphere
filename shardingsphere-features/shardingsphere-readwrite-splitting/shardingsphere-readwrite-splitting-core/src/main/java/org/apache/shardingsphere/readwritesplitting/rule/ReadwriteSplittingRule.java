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
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
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
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.metadata.storage.event.StorageNodeDataSourceChangedEvent;
import org.apache.shardingsphere.readwritesplitting.algorithm.config.AlgorithmProvidedReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.factory.ReadQueryLoadBalanceAlgorithmFactory;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.strategy.type.DynamicReadwriteSplittingStrategy;
import org.apache.shardingsphere.readwritesplitting.strategy.type.StaticReadwriteSplittingStrategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

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
        Preconditions.checkArgument(!ruleConfig.getDataSources().isEmpty(), "Replica query data source rules can not be empty.");
        ruleConfig.getLoadBalancers().forEach((key, value) -> loadBalancers.put(key, ReadQueryLoadBalanceAlgorithmFactory.newInstance(value)));
        dataSourceRules = new HashMap<>(ruleConfig.getDataSources().size(), 1);
        for (ReadwriteSplittingDataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
            // TODO check if can not find load balancer should throw exception.
            ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = Strings.isNullOrEmpty(each.getLoadBalancerName()) || !loadBalancers.containsKey(each.getLoadBalancerName())
                    ? ReadQueryLoadBalanceAlgorithmFactory.newInstance()
                    : loadBalancers.get(each.getLoadBalancerName());
            dataSourceRules.put(each.getName(), new ReadwriteSplittingDataSourceRule(each, loadBalanceAlgorithm, builtRules));
        }
    }
    
    public ReadwriteSplittingRule(final AlgorithmProvidedReadwriteSplittingRuleConfiguration ruleConfig, final Collection<ShardingSphereRule> builtRules) {
        configuration = ruleConfig;
        Preconditions.checkArgument(!ruleConfig.getDataSources().isEmpty(), "Replica query data source rules can not be empty.");
        loadBalancers.putAll(ruleConfig.getLoadBalanceAlgorithms());
        dataSourceRules = new HashMap<>(ruleConfig.getDataSources().size(), 1);
        for (ReadwriteSplittingDataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
            // TODO check if can not find load balancer should throw exception.
            ReadQueryLoadBalanceAlgorithm loadBalanceAlgorithm = Strings.isNullOrEmpty(each.getLoadBalancerName()) || !loadBalancers.containsKey(each.getLoadBalancerName())
                    ? ReadQueryLoadBalanceAlgorithmFactory.newInstance()
                    : loadBalancers.get(each.getLoadBalancerName());
            dataSourceRules.put(each.getName(), new ReadwriteSplittingDataSourceRule(each, loadBalanceAlgorithm, builtRules));
        }
    }
    
    /**
     * Get single data source rule.
     *
     * @return replica query data source rule
     */
    public ReadwriteSplittingDataSourceRule getSingleDataSourceRule() {
        return dataSourceRules.values().iterator().next();
    }
    
    /**
     * Find data source rule.
     *
     * @param dataSourceName data source name
     * @return replica query data source rule
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
        Preconditions.checkState(null != dataSourceRule, "Can 't find readwrite-splitting data source rule in database `%s`.", qualifiedDatabase.getDatabaseName());
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
