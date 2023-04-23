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

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.props.PropertiesConverter;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowRulesStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Show shadow rule executor.
 */
public final class ShowShadowRuleExecutor implements RQLExecutor<ShowShadowRulesStatement> {
    
    private static final String RULE_NAME = "rule_name";
    
    private static final String SOURCE_NAME = "source_name";
    
    private static final String SHADOW_NAME = "shadow_name";
    
    private static final String SHADOW_TABLE = "shadow_table";
    
    private static final String ALGORITHM_TYPE = "algorithm_type";
    
    private static final String ALGORITHM_PROPS = "algorithm_props";
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowShadowRulesStatement sqlStatement) {
        Optional<ShadowRule> rule = database.getRuleMetaData().findSingleRule(ShadowRule.class);
        rule.ifPresent(optional -> buildDataSourceIterator((ShadowRuleConfiguration) optional.getConfiguration(), sqlStatement));
        Iterator<Map<String, String>> data = Collections.emptyIterator();
        if (rule.isPresent()) {
            data = buildDataSourceIterator((ShadowRuleConfiguration) rule.get().getConfiguration(), sqlStatement);
        }
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        while (data.hasNext()) {
            Map<String, String> row = data.next();
            result.add(new LocalDataQueryResultRow(row.get(SHADOW_TABLE), row.get(RULE_NAME), row.get(SOURCE_NAME), row.get(SHADOW_NAME), row.get(ALGORITHM_TYPE), row.get(ALGORITHM_PROPS)));
        }
        return result;
    }
    
    private Iterator<Map<String, String>> buildDataSourceIterator(final ShadowRuleConfiguration ruleConfig, final ShowShadowRulesStatement sqlStatement) {
        Map<String, Map<String, ShadowTableConfiguration>> dataSourceTableMap = convertToDataSourceTableMap(ruleConfig.getTables());
        Collection<ShadowDataSourceConfiguration> specifiedConfigs = !isSpecified(sqlStatement) ? ruleConfig.getDataSources()
                : ruleConfig.getDataSources().stream().filter(each -> each.getName().equalsIgnoreCase(sqlStatement.getRuleName())).collect(Collectors.toList());
        Collection<Map<String, String>> result = new LinkedList<>();
        specifiedConfigs.forEach(each -> result.addAll(buildDataItems(each, dataSourceTableMap, ruleConfig.getShadowAlgorithms())));
        return result.iterator();
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
    
    private Collection<Map<String, String>> buildDataItems(final ShadowDataSourceConfiguration dataSourceConfig, final Map<String, Map<String, ShadowTableConfiguration>> dataSourceTableMap,
                                                           final Map<String, AlgorithmConfiguration> algorithmConfigs) {
        Map<String, ShadowTableConfiguration> dataSourceTable = dataSourceTableMap.getOrDefault(dataSourceConfig.getName(), Collections.emptyMap());
        Collection<Map<String, String>> result = new LinkedList<>();
        dataSourceTable.forEach((key, value) -> value.getShadowAlgorithmNames().forEach(each -> result.add(buildDataItem(dataSourceConfig, algorithmConfigs.get(each), key))));
        return result;
    }
    
    private Map<String, String> buildDataItem(final ShadowDataSourceConfiguration dataSourceConfig, final AlgorithmConfiguration algorithmConfig, final String tableName) {
        Map<String, String> result = new HashMap<>(6, 1);
        result.put(RULE_NAME, dataSourceConfig.getName());
        result.put(SOURCE_NAME, dataSourceConfig.getProductionDataSourceName());
        result.put(SHADOW_NAME, dataSourceConfig.getShadowDataSourceName());
        result.put(ALGORITHM_TYPE, algorithmConfig.getType());
        result.put(ALGORITHM_PROPS, PropertiesConverter.convert(algorithmConfig.getProps()));
        result.put(SHADOW_TABLE, tableName);
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(SHADOW_TABLE, RULE_NAME, SOURCE_NAME, SHADOW_NAME, ALGORITHM_TYPE, ALGORITHM_PROPS);
    }
    
    @Override
    public String getType() {
        return ShowShadowRulesStatement.class.getName();
    }
}
