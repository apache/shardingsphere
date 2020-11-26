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

package org.apache.shardingsphere.sharding.merge.dql.orderby;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultSet;
import org.apache.shardingsphere.infra.merge.result.impl.stream.StreamMergedResult;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Stream merged result for order by.
 */
public class OrderByStreamMergedResult extends StreamMergedResult {
    
    private final Collection<OrderByItem> orderByItems;
    
    @Getter(AccessLevel.PROTECTED)
    private final Queue<OrderByValue> orderByValuesQueue;
    
    @Getter(AccessLevel.PROTECTED)
    private boolean isFirstNext;
    
    public OrderByStreamMergedResult(final List<QueryResultSet> queryResultSets, final SelectStatementContext selectStatementContext, final ShardingSphereSchema schema) throws SQLException {
        orderByItems = selectStatementContext.getOrderByContext().getItems();
        orderByValuesQueue = new PriorityQueue<>(queryResultSets.size());
        orderResultSetsToQueue(queryResultSets, selectStatementContext, schema);
        isFirstNext = true;
    }
    
    private void orderResultSetsToQueue(final List<QueryResultSet> queryResultSets, final SelectStatementContext selectStatementContext, final ShardingSphereSchema schema) throws SQLException {
        for (QueryResultSet each : queryResultSets) {
            OrderByValue orderByValue = new OrderByValue(each, orderByItems, selectStatementContext, schema);
            if (orderByValue.next()) {
                orderByValuesQueue.offer(orderByValue);
            }
        }
        setCurrentQueryResultSet(orderByValuesQueue.isEmpty() ? queryResultSets.get(0) : orderByValuesQueue.peek().getQueryResultSet());
    }
    
    @Override
    public boolean next() throws SQLException {
        if (orderByValuesQueue.isEmpty()) {
            return false;
        }
        if (isFirstNext) {
            isFirstNext = false;
            return true;
        }
        OrderByValue firstOrderByValue = orderByValuesQueue.poll();
        if (firstOrderByValue.next()) {
            orderByValuesQueue.offer(firstOrderByValue);
        }
        if (orderByValuesQueue.isEmpty()) {
            return false;
        }
        setCurrentQueryResultSet(orderByValuesQueue.peek().getQueryResultSet());
        return true;
    }
}
