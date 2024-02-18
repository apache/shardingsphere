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
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.statement.ShowShadowTableRulesStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Show shadow table rules executor.
 */
@Setter
public final class ShowShadowTableRulesExecutor implements DistSQLQueryExecutor<ShowShadowTableRulesStatement>, DistSQLExecutorRuleAware<ShadowRule> {
    
    private static final String SHADOW_TABLE = "shadow_table";
    
    private static final String SHADOW_ALGORITHM_NAME = "shadow_algorithm_name";
    
    private ShadowRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowShadowTableRulesStatement sqlStatement) {
        return Arrays.asList(SHADOW_TABLE, SHADOW_ALGORITHM_NAME);
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowShadowTableRulesStatement sqlStatement, final ContextManager contextManager) {
        return buildData(rule.getConfiguration(), sqlStatement).stream()
                .map(each -> new LocalDataQueryResultRow(each.get(SHADOW_TABLE), each.get(SHADOW_ALGORITHM_NAME))).collect(Collectors.toList());
    }
    
    private Collection<Map<String, String>> buildData(final ShadowRuleConfiguration ruleConfig, final ShowShadowTableRulesStatement sqlStatement) {
        Collection<Map<String, String>> result = new ArrayList<>();
        if (isSpecified(sqlStatement)) {
            ruleConfig.getTables().forEach((key, value) -> {
                Map<String, String> map = new HashMap<>();
                if (key.equalsIgnoreCase(sqlStatement.getTableName())) {
                    map.put(SHADOW_TABLE, key);
                    map.put(SHADOW_ALGORITHM_NAME, convertToString(value.getShadowAlgorithmNames()));
                }
                result.add(map);
            });
        } else {
            ruleConfig.getTables().forEach((key, value) -> {
                Map<String, String> map = new HashMap<>();
                map.put(SHADOW_TABLE, key);
                map.put(SHADOW_ALGORITHM_NAME, convertToString(value.getShadowAlgorithmNames()));
                result.add(map);
            });
        }
        return result;
    }
    
    private boolean isSpecified(final ShowShadowTableRulesStatement sqlStatement) {
        return null != sqlStatement.getTableName() && !sqlStatement.getTableName().isEmpty();
    }
    
    private String convertToString(final Collection<String> shadowTables) {
        return null == shadowTables ? "" : String.join(",", shadowTables);
    }
    
    @Override
    public Class<ShadowRule> getRuleClass() {
        return ShadowRule.class;
    }
    
    @Override
    public Class<ShowShadowTableRulesStatement> getType() {
        return ShowShadowTableRulesStatement.class;
    }
}
