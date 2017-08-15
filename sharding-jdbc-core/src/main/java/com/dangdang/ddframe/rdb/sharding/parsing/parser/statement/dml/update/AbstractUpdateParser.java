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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dml.update;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.AbstractSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatementParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dml.DMLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Update语句解析器.
 *
 * @author zhangliang
 */
public abstract class AbstractUpdateParser implements SQLStatementParser {
    
    private final AbstractSQLParser sqlParser;
    
    private final DMLStatement updateStatement;
    
    @Getter(AccessLevel.NONE)
    private int parametersIndex;
    
    public AbstractUpdateParser(final AbstractSQLParser sqlParser) {
        this.sqlParser = sqlParser;
        updateStatement = new DMLStatement();
    }
    
    @Override
    public DMLStatement parse() {
        sqlParser.getLexer().nextToken();
        sqlParser.skipAll(getSkippedKeywordsBetweenUpdateAndTable());
        if (sqlParser.equalAny(getUnsupportedKeywordsBetweenUpdateAndTable())) {
            throw new SQLParsingUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
        }
        sqlParser.parseSingleTable(updateStatement);
        parseSetItems();
        sqlParser.skipUntil(DefaultKeyword.WHERE);
        sqlParser.setParametersIndex(parametersIndex);
        sqlParser.parseWhere(updateStatement);
        return updateStatement;
    }
    
    protected Keyword[] getSkippedKeywordsBetweenUpdateAndTable() {
        return new Keyword[0];
    }
    
    protected Keyword[] getUnsupportedKeywordsBetweenUpdateAndTable() {
        return new Keyword[0];
    }
    
    private void parseSetItems() {
        sqlParser.accept(DefaultKeyword.SET);
        do {
            parseSetItem();
        } while (sqlParser.skipIfEqual(Symbol.COMMA));
    }
    
    private void parseSetItem() {
        parseSetColumn();
        sqlParser.skipIfEqual(Symbol.EQ, Symbol.COLON_EQ);
        parseSetValue();
    }
    
    private void parseSetColumn() {
        if (sqlParser.equalAny(Symbol.LEFT_PAREN)) {
            sqlParser.skipParentheses();
            return;
        }
        int beginPosition = sqlParser.getLexer().getCurrentToken().getEndPosition();
        String literals = sqlParser.getLexer().getCurrentToken().getLiterals();
        sqlParser.getLexer().nextToken();
        if (sqlParser.skipIfEqual(Symbol.DOT)) {
            if (updateStatement.getTables().getSingleTableName().equalsIgnoreCase(SQLUtil.getExactlyValue(literals))) {
                updateStatement.getSqlTokens().add(new TableToken(beginPosition - literals.length(), literals));
            }
            sqlParser.getLexer().nextToken();
        }
    }
    
    private void parseSetValue() {
        sqlParser.parseExpression(updateStatement);
        parametersIndex = sqlParser.getParametersIndex();
    }
}
