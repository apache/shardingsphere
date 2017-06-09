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

package com.dangdang.ddframe.rdb.sharding.executor;

import com.codahale.metrics.Timer.Context;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.executor.event.AbstractExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.EventExecutionType;
import com.dangdang.ddframe.rdb.sharding.executor.event.ExecutionEventBus;
import com.dangdang.ddframe.rdb.sharding.executor.wrapper.PreparedBatchStatement;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 多线程执行预编译语句对象批量请求的执行器.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class PreparedStatementBatchExecutor {
    
    private final ExecutorEngine executorEngine;
    
    private final SQLType sqlType;
    
    private final Collection<PreparedBatchStatement> preparedBatchStatements;
    
    private final List<List<Object>> parameterSets;
    
    /**
     * 执行批量接口.
     *
     * @return 执行结果
     */
    public int[] executeBatch() {
        Context context = MetricsContext.start("ShardingPreparedStatement-executeUpdate");
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        try {
            if (1 == preparedBatchStatements.size()) {
                return executeBatchInternal(preparedBatchStatements.iterator().next(), isExceptionThrown, dataMap);
            }
            return executorEngine.execute(preparedBatchStatements, new ExecuteUnit<PreparedBatchStatement, int[]>() {
                
                @Override
                public int[] execute(final PreparedBatchStatement input) throws Exception {
                    synchronized (input.getPreparedStatement().getConnection()) {
                        return executeBatchInternal(input, isExceptionThrown, dataMap);
                    }
                }
            }, new MergeUnit<int[], int[]>() {
                
                @Override
                public int[] merge(final List<int[]> results) {
                    if (null == results) {
                        return new int[]{0};
                    }
                    int[] result = new int[parameterSets.size()];
                    int count = 0;
                    for (PreparedBatchStatement each : preparedBatchStatements) {
                        for (Map.Entry<Integer, List<Integer>> entry : each.getBatchIndexes().entrySet()) {
                            for (int index : entry.getValue()) {
                                result[entry.getKey()] += results.get(count)[index];
                            }
                        }
                        count++;
                    }
                    return result;
                }
            });
        } finally {
            MetricsContext.stop(context);
        }
    }
    
    private int[] executeBatchInternal(final PreparedBatchStatement batchPreparedBatchStatement, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        int[] result;
        ExecutorUtils.setThreadLocalData(isExceptionThrown, dataMap);
        List<AbstractExecutionEvent> events = new LinkedList<>();
        for (List<Object> each : parameterSets) {
            AbstractExecutionEvent event = ExecutorUtils.getExecutionEvent(sqlType, batchPreparedBatchStatement.getSqlExecutionUnit(), each);
            events.add(event);
            ExecutionEventBus.getInstance().post(event);
        }
        try {
            result = batchPreparedBatchStatement.getPreparedStatement().executeBatch();
        } catch (final SQLException ex) {
            for (AbstractExecutionEvent each : events) {
                ExecutorUtils.handleException(each, ex);
            }
            return null;
        }
        for (AbstractExecutionEvent each : events) {
            each.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
            ExecutionEventBus.getInstance().post(each);
        }
        return result;
    }
}
