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

import org.apache.shardingsphere.distsql.handler.ral.query.MetaDataRequiredQueryableRALExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.distsql.statement.queryable.ShowSQLFederationRuleStatement;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show SQL federation rule executor.
 */
public final class ShowSQLFederationRuleExecutor implements MetaDataRequiredQueryableRALExecutor<ShowSQLFederationRuleStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereMetaData metaData, final ShowSQLFederationRuleStatement sqlStatement) {
        SQLFederationRuleConfiguration ruleConfig = metaData.getGlobalRuleMetaData().getSingleRule(SQLFederationRule.class).getConfiguration();
        return Collections.singleton(new LocalDataQueryResultRow(String.valueOf(ruleConfig.isSqlFederationEnabled()),
                null != ruleConfig.getExecutionPlanCache() ? ruleConfig.getExecutionPlanCache().toString() : ""));
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("sql_federation_enabled", "execution_plan_cache");
    }
    
    @Override
    public Class<ShowSQLFederationRuleStatement> getType() {
        return ShowSQLFederationRuleStatement.class;
    }
}
