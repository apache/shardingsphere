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
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
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
    
    public OrderByStreamMergedResult(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext, final PhysicalSchemaMetaData schemaMetaData) throws SQLException {
        orderByItems = selectStatementContext.getOrderByContext().getItems();
        orderByValuesQueue = new PriorityQueue<>(queryResults.size());
        orderResultSetsToQueue(queryResults, selectStatementContext, schemaMetaData);
        isFirstNext = true;
    }
    
    private void orderResultSetsToQueue(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext, final PhysicalSchemaMetaData schemaMetaData) throws SQLException {
        for (QueryResult each : queryResults) {
            OrderByValue orderByValue = new OrderByValue(each, orderByItems, selectStatementContext, schemaMetaData);
            if (orderByValue.next()) {
                orderByValuesQueue.offer(orderByValue);
            }
        }
        setCurrentQueryResult(orderByValuesQueue.isEmpty() ? queryResults.get(0) : orderByValuesQueue.peek().getQueryResult());
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
        setCurrentQueryResult(orderByValuesQueue.peek().getQueryResult());
        return true;
    }
}
