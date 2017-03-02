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

package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

public class MySqlSelectParser extends SQLSelectParser {
    
    public MySqlSelectParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    @Override
    public SQLSelectQuery query() {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            SQLSelectQuery select = query();
            getLexer().accept(Token.RIGHT_PAREN);
            queryRest();
            return select;
        }
        MySqlSelectQueryBlock queryBlock = new MySqlSelectQueryBlock();
        if (getLexer().equalToken(Token.SELECT)) {
            getLexer().nextToken();
            while (getLexer().equalToken(Token.HINT) || getLexer().equalToken(Token.COMMENT)) {
                getLexer().nextToken();
            }
            parseDistinct();
            while (getLexer().equalToken(Token.HIGH_PRIORITY) || getLexer().equalToken(Token.STRAIGHT_JOIN) || getLexer().equalToken(Token.SQL_SMALL_RESULT)
                    || getLexer().equalToken(Token.SQL_BIG_RESULT) || getLexer().equalToken(Token.SQL_BUFFER_RESULT) || getLexer().equalToken(Token.SQL_CACHE)
                    || getLexer().equalToken(Token.SQL_NO_CACHE) || getLexer().equalToken(Token.SQL_CALC_FOUND_ROWS)) {
                getLexer().nextToken();
            }
            parseSelectList();
            skipToFrom();
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        getSqlContext().getOrderByContexts().addAll(getExprParser().parseOrderBy());
        if (getLexer().equalToken(Token.LIMIT)) {
            getSqlContext().setLimitContext(((MySqlExprParser) getExprParser()).parseLimit(getParametersIndex(), getSqlContext()));
        }
        if (getLexer().equalToken(Token.PROCEDURE)) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
        queryRest();
        return queryBlock;
    }
    
    private void skipToFrom() {
        while (!getLexer().equalToken(Token.FROM) && !getLexer().equalToken(Token.EOF)) {
            getLexer().nextToken();
        }
    }
    
    @Override
    protected void parseJoinTable() {
        if (getLexer().equalToken(Token.USING)) {
            return;
        }
        if (getLexer().equalToken(Token.USE)) {
            getLexer().nextToken();
            parseIndexHint();
        }
        if (getLexer().identifierEquals("IGNORE")) {
            getLexer().nextToken();
            parseIndexHint();
        }
        if (getLexer().identifierEquals("FORCE")) {
            getLexer().nextToken();
            parseIndexHint();
        }
        super.parseJoinTable();
    }

    private void parseIndexHint() {
        if (getLexer().equalToken(Token.INDEX)) {
            getLexer().nextToken();
        } else {
            getLexer().accept(Token.KEY);
        }
        if (getLexer().equalToken(Token.FOR)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.JOIN)) {
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.ORDER)) {
                getLexer().nextToken();
                getLexer().accept(Token.BY);
            } else {
                getLexer().accept(Token.GROUP);
                getLexer().accept(Token.BY);
            }
        }
        getLexer().skipParentheses();
    }
}
