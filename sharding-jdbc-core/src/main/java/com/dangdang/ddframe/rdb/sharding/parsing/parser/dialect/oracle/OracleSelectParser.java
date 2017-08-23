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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.HavingSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SelectRestSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;

/**
 * Oracle Select语句解析器.
 *
 * @author zhangliang
 */
public final class OracleSelectParser extends AbstractSelectParser {
    
    private final OracleHierarchicalQueryClauseParser hierarchicalQueryClauseParser;
    
    private final OracleModelClauseParser modelClauseParser;
    
    private final OracleForParser forParser;
    
    public OracleSelectParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine, new OracleDistinctSQLParser(lexerEngine), new OracleSelectListSQLParser(shardingRule, lexerEngine), new OracleTableSQLParser(shardingRule, lexerEngine), 
                new OracleWhereSQLParser(lexerEngine), new OracleGroupBySQLParser(lexerEngine), new HavingSQLParser(lexerEngine), new OracleOrderBySQLParser(lexerEngine), 
                new SelectRestSQLParser(lexerEngine));
        hierarchicalQueryClauseParser = new OracleHierarchicalQueryClauseParser(shardingRule, lexerEngine);
        modelClauseParser = new OracleModelClauseParser(lexerEngine);
        forParser = new OracleForParser(lexerEngine);
    }
    
    @Override
    protected void parseInternal(final SelectStatement selectStatement) {
        parseDistinct();
        parseSelectList(selectStatement, getItems());
        parseFrom(selectStatement);
        parseWhere(getShardingRule(), selectStatement, getItems());
        parseHierarchicalQueryClause(selectStatement);
        parseGroupBy(selectStatement);
        parseHaving();
        parseModelClause(selectStatement);
        parseOrderBy(selectStatement);
        parseFor(selectStatement);
        parseSelectRest();
    }
    
    private void parseHierarchicalQueryClause(final SelectStatement selectStatement) {
        hierarchicalQueryClauseParser.parse(selectStatement);
    }
    
    private void parseModelClause(final SelectStatement selectStatement) {
        modelClauseParser.parse(selectStatement);
    }
    
    private void parseFor(final SelectStatement selectStatement) {
        forParser.parse(selectStatement);
    }
}
