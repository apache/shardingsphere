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

import org.apache.shardingsphere.infra.config.TypedSPIConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.properties.PropertiesConverter;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Result set for show readwrite splitting rule.
 */
public final class ReadwriteSplittingRuleQueryResultSet implements DistSQLResultSet {
    
    private Iterator<Collection<Object>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Optional<ReadwriteSplittingRuleConfiguration> ruleConfig = metaData.getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof ReadwriteSplittingRuleConfiguration).map(each -> (ReadwriteSplittingRuleConfiguration) each).findAny();
        ruleConfig.map(optional -> optional.getDataSources().iterator()).orElseGet(Collections::emptyIterator);
        Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers = ruleConfig.map(ReadwriteSplittingRuleConfiguration::getLoadBalancers).orElse(Collections.emptyMap());
        Optional<ExportableRule> exportableRule = getExportableRule(metaData);
        ruleConfig.ifPresent(op -> {
            Collection<Collection<Object>> rows = new LinkedList<>();
            Map<String, Map<String, String>> autoAwareDataSourceMap = Collections.emptyMap();
            Map<String, Map<String, String>> dataSourceMap = Collections.emptyMap();
            if (exportableRule.isPresent()) {
                Map<String, Object> exportable = exportableRule.get().export(Arrays.asList(ExportableConstants.EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE,
                        ExportableConstants.EXPORTABLE_KEY_DATA_SOURCE));
                autoAwareDataSourceMap = (Map<String, Map<String, String>>) exportable.getOrDefault(ExportableConstants.EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE, Collections.emptyMap());
                dataSourceMap = (Map<String, Map<String, String>>) exportable.getOrDefault(ExportableConstants.EXPORTABLE_KEY_DATA_SOURCE, Collections.emptyMap());
            }
            for (ReadwriteSplittingDataSourceRuleConfiguration each : op.getDataSources()) {
                Properties props = each.getProps();
                String autoWareDataSourceName = props.getProperty("auto-aware-data-source-name");
                String writeDataSourceName = props.getProperty("write-data-source-name");
                String readDataSourceNames = props.getProperty("read-data-source-name");
                Optional<ShardingSphereAlgorithmConfiguration> loadBalancer = Optional.ofNullable(loadBalancers.get(each.getLoadBalancerName()));
                Map<String, String> exportDataSources = "Dynamic".equals(each.getType())
                        ? autoAwareDataSourceMap.get(each.getName()) : dataSourceMap.get(each.getName());
                if (null != exportDataSources && !exportDataSources.isEmpty()) {
                    writeDataSourceName = exportDataSources.get(ExportableConstants.PRIMARY_DATA_SOURCE_NAME);
                    readDataSourceNames = exportDataSources.get(ExportableConstants.REPLICA_DATA_SOURCE_NAMES);
                }
                rows.add(Arrays.asList(each.getName(), autoWareDataSourceName, writeDataSourceName, readDataSourceNames,
                        loadBalancer.map(TypedSPIConfiguration::getType).orElse(null),
                        loadBalancer.map(TypedSPIConfiguration::getProps).map(PropertiesConverter::convert).orElse("")));
            }
            data = rows.iterator();
        });
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "auto_aware_data_source_name", "write_data_source_name", "read_data_source_names", "load_balancer_type", "load_balancer_props");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return ShowReadwriteSplittingRulesStatement.class.getCanonicalName();
    }
    
    private Optional<ExportableRule> getExportableRule(final ShardingSphereMetaData metaData) {
        return metaData.getRuleMetaData().getRules().stream()
                .filter(each -> each instanceof ExportableRule).map(each -> (ExportableRule) each)
                .filter(each -> each.containExportableKey(Collections.singletonList(ExportableConstants.EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE))).findAny();
    }
}
