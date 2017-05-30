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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderBy;
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
    
    private final Queue<ResultSetOrderByWrapper> delegateResultSetQueue;
    
    private final List<OrderBy> orderByKeys;
    
    public StreamingOrderByReducerResultSet(final ResultSetMergeContext resultSetMergeContext) throws SQLException {
        super(resultSetMergeContext.getShardingResultSets().getResultSets());
        delegateResultSetQueue = new PriorityQueue<>(getResultSets().size());
        orderByKeys = resultSetMergeContext.getCurrentOrderByKeys();
    }
    
    @Override
    protected boolean firstNext() throws SQLException {
        for (ResultSet each : getResultSets()) {
            ResultSetOrderByWrapper wrapper = new ResultSetOrderByWrapper(each);
            if (wrapper.next()) {
                delegateResultSetQueue.offer(wrapper);
            }
        }
        return doNext();
    }

    @Override
    protected boolean afterFirstNext() throws SQLException {
        ResultSetOrderByWrapper firstResultSet = delegateResultSetQueue.poll();
        setDelegate(firstResultSet.delegate);
        if (firstResultSet.next()) {
            delegateResultSetQueue.offer(firstResultSet);
        }
        return doNext();
    }
    
    private boolean doNext() {
        if (delegateResultSetQueue.isEmpty()) {
            return false;
        }
        setDelegate(delegateResultSetQueue.peek().delegate);
        log.trace("Chosen order by value: {}, current result set hashcode: {}", delegateResultSetQueue.peek().row, getDelegate().hashCode());
        return true;
    }
    
    @RequiredArgsConstructor
    private class ResultSetOrderByWrapper implements Comparable<ResultSetOrderByWrapper> {
        
        private final ResultSet delegate;
        
        private OrderByResultSetRow row;
        
        boolean next() throws SQLException {
            boolean result = delegate.next();
            if (result) {
                row = new OrderByResultSetRow(delegate, orderByKeys);
            }
            return result;
        }
        
        @Override
        public int compareTo(final ResultSetOrderByWrapper o) {
            return row.compareTo(o.row);
        }
    }
}
