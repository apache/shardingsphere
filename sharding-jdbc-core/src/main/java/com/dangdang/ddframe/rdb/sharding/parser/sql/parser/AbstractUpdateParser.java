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

package com.dangdang.ddframe.rdb.sharding.parser.sql.parser;

import com.dangdang.ddframe.rdb.sharding.parser.sql.context.TableToken;
import com.dangdang.ddframe.rdb.sharding.parser.sql.context.UpdateSQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Symbol;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Update语句解析器.
 *
 * @author zhangliang
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractUpdateParser {
    
    private final SQLExprParser exprParser;
    
    private final UpdateSQLContext sqlContext;
    
    private int parametersIndex;
    
    public AbstractUpdateParser(final SQLExprParser exprParser) {
        this.exprParser = exprParser;
        sqlContext = new UpdateSQLContext(exprParser.getLexer().getInput());
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
        exprParser.getLexer().skipUntil(DefaultKeyword.WHERE);
        exprParser.setParametersIndex(parametersIndex);
        Optional<ConditionContext> conditionContext = exprParser.parseWhere(sqlContext);
        if (conditionContext.isPresent()) {
            sqlContext.getConditionContexts().add(conditionContext.get());
        }
        return sqlContext;
    }
    
    protected abstract void skipBetweenUpdateAndTable();
    
    private void parseSetItems() {
        exprParser.getLexer().accept(DefaultKeyword.SET);
        do {
            parseSetItem();
        } while (exprParser.getLexer().skipIfEqual(Symbol.COMMA));
    }
    
    private void parseSetItem() {
        if (exprParser.getLexer().equalToken(Symbol.LEFT_PAREN)) {
            exprParser.getLexer().skipParentheses();
        } else {
            int beginPosition = exprParser.getLexer().getPosition();
            String literals = exprParser.getLexer().getLiterals();
            exprParser.getLexer().nextToken();
            String tableName = sqlContext.getTables().get(0).getName();
            if (exprParser.getLexer().skipIfEqual(Symbol.DOT)) {
                if (tableName.equalsIgnoreCase(SQLUtil.getExactlyValue(literals))) {
                    sqlContext.getSqlTokens().add(new TableToken(beginPosition - literals.length(), literals, tableName));
                }
                exprParser.getLexer().nextToken();
            }
        }
        exprParser.getLexer().skipIfEqual(Symbol.EQ, Symbol.COLON_EQ);
        exprParser.parseExpr(sqlContext);
        parametersIndex = exprParser.getParametersIndex();
    }
}
