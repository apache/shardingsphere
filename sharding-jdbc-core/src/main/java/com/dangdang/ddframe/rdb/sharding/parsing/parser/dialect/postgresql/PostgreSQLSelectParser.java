/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.DistinctSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.GroupBySQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.HavingSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SelectListSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SelectRestSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.TableSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.WhereSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;

/**
 * PostgreSQL Select语句解析器.
 *
 * @author zhangliang
 */
public final class PostgreSQLSelectParser extends AbstractSelectParser {
    
    private final PostgreSQLLimitSQLParser limitSQLParser;
    
    private final PostgreSQLForSQLParser forSQLParser;
    
    private final SelectRestSQLParser selectRestSQLParser;
    
    public PostgreSQLSelectParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine, new DistinctSQLParser(lexerEngine), 
                new SelectListSQLParser(shardingRule, lexerEngine), new TableSQLParser(shardingRule, lexerEngine), new WhereSQLParser(lexerEngine), new GroupBySQLParser(lexerEngine), 
                new HavingSQLParser(lexerEngine), new PostgreSQLOrderBySQLParser(lexerEngine));
        limitSQLParser = new PostgreSQLLimitSQLParser(lexerEngine);
        forSQLParser = new PostgreSQLForSQLParser(lexerEngine);
        selectRestSQLParser = new PostgreSQLSelectRestSQLParser(lexerEngine);
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
        selectRestSQLParser.parse();
    }
    
    private void parseLimit(final SelectStatement selectStatement) {
        limitSQLParser.parse(selectStatement);
    }
    
    private void parseFor() {
        forSQLParser.parse();
    }
}
