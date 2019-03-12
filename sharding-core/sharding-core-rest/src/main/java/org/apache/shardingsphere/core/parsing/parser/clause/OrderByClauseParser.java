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

package org.apache.shardingsphere.core.parsing.parser.clause;

import com.google.common.base.Optional;
import lombok.Getter;
import org.apache.shardingsphere.core.constant.OrderDirection;
import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.lexer.dialect.oracle.OracleKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;
import org.apache.shardingsphere.core.parsing.parser.clause.expression.BasicExpressionParser;
import org.apache.shardingsphere.core.parsing.parser.context.orderby.OrderItem;
import org.apache.shardingsphere.core.parsing.parser.dialect.ExpressionParserFactory;
import org.apache.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLIdentifierExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLIgnoreExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPropertyExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.util.SQLUtil;

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
            OrderItem result = new OrderItem(SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName()), orderDirection, getNullOrderDirection());
            Optional<String> alias = selectStatement.getAlias(SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName()));
            if (alias.isPresent()) {
                result.setAlias(alias.get());
            }
            return Optional.of(result);
        }
        if (sqlExpression instanceof SQLPropertyExpression) {
            SQLPropertyExpression sqlPropertyExpression = (SQLPropertyExpression) sqlExpression;
            OrderItem result = new OrderItem(
                    SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()), SQLUtil.getExactlyValue(sqlPropertyExpression.getName()), orderDirection, getNullOrderDirection());
            Optional<String> alias = selectStatement.getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()) + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getName()));
            if (alias.isPresent()) {
                result.setAlias(alias.get());
            }
            return Optional.of(result);
        }
        if (sqlExpression instanceof SQLIgnoreExpression) {
            SQLIgnoreExpression sqlIgnoreExpression = (SQLIgnoreExpression) sqlExpression;
            OrderItem result = new OrderItem(sqlIgnoreExpression.getExpression(), orderDirection, getNullOrderDirection());
            Optional<String> alias = selectStatement.getAlias(sqlIgnoreExpression.getExpression());
            if (alias.isPresent()) {
                result.setAlias(alias.get());
            }
            return Optional.of(result);
        }
        if (sqlExpression instanceof SQLPlaceholderExpression) {
            return Optional.absent();
        }
        throw new SQLParsingException(lexerEngine);
    }
    
    protected abstract OrderDirection getNullOrderDirection();
}
