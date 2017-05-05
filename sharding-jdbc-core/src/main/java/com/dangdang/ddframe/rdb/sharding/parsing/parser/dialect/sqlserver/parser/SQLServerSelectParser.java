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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.parser;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.lexer.SQLServerKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;

public class SQLServerSelectParser extends AbstractSelectParser {
    
    public SQLServerSelectParser(final SQLParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected void customizedSelect() {
        if (getExprParser().equalAny(DefaultKeyword.FOR)) {
            parseFor();
        }
        if (getExprParser().equalAny(SQLServerKeyword.OFFSET)) {
            ((SQLServerParser) getExprParser()).parseOffset(getSqlContext());
        }
    }
    
    @Override
    public void query() {
        if (getExprParser().skipIfEqual(DefaultKeyword.SELECT)) {
            parseDistinct();
            if (getExprParser().equalAny(SQLServerKeyword.TOP)) {
                // TODO save topContext
                ((SQLServerParser) getExprParser()).parseTop();
            }
            parseSelectList();
        }
        if (getExprParser().equalAny(DefaultKeyword.INTO)) {
            throw new SQLParsingUnsupportedException(getExprParser().getLexer().getCurrentToken().getType());
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        queryRest();
    }
    
    @Override
    protected void parseJoinTable() {
        if (getExprParser().skipIfEqual(DefaultKeyword.WITH)) {
            getExprParser().skipParentheses();
        }
        super.parseJoinTable();
    }
    
    private void parseFor() {
        getExprParser().getLexer().nextToken();
        if (getExprParser().equalAny(SQLServerKeyword.BROWSE)) {
            getExprParser().getLexer().nextToken();
        } else if (getExprParser().skipIfEqual(SQLServerKeyword.XML)) {
            while (true) {
                if (getExprParser().equalAny(SQLServerKeyword.AUTO, SQLServerKeyword.TYPE, SQLServerKeyword.XMLSCHEMA)) {
                    getExprParser().getLexer().nextToken();
                } else if (getExprParser().skipIfEqual(SQLServerKeyword.ELEMENTS)) {
                    getExprParser().skipIfEqual(SQLServerKeyword.XSINIL);
                } else {
                    break;
                }
                if (getExprParser().equalAny(Symbol.COMMA)) {
                    getExprParser().getLexer().nextToken();
                } else {
                    break;
                }
            }
        } else {
            throw new SQLParsingUnsupportedException(getExprParser().getLexer().getCurrentToken().getType());
        }
    }
}
