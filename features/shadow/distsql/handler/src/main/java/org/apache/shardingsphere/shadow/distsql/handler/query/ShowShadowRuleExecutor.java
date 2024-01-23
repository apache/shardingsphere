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

package org.apache.shardingsphere.shadow.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.type.rql.aware.DatabaseRuleAwareRQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.props.PropertiesConverter;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.statement.ShowShadowRulesStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Show shadow rule executor.
 */
@Setter
public final class ShowShadowRuleExecutor implements DatabaseRuleAwareRQLExecutor<ShowShadowRulesStatement, ShadowRule> {
    
    private ShadowRule rule;
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("shadow_table", "rule_name", "source_name", "shadow_name", "algorithm_type", "algorithm_props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowShadowRulesStatement sqlStatement) {
        Map<String, Map<String, ShadowTableConfiguration>> dataSourceTableMap = convertToDataSourceTableMap(rule.getConfiguration().getTables());
        Collection<ShadowDataSourceConfiguration> specifiedConfigs = isSpecified(sqlStatement)
                ? rule.getConfiguration().getDataSources().stream().filter(each -> each.getName().equalsIgnoreCase(sqlStatement.getRuleName())).collect(Collectors.toList())
                : rule.getConfiguration().getDataSources();
        return specifiedConfigs.stream()
                .map(each -> buildColumnData(each, dataSourceTableMap, rule.getConfiguration().getShadowAlgorithms())).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private Map<String, Map<String, ShadowTableConfiguration>> convertToDataSourceTableMap(final Map<String, ShadowTableConfiguration> tables) {
        Map<String, Map<String, ShadowTableConfiguration>> result = tables.values().stream().map(ShadowTableConfiguration::getDataSourceNames)
                .flatMap(Collection::stream).distinct().collect(Collectors.toMap(each -> each, each -> new LinkedHashMap<>()));
        tables.forEach((key, value) -> value.getDataSourceNames().forEach(each -> result.get(each).put(key, value)));
        return result;
    }
    
    private boolean isSpecified(final ShowShadowRulesStatement sqlStatement) {
        return null != sqlStatement.getRuleName() && !sqlStatement.getRuleName().isEmpty();
    }
    
    private Collection<LocalDataQueryResultRow> buildColumnData(final ShadowDataSourceConfiguration dataSourceConfig, final Map<String, Map<String, ShadowTableConfiguration>> dataSourceTableMap,
                                                                final Map<String, AlgorithmConfiguration> algorithmConfigs) {
        Map<String, ShadowTableConfiguration> dataSourceTable = dataSourceTableMap.getOrDefault(dataSourceConfig.getName(), Collections.emptyMap());
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        dataSourceTable.forEach((key, value) -> value.getShadowAlgorithmNames().forEach(each -> {
            AlgorithmConfiguration algorithmConfig = algorithmConfigs.get(each);
            result.add(new LocalDataQueryResultRow(Arrays.asList(key, dataSourceConfig.getName(), dataSourceConfig.getProductionDataSourceName(), dataSourceConfig.getShadowDataSourceName(),
                    algorithmConfig.getType(), PropertiesConverter.convert(algorithmConfig.getProps()))));
        }));
        return result;
    }
    
    @Override
    public Class<ShadowRule> getRuleClass() {
        return ShadowRule.class;
    }
    
    @Override
    public Class<ShowShadowRulesStatement> getType() {
        return ShowShadowRulesStatement.class;
    }
}
