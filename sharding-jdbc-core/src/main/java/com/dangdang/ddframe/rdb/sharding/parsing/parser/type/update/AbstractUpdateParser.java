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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.type.update;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.UpdateSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.type.SQLParser;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Update语句解析器.
 *
 * @author zhangliang
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractUpdateParser implements SQLParser {
    
    private final com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser sqlParser;
    
    private final UpdateSQLContext sqlContext;
    
    private int parametersIndex;
    
    public AbstractUpdateParser(final com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser sqlParser) {
        this.sqlParser = sqlParser;
        sqlContext = new UpdateSQLContext();
    }
    
    @Override
    public UpdateSQLContext parse() {
        sqlParser.getLexer().nextToken();
        skipBetweenUpdateAndTable();
        sqlParser.parseSingleTable(sqlContext);
        parseSetItems();
        sqlParser.skipUntil(DefaultKeyword.WHERE);
        sqlParser.setParametersIndex(parametersIndex);
        sqlParser.parseWhere(sqlContext);
        return sqlContext;
    }
    
    protected abstract void skipBetweenUpdateAndTable();
    
    private void parseSetItems() {
        sqlParser.accept(DefaultKeyword.SET);
        do {
            parseSetItem();
        } while (sqlParser.skipIfEqual(Symbol.COMMA));
    }
    
    private void parseSetItem() {
        if (sqlParser.equalAny(Symbol.LEFT_PAREN)) {
            sqlParser.skipParentheses();
        } else {
            int beginPosition = sqlParser.getLexer().getCurrentToken().getEndPosition();
            String literals = sqlParser.getLexer().getCurrentToken().getLiterals();
            sqlParser.getLexer().nextToken();
            if (sqlParser.skipIfEqual(Symbol.DOT)) {
                if (sqlContext.getTables().get(0).getName().equalsIgnoreCase(SQLUtil.getExactlyValue(literals))) {
                    sqlContext.getSqlTokens().add(new TableToken(beginPosition - literals.length(), literals));
                }
                sqlParser.getLexer().nextToken();
            }
        }
        sqlParser.skipIfEqual(Symbol.EQ, Symbol.COLON_EQ);
        sqlParser.parseExpression(sqlContext);
        parametersIndex = sqlParser.getParametersIndex();
    }
}
