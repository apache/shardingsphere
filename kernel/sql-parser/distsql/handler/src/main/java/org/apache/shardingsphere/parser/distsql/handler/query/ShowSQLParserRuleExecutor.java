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

package org.apache.shardingsphere.parser.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.distsql.statement.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.parser.rule.SQLParserRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show SQL parser rule executor.
 */
@Setter
public final class ShowSQLParserRuleExecutor implements DistSQLQueryExecutor<ShowSQLParserRuleStatement>, DistSQLExecutorRuleAware<SQLParserRule> {
    
    private SQLParserRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowSQLParserRuleStatement sqlStatement) {
        return Arrays.asList("parse_tree_cache", "sql_statement_cache");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowSQLParserRuleStatement sqlStatement, final ContextManager contextManager) {
        SQLParserRuleConfiguration ruleConfig = rule.getConfiguration();
        return Collections.singleton(new LocalDataQueryResultRow(null != ruleConfig.getParseTreeCache() ? ruleConfig.getParseTreeCache().toString() : "",
                null != ruleConfig.getSqlStatementCache() ? ruleConfig.getSqlStatementCache().toString() : ""));
    }
    
    @Override
    public Class<SQLParserRule> getRuleClass() {
        return SQLParserRule.class;
    }
    
    @Override
    public Class<ShowSQLParserRuleStatement> getType() {
        return ShowSQLParserRuleStatement.class;
    }
}
