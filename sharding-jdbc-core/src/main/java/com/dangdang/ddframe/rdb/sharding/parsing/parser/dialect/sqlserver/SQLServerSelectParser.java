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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.MySQLOrderBySQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.DistinctSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.GroupBySQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.HavingSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.OrderBySQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SelectRestSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.WhereSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;

/**
 * SQLServer Select语句解析器.
 *
 * @author zhangliang
 */
public final class SQLServerSelectParser extends AbstractSelectParser {
    
    private final SQLServerTopParser sqlServerTopParser;
    
    private final WhereSQLParser whereSQLParser;
    
    private final GroupBySQLParser groupBySQLParser;
    
    private final HavingSQLParser havingSQLParser;
    
    private final OrderBySQLParser orderBySQLParser;
    
    private final SQLServerOffsetSQLParser offsetSQLParser;
    
    private final SQLServerForSQLParser forSQLParser;
    
    private final SelectRestSQLParser selectRestSQLParser;
    
    public SQLServerSelectParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine, new DistinctSQLParser(lexerEngine), new SQLServerSelectListSQLParser(shardingRule, lexerEngine), new SQLServerTableSQLParser(shardingRule, lexerEngine));
        sqlServerTopParser = new SQLServerTopParser(lexerEngine);
        whereSQLParser = new SQLServerWhereSQLParser(lexerEngine);
        groupBySQLParser = new GroupBySQLParser(lexerEngine);
        havingSQLParser = new HavingSQLParser(lexerEngine);
        orderBySQLParser = new MySQLOrderBySQLParser(lexerEngine);
        offsetSQLParser = new SQLServerOffsetSQLParser(lexerEngine);
        forSQLParser = new SQLServerForSQLParser(lexerEngine);
        selectRestSQLParser = new SelectRestSQLParser(lexerEngine);
    }
    
    @Override
    protected void parseInternal(final SelectStatement selectStatement) {
        parseDistinct();
        parseTop(selectStatement);
        parseSelectList(selectStatement, getItems());
        parseFrom(selectStatement);
        whereSQLParser.parse(getShardingRule(), selectStatement, getItems());
        groupBySQLParser.parse(selectStatement);
        havingSQLParser.parse();
        orderBySQLParser.parse(selectStatement);
        offsetSQLParser.parse(selectStatement);
        forSQLParser.parse();
        selectRestSQLParser.parse();
    }
    
    private void parseTop(final SelectStatement selectStatement) {
        sqlServerTopParser.parse(selectStatement);
    }
}
