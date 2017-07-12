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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.CommonSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.google.common.base.Optional;

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
                ((SQLServerParser) getSqlParser()).parseTop(getSelectStatement());
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
    protected boolean isRowNumberSelectItem() {
        return getSqlParser().getLexer().getCurrentToken().getLiterals().equalsIgnoreCase("ROW_NUMBER");
    }
    
    @Override
    protected SelectItem parseRowNumberSelectItem(final SelectStatement selectStatement) {
        getSqlParser().getLexer().nextToken();
        if (getSqlParser().equalAny(Symbol.LEFT_PAREN)) {
            getSqlParser().skipUntil(DefaultKeyword.OVER);
            getSqlParser().getLexer().nextToken();
            getSqlParser().skipIfEqual(Symbol.LEFT_PAREN);
            selectStatement.getOrderByItems().addAll(parseOrderBy());
            getSqlParser().skipIfEqual(Symbol.RIGHT_PAREN);
            getSqlParser().skipUntil(DefaultKeyword.AS);
            getSqlParser().getLexer().nextToken();
            SelectItem result = new CommonSelectItem("ROW_NUMBER", Optional.of(getSqlParser().getLexer().getCurrentToken().getLiterals()), false);
            getSqlParser().getLexer().nextToken();
            return result;
        }
        return new CommonSelectItem("ROW_NUMBER", getSqlParser().parseAlias(), false);
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
