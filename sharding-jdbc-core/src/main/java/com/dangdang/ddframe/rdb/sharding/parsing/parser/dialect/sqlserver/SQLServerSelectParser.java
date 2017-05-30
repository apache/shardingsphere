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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;

public class SQLServerSelectParser extends AbstractSelectParser {
    
    public SQLServerSelectParser(final SQLParser sqlParser) {
        super(sqlParser);
    }
    
    @Override
    protected void customizedSelect() {
        if (getSqlParser().equalAny(DefaultKeyword.FOR)) {
            parseFor();
        }
        if (getSqlParser().equalAny(SQLServerKeyword.OFFSET)) {
            ((SQLServerParser) getSqlParser()).parseOffset(getSelectStatement());
        }
    }
    
    @Override
    public void query() {
        if (getSqlParser().skipIfEqual(DefaultKeyword.SELECT)) {
            parseDistinct();
            if (getSqlParser().equalAny(SQLServerKeyword.TOP)) {
                // TODO save topContext
                ((SQLServerParser) getSqlParser()).parseTop();
            }
            parseSelectList();
        }
        if (getSqlParser().equalAny(DefaultKeyword.INTO)) {
            throw new SQLParsingUnsupportedException(getSqlParser().getLexer().getCurrentToken().getType());
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        queryRest();
    }
    
    @Override
    protected void parseJoinTable() {
        if (getSqlParser().skipIfEqual(DefaultKeyword.WITH)) {
            getSqlParser().skipParentheses();
        }
        super.parseJoinTable();
    }
    
    private void parseFor() {
        getSqlParser().getLexer().nextToken();
        if (getSqlParser().equalAny(SQLServerKeyword.BROWSE)) {
            getSqlParser().getLexer().nextToken();
        } else if (getSqlParser().skipIfEqual(SQLServerKeyword.XML)) {
            while (true) {
                if (getSqlParser().equalAny(SQLServerKeyword.AUTO, SQLServerKeyword.TYPE, SQLServerKeyword.XMLSCHEMA)) {
                    getSqlParser().getLexer().nextToken();
                } else if (getSqlParser().skipIfEqual(SQLServerKeyword.ELEMENTS)) {
                    getSqlParser().skipIfEqual(SQLServerKeyword.XSINIL);
                } else {
                    break;
                }
                if (getSqlParser().equalAny(Symbol.COMMA)) {
                    getSqlParser().getLexer().nextToken();
                } else {
                    break;
                }
            }
        } else {
            throw new SQLParsingUnsupportedException(getSqlParser().getLexer().getCurrentToken().getType());
        }
    }
}
