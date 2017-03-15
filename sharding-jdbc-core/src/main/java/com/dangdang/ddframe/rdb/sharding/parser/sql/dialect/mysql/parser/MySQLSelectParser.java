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

package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.parser;

import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.lexer.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.oracle.lexer.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Assist;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.ParserUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.SQLExprParser;

public class MySQLSelectParser extends AbstractSelectParser {
    
    public MySQLSelectParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    public void query() {
        if (getExprParser().equal(DefaultKeyword.SELECT)) {
            getExprParser().getLexer().nextToken();
            parseDistinct();
            while (getExprParser().equal(MySQLKeyword.HIGH_PRIORITY) || getExprParser().equal(DefaultKeyword.STRAIGHT_JOIN)
                    || getExprParser().equal(MySQLKeyword.SQL_SMALL_RESULT)
                    || getExprParser().equal(MySQLKeyword.SQL_BIG_RESULT) || getExprParser().equal(MySQLKeyword.SQL_BUFFER_RESULT)
                    || getExprParser().equal(MySQLKeyword.SQL_CACHE)
                    || getExprParser().equal(MySQLKeyword.SQL_NO_CACHE) || getExprParser().equal(MySQLKeyword.SQL_CALC_FOUND_ROWS)) {
                getExprParser().getLexer().nextToken();
            }
            parseSelectList();
            skipToFrom();
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        getSqlContext().getOrderByContexts().addAll(getExprParser().parseOrderBy(getSqlContext()));
        if (getExprParser().equal(MySQLKeyword.LIMIT)) {
            getSqlContext().setLimitContext(((MySQLExprParser) getExprParser()).parseLimit(getParametersIndex(), getSqlContext()));
        }
        if (getExprParser().equal(DefaultKeyword.PROCEDURE)) {
            throw new ParserUnsupportedException(getExprParser().getLexer().getToken().getType());
        }
        queryRest();
    }
    
    private void skipToFrom() {
        while (!getExprParser().equal(DefaultKeyword.FROM) && !getExprParser().equal(Assist.EOF)) {
            getExprParser().getLexer().nextToken();
        }
    }
    
    @Override
    protected void parseJoinTable() {
        if (getExprParser().equal(DefaultKeyword.USING)) {
            return;
        }
        if (getExprParser().equal(DefaultKeyword.USE)) {
            getExprParser().getLexer().nextToken();
            parseIndexHint();
        }
        if (getExprParser().equal(OracleKeyword.IGNORE)) {
            getExprParser().getLexer().nextToken();
            parseIndexHint();
        }
        if (getExprParser().equal(OracleKeyword.FORCE)) {
            getExprParser().getLexer().nextToken();
            parseIndexHint();
        }
        super.parseJoinTable();
    }

    private void parseIndexHint() {
        if (getExprParser().equal(DefaultKeyword.INDEX)) {
            getExprParser().getLexer().nextToken();
        } else {
            getExprParser().accept(DefaultKeyword.KEY);
        }
        if (getExprParser().equal(DefaultKeyword.FOR)) {
            getExprParser().getLexer().nextToken();
            if (getExprParser().equal(DefaultKeyword.JOIN)) {
                getExprParser().getLexer().nextToken();
            } else if (getExprParser().equal(DefaultKeyword.ORDER)) {
                getExprParser().getLexer().nextToken();
                getExprParser().accept(DefaultKeyword.BY);
            } else {
                getExprParser().accept(DefaultKeyword.GROUP);
                getExprParser().accept(DefaultKeyword.BY);
            }
        }
        getExprParser().skipParentheses();
    }
}
