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

import org.apache.shardingsphere.distsql.handler.ral.query.MetaDataRequiredQueryableRALExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.distsql.parser.statement.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.parser.rule.SQLParserRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show SQL parser rule executor.
 */
public final class ShowSQLParserRuleExecutor implements MetaDataRequiredQueryableRALExecutor<ShowSQLParserRuleStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereMetaData metaData, final ShowSQLParserRuleStatement sqlStatement) {
        SQLParserRuleConfiguration ruleConfig = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class).getConfiguration();
        return Collections.singleton(new LocalDataQueryResultRow(String.valueOf(ruleConfig.isSqlCommentParseEnabled()),
                null != ruleConfig.getParseTreeCache() ? ruleConfig.getParseTreeCache().toString() : "",
                null != ruleConfig.getSqlStatementCache() ? ruleConfig.getSqlStatementCache().toString() : ""));
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("sql_comment_parse_enabled", "parse_tree_cache", "sql_statement_cache");
    }
    
    @Override
    public Class<ShowSQLParserRuleStatement> getType() {
        return ShowSQLParserRuleStatement.class;
    }
}
