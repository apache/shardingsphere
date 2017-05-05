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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.update;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.TableToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.UpdateSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Update语句解析器.
 *
 * @author zhangliang
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractUpdateParser {
    
    private final SQLParser exprParser;
    
    private final UpdateSQLContext sqlContext;
    
    private int parametersIndex;
    
    public AbstractUpdateParser(final SQLParser exprParser) {
        this.exprParser = exprParser;
        sqlContext = new UpdateSQLContext();
        sqlContext.setSqlBuilderContext(exprParser.getSqlBuilderContext());
    }
    
    /**
     * 解析Update语句.
     *
     * @return 解析结果
     */
    public UpdateSQLContext parse() {
        exprParser.getLexer().nextToken();
        skipBetweenUpdateAndTable();
        exprParser.parseSingleTable(sqlContext);
        parseSetItems();
        exprParser.skipUntil(DefaultKeyword.WHERE);
        exprParser.setParametersIndex(parametersIndex);
        exprParser.parseWhere(sqlContext);
        return sqlContext;
    }
    
    protected abstract void skipBetweenUpdateAndTable();
    
    private void parseSetItems() {
        exprParser.accept(DefaultKeyword.SET);
        do {
            parseSetItem();
        } while (exprParser.skipIfEqual(Symbol.COMMA));
    }
    
    private void parseSetItem() {
        if (exprParser.equalAny(Symbol.LEFT_PAREN)) {
            exprParser.skipParentheses();
        } else {
            int beginPosition = exprParser.getLexer().getCurrentToken().getEndPosition();
            String literals = exprParser.getLexer().getCurrentToken().getLiterals();
            exprParser.getLexer().nextToken();
            String tableName = sqlContext.getTables().get(0).getName();
            if (exprParser.skipIfEqual(Symbol.DOT)) {
                if (tableName.equalsIgnoreCase(SQLUtil.getExactlyValue(literals))) {
                    exprParser.getSqlBuilderContext().getSqlTokens().add(new TableToken(beginPosition - literals.length(), literals, tableName));
                }
                exprParser.getLexer().nextToken();
            }
        }
        exprParser.skipIfEqual(Symbol.EQ, Symbol.COLON_EQ);
        exprParser.parseExpression(sqlContext);
        parametersIndex = exprParser.getParametersIndex();
    }
}
