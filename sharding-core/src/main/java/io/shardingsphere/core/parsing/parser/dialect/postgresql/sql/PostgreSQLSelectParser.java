/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.dialect.postgresql.sql;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.clause.PostgreSQLForClauseParser;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.clause.PostgreSQLLimitClauseParser;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.clause.facade.PostgreSQLSelectClauseParserFacade;
import io.shardingsphere.core.parsing.parser.sql.dql.select.AbstractSelectParser;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.rule.ShardingRule;

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
        parseDistinct();
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
