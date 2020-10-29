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

package org.apache.shardingsphere.infra.binder.segment.select.groupby.engine;

import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Group by context engine.
 */
public final class GroupByContextEngine {
    
    /**
     * Create group by context.
     *
     * @param selectStatement select statement
     * @return group by context
     */
    public GroupByContext createGroupByContext(final SelectStatement selectStatement) {
        if (!selectStatement.getGroupBy().isPresent()) {
            return new GroupByContext(new LinkedList<>(), 0);
        }
        Collection<OrderByItem> groupByItems = new LinkedList<>();
        for (OrderByItemSegment each : selectStatement.getGroupBy().get().getGroupByItems()) {
            OrderByItem orderByItem = new OrderByItem(each);
            if (each instanceof IndexOrderByItemSegment) {
                orderByItem.setIndex(((IndexOrderByItemSegment) each).getColumnIndex());
            }
            groupByItems.add(orderByItem);
        }
        return new GroupByContext(groupByItems, selectStatement.getGroupBy().get().getStopIndex());
    }
}
