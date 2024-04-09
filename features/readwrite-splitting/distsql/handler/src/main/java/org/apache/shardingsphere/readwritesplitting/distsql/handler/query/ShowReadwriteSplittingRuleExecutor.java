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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.query;

import com.google.common.base.Joiner;
import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.rule.attribute.exportable.ExportableRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.infra.rule.attribute.exportable.constant.ExportableItemConstants;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Show readwrite-splitting rule executor.
 */
@Setter
public final class ShowReadwriteSplittingRuleExecutor implements DistSQLQueryExecutor<ShowReadwriteSplittingRulesStatement>, DistSQLExecutorRuleAware<ReadwriteSplittingRule> {
    
    private ReadwriteSplittingRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowReadwriteSplittingRulesStatement sqlStatement) {
        return Arrays.asList("name", "write_storage_unit_name", "read_storage_unit_names", "transactional_read_query_strategy", "load_balancer_type", "load_balancer_props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowReadwriteSplittingRulesStatement sqlStatement, final ContextManager contextManager) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        Map<String, Map<String, String>> exportableDataSourceMap = getExportableDataSourceMap(rule);
        ReadwriteSplittingRuleConfiguration ruleConfig = rule.getConfiguration();
        ruleConfig.getDataSources().forEach(each -> {
            LocalDataQueryResultRow dataItem = buildDataItem(exportableDataSourceMap, each, getLoadBalancers(ruleConfig));
            if (null == sqlStatement.getRuleName() || sqlStatement.getRuleName().equalsIgnoreCase(each.getName())) {
                result.add(dataItem);
            }
        });
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> getExportableDataSourceMap(final ReadwriteSplittingRule rule) {
        return (Map<String, Map<String, String>>) rule.getAttributes().getAttribute(ExportableRuleAttribute.class).getExportData().get(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE);
    }
    
    private LocalDataQueryResultRow buildDataItem(final Map<String, Map<String, String>> exportableDataSourceMap,
                                                  final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig, final Map<String, AlgorithmConfiguration> loadBalancers) {
        String name = dataSourceRuleConfig.getName();
        Map<String, String> exportDataSources = exportableDataSourceMap.get(name);
        Optional<AlgorithmConfiguration> loadBalancer = Optional.ofNullable(loadBalancers.get(dataSourceRuleConfig.getLoadBalancerName()));
        return new LocalDataQueryResultRow(name,
                getWriteDataSourceName(dataSourceRuleConfig, exportDataSources),
                getReadDataSourceNames(dataSourceRuleConfig, exportDataSources),
                dataSourceRuleConfig.getTransactionalReadQueryStrategy().name(),
                loadBalancer.map(AlgorithmConfiguration::getType).orElse(null),
                loadBalancer.map(AlgorithmConfiguration::getProps).orElse(null));
    }
    
    private Map<String, AlgorithmConfiguration> getLoadBalancers(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        Map<String, AlgorithmConfiguration> loadBalancers = ruleConfig.getLoadBalancers();
        return null == loadBalancers ? Collections.emptyMap() : loadBalancers;
    }
    
    private String getWriteDataSourceName(final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig, final Map<String, String> exportDataSources) {
        return null == exportDataSources ? dataSourceRuleConfig.getWriteDataSourceName() : exportDataSources.get(ExportableItemConstants.PRIMARY_DATA_SOURCE_NAME);
    }
    
    private String getReadDataSourceNames(final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig, final Map<String, String> exportDataSources) {
        return null == exportDataSources ? Joiner.on(",").join(dataSourceRuleConfig.getReadDataSourceNames()) : exportDataSources.get(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES);
    }
    
    @Override
    public Class<ReadwriteSplittingRule> getRuleClass() {
        return ReadwriteSplittingRule.class;
    }
    
    @Override
    public Class<ShowReadwriteSplittingRulesStatement> getType() {
        return ShowReadwriteSplittingRulesStatement.class;
    }
}
