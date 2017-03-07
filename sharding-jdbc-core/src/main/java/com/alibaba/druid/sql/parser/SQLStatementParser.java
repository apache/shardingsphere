/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
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
 */

package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.context.SQLContext;
import com.alibaba.druid.sql.lexer.Token;
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
        if (exprParser.getLexer().equalToken(Token.SEMI)) {
            exprParser.getLexer().nextToken();
        }
        if (exprParser.getLexer().equalToken(Token.WITH)) {
            parseWith();
        }
        if (exprParser.getLexer().equalToken(Token.SELECT)) {
            return SQLSelectParserFactory.newInstance(exprParser, dbType).parse();
        }
        if (exprParser.getLexer().equalToken(Token.INSERT)) {
            return SQLInsertParserFactory.newInstance(shardingRule, parameters, exprParser, dbType).parse();
        }
        if (exprParser.getLexer().equalToken(Token.UPDATE)) {
            return SQLUpdateParserFactory.newInstance(exprParser, dbType).parse();
        }
        if (exprParser.getLexer().equalToken(Token.DELETE)) {
            return SQLDeleteParserFactory.newInstance(exprParser, dbType).parse();
        }
        throw new ParserUnsupportedException(exprParser.getLexer().getToken());
    }
    
    private void parseWith() {
        exprParser.getLexer().nextToken();
        do {
            parseWithQuery();
            if (exprParser.getLexer().equalToken(Token.EOF)) {
                return;
            }
        } while (exprParser.getLexer().equalToken(Token.COMMA));
    }
    
    private void parseWithQuery() {
        while (!exprParser.getLexer().equalToken(Token.AS)) {
            exprParser.getLexer().nextToken();
            if (exprParser.getLexer().equalToken(Token.EOF)) {
                return;
            }
        }
        exprParser.getLexer().accept(Token.AS);
        exprParser.getLexer().accept(Token.LEFT_PAREN);
        while (!exprParser.getLexer().equalToken(Token.RIGHT_PAREN)) {
            exprParser.getLexer().nextToken();
            if (exprParser.getLexer().equalToken(Token.EOF)) {
                return;
            }
        }
        exprParser.getLexer().accept(Token.RIGHT_PAREN);
    }
}
