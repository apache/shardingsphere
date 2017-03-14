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

package com.dangdang.ddframe.rdb.sharding.parser.sql.parser;

import com.dangdang.ddframe.rdb.sharding.parser.sql.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Symbol;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;

import java.util.List;

/**
 * SQL解析器.
 *
 * @author zhangliang
 */
public final class SQLStatementParser {
    
    private final DatabaseType dbType;
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    private final SQLExprParser exprParser;
    
    public SQLStatementParser(final DatabaseType dbType, final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        this.dbType = dbType;
        this.shardingRule = shardingRule;
        this.parameters = parameters;
        this.exprParser = exprParser;
    }
    
    /**
     * 解析SQL.
     * 
     * @return SQL解析对象
     */
    public SQLContext parseStatement() {
        exprParser.getLexer().skipIfEqual(Symbol.SEMI);
        if (exprParser.getLexer().equal(DefaultKeyword.WITH)) {
            skipWith();
        }
        if (exprParser.getLexer().equal(DefaultKeyword.SELECT)) {
            return SQLSelectParserFactory.newInstance(exprParser, dbType).parse();
        }
        if (exprParser.getLexer().equal(DefaultKeyword.INSERT)) {
            return SQLInsertParserFactory.newInstance(shardingRule, parameters, exprParser, dbType).parse();
        }
        if (exprParser.getLexer().equal(DefaultKeyword.UPDATE)) {
            return SQLUpdateParserFactory.newInstance(exprParser, dbType).parse();
        }
        if (exprParser.getLexer().equal(DefaultKeyword.DELETE)) {
            return SQLDeleteParserFactory.newInstance(exprParser, dbType).parse();
        }
        throw new ParserUnsupportedException(exprParser.getLexer().getToken().getType());
    }
    
    private void skipWith() {
        exprParser.getLexer().nextToken();
        do {
            exprParser.getLexer().skipUntil(DefaultKeyword.AS);
            exprParser.getLexer().accept(DefaultKeyword.AS);
            exprParser.getLexer().skipParentheses();
        } while (exprParser.getLexer().skipIfEqual(Symbol.COMMA));
    }
}
