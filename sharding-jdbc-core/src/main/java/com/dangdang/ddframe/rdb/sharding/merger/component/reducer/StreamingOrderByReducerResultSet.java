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

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractResultSetAdapter;
import com.dangdang.ddframe.rdb.sharding.merger.component.ReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.row.OrderByRow;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
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
public class StreamingOrderByReducerResultSet extends AbstractResultSetAdapter implements ReducerResultSet {
    
    private final List<OrderByColumn> orderByColumns;
    
    // TODO 作用?
    private boolean initial;
    
    // TODO 为什么用Queue
    private Queue<ResultSet> effectiveResultSetQueue;
    
    // TODO 使用lombok?
    public StreamingOrderByReducerResultSet(final List<OrderByColumn> orderByColumns) {
        this.orderByColumns = orderByColumns;
    }
    
    @Override
    // TODO preResultSet什么意思, 如果是复数需要加s
    public void init(final List<ResultSet> preResultSet) throws SQLException {
        // TODO 以下两步可否通过构造器
        setResultSets(preResultSet);
        setCurrentResultSet(preResultSet.get(0));
        effectiveResultSetQueue = new LinkedList<>(Collections2.filter(preResultSet, new Predicate<ResultSet>() {
            
            @Override
            public boolean apply(final ResultSet input) {
                try {
                    // TODO 之前在WrapperResultSet的构造器里已经next过了, 会否有问题
                    return input.next();
                    // TODO next问题是否直接抛SQLException, 目前方法签名的SQLException并不需要
                } catch (final SQLException ex) {
                    throw new ShardingJdbcException(ex);
                }
            }
        }));
        // TODO log是否有意义,没有覆盖toString能否看清调试信息
        log.trace("Effective result set: {}", effectiveResultSetQueue);
    }
    
    @Override
    public boolean next() throws SQLException {
        if (initial) {
            nextEffectiveResultSets();
        } else {
            initial = true;
        }
        // TODO 单独提炼一个getComparedResultSet这样的方法是否好一些
        OrderByRow chosenOrderByValue = null;
        for (ResultSet each : effectiveResultSetQueue) {
            // TODO 变量名字是否应该叫orderByRow
            OrderByRow eachOrderByValue = new OrderByRow(orderByColumns, each);
            if (null == chosenOrderByValue || chosenOrderByValue.compareTo(eachOrderByValue) > 0) {
                chosenOrderByValue = eachOrderByValue;
                // TODO 作用?
                setCurrentResultSet(each);
            }
        }
        if (!effectiveResultSetQueue.isEmpty()) {
            // TODO toString是否应删除, 将内容直接挪入log
            log.trace(toString());
        }
        return !effectiveResultSetQueue.isEmpty();
    }
    
    private void nextEffectiveResultSets() throws SQLException {
        // TODO next rename => hasNext
        boolean next = getCurrentResultSet().next();
        if (!next) {
            effectiveResultSetQueue.remove(getCurrentResultSet());
            log.trace("Result set {} finish", getCurrentResultSet());
        }
    }
    
    @Override
    // TODO toString应该展现变量状态, 描述词语Current result set: 是否应去掉, 而且ToString是否不应只展现getCurrentResultSet的状态?
    public String toString() {
        return String.format("Current result set:%s", getCurrentResultSet());
    }
}
