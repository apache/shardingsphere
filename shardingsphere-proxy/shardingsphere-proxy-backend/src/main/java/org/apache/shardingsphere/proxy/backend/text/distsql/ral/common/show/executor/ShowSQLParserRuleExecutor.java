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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sharding.merge.dal.common.MultipleLocalDataMergedResult;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Show SQL parser rule executor.
 */
@RequiredArgsConstructor
@Getter
public final class ShowSQLParserRuleExecutor extends AbstractShowExecutor {
    
    private static final Gson GSON = new Gson();

    private static final String SQL_COMMENT_PARSE_ENABLE = "sql_comment_parse_enable";

    private static final String PARSE_TREE_CACHE = "parser_tree_cache";

    private static final String SQL_STATEMENT_CACHE = "sql_statement_cache";
    
    private final ShowSQLParserRuleStatement sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    @Override
    protected List<QueryHeader> createQueryHeaders() {
        return Arrays.asList(
                new QueryHeader("", "", SQL_COMMENT_PARSE_ENABLE, SQL_COMMENT_PARSE_ENABLE, Types.VARCHAR, "VARCHAR", 6, 0, false, false, false, false),
                new QueryHeader("", "", PARSE_TREE_CACHE, PARSE_TREE_CACHE, Types.VARCHAR, "VARCHAR", 255, 0, false, false, false, false),
                new QueryHeader("", "", SQL_STATEMENT_CACHE, SQL_STATEMENT_CACHE, Types.VARCHAR, "VARCHAR", 255, 0, false, false, false, false)
        );
    }
    
    @Override
    protected MergedResult createMergedResult() {
        Optional<SQLParserRuleConfiguration> sqlParserRuleConfigurationOptional = ProxyContext.getInstance().getContextManager()
                .getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(SQLParserRuleConfiguration.class).stream().findAny();
        if (!sqlParserRuleConfigurationOptional.isPresent()) {
            return new MultipleLocalDataMergedResult(Collections.emptyList());
        }
        SQLParserRuleConfiguration sqlParserRuleConfiguration = sqlParserRuleConfigurationOptional.get();
        List<Object> row = new LinkedList<>();
        row.add(String.valueOf(sqlParserRuleConfiguration.isSqlCommentParseEnabled()));
        row.add(GSON.toJson(sqlParserRuleConfiguration.getParseTreeCache()));
        row.add(GSON.toJson(sqlParserRuleConfiguration.getSqlStatementCache()));
        List<List<Object>> rows = new LinkedList<>();
        rows.add(row);
        return new MultipleLocalDataMergedResult(rows);
    }
}
