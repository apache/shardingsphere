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

package org.apache.shardingsphere.core.parse.parser.dialect.postgresql.sql;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.lexer.LexerEngine;
import org.apache.shardingsphere.core.parse.parser.dialect.postgresql.clause.PostgreSQLForClauseParser;
import org.apache.shardingsphere.core.parse.parser.dialect.postgresql.clause.PostgreSQLLimitClauseParser;
import org.apache.shardingsphere.core.parse.parser.dialect.postgresql.clause.facade.PostgreSQLSelectClauseParserFacade;
import org.apache.shardingsphere.core.parse.parser.sql.dql.select.AbstractSelectParser;
import org.apache.shardingsphere.core.parse.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Select parser for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLSelectParser extends AbstractSelectParser {
    
    private final PostgreSQLLimitClauseParser limitClauseParser;
    
    private final PostgreSQLForClauseParser forClauseParser;
    
    public PostgreSQLSelectParser(final ShardingRule shardingRule, final LexerEngine lexerEngine, final ShardingTableMetaData shardingTableMetaData) {
        super(shardingRule, lexerEngine, new PostgreSQLSelectClauseParserFacade(shardingRule, lexerEngine), shardingTableMetaData);
        limitClauseParser = new PostgreSQLLimitClauseParser(lexerEngine);
        forClauseParser = new PostgreSQLForClauseParser(lexerEngine);
    }
    
    @Override
    protected void parseInternal(final SelectStatement selectStatement) {
        parseSelectList(selectStatement, getItems());
        parseFrom(selectStatement);
        parseWhere(getShardingRule(), selectStatement, getItems());
        parseGroupBy(selectStatement);
        parseHaving();
        parseOrderBy(selectStatement);
        parseLimit(selectStatement);
        parseFor();
        parseSelectRest();
    }
    
    private void parseLimit(final SelectStatement selectStatement) {
        limitClauseParser.parse(selectStatement);
    }
    
    private void parseFor() {
        forClauseParser.parse();
    }
}
