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

package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.postgresql.parser;

import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.postgresql.lexer.PostgreSQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLIdentifierExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Literals;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Symbol;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.ParserException;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.ParserUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.SQLExprParser;

public class PostgreSQLSelectParser extends AbstractSelectParser {
    
    public PostgreSQLSelectParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    public void query() {
        if (getExprParser().skipIfEqual(DefaultKeyword.SELECT)) {
            parseDistinct();
            parseSelectList();
            if (getExprParser().skipIfEqual(DefaultKeyword.INTO)) {
                getExprParser().skipIfEqual(PostgreSQLKeyword.TEMPORARY, PostgreSQLKeyword.TEMP, PostgreSQLKeyword.UNLOGGED);
                getExprParser().skipIfEqual(DefaultKeyword.TABLE);
                // TODO
                // getExprParser().name();
            }
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        if (getExprParser().equal(PostgreSQLKeyword.WINDOW)) {
            throw new ParserUnsupportedException(PostgreSQLKeyword.WINDOW);
        }
        getSqlContext().getOrderByContexts().addAll(getExprParser().parseOrderBy(getSqlContext()));
        parseLimit();
        if (getExprParser().skipIfEqual(DefaultKeyword.FETCH)) {
            throw new ParserUnsupportedException(DefaultKeyword.FETCH);
        }
        if (getExprParser().skipIfEqual(DefaultKeyword.FOR)) {
            getExprParser().skipIfEqual(DefaultKeyword.UPDATE, PostgreSQLKeyword.SHARE);
            if (getExprParser().equal(PostgreSQLKeyword.OF)) {
                throw new ParserUnsupportedException(PostgreSQLKeyword.OF);
            }
            getExprParser().skipIfEqual(PostgreSQLKeyword.NOWAIT);
        }
        queryRest();
    }
    
    // TODO 解析和改写limit
    private void parseLimit() {
        while (true) {
            if (getExprParser().equal(PostgreSQLKeyword.LIMIT)) {
                
                getExprParser().getLexer().nextToken();
                if (getExprParser().equal(DefaultKeyword.ALL)) {
                    new SQLIdentifierExpr("ALL");
                    getExprParser().getLexer().nextToken();
                } else {
                    // rowCount
                    if (getExprParser().equal(Literals.INT)) {
                    } else if (getExprParser().equal(Symbol.QUESTION)) {
                    } else {
                        throw new ParserException(getExprParser().getLexer());
                    }
                    getExprParser().getLexer().nextToken();
                }
            } else if (getExprParser().equal(PostgreSQLKeyword.OFFSET)) {
                getExprParser().getLexer().nextToken();
                // offset
                if (getExprParser().equal(Literals.INT)) {
                } else if (getExprParser().equal(Symbol.QUESTION)) {
                } else {
                    throw new ParserException(getExprParser().getLexer());
                }
                getExprParser().getLexer().nextToken();
                getExprParser().skipIfEqual(PostgreSQLKeyword.ROW, PostgreSQLKeyword.ROWS);
            } else {
                break;
            }
        }
    }
    
    protected boolean hasDistinctOn() {
        return true;
    }
}
