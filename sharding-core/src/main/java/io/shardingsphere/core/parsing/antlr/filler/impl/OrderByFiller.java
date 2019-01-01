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

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.OrderBySegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.item.ColumnNameOrderByItemSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.item.ExpressionOrderByItemSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.item.IndexOrderByItemSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.order.item.OrderByItemSegment;
import io.shardingsphere.core.parsing.parser.context.orderby.OrderItem;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.util.SQLUtil;

import java.util.List;

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
            selectStatement.getOrderByItems().add(createOrderItem(selectStatement, each));
        }
    }
    
    /**
     * create order item.
     * 
     * @param selectStatement select statement
     * @param orderByItemSegment order by item segment
     * @return order item
     */
    public OrderItem createOrderItem(final SelectStatement selectStatement, final OrderByItemSegment orderByItemSegment) {
        if (orderByItemSegment instanceof IndexOrderByItemSegment) {
            return createOrderItem((IndexOrderByItemSegment) orderByItemSegment);
        }
        if (orderByItemSegment instanceof ColumnNameOrderByItemSegment) {
            return createOrderItem(selectStatement, (ColumnNameOrderByItemSegment) orderByItemSegment);
        }
        if (orderByItemSegment instanceof ExpressionOrderByItemSegment) {
            return createOrderItem(selectStatement, (ExpressionOrderByItemSegment) orderByItemSegment);
        }
        throw new UnsupportedOperationException();
    }
    
    private OrderItem createOrderItem(final IndexOrderByItemSegment indexOrderByItemSegment) {
        return new OrderItem(indexOrderByItemSegment.getIndex(), indexOrderByItemSegment.getOrderDirection(), indexOrderByItemSegment.getNullOrderDirection());
    }
    
    private OrderItem createOrderItem(final SelectStatement selectStatement, final ColumnNameOrderByItemSegment columnNameOrderByItemSegment) {
        String columnName = SQLUtil.getExactlyValue(columnNameOrderByItemSegment.getColumnName());
        if (!columnName.contains(".")) {
            OrderItem result = new OrderItem(columnName, columnNameOrderByItemSegment.getOrderDirection(), columnNameOrderByItemSegment.getNullOrderDirection());
            Optional<String> alias = selectStatement.getAlias(columnName);
            if (alias.isPresent()) {
                result.setAlias(alias.get());
            }
            return result;
        }
        List<String> values = Splitter.on(".").splitToList(columnName);
        String owner = values.get(0);
        String name = values.get(1);
        if (selectStatement.getTables().getTableNames().contains(owner)) {
            selectStatement.addSQLToken(new TableToken(columnNameOrderByItemSegment.getBeginPosition(), 0, owner));
        }
        OrderItem result = new OrderItem(owner, name, columnNameOrderByItemSegment.getOrderDirection(), columnNameOrderByItemSegment.getNullOrderDirection());
        Optional<String> alias = selectStatement.getAlias(owner + "." + name);
        if (alias.isPresent()) {
            result.setAlias(alias.get());
        }
        return result;
    }
    
    private OrderItem createOrderItem(final SelectStatement selectStatement, final ExpressionOrderByItemSegment expressionOrderByItemSegment) {
        OrderItem result = new OrderItem(expressionOrderByItemSegment.getExpression(), expressionOrderByItemSegment.getOrderDirection(), expressionOrderByItemSegment.getNullOrderDirection());
        Optional<String> alias = selectStatement.getAlias(expressionOrderByItemSegment.getExpression());
        if (alias.isPresent()) {
            result.setAlias(alias.get());
        }
        return result;
    }
}
