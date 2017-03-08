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

package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractSelectParser;
import com.alibaba.druid.sql.parser.SQLExprParser;

public class PGSelectParser extends AbstractSelectParser {
    
    public PGSelectParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    public void query() {
        if (getExprParser().getLexer().skipIfEqual(Token.SELECT)) {
            getExprParser().getLexer().skipIfEqual(Token.COMMENT);
            parseDistinct();
            parseSelectList();
            if (getExprParser().getLexer().skipIfEqual(Token.INTO)) {
                getExprParser().getLexer().skipIfEqual(Token.TEMPORARY, Token.TEMP, Token.UNLOGGED);
                getExprParser().getLexer().skipIfEqual(Token.TABLE);
                getExprParser().name();
            }
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        if (getExprParser().getLexer().skipIfEqual(Token.WINDOW)) {
            getExprParser().expr();
            getExprParser().getLexer().accept(Token.AS);
            while (true) {
                getExprParser().expr();
                if (getExprParser().getLexer().equalToken(Token.COMMA)) {
                    getExprParser().getLexer().nextToken();
                } else {
                    break;
                }
            }
        }
        getSqlContext().getOrderByContexts().addAll(getExprParser().parseOrderBy());
        parseLimit();
        if (getExprParser().getLexer().skipIfEqual(Token.FETCH)) {
            getExprParser().getLexer().skipIfEqual(Token.FIRST, Token.NEXT);
            getExprParser().expr();
            getExprParser().getLexer().skipIfEqual(Token.ROW, Token.ROWS);
            getExprParser().getLexer().skipIfEqual(Token.ONLY);
        }
        if (getExprParser().getLexer().skipIfEqual(Token.FOR)) {
            getExprParser().getLexer().skipIfEqual(Token.UPDATE, Token.SHARE);
            if (getExprParser().getLexer().equalToken(Token.OF)) {
                while (true) {
                    getExprParser().expr();
                    if (getExprParser().getLexer().equalToken(Token.COMMA)) {
                        getExprParser().getLexer().nextToken();
                    } else {
                        break;
                    }
                }
            }
            getExprParser().getLexer().skipIfEqual(Token.NOWAIT);
        }
        queryRest();
    }
    
    // TODO 解析和改写limit
    private void parseLimit() {
        while (true) {
            if (getExprParser().getLexer().equalToken(Token.LIMIT)) {
                
                getExprParser().getLexer().nextToken();
                if (getExprParser().getLexer().equalToken(Token.ALL)) {
                    new SQLIdentifierExpr("ALL");
                    getExprParser().getLexer().nextToken();
                } else {
                    // rowCount
                    getExprParser().expr();
                }
            } else if (getExprParser().getLexer().equalToken(Token.OFFSET)) {
                getExprParser().getLexer().nextToken();
                // offset
                getExprParser().expr();
                getExprParser().getLexer().skipIfEqual(Token.ROW, Token.ROWS);
            } else {
                break;
            }
        }
    }
    
    protected boolean hasDistinctOn() {
        return true;
    }
}
