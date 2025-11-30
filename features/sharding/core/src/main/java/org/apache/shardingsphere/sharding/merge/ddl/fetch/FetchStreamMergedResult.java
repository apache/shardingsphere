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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.JDBCMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.stream.StreamMergedResult;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.cursor.FetchGroup;
import org.apache.shardingsphere.sharding.exception.connection.CursorNameNotFoundException;
import org.apache.shardingsphere.sharding.merge.dql.orderby.OrderByValue;
import org.apache.shardingsphere.sql.parser.statement.core.enums.DirectionType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.DirectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.FetchStatement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
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
    
    private boolean isExecutedAllDirection;
    
    public FetchStreamMergedResult(final List<QueryResult> queryResults, final CursorHeldSQLStatementContext sqlStatementContext,
                                   final ShardingSphereSchema schema, final ConnectionContext connectionContext) throws SQLException {
        orderByValuesQueue = new PriorityQueue<>(queryResults.size());
        FetchStatement fetchStatement = (FetchStatement) sqlStatementContext.getSqlStatement();
        directionType = fetchStatement.getDirection().map(DirectionSegment::getDirectionType).orElse(DirectionType.NEXT);
        fetchCount = fetchStatement.getDirection().flatMap(DirectionSegment::getCount).orElse(1L);
        SelectStatementContext selectStatementContext = sqlStatementContext.getCursorStatementContext().getSelectStatementContext();
        ShardingSpherePreconditions.checkNotNull(fetchStatement.getCursorName(), CursorNameNotFoundException::new);
        String cursorName = fetchStatement.getCursorName().getIdentifier().getValue().toLowerCase();
        List<FetchOrderByValueGroup> fetchOrderByValueGroups = getFetchOrderByValueGroups(queryResults, selectStatementContext, schema, cursorName, connectionContext);
        addOrderedResultSetsToQueue(fetchOrderByValueGroups, queryResults);
        setMinResultSetRowCount(cursorName, connectionContext);
        handleExecutedAllDirections(connectionContext, cursorName);
        isFirstNext = true;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (isExecutedAllDirection) {
            return false;
        }
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
        return DirectionType.isAllDirectionType(directionType) || fetchCount-- > 0L;
    }
    
    private List<FetchOrderByValueGroup> getFetchOrderByValueGroups(final List<QueryResult> queryResults, final SelectStatementContext selectStatementContext,
                                                                    final ShardingSphereSchema schema, final String cursorName, final ConnectionContext connectionContext) throws SQLException {
        long actualFetchCount = Math.max(fetchCount - connectionContext.getCursorContext().getMinGroupRowCounts().getOrDefault(cursorName, 0L), 0L);
        List<FetchGroup> fetchGroups = connectionContext.getCursorContext().getOrderByValueGroups().computeIfAbsent(cursorName, key -> createFetchOrderByValueGroups(queryResults.size()));
        List<FetchOrderByValueGroup> result = new ArrayList<>(fetchGroups.size());
        for (FetchGroup each : fetchGroups) {
            result.add((FetchOrderByValueGroup) each);
        }
        result.forEach(each -> each.getOrderByValues().removeIf(this::isEmptyOrderByValue));
        if (0L == actualFetchCount && !DirectionType.isAllDirectionType(directionType)) {
            return result;
        }
        if (connectionContext.getCursorContext().getExecutedAllDirections().containsKey(cursorName)) {
            result.forEach(each -> each.getOrderByValues().clear());
            return result;
        }
        Collection<OrderByItem> items = selectStatementContext.getOrderByContext().getItems();
        int index = 0;
        for (QueryResult each : queryResults) {
            QueryResult queryResult = decorate(each, selectStatementContext.getSqlStatement().getDatabaseType());
            OrderByValue orderByValue = new OrderByValue(queryResult, items, selectStatementContext, schema);
            if (orderByValue.next()) {
                result.get(index).getOrderByValues().add(orderByValue);
            }
            index++;
        }
        return result;
    }
    
    private List<FetchGroup> createFetchOrderByValueGroups(final int queryResultSize) {
        List<FetchGroup> result = new ArrayList<>(queryResultSize);
        for (int index = 0; index < queryResultSize; index++) {
            result.add(new FetchOrderByValueGroup());
        }
        return result;
    }
    
    private boolean isEmptyOrderByValue(final OrderByValue orderByValue) {
        return orderByValue.getQueryResult() instanceof JDBCMemoryQueryResult && 0L == ((JDBCMemoryQueryResult) orderByValue.getQueryResult()).getRowCount()
                && null == ((JDBCMemoryQueryResult) orderByValue.getQueryResult()).getCurrentRow();
    }
    
    private void addOrderedResultSetsToQueue(final List<FetchOrderByValueGroup> fetchOrderByValueGroups, final List<QueryResult> queryResults) {
        for (FetchOrderByValueGroup each : fetchOrderByValueGroups) {
            for (OrderByValue orderByValue : each.getOrderByValues()) {
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
    
    private void setMinResultSetRowCount(final String cursorName, final ConnectionContext connectionContext) {
        Collection<Long> rowCounts = new LinkedList<>();
        List<FetchGroup> fetchOrderByValueGroups = connectionContext.getCursorContext().getOrderByValueGroups().getOrDefault(cursorName, new LinkedList<>());
        for (FetchGroup each : fetchOrderByValueGroups) {
            rowCounts.add(getGroupRowCount((FetchOrderByValueGroup) each));
        }
        long minResultSetRowCount = DirectionType.isAllDirectionType(directionType) ? 0L : Collections.min(rowCounts) - fetchCount;
        connectionContext.getCursorContext().getMinGroupRowCounts().put(cursorName, Math.max(minResultSetRowCount, 0L));
    }
    
    private void handleExecutedAllDirections(final ConnectionContext connectionContext, final String cursorName) {
        if (connectionContext.getCursorContext().getExecutedAllDirections().containsKey(cursorName)) {
            isExecutedAllDirection = true;
        }
        if (DirectionType.isAllDirectionType(directionType)) {
            connectionContext.getCursorContext().getExecutedAllDirections().put(cursorName, true);
        }
    }
    
    private long getGroupRowCount(final FetchOrderByValueGroup fetchOrderByValueGroup) {
        long result = 0L;
        for (OrderByValue each : fetchOrderByValueGroup.getOrderByValues()) {
            if (each.getQueryResult() instanceof JDBCMemoryQueryResult) {
                JDBCMemoryQueryResult queryResult = (JDBCMemoryQueryResult) each.getQueryResult();
                result += null == queryResult.getCurrentRow() ? queryResult.getRowCount() : queryResult.getRowCount() + 1;
            }
        }
        return result;
    }
}
