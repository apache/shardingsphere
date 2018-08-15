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

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.OrderDirection;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.dialect.oracle.OracleKeyword;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.clause.expression.BasicExpressionParser;
import io.shardingsphere.core.parsing.parser.context.OrderItem;
import io.shardingsphere.core.parsing.parser.dialect.ExpressionParserFactory;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import io.shardingsphere.core.parsing.parser.expression.SQLExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLIdentifierExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLIgnoreExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLPropertyExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.util.SQLUtil;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * Order by clause parser.
 *
 * @author zhangliang
 */
public abstract class OrderByClauseParser implements SQLClauseParser {
    
    @Getter
    private final LexerEngine lexerEngine;
    
    private final BasicExpressionParser basicExpressionParser;
    
    public OrderByClauseParser(final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        basicExpressionParser = ExpressionParserFactory.createBasicExpressionParser(lexerEngine);
    }
    
    /**
     * Parse order by.
     *
     * @param selectStatement select statement
     */
    public final void parse(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(DefaultKeyword.ORDER)) {
            return;
        }
        List<OrderItem> result = new LinkedList<>();
        lexerEngine.skipIfEqual(OracleKeyword.SIBLINGS);
        lexerEngine.accept(DefaultKeyword.BY);
        do {
            Optional<OrderItem> orderItem = parseSelectOrderByItem(selectStatement);
            if (orderItem.isPresent()) {
                result.add(orderItem.get());
            }
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        selectStatement.getOrderByItems().addAll(result);
    }
    
    private Optional<OrderItem> parseSelectOrderByItem(final SelectStatement selectStatement) {
        SQLExpression sqlExpression = basicExpressionParser.parse(selectStatement);
        OrderDirection orderDirection = OrderDirection.ASC;
        if (lexerEngine.skipIfEqual(DefaultKeyword.ASC)) {
            orderDirection = OrderDirection.ASC;
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.DESC)) {
            orderDirection = OrderDirection.DESC;
        }
        if (sqlExpression instanceof SQLTextExpression) {
            return Optional.of(new OrderItem(SQLUtil.getExactlyValue(((SQLTextExpression) sqlExpression).getText()), orderDirection, getNullOrderDirection()));
        }
        if (sqlExpression instanceof SQLNumberExpression) {
            return Optional.of(new OrderItem(((SQLNumberExpression) sqlExpression).getNumber().intValue(), orderDirection, getNullOrderDirection()));
        }
        if (sqlExpression instanceof SQLIdentifierExpression) {
            return Optional.of(new OrderItem(SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName()),
                    orderDirection, getNullOrderDirection(), selectStatement.getAlias(SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName()))));
        }
        if (sqlExpression instanceof SQLPropertyExpression) {
            SQLPropertyExpression sqlPropertyExpression = (SQLPropertyExpression) sqlExpression;
            return Optional.of(
                new OrderItem(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()), SQLUtil.getExactlyValue(sqlPropertyExpression.getName()), orderDirection, getNullOrderDirection(),
                    selectStatement.getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()) + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getName()))));
        }
        if (sqlExpression instanceof SQLIgnoreExpression) {
            SQLIgnoreExpression sqlIgnoreExpression = (SQLIgnoreExpression) sqlExpression;
            return Optional.of(new OrderItem(sqlIgnoreExpression.getExpression(), orderDirection, getNullOrderDirection(), selectStatement.getAlias(sqlIgnoreExpression.getExpression())));
        }
        if (sqlExpression instanceof SQLPlaceholderExpression) {
            return Optional.absent();
        }
        throw new SQLParsingException(lexerEngine);
    }
    
    protected abstract OrderDirection getNullOrderDirection();
}
