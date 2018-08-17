/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.clause;

import io.shardingsphere.core.constant.OrderDirection;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.clause.expression.BasicExpressionParser;
import io.shardingsphere.core.parsing.parser.context.OrderItem;
import io.shardingsphere.core.parsing.parser.dialect.ExpressionParserFactory;
import io.shardingsphere.core.parsing.parser.expression.SQLExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLIdentifierExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLIgnoreExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLPropertyExpression;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.util.SQLUtil;

/**
 * Group by clause parser.
 *
 * @author zhangliang
 */
public abstract class GroupByClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    private final BasicExpressionParser basicExpressionParser;
    
    public GroupByClauseParser(final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        basicExpressionParser = ExpressionParserFactory.createBasicExpressionParser(lexerEngine);
    }
    
    /**
     * Parse group by.
     *
     * @param selectStatement select statement
     */
    public final void parse(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(DefaultKeyword.GROUP)) {
            return;
        }
        lexerEngine.accept(DefaultKeyword.BY);
        while (true) {
            addGroupByItem(basicExpressionParser.parse(selectStatement), selectStatement);
            if (!lexerEngine.equalAny(Symbol.COMMA)) {
                break;
            }
            lexerEngine.nextToken();
        }
        lexerEngine.skipAll(getSkippedKeywordAfterGroupBy());
        selectStatement.setGroupByLastPosition(lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length());
    }
    
    private void addGroupByItem(final SQLExpression sqlExpression, final SelectStatement selectStatement) {
        lexerEngine.unsupportedIfEqual(getUnsupportedKeywordBeforeGroupByItem());
        OrderDirection orderDirection = OrderDirection.ASC;
        if (lexerEngine.equalAny(DefaultKeyword.ASC)) {
            lexerEngine.nextToken();
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.DESC)) {
            orderDirection = OrderDirection.DESC;
        }
        OrderItem orderItem;
        if (sqlExpression instanceof SQLPropertyExpression) {
            SQLPropertyExpression sqlPropertyExpression = (SQLPropertyExpression) sqlExpression;
            orderItem = new OrderItem(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()), SQLUtil.getExactlyValue(sqlPropertyExpression.getName()), orderDirection, OrderDirection.ASC,
                    selectStatement.getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName() + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getName()))));
        } else if (sqlExpression instanceof SQLIdentifierExpression) {
            SQLIdentifierExpression sqlIdentifierExpression = (SQLIdentifierExpression) sqlExpression;
            orderItem = new OrderItem(SQLUtil.getExactlyValue(sqlIdentifierExpression.getName()), 
                    orderDirection, OrderDirection.ASC, selectStatement.getAlias(SQLUtil.getExactlyValue(sqlIdentifierExpression.getName())));
        } else if (sqlExpression instanceof SQLIgnoreExpression) {
            SQLIgnoreExpression sqlIgnoreExpression = (SQLIgnoreExpression) sqlExpression;
            orderItem = new OrderItem(sqlIgnoreExpression.getExpression(), orderDirection, OrderDirection.ASC, selectStatement.getAlias(sqlIgnoreExpression.getExpression()));
        } else if (sqlExpression instanceof SQLNumberExpression) {
            orderItem = new OrderItem(((SQLNumberExpression) sqlExpression).getNumber().intValue(), orderDirection, orderDirection);
        } else {
            return;
        }
        selectStatement.getGroupByItems().add(orderItem);
    }
    
    protected abstract Keyword[] getUnsupportedKeywordBeforeGroupByItem();
    
    protected abstract Keyword[] getSkippedKeywordAfterGroupBy();
}
