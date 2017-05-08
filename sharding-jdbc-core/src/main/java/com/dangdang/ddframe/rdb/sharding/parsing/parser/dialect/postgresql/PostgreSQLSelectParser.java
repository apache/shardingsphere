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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.postgresql.PostgreSQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLIdentifierExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;

public class PostgreSQLSelectParser extends AbstractSelectParser {
    
    public PostgreSQLSelectParser(final SQLParser sqlParser) {
        super(sqlParser);
    }
    
    @Override
    public void query() {
        if (getSqlParser().skipIfEqual(DefaultKeyword.SELECT)) {
            parseDistinct();
            parseSelectList();
            if (getSqlParser().skipIfEqual(DefaultKeyword.INTO)) {
                getSqlParser().skipIfEqual(PostgreSQLKeyword.TEMPORARY, PostgreSQLKeyword.TEMP, PostgreSQLKeyword.UNLOGGED);
                getSqlParser().skipIfEqual(DefaultKeyword.TABLE);
                // TODO
                // getSqlParser().name();
            }
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        if (getSqlParser().equalAny(PostgreSQLKeyword.WINDOW)) {
            throw new SQLParsingUnsupportedException(PostgreSQLKeyword.WINDOW);
        }
        getSqlContext().getOrderByContexts().addAll(parseOrderBy(getSqlContext()));
        parseLimit();
        if (getSqlParser().skipIfEqual(DefaultKeyword.FETCH)) {
            throw new SQLParsingUnsupportedException(DefaultKeyword.FETCH);
        }
        if (getSqlParser().skipIfEqual(DefaultKeyword.FOR)) {
            getSqlParser().skipIfEqual(DefaultKeyword.UPDATE, PostgreSQLKeyword.SHARE);
            if (getSqlParser().equalAny(PostgreSQLKeyword.OF)) {
                throw new SQLParsingUnsupportedException(PostgreSQLKeyword.OF);
            }
            getSqlParser().skipIfEqual(PostgreSQLKeyword.NOWAIT);
        }
        queryRest();
    }
    
    // TODO 解析和改写limit
    private void parseLimit() {
        while (true) {
            if (getSqlParser().equalAny(PostgreSQLKeyword.LIMIT)) {
                
                getSqlParser().getLexer().nextToken();
                if (getSqlParser().equalAny(DefaultKeyword.ALL)) {
                    new SQLIdentifierExpr("ALL");
                    getSqlParser().getLexer().nextToken();
                } else {
                    // rowCount
                    if (getSqlParser().equalAny(Literals.INT)) {
                    } else if (getSqlParser().equalAny(Symbol.QUESTION)) {
                    } else {
                        throw new SQLParsingException(getSqlParser().getLexer());
                    }
                    getSqlParser().getLexer().nextToken();
                }
            } else if (getSqlParser().equalAny(PostgreSQLKeyword.OFFSET)) {
                getSqlParser().getLexer().nextToken();
                // offset
                if (getSqlParser().equalAny(Literals.INT)) {
                } else if (getSqlParser().equalAny(Symbol.QUESTION)) {
                } else {
                    throw new SQLParsingException(getSqlParser().getLexer());
                }
                getSqlParser().getLexer().nextToken();
                getSqlParser().skipIfEqual(PostgreSQLKeyword.ROW, PostgreSQLKeyword.ROWS);
            } else {
                break;
            }
        }
    }
    
    protected boolean hasDistinctOn() {
        return true;
    }
}
