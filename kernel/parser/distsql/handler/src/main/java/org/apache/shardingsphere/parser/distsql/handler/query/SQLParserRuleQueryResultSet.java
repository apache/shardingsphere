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

import org.apache.shardingsphere.infra.distsql.query.GlobalRuleDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.distsql.parser.statement.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Query result set for SQL parser rule.
 */
public final class SQLParserRuleQueryResultSet implements GlobalRuleDistSQLResultSet {
    
    private static final String SQL_COMMENT_PARSE_ENABLE = "sql_comment_parse_enable";
    
    private static final String PARSE_TREE_CACHE = "parse_tree_cache";
    
    private static final String SQL_STATEMENT_CACHE = "sql_statement_cache";
    
    private Iterator<Collection<Object>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereRuleMetaData ruleMetaData, final SQLStatement sqlStatement) {
        ruleMetaData.findSingleRule(SQLParserRule.class).ifPresent(optional -> data = buildData(optional.getConfiguration()).iterator());
    }
    
    private Collection<Collection<Object>> buildData(final SQLParserRuleConfiguration ruleConfig) {
        return Collections.singleton(Arrays.asList(String.valueOf(ruleConfig.isSqlCommentParseEnabled()), null != ruleConfig.getParseTreeCache() ? ruleConfig.getParseTreeCache().toString() : "",
                null != ruleConfig.getSqlStatementCache() ? ruleConfig.getSqlStatementCache().toString() : ""));
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(SQL_COMMENT_PARSE_ENABLE, PARSE_TREE_CACHE, SQL_STATEMENT_CACHE);
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return ShowSQLParserRuleStatement.class.getName();
    }
}
