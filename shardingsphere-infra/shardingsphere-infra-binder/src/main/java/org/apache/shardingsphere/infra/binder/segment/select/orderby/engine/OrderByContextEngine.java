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

package org.apache.shardingsphere.infra.binder.segment.select.orderby.engine;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.shardingsphere.infra.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;

/**
 * Order by context engine.
 */
public final class OrderByContextEngine {
    
    /**
     * Create order by context.
     *
     * @param schema ShardingSphere schema
     * @param selectStatement select statement
     * @param groupByContext group by context
     * @return order by context
     */
    public OrderByContext createOrderBy(final ShardingSphereSchema schema, final SelectStatement selectStatement, final GroupByContext groupByContext) {
        if (!selectStatement.getOrderBy().isPresent() || selectStatement.getOrderBy().get().getOrderByItems().isEmpty()) {
            if (groupByContext.getItems().isEmpty() && selectStatement.getProjections().isDistinctRow()) {
                OrderByContext result = createOrderByContextForDistinctRowWithoutGroupBy(selectStatement, groupByContext);
                return null != result ? result : getDefaultOrderByContextWithoutOrderBy(groupByContext);
            } else if (selectStatement instanceof MySQLSelectStatement) {
                Optional<OrderByContext> result = createOrderByContextForMySQLSelectWithoutOrderBy(schema, selectStatement, groupByContext);
                return result.orElse(getDefaultOrderByContextWithoutOrderBy(groupByContext));
            }
            return getDefaultOrderByContextWithoutOrderBy(groupByContext);
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
    
    private OrderByContext getDefaultOrderByContextWithoutOrderBy(final GroupByContext groupByContext) {
        return new OrderByContext(groupByContext.getItems(), !groupByContext.getItems().isEmpty());
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
    
    private Optional<OrderByContext> createOrderByContextForMySQLSelectWithoutOrderBy(final ShardingSphereSchema schema, final SelectStatement selectStatement, final GroupByContext groupByContext) {
        if (!isNeedProcessMySQLSelectWithoutOrderBy(selectStatement, groupByContext)) {
            return Optional.empty();
        }
        int index = 0;
        List<OrderByItem> orderByItems = new LinkedList<>();
        TableMetaData tableMetaData = schema.get(((SimpleTableSegment) selectStatement.getFrom()).getTableName().getIdentifier().getValue());
        if (null == tableMetaData) {
            return Optional.empty();
        }
        for (String each : tableMetaData.getPrimaryKeyColumns()) {
            ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue(each));
            OrderByItem item = new OrderByItem(new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC));
            item.setIndex(index++);
            orderByItems.add(item);
        }
        return orderByItems.isEmpty() ? Optional.empty() : Optional.of(new OrderByContext(orderByItems, true));
    }
    
    private boolean isNeedProcessMySQLSelectWithoutOrderBy(final SelectStatement selectStatement, final GroupByContext groupByContext) {
        if (!groupByContext.getItems().isEmpty()) {
            return false;
        }
        TableSegment tableSegment = selectStatement.getFrom();
        if (null == tableSegment) {
            return false;
        }
        if (!(tableSegment instanceof SimpleTableSegment)) {
            return false;
        }
        for (ProjectionSegment projectionSegment : selectStatement.getProjections().getProjections()) {
            if (projectionSegment instanceof AggregationProjectionSegment) {
                return false;
            }
        }
        return true;
    }
}
