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
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Order by engine.
 *
 * @author zhangliang
 */
public final class OrderByEngine {
    
    /**
     * Get order by items.
     * 
     * @param sqlStatement SQL statement
     * @return order by items
     */
    public Collection<OrderByItem> getOrderByItems(final SQLStatement sqlStatement) {
        Optional<OrderBySegment> orderBySegment = sqlStatement.findSQLSegment(OrderBySegment.class);
        return orderBySegment.isPresent() ? createOrderByItems(orderBySegment.get().getOrderByItems()) : Collections.<OrderByItem>emptyList();
    }
    
    /**
     * Get group by items.
     *
     * @param sqlStatement SQL statement
     * @return group by items
     */
    public Collection<OrderByItem> getGroupByItems(final SQLStatement sqlStatement) {
        Optional<GroupBySegment> groupBySegment = sqlStatement.findSQLSegment(GroupBySegment.class);
        return groupBySegment.isPresent() ? createOrderByItems(groupBySegment.get().getGroupByItems()) : Collections.<OrderByItem>emptyList();
    }
    
    private Collection<OrderByItem> createOrderByItems(final Collection<OrderByItemSegment> orderByItemSegments) {
        List<OrderByItem> result = new LinkedList<>();
        for (OrderByItemSegment each : orderByItemSegments) {
            OrderByItem orderByItem = new OrderByItem(each);
            if (each instanceof IndexOrderByItemSegment) {
                orderByItem.setIndex(((IndexOrderByItemSegment) each).getColumnIndex());
            }
            result.add(orderByItem);
        }
        return result;
    }
    
    /**
     * Get last index of group by segment.
     * 
     * @param sqlStatement SQL statement
     * @return last index of group by segment
     */
    public int getGroupByLastIndex(final SQLStatement sqlStatement) {
        Optional<GroupBySegment> groupBySegment = sqlStatement.findSQLSegment(GroupBySegment.class);
        return groupBySegment.isPresent() ? groupBySegment.get().getStopIndex() : 0;
    }
}
