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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;

public class MySQLSelectParser extends AbstractSelectParser {
    
    public MySQLSelectParser(final SQLParser sqlParser) {
        super(sqlParser);
    }
    
    @Override
    public void query() {
        if (getSqlParser().equalAny(DefaultKeyword.SELECT)) {
            getSqlParser().getLexer().nextToken();
            parseDistinct();
            getSqlParser().skipAll(MySQLKeyword.HIGH_PRIORITY, DefaultKeyword.STRAIGHT_JOIN, MySQLKeyword.SQL_SMALL_RESULT, MySQLKeyword.SQL_BIG_RESULT, MySQLKeyword.SQL_BUFFER_RESULT,
                    MySQLKeyword.SQL_CACHE, MySQLKeyword.SQL_NO_CACHE, MySQLKeyword.SQL_CALC_FOUND_ROWS);
            parseSelectList();
            skipToFrom();
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        getSelectStatement().getOrderByList().addAll(parseOrderBy());
        if (getSqlParser().equalAny(MySQLKeyword.LIMIT)) {
            getSelectStatement().setLimit(((MySQLParser) getSqlParser()).parseLimit(getSelectStatement(), getParametersIndex()));
        }
        if (getSqlParser().equalAny(DefaultKeyword.PROCEDURE)) {
            throw new SQLParsingUnsupportedException(getSqlParser().getLexer().getCurrentToken().getType());
        }
        queryRest();
    }
    
    private void skipToFrom() {
        while (!getSqlParser().equalAny(DefaultKeyword.FROM) && !getSqlParser().equalAny(Assist.END)) {
            getSqlParser().getLexer().nextToken();
        }
    }
    
    @Override
    protected void parseJoinTable() {
        if (getSqlParser().equalAny(DefaultKeyword.USING)) {
            return;
        }
        if (getSqlParser().equalAny(DefaultKeyword.USE)) {
            getSqlParser().getLexer().nextToken();
            parseIndexHint();
        }
        if (getSqlParser().equalAny(OracleKeyword.IGNORE)) {
            getSqlParser().getLexer().nextToken();
            parseIndexHint();
        }
        if (getSqlParser().equalAny(OracleKeyword.FORCE)) {
            getSqlParser().getLexer().nextToken();
            parseIndexHint();
        }
        super.parseJoinTable();
    }

    private void parseIndexHint() {
        if (getSqlParser().equalAny(DefaultKeyword.INDEX)) {
            getSqlParser().getLexer().nextToken();
        } else {
            getSqlParser().accept(DefaultKeyword.KEY);
        }
        if (getSqlParser().equalAny(DefaultKeyword.FOR)) {
            getSqlParser().getLexer().nextToken();
            if (getSqlParser().equalAny(DefaultKeyword.JOIN)) {
                getSqlParser().getLexer().nextToken();
            } else if (getSqlParser().equalAny(DefaultKeyword.ORDER)) {
                getSqlParser().getLexer().nextToken();
                getSqlParser().accept(DefaultKeyword.BY);
            } else {
                getSqlParser().accept(DefaultKeyword.GROUP);
                getSqlParser().accept(DefaultKeyword.BY);
            }
        }
        getSqlParser().skipParentheses();
    }
}
