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
import com.dangdang.ddframe.rdb.sharding.executor.event.EventExecutionType;
import com.dangdang.ddframe.rdb.sharding.executor.wrapper.PreparedStatementExecutorWrapper;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.google.common.base.Optional;

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
public final class PreparedStatementExecutor {
    
    private final ExecutorEngine executorEngine;
    
    private final Collection<PreparedStatementExecutorWrapper> preparedStatementExecutorWrappers;
    
    private final EventPostman eventPostman;
    
    public PreparedStatementExecutor(final ExecutorEngine executorEngine, final Collection<PreparedStatementExecutorWrapper> preparedStatementExecutorWrappers) {
        this.executorEngine = executorEngine;
        this.preparedStatementExecutorWrappers = preparedStatementExecutorWrappers;
        this.eventPostman = new EventPostman(preparedStatementExecutorWrappers);
    }
    
    /**
     * 执行SQL查询.
     * 
     * @return 结果集列表
     */
    public List<ResultSet> executeQuery() {
        Context context = MetricsContext.start("ShardingPreparedStatement-executeQuery");
        eventPostman.postExecutionEvents();
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
    
    private ResultSet executeQueryInternal(final PreparedStatementExecutorWrapper preparedStatementExecutorWrapper,
                                           final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        ResultSet result;
        ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
        ExecutorDataMap.setDataMap(dataMap);
        try {
            result = preparedStatementExecutorWrapper.getPreparedStatement().executeQuery();
        } catch (final SQLException ex) {
            eventPostman.postExecutionEventsAfterExecution(preparedStatementExecutorWrapper, EventExecutionType.EXECUTE_FAILURE, Optional.of(ex));
            ExecutorExceptionHandler.handleException(ex);
            return null;
        }
        eventPostman.postExecutionEventsAfterExecution(preparedStatementExecutorWrapper);
        return result;
    }
    
    /**
     * 执行SQL更新.
     * 
     * @return 更新数量
     */
    public int executeUpdate() {
        Context context = MetricsContext.start("ShardingPreparedStatement-executeUpdate");
        eventPostman.postExecutionEvents();
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
    
    private int executeUpdateInternal(final PreparedStatementExecutorWrapper preparedStatementExecutorWrapper,
                                      final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        int result;
        ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
        ExecutorDataMap.setDataMap(dataMap);
        try {
            result =  preparedStatementExecutorWrapper.getPreparedStatement().executeUpdate();
        } catch (final SQLException ex) {
            eventPostman.postExecutionEventsAfterExecution(preparedStatementExecutorWrapper, EventExecutionType.EXECUTE_FAILURE, Optional.of(ex));
            ExecutorExceptionHandler.handleException(ex);
            return 0;
        }
        eventPostman.postExecutionEventsAfterExecution(preparedStatementExecutorWrapper);
        return result;
    }
    
    /**
     * 执行SQL请求.
     * 
     * @return true表示执行DQL, false表示执行的DML
     */
    public boolean execute() {
        Context context = MetricsContext.start("ShardingPreparedStatement-execute");
        eventPostman.postExecutionEvents();
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
    
    private boolean executeInternal(final PreparedStatementExecutorWrapper preparedStatementExecutorWrapper,
                                    final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        boolean result;
        ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
        ExecutorDataMap.setDataMap(dataMap);
        try {
            result = preparedStatementExecutorWrapper.getPreparedStatement().execute();
        } catch (final SQLException ex) {
            eventPostman.postExecutionEventsAfterExecution(preparedStatementExecutorWrapper, EventExecutionType.EXECUTE_FAILURE, Optional.of(ex));
            ExecutorExceptionHandler.handleException(ex);
            return false;
        }
        eventPostman.postExecutionEventsAfterExecution(preparedStatementExecutorWrapper);
        return result;
    }
    
    
    /**
     * 执行批量接口.
     *
     * @return 每个
     */
    public int[] executeBatch() {
        Context context = MetricsContext.start("ShardingPreparedStatement-executeUpdate");
        eventPostman.postExecutionEvents();
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        try {
            if (1 == preparedStatementExecutorWrappers.size()) {
                return executeBatchInternal(preparedStatementExecutorWrappers.iterator().next(), isExceptionThrown, dataMap);
            }
            return executorEngine.execute(preparedStatementExecutorWrappers, new ExecuteUnit<PreparedStatementExecutorWrapper, int[]>() {
                
                @Override
                public int[] execute(final PreparedStatementExecutorWrapper input) throws Exception {
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
                    int length = 0;
                    for (int[] array : results) {
                        length += array.length;
                    }
                    int[] result = new int[length];
                    int pos = 0;
                    for (int[] array : results) {
                        System.arraycopy(array, 0, result, pos, array.length);
                        pos += array.length;
                    }
                    return result;
                }
            });
        } finally {
            MetricsContext.stop(context);
        }
    }
    
    private int[] executeBatchInternal(final PreparedStatementExecutorWrapper batchPreparedStatementExecutorWrapper, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        int[] result;
        ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
        ExecutorDataMap.setDataMap(dataMap);
        try {
            result = batchPreparedStatementExecutorWrapper.getPreparedStatement().executeBatch();
        } catch (final SQLException ex) {
            eventPostman.postExecutionEventsAfterExecution(batchPreparedStatementExecutorWrapper, EventExecutionType.EXECUTE_FAILURE, Optional.of(ex));
            ExecutorExceptionHandler.handleException(ex);
            return null;
        }
        eventPostman.postExecutionEventsAfterExecution(batchPreparedStatementExecutorWrapper);
        return result;
    }
}
