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
import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableItemConstants;
import org.apache.shardingsphere.infra.util.props.PropertiesConverter;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Show readwrite splitting rule executor.
 */
public final class ShowReadwriteSplittingRuleExecutor implements RQLExecutor<ShowReadwriteSplittingRulesStatement> {
    
    private Map<String, Map<String, String>> exportableAutoAwareDataSource = Collections.emptyMap();
    
    private Map<String, Map<String, String>> exportableDataSourceMap = Collections.emptyMap();
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowReadwriteSplittingRulesStatement sqlStatement) {
        Optional<ReadwriteSplittingRule> rule = database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class);
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        if (rule.isPresent()) {
            buildExportableMap(rule.get());
            result = buildData(rule.get(), sqlStatement);
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private void buildExportableMap(final ReadwriteSplittingRule rule) {
        Map<String, Object> exportedData = rule.getExportData();
        exportableAutoAwareDataSource = (Map<String, Map<String, String>>) exportedData.get(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE);
        exportableDataSourceMap = (Map<String, Map<String, String>>) exportedData.get(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE);
    }
    
    private Collection<LocalDataQueryResultRow> buildData(final ReadwriteSplittingRule rule, final ShowReadwriteSplittingRulesStatement sqlStatement) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        ((ReadwriteSplittingRuleConfiguration) rule.getConfiguration()).getDataSources().forEach(each -> {
            LocalDataQueryResultRow dataItem = buildDataItem(each, getLoadBalancers((ReadwriteSplittingRuleConfiguration) rule.getConfiguration()));
            if (null == sqlStatement.getRuleName() || sqlStatement.getRuleName().equalsIgnoreCase(each.getName())) {
                result.add(dataItem);
            }
        });
        return result;
    }
    
    private LocalDataQueryResultRow buildDataItem(final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig, final Map<String, AlgorithmConfiguration> loadBalancers) {
        String name = dataSourceRuleConfig.getName();
        Map<String, String> exportDataSources = null == dataSourceRuleConfig.getDynamicStrategy() ? exportableDataSourceMap.get(name) : exportableAutoAwareDataSource.get(name);
        Optional<AlgorithmConfiguration> loadBalancer = Optional.ofNullable(loadBalancers.get(dataSourceRuleConfig.getLoadBalancerName()));
        return new LocalDataQueryResultRow(name,
                getAutoAwareDataSourceName(dataSourceRuleConfig),
                getWriteDataSourceName(dataSourceRuleConfig, exportDataSources),
                getReadDataSourceNames(dataSourceRuleConfig, exportDataSources),
                loadBalancer.map(AlgorithmConfiguration::getType).orElse(""),
                loadBalancer.map(each -> PropertiesConverter.convert(each.getProps())).orElse(""));
    }
    
    private Map<String, AlgorithmConfiguration> getLoadBalancers(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        Map<String, AlgorithmConfiguration> loadBalancers = ruleConfig.getLoadBalancers();
        return null == loadBalancers ? Collections.emptyMap() : loadBalancers;
    }
    
    private String getAutoAwareDataSourceName(final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig) {
        return null == dataSourceRuleConfig.getDynamicStrategy() ? "" : dataSourceRuleConfig.getDynamicStrategy().getAutoAwareDataSourceName();
    }
    
    private String getWriteDataSourceName(final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig, final Map<String, String> exportDataSources) {
        return null == exportDataSources ? dataSourceRuleConfig.getStaticStrategy().getWriteDataSourceName() : exportDataSources.get(ExportableItemConstants.PRIMARY_DATA_SOURCE_NAME);
    }
    
    private String getReadDataSourceNames(final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig, final Map<String, String> exportDataSources) {
        return null == exportDataSources
                ? Joiner.on(",").join(dataSourceRuleConfig.getStaticStrategy().getReadDataSourceNames())
                : exportDataSources.get(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES);
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "auto_aware_data_source_name", "write_storage_unit_name", "read_storage_unit_names", "load_balancer_type", "load_balancer_props");
    }
    
    @Override
    public String getType() {
        return ShowReadwriteSplittingRulesStatement.class.getName();
    }
}
