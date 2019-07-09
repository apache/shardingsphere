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

package org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.orderby;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.groupby.GroupBy;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

import java.util.LinkedList;
import java.util.List;

/**
 * Order by engine.
 *
 * @author zhangliang
 */
public final class OrderByEngine {
    
    /**
     * Create order by.
     * 
     * @param sqlStatement SQL statement
     * @param groupBy group by
     * @return order by
     */
    public OrderBy createOrderBy(final SQLStatement sqlStatement, final GroupBy groupBy) {
        Optional<OrderBySegment> orderBySegment = sqlStatement.findSQLSegment(OrderBySegment.class);
        if (!orderBySegment.isPresent() || orderBySegment.get().getOrderByItems().isEmpty()) {
            return new OrderBy(groupBy.getItems(), !groupBy.getItems().isEmpty());
        }
        List<OrderByItem> orderByItems = new LinkedList<>();
        for (OrderByItemSegment each : orderBySegment.get().getOrderByItems()) {
            OrderByItem orderByItem = new OrderByItem(each);
            if (each instanceof IndexOrderByItemSegment) {
                orderByItem.setIndex(((IndexOrderByItemSegment) each).getColumnIndex());
            }
            orderByItems.add(orderByItem);
        }
        return new OrderBy(orderByItems, false);
    }
    
}
