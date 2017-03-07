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
import com.alibaba.druid.sql.parser.AbstractSelectParser;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;

public class MySqlSelectParser extends AbstractSelectParser {
    
    public MySqlSelectParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    public SQLSelectQuery query() {
        if (getExprParser().getLexer().equalToken(Token.LEFT_PAREN)) {
            getExprParser().getLexer().nextToken();
            SQLSelectQuery select = query();
            getExprParser().getLexer().accept(Token.RIGHT_PAREN);
            queryRest();
            return select;
        }
        MySqlSelectQueryBlock queryBlock = new MySqlSelectQueryBlock();
        if (getExprParser().getLexer().equalToken(Token.SELECT)) {
            getExprParser().getLexer().nextToken();
            while (getExprParser().getLexer().equalToken(Token.HINT) || getExprParser().getLexer().equalToken(Token.COMMENT)) {
                getExprParser().getLexer().nextToken();
            }
            parseDistinct();
            while (getExprParser().getLexer().equalToken(Token.HIGH_PRIORITY) || getExprParser().getLexer().equalToken(Token.STRAIGHT_JOIN) || getExprParser().getLexer().equalToken(Token.SQL_SMALL_RESULT)
                    || getExprParser().getLexer().equalToken(Token.SQL_BIG_RESULT) || getExprParser().getLexer().equalToken(Token.SQL_BUFFER_RESULT) || getExprParser().getLexer().equalToken(Token.SQL_CACHE)
                    || getExprParser().getLexer().equalToken(Token.SQL_NO_CACHE) || getExprParser().getLexer().equalToken(Token.SQL_CALC_FOUND_ROWS)) {
                getExprParser().getLexer().nextToken();
            }
            parseSelectList();
            skipToFrom();
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        getSqlContext().getOrderByContexts().addAll(getExprParser().parseOrderBy());
        if (getExprParser().getLexer().equalToken(Token.LIMIT)) {
            getSqlContext().setLimitContext(((MySqlExprParser) getExprParser()).parseLimit(getParametersIndex(), getSqlContext()));
        }
        if (getExprParser().getLexer().equalToken(Token.PROCEDURE)) {
            throw new ParserUnsupportedException(getExprParser().getLexer().getToken());
        }
        queryRest();
        return queryBlock;
    }
    
    private void skipToFrom() {
        while (!getExprParser().getLexer().equalToken(Token.FROM) && !getExprParser().getLexer().equalToken(Token.EOF)) {
            getExprParser().getLexer().nextToken();
        }
    }
    
    @Override
    protected void parseJoinTable() {
        if (getExprParser().getLexer().equalToken(Token.USING)) {
            return;
        }
        if (getExprParser().getLexer().equalToken(Token.USE)) {
            getExprParser().getLexer().nextToken();
            parseIndexHint();
        }
        if (getExprParser().getLexer().identifierEquals("IGNORE")) {
            getExprParser().getLexer().nextToken();
            parseIndexHint();
        }
        if (getExprParser().getLexer().identifierEquals("FORCE")) {
            getExprParser().getLexer().nextToken();
            parseIndexHint();
        }
        super.parseJoinTable();
    }

    private void parseIndexHint() {
        if (getExprParser().getLexer().equalToken(Token.INDEX)) {
            getExprParser().getLexer().nextToken();
        } else {
            getExprParser().getLexer().accept(Token.KEY);
        }
        if (getExprParser().getLexer().equalToken(Token.FOR)) {
            getExprParser().getLexer().nextToken();
            if (getExprParser().getLexer().equalToken(Token.JOIN)) {
                getExprParser().getLexer().nextToken();
            } else if (getExprParser().getLexer().equalToken(Token.ORDER)) {
                getExprParser().getLexer().nextToken();
                getExprParser().getLexer().accept(Token.BY);
            } else {
                getExprParser().getLexer().accept(Token.GROUP);
                getExprParser().getLexer().accept(Token.BY);
            }
        }
        getExprParser().getLexer().skipParentheses();
    }
}
