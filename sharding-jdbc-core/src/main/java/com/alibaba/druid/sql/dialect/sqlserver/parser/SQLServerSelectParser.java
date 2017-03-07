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

package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerSelectQueryBlock;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractSelectParser;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;

public class SQLServerSelectParser extends AbstractSelectParser {
    
    public SQLServerSelectParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected void customizedSelect() {
        if (getExprParser().getLexer().equalToken(Token.FOR)) {
            parseFor();
        }
        if (getExprParser().getLexer().equalToken(Token.OFFSET)) {
            ((SQLServerExprParser) getExprParser()).parseOffset(getSqlContext());
        }
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
        SQLServerSelectQueryBlock queryBlock = new SQLServerSelectQueryBlock();
        if (getExprParser().getLexer().equalToken(Token.SELECT)) {
            getExprParser().getLexer().nextToken();
            if (getExprParser().getLexer().equalToken(Token.COMMENT)) {
                getExprParser().getLexer().nextToken();
            }
            parseDistinct();
            if (getExprParser().getLexer().equalToken(Token.TOP)) {
                queryBlock.setTop(((SQLServerExprParser) getExprParser()).parseTop());
            }
            parseSelectList();
        }
        if (getExprParser().getLexer().equalToken(Token.INTO)) {
            throw new ParserUnsupportedException(getExprParser().getLexer().getToken());
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        queryRest();
        return queryBlock;
    }
    
    @Override
    protected void parseJoinTable() {
        if (getExprParser().getLexer().skipIfEqual(Token.WITH)) {
            getExprParser().getLexer().skipParentheses();
        }
        super.parseJoinTable();
    }
    
    private void parseFor() {
        getExprParser().getLexer().nextToken();
        if (getExprParser().getLexer().identifierEquals("BROWSE")) {
            getExprParser().getLexer().nextToken();
        } else if (getExprParser().getLexer().identifierEquals("XML")) {
            getExprParser().getLexer().nextToken();
            while (true) {
                if (getExprParser().getLexer().identifierEquals("AUTO") || getExprParser().getLexer().identifierEquals("TYPE") || getExprParser().getLexer().identifierEquals("XMLSCHEMA")) {
                    getExprParser().getLexer().nextToken();
                } else if (getExprParser().getLexer().identifierEquals("ELEMENTS")) {
                    getExprParser().getLexer().nextToken();
                    if (getExprParser().getLexer().identifierEquals("XSINIL")) {
                        getExprParser().getLexer().nextToken();
                    }
                } else {
                    break;
                }
                if (getExprParser().getLexer().equalToken(Token.COMMA)) {
                    getExprParser().getLexer().nextToken();
                } else {
                    break;
                }
            }
        } else {
            throw new ParserUnsupportedException(getExprParser().getLexer().getToken());
        }
    }
}
