/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.parsing.parser.clause;

import io.shardingjdbc.core.constant.OrderDirection;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.oracle.OracleKeyword;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.clause.expression.BasicExpressionParser;
import io.shardingjdbc.core.parsing.parser.context.OrderItem;
import io.shardingjdbc.core.parsing.parser.dialect.ExpressionParserFactory;
import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;
import io.shardingjdbc.core.parsing.parser.expression.SQLExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLIdentifierExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLIgnoreExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLPropertyExpression;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.util.SQLUtil;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * Order by clause parser.
 *
 * @author zhangliang
 */
public class OrderByClauseParser implements SQLClauseParser {
    
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
            result.add(parseSelectOrderByItem(selectStatement));
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        selectStatement.getOrderByItems().addAll(result);
    }
    
    private OrderItem parseSelectOrderByItem(final SelectStatement selectStatement) {
        SQLExpression sqlExpression = basicExpressionParser.parse(selectStatement);
        OrderDirection orderDirection = OrderDirection.ASC;
        if (lexerEngine.skipIfEqual(DefaultKeyword.ASC)) {
            orderDirection = OrderDirection.ASC;
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.DESC)) {
            orderDirection = OrderDirection.DESC;
        }
        if (sqlExpression instanceof SQLNumberExpression) {
            return new OrderItem(((SQLNumberExpression) sqlExpression).getNumber().intValue(), orderDirection, getNullOrderDirection());
        }
        if (sqlExpression instanceof SQLIdentifierExpression) {
            return new OrderItem(SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName()),
                    orderDirection, getNullOrderDirection(), selectStatement.getAlias(SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName())));
        }
        if (sqlExpression instanceof SQLPropertyExpression) {
            SQLPropertyExpression sqlPropertyExpression = (SQLPropertyExpression) sqlExpression;
            return new OrderItem(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()), SQLUtil.getExactlyValue(sqlPropertyExpression.getName()), orderDirection, getNullOrderDirection(),
                    selectStatement.getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()) + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getName())));
        }
        if (sqlExpression instanceof SQLIgnoreExpression) {
            SQLIgnoreExpression sqlIgnoreExpression = (SQLIgnoreExpression) sqlExpression;
            return new OrderItem(sqlIgnoreExpression.getExpression(), orderDirection, getNullOrderDirection(), selectStatement.getAlias(sqlIgnoreExpression.getExpression()));
        }
        throw new SQLParsingException(lexerEngine);
    }
    
    protected OrderDirection getNullOrderDirection() {
        return OrderDirection.ASC;
    }
}
