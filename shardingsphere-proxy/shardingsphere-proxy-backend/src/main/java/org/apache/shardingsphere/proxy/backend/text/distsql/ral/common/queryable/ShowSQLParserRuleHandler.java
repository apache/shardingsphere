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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import com.google.gson.Gson;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Show SQL parser rule handler.
 */
public final class ShowSQLParserRuleHandler extends QueryableRALBackendHandler<ShowSQLParserRuleStatement, ShowSQLParserRuleHandler> {
    
    private static final Gson GSON = new Gson();
    
    private static final String SQL_COMMENT_PARSE_ENABLE = "sql_comment_parse_enable";
    
    private static final String PARSE_TREE_CACHE = "parse_tree_cache";
    
    private static final String SQL_STATEMENT_CACHE = "sql_statement_cache";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(SQL_COMMENT_PARSE_ENABLE, PARSE_TREE_CACHE, SQL_STATEMENT_CACHE);
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) {
        Optional<SQLParserRuleConfiguration> sqlParserRuleConfig = ProxyContext.getInstance().getContextManager()
                .getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findRuleConfigurations(SQLParserRuleConfiguration.class).stream().findAny();
        return sqlParserRuleConfig.isPresent() ? Collections.singleton(getRow(sqlParserRuleConfig.get())) : Collections.emptyList();
    }
    
    private List<Object> getRow(final SQLParserRuleConfiguration sqlParserRuleConfig) {
        List<Object> result = new LinkedList<>();
        result.add(String.valueOf(sqlParserRuleConfig.isSqlCommentParseEnabled()));
        result.add(GSON.toJson(sqlParserRuleConfig.getParseTreeCache()));
        result.add(GSON.toJson(sqlParserRuleConfig.getSqlStatementCache()));
        return result;
    }
}
