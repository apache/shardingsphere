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

package org.apache.shardingsphere.sqlfederation.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.type.rql.aware.GlobalRuleAwareRQLExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.distsql.statement.queryable.ShowSQLFederationRuleStatement;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show SQL federation rule executor.
 */
@Setter
public final class ShowSQLFederationRuleExecutor implements GlobalRuleAwareRQLExecutor<ShowSQLFederationRuleStatement, SQLFederationRule> {
    
    private SQLFederationRule rule;
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("sql_federation_enabled", "all_query_use_sql_federation", "execution_plan_cache");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowSQLFederationRuleStatement sqlStatement) {
        SQLFederationRuleConfiguration ruleConfig = rule.getConfiguration();
        String sqlFederationEnabled = String.valueOf(ruleConfig.isSqlFederationEnabled());
        String allQueryUseSQLFederation = String.valueOf(ruleConfig.isAllQueryUseSQLFederation());
        String executionPlanCache = null != ruleConfig.getExecutionPlanCache() ? ruleConfig.getExecutionPlanCache().toString() : "";
        LocalDataQueryResultRow row = new LocalDataQueryResultRow(sqlFederationEnabled, allQueryUseSQLFederation, executionPlanCache);
        return Collections.singleton(row);
    }
    
    @Override
    public Class<SQLFederationRule> getRuleClass() {
        return SQLFederationRule.class;
    }
    
    @Override
    public Class<ShowSQLFederationRuleStatement> getType() {
        return ShowSQLFederationRuleStatement.class;
    }
}
