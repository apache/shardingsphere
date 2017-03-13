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

import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLIdentifierExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.postgresql.lexer.PostgreSQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Literals;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.DefaultKeyword;
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
        if (getExprParser().getLexer().skipIfEqual(DefaultKeyword.SELECT)) {
            getExprParser().getLexer().skipIfEqual(Literals.COMMENT);
            parseDistinct();
            parseSelectList();
            if (getExprParser().getLexer().skipIfEqual(DefaultKeyword.INTO)) {
                getExprParser().getLexer().skipIfEqual(PostgreSQLKeyword.TEMPORARY, PostgreSQLKeyword.TEMP, PostgreSQLKeyword.UNLOGGED);
                getExprParser().getLexer().skipIfEqual(DefaultKeyword.TABLE);
                // TODO
                // getExprParser().name();
            }
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        if (getExprParser().getLexer().equalToken(PostgreSQLKeyword.WINDOW)) {
            throw new ParserUnsupportedException(PostgreSQLKeyword.WINDOW);
        }
        getSqlContext().getOrderByContexts().addAll(getExprParser().parseOrderBy(getSqlContext()));
        parseLimit();
        if (getExprParser().getLexer().skipIfEqual(DefaultKeyword.FETCH)) {
            throw new ParserUnsupportedException(DefaultKeyword.FETCH);
        }
        if (getExprParser().getLexer().skipIfEqual(DefaultKeyword.FOR)) {
            getExprParser().getLexer().skipIfEqual(DefaultKeyword.UPDATE, PostgreSQLKeyword.SHARE);
            if (getExprParser().getLexer().equalToken(PostgreSQLKeyword.OF)) {
                throw new ParserUnsupportedException(PostgreSQLKeyword.OF);
            }
            getExprParser().getLexer().skipIfEqual(PostgreSQLKeyword.NOWAIT);
        }
        queryRest();
    }
    
    // TODO 解析和改写limit
    private void parseLimit() {
        while (true) {
            if (getExprParser().getLexer().equalToken(PostgreSQLKeyword.LIMIT)) {
                
                getExprParser().getLexer().nextToken();
                if (getExprParser().getLexer().equalToken(DefaultKeyword.ALL)) {
                    new SQLIdentifierExpr("ALL");
                    getExprParser().getLexer().nextToken();
                } else {
                    // rowCount
                    if (getExprParser().getLexer().equalToken(Literals.INT)) {
                    } else if (getExprParser().getLexer().equalToken(Symbol.QUESTION)) {
                    } else {
                        throw new ParserException(getExprParser().getLexer());
                    }
                    getExprParser().getLexer().nextToken();
                }
            } else if (getExprParser().getLexer().equalToken(PostgreSQLKeyword.OFFSET)) {
                getExprParser().getLexer().nextToken();
                // offset
                if (getExprParser().getLexer().equalToken(Literals.INT)) {
                } else if (getExprParser().getLexer().equalToken(Symbol.QUESTION)) {
                } else {
                    throw new ParserException(getExprParser().getLexer());
                }
                getExprParser().getLexer().nextToken();
                getExprParser().getLexer().skipIfEqual(PostgreSQLKeyword.ROW, PostgreSQLKeyword.ROWS);
            } else {
                break;
            }
        }
    }
    
    protected boolean hasDistinctOn() {
        return true;
    }
}
