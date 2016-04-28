/**
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

package com.dangdang.ddframe.rdb.sharding.merger.component.reducer;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractReducerResultSetAdapter;
import com.dangdang.ddframe.rdb.sharding.merger.component.ComponentResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.row.OrderByRow;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 流式排序.
 *
 * @author gaohongtao
 */
@Slf4j
public class StreamingOrderByReducerResultSet extends AbstractReducerResultSetAdapter implements ReducerResultSet {
    
    private final List<OrderByColumn> orderByColumns;
    
    private boolean initial;
    
    private Queue<ResultSet> effectiveResultSetQueue;
    
    public StreamingOrderByReducerResultSet(final List<OrderByColumn> orderByColumns) {
        this.orderByColumns = orderByColumns;
    }
    
    @Override
    public ComponentResultSet init(final List<ResultSet> preResultSet) throws SQLException {
        input(preResultSet);
        setDelegate(preResultSet.get(0));
        effectiveResultSetQueue = new LinkedList<>();
        for (ResultSet each : preResultSet) {
            if (each.next()) {
                effectiveResultSetQueue.add(each);
            }
        }
        return this;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (initial) {
            nextEffectiveResultSets();
        } else {
            initial = true;
        }
        OrderByRow chosenOrderByValue = null;
        for (ResultSet each : effectiveResultSetQueue) {
            OrderByRow eachOrderByValue = new OrderByRow(orderByColumns, each);
            if (null == chosenOrderByValue || chosenOrderByValue.compareTo(eachOrderByValue) > 0) {
                chosenOrderByValue = eachOrderByValue;
                setDelegate(each);
            }
        }
        log.trace("Chosen order by value is {}, current result set is {}", chosenOrderByValue, getDelegate().hashCode());
        return !effectiveResultSetQueue.isEmpty();
    }
    
    private void nextEffectiveResultSets() throws SQLException {
        boolean next = getDelegate().next();
        if (!next) {
            effectiveResultSetQueue.remove(getDelegate());
            log.trace("Result set {} finished", getDelegate().hashCode());
        }
    }
}
