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

package org.apache.shardingsphere.core.parse.parser.clause;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.constant.OrderDirection;
import org.apache.shardingsphere.core.parse.lexer.LexerEngine;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.lexer.token.Keyword;
import org.apache.shardingsphere.core.parse.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.parser.clause.expression.BasicExpressionParser;
import org.apache.shardingsphere.core.parse.parser.context.orderby.OrderItem;
import org.apache.shardingsphere.core.parse.parser.dialect.ExpressionParserFactory;
import org.apache.shardingsphere.core.parse.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLIdentifierExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLIgnoreExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLPropertyExpression;
import org.apache.shardingsphere.core.parse.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

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
        selectStatement.setGroupByLastIndex(lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length() - 1);
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
            orderItem = new OrderItem(
                    SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()), SQLUtil.getExactlyValue(sqlPropertyExpression.getName()), orderDirection, OrderDirection.ASC);
            Optional<String> alias = selectStatement.getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName() + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getName())));
            if (alias.isPresent()) {
                orderItem.setAlias(alias.get());
            }
        } else if (sqlExpression instanceof SQLIdentifierExpression) {
            SQLIdentifierExpression sqlIdentifierExpression = (SQLIdentifierExpression) sqlExpression;
            orderItem = new OrderItem(SQLUtil.getExactlyValue(sqlIdentifierExpression.getName()), orderDirection, OrderDirection.ASC);
            Optional<String> alias = selectStatement.getAlias(SQLUtil.getExactlyValue(sqlIdentifierExpression.getName()));
            if (alias.isPresent()) {
                orderItem.setAlias(alias.get());
            }
        } else if (sqlExpression instanceof SQLIgnoreExpression) {
            SQLIgnoreExpression sqlIgnoreExpression = (SQLIgnoreExpression) sqlExpression;
            orderItem = new OrderItem(sqlIgnoreExpression.getExpression(), orderDirection, OrderDirection.ASC);
            Optional<String> alias = selectStatement.getAlias(sqlIgnoreExpression.getExpression());
            if (alias.isPresent()) {
                orderItem.setAlias(alias.get());
            }
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
