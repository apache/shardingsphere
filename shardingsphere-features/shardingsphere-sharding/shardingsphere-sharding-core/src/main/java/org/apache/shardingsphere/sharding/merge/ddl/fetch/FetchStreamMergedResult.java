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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.JDBCMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.stream.StreamMergedResult;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.merge.dql.orderby.OrderByValue;
import org.apache.shardingsphere.sql.parser.sql.common.constant.DirectionType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.cursor.DirectionSegment;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Stream merged result for fetch.
 */
public final class FetchStreamMergedResult extends StreamMergedResult {
    
    private final Queue<OrderByValue> orderByValuesQueue;
    
    private final DirectionType directionType;
    
    private long fetchCount;
    
    private boolean isFirstNext;
    
    public FetchStreamMergedResult(final List<QueryResult> queryResults, final FetchStatementContext fetchStatementContext, final ShardingSphereSchema schema) throws SQLException {
        String cursorName = fetchStatementContext.getCursorName().getIdentifier().getValue().toLowerCase();
        orderByValuesQueue = FetchOrderByValueQueuesHolder.getOrderByValueQueues().computeIfAbsent(cursorName, key -> new PriorityQueue<>(queryResults.size()));
        directionType = fetchStatementContext.getSqlStatement().getDirection().map(DirectionSegment::getDirectionType).orElse(DirectionType.NEXT);
        fetchCount = fetchStatementContext.getSqlStatement().getDirection().flatMap(DirectionSegment::getCount).orElse(1L);
        SelectStatementContext selectStatementContext = fetchStatementContext.getCursorStatementContext().getSelectStatementContext();
        addOrderedResultSetsToQueue(queryResults, selectStatementContext, schema);
        mergeRemainingRowCount(cursorName);
        isFirstNext = true;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (orderByValuesQueue.isEmpty()) {
            return false;
        }
        if (isFirstNext) {
            isFirstNext = false;
            fetchCount--;
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
        return DirectionType.isAllDirectionType(directionType) || fetchCount-- > 0;
    }
    
    private void addOrderedResultSetsToQueue(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext, final ShardingSphereSchema schema) throws SQLException {
        Collection<OrderByItem> items = selectStatementContext.getOrderByContext().getItems();
        for (QueryResult each : queryResults) {
            QueryResult queryResult = decorate(each, selectStatementContext.getDatabaseType());
            OrderByValue orderByValue = new OrderByValue(queryResult, items, selectStatementContext, schema);
            if (orderByValue.next()) {
                orderByValuesQueue.offer(orderByValue);
            }
        }
        setCurrentQueryResult(orderByValuesQueue.isEmpty() ? queryResults.get(0) : orderByValuesQueue.peek().getQueryResult());
    }
    
    private QueryResult decorate(final QueryResult queryResult, final DatabaseType databaseType) throws SQLException {
        if (!DirectionType.isAllDirectionType(directionType) && queryResult instanceof JDBCStreamQueryResult) {
            return new JDBCMemoryQueryResult(((JDBCStreamQueryResult) queryResult).getResultSet(), databaseType);
        }
        return queryResult;
    }
    
    private void mergeRemainingRowCount(final String cursorName) {
        long remainingRowCount = 0L;
        for (OrderByValue each : orderByValuesQueue) {
            if (each.getQueryResult() instanceof JDBCMemoryQueryResult) {
                remainingRowCount += ((JDBCMemoryQueryResult) each.getQueryResult()).getRowCount();
            }
        }
        remainingRowCount = DirectionType.isAllDirectionType(directionType) ? 0 : remainingRowCount - fetchCount;
        FetchOrderByValueQueuesHolder.getRemainingRowCounts().put(cursorName, Math.max(remainingRowCount, 0));
    }
}
