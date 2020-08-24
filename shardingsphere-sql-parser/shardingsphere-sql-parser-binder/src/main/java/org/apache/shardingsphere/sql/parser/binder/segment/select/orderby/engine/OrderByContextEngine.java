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

package org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.engine;

import org.apache.shardingsphere.sql.parser.sql.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;

import java.util.LinkedList;
import java.util.List;

/**
 * Order by context engine.
 */
public final class OrderByContextEngine {
    
    /**
     * Create order by context.
     * 
     * @param selectStatement select statement
     * @param groupByContext group by context
     * @return order by context
     */
    public OrderByContext createOrderBy(final SelectStatement selectStatement, final GroupByContext groupByContext) {
        if (!selectStatement.getOrderBy().isPresent() || selectStatement.getOrderBy().get().getOrderByItems().isEmpty()) {
            OrderByContext orderByItems = createOrderByContextForDistinctRowWithoutGroupBy(selectStatement, groupByContext);
            return null != orderByItems ? orderByItems : new OrderByContext(groupByContext.getItems(), !groupByContext.getItems().isEmpty());
        }
        List<OrderByItem> orderByItems = new LinkedList<>();
        for (OrderByItemSegment each : selectStatement.getOrderBy().get().getOrderByItems()) {
            OrderByItem orderByItem = new OrderByItem(each);
            if (each instanceof IndexOrderByItemSegment) {
                orderByItem.setIndex(((IndexOrderByItemSegment) each).getColumnIndex());
            }
            orderByItems.add(orderByItem);
        }
        return new OrderByContext(orderByItems, false);
    }

    private OrderByContext createOrderByContextForDistinctRowWithoutGroupBy(final SelectStatement selectStatement, final GroupByContext groupByContext) {
        if (groupByContext.getItems().isEmpty() && selectStatement.getProjections().isDistinctRow()) {
            int index = 0;
            List<OrderByItem> orderByItems = new LinkedList<>();
            for (ProjectionSegment projectionSegment : selectStatement.getProjections().getProjections()) {
                if (projectionSegment instanceof ColumnProjectionSegment) {
                    ColumnProjectionSegment columnProjectionSegment = (ColumnProjectionSegment) projectionSegment;
                    ColumnOrderByItemSegment columnOrderByItemSegment = new ColumnOrderByItemSegment(columnProjectionSegment.getColumn(), OrderDirection.ASC);
                    OrderByItem item = new OrderByItem(columnOrderByItemSegment);
                    item.setIndex(index++);
                    orderByItems.add(item);
                }
            }
            if (!orderByItems.isEmpty()) {
                return new OrderByContext(orderByItems, true);
            }
        }
        return null;
    }
}
