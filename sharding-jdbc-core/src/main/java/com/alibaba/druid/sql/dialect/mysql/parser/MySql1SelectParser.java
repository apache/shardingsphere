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

import com.alibaba.druid.sql.dialect.mysql.lexer.MySQLKeyword;
import com.alibaba.druid.sql.dialect.oracle.lexer.OracleKeyword;
import com.alibaba.druid.sql.lexer.DataType;
import com.alibaba.druid.sql.lexer.DefaultKeyword;
import com.alibaba.druid.sql.parser.AbstractSelectParser;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;

public class MySql1SelectParser extends AbstractSelectParser {
    
    public MySql1SelectParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    public void query() {
        if (getExprParser().getLexer().equalToken(DefaultKeyword.SELECT)) {
            getExprParser().getLexer().nextToken();
            while (getExprParser().getLexer().equalToken(DataType.HINT) || getExprParser().getLexer().equalToken(DataType.COMMENT)) {
                getExprParser().getLexer().nextToken();
            }
            parseDistinct();
            while (getExprParser().getLexer().equalToken(MySQLKeyword.HIGH_PRIORITY) || getExprParser().getLexer().equalToken(DefaultKeyword.STRAIGHT_JOIN)
                    || getExprParser().getLexer().equalToken(MySQLKeyword.SQL_SMALL_RESULT)
                    || getExprParser().getLexer().equalToken(MySQLKeyword.SQL_BIG_RESULT) || getExprParser().getLexer().equalToken(MySQLKeyword.SQL_BUFFER_RESULT)
                    || getExprParser().getLexer().equalToken(MySQLKeyword.SQL_CACHE)
                    || getExprParser().getLexer().equalToken(MySQLKeyword.SQL_NO_CACHE) || getExprParser().getLexer().equalToken(MySQLKeyword.SQL_CALC_FOUND_ROWS)) {
                getExprParser().getLexer().nextToken();
            }
            parseSelectList();
            skipToFrom();
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        getSqlContext().getOrderByContexts().addAll(getExprParser().parseOrderBy(getSqlContext()));
        if (getExprParser().getLexer().equalToken(MySQLKeyword.LIMIT)) {
            getSqlContext().setLimitContext(((MySql1ExprParser) getExprParser()).parseLimit(getParametersIndex(), getSqlContext()));
        }
        if (getExprParser().getLexer().equalToken(DefaultKeyword.PROCEDURE)) {
            throw new ParserUnsupportedException(getExprParser().getLexer().getToken());
        }
        queryRest();
    }
    
    private void skipToFrom() {
        while (!getExprParser().getLexer().equalToken(DefaultKeyword.FROM) && !getExprParser().getLexer().equalToken(DataType.EOF)) {
            getExprParser().getLexer().nextToken();
        }
    }
    
    @Override
    protected void parseJoinTable() {
        if (getExprParser().getLexer().equalToken(DefaultKeyword.USING)) {
            return;
        }
        if (getExprParser().getLexer().equalToken(DefaultKeyword.USE)) {
            getExprParser().getLexer().nextToken();
            parseIndexHint();
        }
        if (getExprParser().getLexer().equalToken(OracleKeyword.IGNORE)) {
            getExprParser().getLexer().nextToken();
            parseIndexHint();
        }
        if (getExprParser().getLexer().equalToken(OracleKeyword.FORCE)) {
            getExprParser().getLexer().nextToken();
            parseIndexHint();
        }
        super.parseJoinTable();
    }

    private void parseIndexHint() {
        if (getExprParser().getLexer().equalToken(DefaultKeyword.INDEX)) {
            getExprParser().getLexer().nextToken();
        } else {
            getExprParser().getLexer().accept(DefaultKeyword.KEY);
        }
        if (getExprParser().getLexer().equalToken(DefaultKeyword.FOR)) {
            getExprParser().getLexer().nextToken();
            if (getExprParser().getLexer().equalToken(DefaultKeyword.JOIN)) {
                getExprParser().getLexer().nextToken();
            } else if (getExprParser().getLexer().equalToken(DefaultKeyword.ORDER)) {
                getExprParser().getLexer().nextToken();
                getExprParser().getLexer().accept(DefaultKeyword.BY);
            } else {
                getExprParser().getLexer().accept(DefaultKeyword.GROUP);
                getExprParser().getLexer().accept(DefaultKeyword.BY);
            }
        }
        getExprParser().getLexer().skipParentheses();
    }
}
