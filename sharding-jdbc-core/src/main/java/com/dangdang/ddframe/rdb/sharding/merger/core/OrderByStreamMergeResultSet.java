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

package com.dangdang.ddframe.rdb.sharding.merger.core;

import com.dangdang.ddframe.rdb.sharding.merger.memory.row.OrderByResultSetRow;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * 排序归并结果集接口.
 *
 * @author zhangliang
 */
public final class OrderByStreamMergeResultSet extends AbstractStreamMergeResultSet {
    
    private final Queue<ComparableResultSet> resultSets;
    
    private final List<OrderItem> orderByItems;
    
    private boolean isFirstNext = true;
    
    public OrderByStreamMergeResultSet(final List<ResultSet> resultSets, final List<OrderItem> orderByItems) throws SQLException {
        this.resultSets = new PriorityQueue<>(resultSets.size());
        this.orderByItems = orderByItems;
        initResultSet(resultSets);
        
    }
    
    private void initResultSet(final Collection<ResultSet> resultSets) throws SQLException {
        for (ResultSet each : resultSets) {
            ComparableResultSet comparableResultSet = new ComparableResultSet(each);
            if (comparableResultSet.next()) {
                this.resultSets.offer(comparableResultSet);
            }
        }
        setCurrentResultSet(this.resultSets.peek().resultSet);
    }
    
    @Override
    public boolean next() throws SQLException {
        if (isFirstNext) {
            isFirstNext = false;
            return hasNext();
        }
        ComparableResultSet firstResultSet = resultSets.poll();
        setCurrentResultSet(firstResultSet.resultSet);
        if (firstResultSet.next()) {
            resultSets.offer(firstResultSet);
        }
        return hasNext();
    }
    
    private boolean hasNext() {
        if (resultSets.isEmpty()) {
            return false;
        }
        setCurrentResultSet(resultSets.peek().resultSet);
        return true;
    }
    
    @RequiredArgsConstructor
    private class ComparableResultSet implements Comparable<ComparableResultSet> {
        
        private final ResultSet resultSet;
        
        private OrderByResultSetRow row;
        
        boolean next() throws SQLException {
            boolean result = resultSet.next();
            if (result) {
                row = new OrderByResultSetRow(resultSet, orderByItems);
            }
            return result;
        }
        
        @Override
        public int compareTo(final ComparableResultSet o) {
            return row.compareTo(o.row);
        }
    }
}
