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
import lombok.RequiredArgsConstructor;

/**
 * Update语句解析器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class AbstractUpdateParser implements SQLStatementParser {
    
    private final AbstractSQLParser sqlParser;
    
    @Getter(AccessLevel.NONE)
    private int parametersIndex;
    
    @Override
    public DMLStatement parse() {
        sqlParser.getLexer().nextToken();
        sqlParser.skipAll(getSkippedKeywordsBetweenUpdateAndTable());
        if (sqlParser.equalAny(getUnsupportedKeywordsBetweenUpdateAndTable())) {
            throw new SQLParsingUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
        }
        DMLStatement result = new DMLStatement();
        sqlParser.parseSingleTable(result);
        parseSetItems(result);
        sqlParser.skipUntil(DefaultKeyword.WHERE);
        sqlParser.setParametersIndex(parametersIndex);
        sqlParser.parseWhere(result);
        return result;
    }
    
    protected Keyword[] getSkippedKeywordsBetweenUpdateAndTable() {
        return new Keyword[0];
    }
    
    protected Keyword[] getUnsupportedKeywordsBetweenUpdateAndTable() {
        return new Keyword[0];
    }
    
    private void parseSetItems(final DMLStatement updateStatement) {
        sqlParser.accept(DefaultKeyword.SET);
        do {
            parseSetItem(updateStatement);
        } while (sqlParser.skipIfEqual(Symbol.COMMA));
    }
    
    private void parseSetItem(final DMLStatement updateStatement) {
        parseSetColumn(updateStatement);
        sqlParser.skipIfEqual(Symbol.EQ, Symbol.COLON_EQ);
        parseSetValue(updateStatement);
    }
    
    private void parseSetColumn(final DMLStatement updateStatement) {
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
    
    private void parseSetValue(final DMLStatement updateStatement) {
        sqlParser.parseExpression(updateStatement);
        parametersIndex = sqlParser.getParametersIndex();
    }
}
