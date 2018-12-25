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

package io.shardingsphere.core.parsing.antlr.filler.impl;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.OrderByItemSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.OrderBySegment;
import io.shardingsphere.core.parsing.parser.context.OrderItem;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.util.SQLUtil;

/**
 * Order by filler.
 *
 * @author duhongjun
 */
public final class OrderByFiller implements SQLStatementFiller<OrderBySegment> {
    
    @Override
    public void fill(final OrderBySegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        if (!selectStatement.getSubQueryStatements().isEmpty()) {
            return;
        }
        for (OrderByItemSegment each : sqlSegment.getOrderByItems()) {
            selectStatement.getOrderByItems().add(buildOrderItemAndFillToken(selectStatement, each, sql));
        }
    }
    
    protected OrderItem buildOrderItemAndFillToken(final SelectStatement selectStatement, final OrderByItemSegment orderByItemSegment, final String sql) {
        if (-1 < orderByItemSegment.getIndex()) {
            return new OrderItem(orderByItemSegment.getIndex(), orderByItemSegment.getOrderDirection(), orderByItemSegment.getNullOrderDirection());
        }
        String expression = sql.substring(orderByItemSegment.getExpressionStartPosition(), orderByItemSegment.getExpressionEndPosition() + 1);
        if (!orderByItemSegment.isIdentifier()) {
            return new OrderItem(expression, orderByItemSegment.getOrderDirection(), orderByItemSegment.getNullOrderDirection(), selectStatement.getAlias(expression));
        }
        expression = SQLUtil.getExactlyValue(expression);
        int dotPosition = expression.indexOf(".");
        String name = expression;
        if (0 < dotPosition) {
            name = expression.substring(dotPosition + 1);
            String owner = expression.substring(0, dotPosition);
            if (selectStatement.getTables().getTableNames().contains(owner)) {
                selectStatement.addSQLToken(new TableToken(orderByItemSegment.getExpressionStartPosition(), 0, owner));
            }
            return new OrderItem(owner, name, orderByItemSegment.getOrderDirection(), orderByItemSegment.getNullOrderDirection(), selectStatement.getAlias(owner + "." + name));
        }
        return new OrderItem(name, orderByItemSegment.getOrderDirection(), orderByItemSegment.getNullOrderDirection(), selectStatement.getAlias(name));
    }
}
