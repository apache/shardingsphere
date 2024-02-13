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

package org.apache.shardingsphere.sqltranslator.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.distsql.statement.queryable.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show SQL translator rule executor.
 */
@Setter
public final class ShowSQLTranslatorRuleExecutor implements DistSQLQueryExecutor<ShowSQLTranslatorRuleStatement>, DistSQLExecutorRuleAware<SQLTranslatorRule> {
    
    private SQLTranslatorRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowSQLTranslatorRuleStatement sqlStatement) {
        return Arrays.asList("type", "props", "use_original_sql_when_translating_failed");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowSQLTranslatorRuleStatement sqlStatement, final ContextManager contextManager) {
        SQLTranslatorRuleConfiguration ruleConfig = rule.getConfiguration();
        return Collections.singleton(new LocalDataQueryResultRow(ruleConfig.getType(), ruleConfig.getProps(), ruleConfig.isUseOriginalSQLWhenTranslatingFailed()));
    }
    
    @Override
    public Class<SQLTranslatorRule> getRuleClass() {
        return SQLTranslatorRule.class;
    }
    
    @Override
    public Class<ShowSQLTranslatorRuleStatement> getType() {
        return ShowSQLTranslatorRuleStatement.class;
    }
}
