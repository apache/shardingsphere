/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parse.parser.clause.expression;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.lexer.LexerEngine;
import org.apache.shardingsphere.core.parse.lexer.token.Literals;
import org.apache.shardingsphere.core.parse.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLIdentifierExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLIgnoreExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLPropertyExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.parse.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parse.parser.token.TableToken;
import org.apache.shardingsphere.core.parse.util.SQLUtil;
import org.apache.shardingsphere.core.util.NumberUtil;

/**
 * Basic expression parser.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class BasicExpressionParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * Parse expression.
     *
     * @param sqlStatement SQL statement
     * @return expression
     */
    public SQLExpression parse(final SQLStatement sqlStatement) {
        int beginPosition = lexerEngine.getCurrentToken().getEndPosition();
        SQLExpression result = parseExpression(sqlStatement);
        if (result instanceof SQLPropertyExpression) {
            setTableToken(sqlStatement, beginPosition, (SQLPropertyExpression) result);
        }
        return result;
    }
    
    // TODO complete more expression parse
    private SQLExpression parseExpression(final SQLStatement sqlStatement) {
        String literals = lexerEngine.getCurrentToken().getLiterals();
        final int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - literals.length();
        final SQLExpression expression = getExpression(literals, sqlStatement);
        lexerEngine.nextToken();
        if (lexerEngine.skipIfEqual(Symbol.DOT)) {
            String property = lexerEngine.getCurrentToken().getLiterals();
            lexerEngine.nextToken();
            return skipIfCompositeExpression(sqlStatement)
                    ? new SQLIgnoreExpression(lexerEngine.getInput().substring(beginPosition, lexerEngine.getCurrentToken().getEndPosition()))
                    : new SQLPropertyExpression(new SQLIdentifierExpression(literals), property);
        }
        if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
            lexerEngine.skipParentheses(sqlStatement);
            skipRestCompositeExpression(sqlStatement);
            return new SQLIgnoreExpression(lexerEngine.getInput().substring(beginPosition,
                    lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length()).trim());
        }
        return skipIfCompositeExpression(sqlStatement)
                ? new SQLIgnoreExpression(lexerEngine.getInput().substring(beginPosition, lexerEngine.getCurrentToken().getEndPosition())) : expression;
    }
    
    private SQLExpression getExpression(final String literals, final SQLStatement sqlStatement) {
        if (lexerEngine.equalAny(Symbol.QUESTION)) {
            sqlStatement.increaseParametersIndex();
            return new SQLPlaceholderExpression(sqlStatement.getParametersIndex() - 1);
        }
        if (lexerEngine.equalAny(Literals.CHARS)) {
            return new SQLTextExpression(literals);
        }
        if (lexerEngine.equalAny(Literals.INT)) {
            return new SQLNumberExpression(NumberUtil.getExactlyNumber(literals, 10));
        }
        if (lexerEngine.equalAny(Literals.FLOAT)) {
            return new SQLNumberExpression(Double.parseDouble(literals));
        }
        if (lexerEngine.equalAny(Literals.HEX)) {
            return new SQLNumberExpression(NumberUtil.getExactlyNumber(literals, 16));
        }
        if (lexerEngine.equalAny(Literals.IDENTIFIER)) {
            return new SQLIdentifierExpression(SQLUtil.getExactlyValue(literals));
        }
        return new SQLIgnoreExpression(literals);
    }
    
    private boolean skipIfCompositeExpression(final SQLStatement sqlStatement) {
        if (lexerEngine.equalAny(
                Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH, Symbol.PERCENT, Symbol.AMP, Symbol.BAR, Symbol.DOUBLE_AMP, Symbol.DOUBLE_BAR, Symbol.CARET, Symbol.DOT, Symbol.LEFT_PAREN)) {
            lexerEngine.skipParentheses(sqlStatement);
            skipRestCompositeExpression(sqlStatement);
            return true;
        }
        if ((Literals.INT == lexerEngine.getCurrentToken().getType() || Literals.FLOAT == lexerEngine.getCurrentToken().getType()) && lexerEngine.getCurrentToken().getLiterals().startsWith("-")) {
            lexerEngine.nextToken();
            return true;
        }
        return false;
    }
    
    private void skipRestCompositeExpression(final SQLStatement sqlStatement) {
        while (lexerEngine.skipIfEqual(Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH, Symbol.PERCENT, Symbol.AMP, Symbol.BAR, Symbol.DOUBLE_AMP, Symbol.DOUBLE_BAR, Symbol.CARET, Symbol.DOT)) {
            if (lexerEngine.equalAny(Symbol.QUESTION)) {
                sqlStatement.increaseParametersIndex();
            }
            lexerEngine.nextToken();
            lexerEngine.skipParentheses(sqlStatement);
        }
    }
    
    private void setTableToken(final SQLStatement sqlStatement, final int beginPosition, final SQLPropertyExpression propertyExpr) {
        String owner = propertyExpr.getOwner().getName();
        if (sqlStatement.getTables().getTableNames().contains(SQLUtil.getExactlyValue(propertyExpr.getOwner().getName()))) {
            sqlStatement.addSQLToken(new TableToken(beginPosition - owner.length(), 0, SQLUtil.getExactlyValue(owner), QuoteCharacter.getQuoteCharacter(owner)));
        }
    }
}
