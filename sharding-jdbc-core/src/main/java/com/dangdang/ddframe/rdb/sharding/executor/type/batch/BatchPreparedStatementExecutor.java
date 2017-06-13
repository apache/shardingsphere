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

package com.dangdang.ddframe.rdb.sharding.executor.type.batch;

import com.codahale.metrics.Timer.Context;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.executor.BaseStatementUnit;
import com.dangdang.ddframe.rdb.sharding.executor.ExecuteUnit;
import com.dangdang.ddframe.rdb.sharding.executor.ExecutorEngine;
import com.dangdang.ddframe.rdb.sharding.executor.event.AbstractExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.EventExecutionType;
import com.dangdang.ddframe.rdb.sharding.executor.threadlocal.ExecutorDataMap;
import com.dangdang.ddframe.rdb.sharding.executor.threadlocal.ExecutorExceptionHandler;
import com.dangdang.ddframe.rdb.sharding.executor.type.ExecutorUtils;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.dangdang.ddframe.rdb.sharding.util.EventBusInstance;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 多线程执行预编译语句对象批量请求的执行器.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class BatchPreparedStatementExecutor {
    
    private final ExecutorEngine executorEngine;
    
    private final SQLType sqlType;
    
    private final Collection<BatchPreparedStatementUnit> batchStatementUnits;
    
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
            List<int[]> results = executorEngine.execute(batchStatementUnits, new ExecuteUnit<int[]>() {
                
                @Override
                public int[] execute(final BaseStatementUnit baseStatementUnit) throws Exception {
                    return executeBatch(baseStatementUnit, isExceptionThrown, dataMap);
                }
            });
            return accumulate(results);
        } finally {
            MetricsContext.stop(context);
        }
    }
    
    private int[] executeBatch(final BaseStatementUnit batchPreparedStatementUnit, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        int[] result;
        ExecutorUtils.setThreadLocalData(isExceptionThrown, dataMap);
        List<AbstractExecutionEvent> events = new LinkedList<>();
        for (List<Object> each : parameterSets) {
            AbstractExecutionEvent event = ExecutorUtils.getExecutionEvent(sqlType, batchPreparedStatementUnit.getSqlExecutionUnit(), each);
            events.add(event);
            EventBusInstance.getInstance().post(event);
        }
        try {
            result = batchPreparedStatementUnit.getStatement().executeBatch();
        } catch (final SQLException ex) {
            for (AbstractExecutionEvent each : events) {
                ExecutorUtils.handleException(each, ex);
            }
            return null;
        }
        for (AbstractExecutionEvent each : events) {
            each.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
            EventBusInstance.getInstance().post(each);
        }
        return result;
    }
    
    private int[] accumulate(final List<int[]> results) {
        int[] result = new int[parameterSets.size()];
        int count = 0;
        for (BatchPreparedStatementUnit each : batchStatementUnits) {
            for (Entry<Integer, Integer> entry : each.getOuterAndInnerAddBatchCountMap().entrySet()) {
                result[entry.getKey()] += results.get(count)[entry.getValue()];
            }
            count++;
        }
        return result;
    }
}
