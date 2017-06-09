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
import com.dangdang.ddframe.rdb.sharding.executor.wrapper.PreparedStatementExecutorWrapper;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 多线程执行预编译语句对象请求的执行器.
 * 
 * @author zhangliang
 * @author caohao
 */
@RequiredArgsConstructor
public final class PreparedStatementExecutor {
    
    private final ExecutorEngine executorEngine;
    
    private final SQLType sqlType;
    
    private final Collection<PreparedStatementExecutorWrapper> preparedStatementExecutorWrappers;
    
    /**
     * 执行SQL查询.
     * 
     * @return 结果集列表
     */
    public List<ResultSet> executeQuery() {
        Context context = MetricsContext.start("ShardingPreparedStatement-executeQuery");
        List<ResultSet> result;
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        try {
            if (1 == preparedStatementExecutorWrappers.size()) {
                return Collections.singletonList(executeQueryInternal(preparedStatementExecutorWrappers.iterator().next(), isExceptionThrown, dataMap));
            }
            result = executorEngine.execute(preparedStatementExecutorWrappers, new ExecuteUnit<PreparedStatementExecutorWrapper, ResultSet>() {
        
                @Override
                public ResultSet execute(final PreparedStatementExecutorWrapper input) throws Exception {
                    synchronized (input.getPreparedStatement().getConnection()) {
                        return executeQueryInternal(input, isExceptionThrown, dataMap);
                    }
                }
            });
        } finally {
            MetricsContext.stop(context);
        }
        return result;
    }
    
    private ResultSet executeQueryInternal(final PreparedStatementExecutorWrapper preparedStatementExecutorWrapper, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        ResultSet result;
        ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
        ExecutorDataMap.setDataMap(dataMap);
        AbstractExecutionEvent event = ExecutorUtils.getExecutionEvent(sqlType, preparedStatementExecutorWrapper.getSqlExecutionUnit());
        ExecutionEventBus.getInstance().post(event);
        try {
            result = preparedStatementExecutorWrapper.getPreparedStatement().executeQuery();
        } catch (final SQLException ex) {
            ExecutorUtils.handleException(event, ex);
            return null;
        }
        event.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        ExecutionEventBus.getInstance().post(event);
        return result;
    }
    
    /**
     * 执行SQL更新.
     * 
     * @return 更新数量
     */
    public int executeUpdate() {
        Context context = MetricsContext.start("ShardingPreparedStatement-executeUpdate");
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        try {
            if (1 == preparedStatementExecutorWrappers.size()) {
                return executeUpdateInternal(preparedStatementExecutorWrappers.iterator().next(), isExceptionThrown, dataMap);
            }
            return executorEngine.execute(preparedStatementExecutorWrappers, new ExecuteUnit<PreparedStatementExecutorWrapper, Integer>() {
        
                @Override
                public Integer execute(final PreparedStatementExecutorWrapper input) throws Exception {
                    synchronized (input.getPreparedStatement().getConnection()) {
                        return executeUpdateInternal(input, isExceptionThrown, dataMap);
                    }
                }
            }, new MergeUnit<Integer, Integer>() {
        
                @Override
                public Integer merge(final List<Integer> results) {
                    if (null == results) {
                        return 0;
                    }
                    int result = 0;
                    for (Integer each : results) {
                        result += each;
                    }
                    return result;
                }
            });
        } finally {
            MetricsContext.stop(context);
        }
    }
    
    private int executeUpdateInternal(final PreparedStatementExecutorWrapper preparedStatementExecutorWrapper, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        int result;
        ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
        ExecutorDataMap.setDataMap(dataMap);
        AbstractExecutionEvent event = ExecutorUtils.getExecutionEvent(sqlType, preparedStatementExecutorWrapper.getSqlExecutionUnit());
        ExecutionEventBus.getInstance().post(event);
        try {
            result =  preparedStatementExecutorWrapper.getPreparedStatement().executeUpdate();
        } catch (final SQLException ex) {
            ExecutorUtils.handleException(event, ex);
            return 0;
        }
        event.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        ExecutionEventBus.getInstance().post(event);
        return result;
    }
    
    /**
     * 执行SQL请求.
     * 
     * @return true表示执行DQL, false表示执行的DML
     */
    public boolean execute() {
        Context context = MetricsContext.start("ShardingPreparedStatement-execute");
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        try {
            if (1 == preparedStatementExecutorWrappers.size()) {
                PreparedStatementExecutorWrapper preparedStatementExecutorWrapper = preparedStatementExecutorWrappers.iterator().next();
                return executeInternal(preparedStatementExecutorWrapper, isExceptionThrown, dataMap);
            }
            List<Boolean> result = executorEngine.execute(preparedStatementExecutorWrappers, new ExecuteUnit<PreparedStatementExecutorWrapper, Boolean>() {
        
                @Override
                public Boolean execute(final PreparedStatementExecutorWrapper input) throws Exception {
                    synchronized (input.getPreparedStatement().getConnection()) {
                        return executeInternal(input, isExceptionThrown, dataMap);
                    }
                }
            });
            return (null == result || result.isEmpty()) ? false : result.get(0);
        } finally {
            MetricsContext.stop(context);
        }
    }
    
    private boolean executeInternal(final PreparedStatementExecutorWrapper preparedStatementExecutorWrapper, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        boolean result;
        ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
        ExecutorDataMap.setDataMap(dataMap);
        AbstractExecutionEvent event = ExecutorUtils.getExecutionEvent(sqlType, preparedStatementExecutorWrapper.getSqlExecutionUnit());
        ExecutionEventBus.getInstance().post(event);
        try {
            result = preparedStatementExecutorWrapper.getPreparedStatement().execute();
        } catch (final SQLException ex) {
            ExecutorUtils.handleException(event, ex);
            return false;
        }
        event.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        ExecutionEventBus.getInstance().post(event);
        return result;
    }
}
