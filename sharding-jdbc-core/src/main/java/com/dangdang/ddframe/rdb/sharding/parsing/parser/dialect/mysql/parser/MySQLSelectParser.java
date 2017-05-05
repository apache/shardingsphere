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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.parser;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.lexer.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.lexer.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.ParserUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;

public class MySQLSelectParser extends AbstractSelectParser {
    
    public MySQLSelectParser(final SQLParser exprParser) {
        super(exprParser);
    }
    
    @Override
    public void query() {
        if (getExprParser().equalAny(DefaultKeyword.SELECT)) {
            getExprParser().getLexer().nextToken();
            parseDistinct();
            getExprParser().skipAll(MySQLKeyword.HIGH_PRIORITY, DefaultKeyword.STRAIGHT_JOIN, MySQLKeyword.SQL_SMALL_RESULT, MySQLKeyword.SQL_BIG_RESULT, MySQLKeyword.SQL_BUFFER_RESULT,
                    MySQLKeyword.SQL_CACHE, MySQLKeyword.SQL_NO_CACHE, MySQLKeyword.SQL_CALC_FOUND_ROWS);
            parseSelectList();
            skipToFrom();
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        getSqlContext().getOrderByContexts().addAll(parseOrderBy(getSqlContext()));
        if (getExprParser().equalAny(MySQLKeyword.LIMIT)) {
            getSqlContext().setLimitContext(((MySQLParser) getExprParser()).parseLimit(getParametersIndex()));
        }
        if (getExprParser().equalAny(DefaultKeyword.PROCEDURE)) {
            throw new ParserUnsupportedException(getExprParser().getLexer().getCurrentToken().getType());
        }
        queryRest();
    }
    
    private void skipToFrom() {
        while (!getExprParser().equalAny(DefaultKeyword.FROM) && !getExprParser().equalAny(Assist.END)) {
            getExprParser().getLexer().nextToken();
        }
    }
    
    @Override
    protected void parseJoinTable() {
        if (getExprParser().equalAny(DefaultKeyword.USING)) {
            return;
        }
        if (getExprParser().equalAny(DefaultKeyword.USE)) {
            getExprParser().getLexer().nextToken();
            parseIndexHint();
        }
        if (getExprParser().equalAny(OracleKeyword.IGNORE)) {
            getExprParser().getLexer().nextToken();
            parseIndexHint();
        }
        if (getExprParser().equalAny(OracleKeyword.FORCE)) {
            getExprParser().getLexer().nextToken();
            parseIndexHint();
        }
        super.parseJoinTable();
    }

    private void parseIndexHint() {
        if (getExprParser().equalAny(DefaultKeyword.INDEX)) {
            getExprParser().getLexer().nextToken();
        } else {
            getExprParser().accept(DefaultKeyword.KEY);
        }
        if (getExprParser().equalAny(DefaultKeyword.FOR)) {
            getExprParser().getLexer().nextToken();
            if (getExprParser().equalAny(DefaultKeyword.JOIN)) {
                getExprParser().getLexer().nextToken();
            } else if (getExprParser().equalAny(DefaultKeyword.ORDER)) {
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
