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
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * 流式排序的聚集结果集.
 *
 * @author gaohongtao
 * @author zhangliang
 */
@Slf4j
public final class StreamingOrderByReducerResultSet extends AbstractDelegateResultSet {
    
    private final List<OrderByColumn> orderByColumns;
    
    private final List<ResultSet> resultSets = new LinkedList<>();
    
    public StreamingOrderByReducerResultSet(final ResultSetMergeContext resultSetMergeContext) throws SQLException {
        super(resultSetMergeContext.getShardingResultSets().getResultSets());
        orderByColumns = resultSetMergeContext.getCurrentOrderByKeys();
        resultSets.addAll(resultSetMergeContext.getShardingResultSets().getResultSets());
    }
    
    @Override
    protected boolean firstNext() throws SQLException {
        initialResultSetCursors();
        return doNext();
    }
    
    private void initialResultSetCursors() throws SQLException {
        for (ResultSet each : resultSets) {
            each.next();
        }
    }
    
    @Override
    protected boolean afterFirstNext() throws SQLException {
        if (!getDelegate().next()) {
            resultSets.remove(getDelegate());
        }
        return doNext();
    }
    
    private boolean doNext() throws SQLException {
        setDelegateResultSet();
        return !resultSets.isEmpty();
    }
    
    private void setDelegateResultSet() throws SQLException {
        OrderByResultSetRow chosenOrderByValue = null;
        for (ResultSet each : resultSets) {
            OrderByResultSetRow eachOrderByValue = new OrderByResultSetRow(each, orderByColumns);
            if (null == chosenOrderByValue || chosenOrderByValue.compareTo(eachOrderByValue) > 0) {
                chosenOrderByValue = eachOrderByValue;
                setDelegate(each);
            }
        }
        log.trace("Chosen order by value: {}, current result set hashcode: {}", chosenOrderByValue, getDelegate().hashCode());
    }
}
