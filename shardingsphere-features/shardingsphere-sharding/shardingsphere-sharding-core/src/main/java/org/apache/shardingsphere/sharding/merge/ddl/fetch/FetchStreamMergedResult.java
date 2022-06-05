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

package org.apache.shardingsphere.sharding.merge.ddl.fetch;

import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.statement.ddl.FetchStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.stream.StreamMergedResult;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.merge.dql.orderby.OrderByValue;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Stream merged result for fetch.
 */
public final class FetchStreamMergedResult extends StreamMergedResult {
    
    private final Collection<OrderByItem> orderByItems;
    
    private final Queue<OrderByValue> orderByValuesQueue;
    
    private boolean isFirstNext;
    
    public FetchStreamMergedResult(final List<QueryResult> queryResults, final FetchStatementContext fetchStatementContext, final ShardingSphereSchema schema) throws SQLException {
        String cursorName = fetchStatementContext.getCursorName().getIdentifier().getValue().toLowerCase();
        SelectStatementContext selectStatementContext = fetchStatementContext.getCursorStatementContext().getSelectStatementContext();
        orderByItems = selectStatementContext.getOrderByContext().getItems();
        orderByValuesQueue = FetchOrderByValueQueuesHolder.get().computeIfAbsent(cursorName, key -> new PriorityQueue<>(queryResults.size()));
        orderResultSetsToQueue(queryResults, selectStatementContext, schema);
        isFirstNext = true;
    }
    
    private void orderResultSetsToQueue(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext, final ShardingSphereSchema schema) throws SQLException {
        for (QueryResult each : queryResults) {
            OrderByValue orderByValue = new OrderByValue(each, orderByItems, selectStatementContext, schema);
            if (orderByValue.next()) {
                orderByValuesQueue.offer(orderByValue);
            }
        }
    }
    
    @Override
    public boolean next() throws SQLException {
        if (orderByValuesQueue.isEmpty()) {
            return false;
        }
        // TODO support fetch count and fetch all statement
        if (isFirstNext) {
            setCurrentQueryResult(orderByValuesQueue.poll().getQueryResult());
            isFirstNext = false;
            return true;
        } else {
            return false;
        }
    }
}
