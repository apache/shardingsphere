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

package org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.groupby;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Group by engine.
 *
 * @author zhangliang
 */
public final class GroupByEngine {
    
    /**
     * Create group by.
     *
     * @param sqlStatement SQL statement
     * @return group by
     */
    public GroupBy createGroupBy(final SQLStatement sqlStatement) {
        Optional<GroupBySegment> groupBySegment = sqlStatement.findSQLSegment(GroupBySegment.class);
        if (!groupBySegment.isPresent()) {
            return new GroupBy(Collections.<OrderByItem>emptyList(), 0);
        }
        Collection<OrderByItem> groupByItems = new LinkedList<>();
        for (OrderByItemSegment each : groupBySegment.get().getGroupByItems()) {
            OrderByItem orderByItem = new OrderByItem(each);
            if (each instanceof IndexOrderByItemSegment) {
                orderByItem.setIndex(((IndexOrderByItemSegment) each).getColumnIndex());
            }
            groupByItems.add(orderByItem);
        }
        return new GroupBy(groupByItems, groupBySegment.get().getStopIndex());
    }
}
