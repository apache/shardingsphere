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

package org.apache.shardingsphere.core.parse.antlr.filler.common.segment.impl.dql;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.order.item.ColumnNameOrderByItemSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.order.item.OrderByItemSegment;
import org.apache.shardingsphere.core.parse.parser.context.orderby.OrderItem;
import org.apache.shardingsphere.core.parse.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.parse.parser.token.TableToken;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

import java.util.List;

/**
 * Order item builder.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class OrderItemBuilder {
    
    private final SelectStatement selectStatement;
    
    private final OrderByItemSegment orderByItemSegment;
    
    /**
     * Create order item.
     * 
     * @return order item
     */
    public OrderItem createOrderItem() {
        if (orderByItemSegment instanceof IndexOrderByItemSegment) {
            return createOrderItem((IndexOrderByItemSegment) orderByItemSegment);
        }
        if (orderByItemSegment instanceof ColumnNameOrderByItemSegment) {
            OrderItem result = createOrderItem(selectStatement, (ColumnNameOrderByItemSegment) orderByItemSegment);
            if (result.getOwner().isPresent() && selectStatement.getTables().getTableNames().contains(result.getOwner().get())) {
                // TODO check if order by `xxx`.xx, maybe has problem
                String owner = result.getOwner().get();
                selectStatement.addSQLToken(new TableToken(
                        ((ColumnNameOrderByItemSegment) orderByItemSegment).getStartIndex(), 0, SQLUtil.getExactlyValue(owner), QuoteCharacter.getQuoteCharacter(owner)));
            }
            return result;
        }
        if (orderByItemSegment instanceof ExpressionOrderByItemSegment) {
            return createOrderItem(selectStatement, (ExpressionOrderByItemSegment) orderByItemSegment);
        }
        throw new UnsupportedOperationException();
    }
    
    private OrderItem createOrderItem(final IndexOrderByItemSegment indexOrderByItemSegment) {
        return new OrderItem(indexOrderByItemSegment.getColumnIndex(), indexOrderByItemSegment.getOrderDirection(), indexOrderByItemSegment.getNullOrderDirection());
    }
    
    private OrderItem createOrderItem(final SelectStatement selectStatement, final ColumnNameOrderByItemSegment columnNameOrderByItemSegment) {
        OrderItem result;
        String columnName = SQLUtil.getExactlyValue(columnNameOrderByItemSegment.getColumnName());
        if (columnName.contains(".")) {
            List<String> values = Splitter.on(".").splitToList(columnName);
            result = new OrderItem(values.get(0), values.get(1), columnNameOrderByItemSegment.getOrderDirection(), columnNameOrderByItemSegment.getNullOrderDirection());
        } else {
            result = new OrderItem(columnName, columnNameOrderByItemSegment.getOrderDirection(), columnNameOrderByItemSegment.getNullOrderDirection());
        }
        Optional<String> alias = selectStatement.getAlias(columnName);
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
