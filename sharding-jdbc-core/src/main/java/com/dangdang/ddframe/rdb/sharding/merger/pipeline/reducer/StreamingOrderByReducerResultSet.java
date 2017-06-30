/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger.pipeline.reducer;

import com.dangdang.ddframe.rdb.sharding.merger.ResultSetMergeContext;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.delegate.AbstractDelegateResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row.OrderByResultSetRow;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * 流式排序的聚集结果集.
 *
 * @author gaohongtao
 * @author zhangliang
 */
@Slf4j
public final class StreamingOrderByReducerResultSet extends AbstractDelegateResultSet {
    
    private final Queue<ComparableResultSet> priorityQueue;
    
    private final List<OrderItem> orderItems;
    
    public StreamingOrderByReducerResultSet(final ResultSetMergeContext resultSetMergeContext) throws SQLException {
        super(resultSetMergeContext.getShardingResultSets().getResultSets());
        priorityQueue = new PriorityQueue<>(getResultSets().size());
        orderItems = resultSetMergeContext.getCurrentOrderByKeys();
    }
    
    @Override
    protected boolean firstNext() throws SQLException {
        for (ResultSet each : getResultSets()) {
            ComparableResultSet comparableResultSet = new ComparableResultSet(each);
            if (comparableResultSet.next()) {
                priorityQueue.offer(comparableResultSet);
            }
        }
        return hasNext();
    }
    
    @Override
    protected boolean afterFirstNext() throws SQLException {
        ComparableResultSet firstResultSet = priorityQueue.poll();
        setDelegate(firstResultSet.resultSet);
        if (firstResultSet.next()) {
            priorityQueue.offer(firstResultSet);
        }
        return hasNext();
    }
    
    private boolean hasNext() {
        if (priorityQueue.isEmpty()) {
            return false;
        }
        setDelegate(priorityQueue.peek().resultSet);
        log.trace("Chosen order by value: {}", priorityQueue.peek().row);
        return true;
    }
    
    @RequiredArgsConstructor
    private class ComparableResultSet implements Comparable<ComparableResultSet> {
        
        private final ResultSet resultSet;
        
        private OrderByResultSetRow row;
        
        boolean next() throws SQLException {
            boolean result = resultSet.next();
            if (result) {
                row = new OrderByResultSetRow(resultSet, orderItems);
            }
            return result;
        }
        
        @Override
        public int compareTo(final ComparableResultSet o) {
            return row.compareTo(o.row);
        }
    }
}
