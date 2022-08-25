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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import com.google.gson.Gson;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.QueryableRALBackendHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show SQL parser rule handler.
 */
public final class ShowSQLParserRuleHandler extends QueryableRALBackendHandler<ShowSQLParserRuleStatement> {
    
    private static final Gson GSON = new Gson();
    
    private static final String SQL_COMMENT_PARSE_ENABLE = "sql_comment_parse_enable";
    
    private static final String PARSE_TREE_CACHE = "parse_tree_cache";
    
    private static final String SQL_STATEMENT_CACHE = "sql_statement_cache";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(SQL_COMMENT_PARSE_ENABLE, PARSE_TREE_CACHE, SQL_STATEMENT_CACHE);
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        SQLParserRule sqlParserRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        return Collections.singleton(getRow(sqlParserRule));
    }
    
    private LocalDataQueryResultRow getRow(final SQLParserRule sqlParserRule) {
        return new LocalDataQueryResultRow(
                String.valueOf(sqlParserRule.isSqlCommentParseEnabled()), GSON.toJson(sqlParserRule.getParseTreeCache()), GSON.toJson(sqlParserRule.getSqlStatementCache()));
    }
}
