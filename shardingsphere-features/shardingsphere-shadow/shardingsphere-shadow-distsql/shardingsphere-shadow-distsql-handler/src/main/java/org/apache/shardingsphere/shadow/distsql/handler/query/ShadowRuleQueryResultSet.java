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

import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowRulesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Result set for show shadow rules.
 */
public final class ShadowRuleQueryResultSet implements DistSQLResultSet {
    
    private static final String RULE_NAME = "rule_name";
    
    private static final String SOURCE_NAME = "source_name";
    
    private static final String SHADOW_NAME = "shadow_name";
    
    private static final String SHADOW_TABLE = "shadow_table";
    
    private Iterator<Map<String, String>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Optional<ShadowRuleConfiguration> ruleConfigurations = metaData.getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof ShadowRuleConfiguration).map(each -> (ShadowRuleConfiguration) each).findAny();
        ruleConfigurations.ifPresent(configuration -> buildDataSourceIterator(configuration, (ShowShadowRulesStatement) sqlStatement));
    }
    
    private void buildDataSourceIterator(final ShadowRuleConfiguration configuration, final ShowShadowRulesStatement sqlStatement) {
        Map<String, Map<String, ShadowTableConfiguration>> dataSourceTableMap = convertToDataSourceTableMap(configuration.getTables());
        Collection<Entry<String, ShadowDataSourceConfiguration>> specifiedConfigurations = !isSpecified(sqlStatement) ? configuration.getDataSources().entrySet()
                : configuration.getDataSources().entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(sqlStatement.getRuleName())).collect(Collectors.toList());
        data = specifiedConfigurations.stream().map(each -> buildDataItem(each, dataSourceTableMap)).collect(Collectors.toList()).iterator();
    }
    
    private Map<String, Map<String, ShadowTableConfiguration>> convertToDataSourceTableMap(final Map<String, ShadowTableConfiguration> tables) {
        return tables.entrySet().stream().collect(Collectors.groupingBy(entry -> entry.getValue().getDataSourceName(), Collectors.toMap(Entry::getKey, Entry::getValue)));
    }
    
    private boolean isSpecified(final ShowShadowRulesStatement sqlStatement) {
        return null != sqlStatement.getRuleName() && !sqlStatement.getRuleName().isEmpty();
    }
    
    private Map<String, String> buildDataItem(final Entry<String, ShadowDataSourceConfiguration> dataSource, final Map<String, Map<String, ShadowTableConfiguration>> dataSourceTableMap) {
        Map<String, String> result = convertToDataSourceMap(dataSource);
        Map<String, ShadowTableConfiguration> dataSourceTable = dataSourceTableMap.getOrDefault(result.get(RULE_NAME), Collections.emptyMap());
        result.put(SHADOW_TABLE, convertToString(dataSourceTable.keySet()));
        return result;
    }
    
    private Map<String, String> convertToDataSourceMap(final Entry<String, ShadowDataSourceConfiguration> dataSource) {
        Map<String, String> result = new HashMap<>();
        result.put(RULE_NAME, dataSource.getKey());
        result.put(SOURCE_NAME, dataSource.getValue().getSourceDataSourceName());
        result.put(SHADOW_NAME, dataSource.getValue().getShadowDataSourceName());
        return result;
    }
    
    private String convertToString(final Collection<String> shadowTables) {
        if (null != shadowTables) {
            return String.join(",", shadowTables);
        }
        return "";
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(RULE_NAME, SOURCE_NAME, SHADOW_NAME, SHADOW_TABLE);
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return buildRowData(data.next());
    }
    
    private Collection<Object> buildRowData(final Map<String, String> data) {
        return Arrays.asList(data.get(RULE_NAME), data.get(SOURCE_NAME), data.get(SHADOW_NAME), data.getOrDefault(SHADOW_TABLE, ""));
    }
    
    @Override
    public String getType() {
        return ShowShadowRulesStatement.class.getCanonicalName();
    }
}
