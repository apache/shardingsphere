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

package com.dangdang.ddframe.rdb.sharding.executor.type.prepared;

import com.codahale.metrics.Timer.Context;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
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
    
    private final Collection<PreparedStatementUnit> preparedStatementUnits;
    
    private final List<Object> parameters;
    
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
            if (1 == preparedStatementUnits.size()) {
                PreparedStatementUnit preparedStatementUnit = preparedStatementUnits.iterator().next();
                return Collections.singletonList(executeQuery(preparedStatementUnit, isExceptionThrown, dataMap));
            }
            result = executorEngine.execute(preparedStatementUnits, new ExecuteUnit<PreparedStatementUnit, ResultSet>() {
                
                @Override
                public ResultSet execute(final PreparedStatementUnit input) throws Exception {
                    synchronized (input.getStatement().getConnection()) {
                        return executeQuery(input, isExceptionThrown, dataMap);
                    }
                }
            });
        } finally {
            MetricsContext.stop(context);
        }
        return result;
    }
    
    private ResultSet executeQuery(final PreparedStatementUnit preparedStatementUnit, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        ResultSet result;
        ExecutorUtils.setThreadLocalData(isExceptionThrown, dataMap);
        AbstractExecutionEvent event = ExecutorUtils.getExecutionEvent(sqlType, preparedStatementUnit.getSqlExecutionUnit(), parameters);
        EventBusInstance.getInstance().post(event);
        try {
            result = preparedStatementUnit.getStatement().executeQuery();
        } catch (final SQLException ex) {
            ExecutorUtils.handleException(event, ex);
            return null;
        }
        event.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        EventBusInstance.getInstance().post(event);
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
            if (1 == preparedStatementUnits.size()) {
                PreparedStatementUnit preparedStatementUnit = preparedStatementUnits.iterator().next();
                return executeUpdate(preparedStatementUnit, isExceptionThrown, dataMap);
            }
            List<Integer> results = executorEngine.execute(preparedStatementUnits, new ExecuteUnit<PreparedStatementUnit, Integer>() {
                
                @Override
                public Integer execute(final PreparedStatementUnit input) throws Exception {
                    synchronized (input.getStatement().getConnection()) {
                        return executeUpdate(input, isExceptionThrown, dataMap);
                    }
                }
            });
            return accumulate(results);
        } finally {
            MetricsContext.stop(context);
        }
    }
    
    private int executeUpdate(final PreparedStatementUnit preparedStatementUnit, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        int result;
        ExecutorUtils.setThreadLocalData(isExceptionThrown, dataMap);
        AbstractExecutionEvent event = ExecutorUtils.getExecutionEvent(sqlType, preparedStatementUnit.getSqlExecutionUnit(), parameters);
        EventBusInstance.getInstance().post(event);
        try {
            result =  preparedStatementUnit.getStatement().executeUpdate();
        } catch (final SQLException ex) {
            ExecutorUtils.handleException(event, ex);
            return 0;
        }
        event.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        EventBusInstance.getInstance().post(event);
        return result;
    }
    
    private int accumulate(final List<Integer> results) {
        int result = 0;
        for (int each : results) {
            result += each;
        }
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
            if (1 == preparedStatementUnits.size()) {
                PreparedStatementUnit preparedStatementUnit = preparedStatementUnits.iterator().next();
                return execute(preparedStatementUnit, isExceptionThrown, dataMap);
            }
            List<Boolean> result = executorEngine.execute(preparedStatementUnits, new ExecuteUnit<PreparedStatementUnit, Boolean>() {
                
                @Override
                public Boolean execute(final PreparedStatementUnit input) throws Exception {
                    synchronized (input.getStatement().getConnection()) {
                        return PreparedStatementExecutor.this.execute(input, isExceptionThrown, dataMap);
                    }
                }
            });
            return (null == result || result.isEmpty()) ? false : result.get(0);
        } finally {
            MetricsContext.stop(context);
        }
    }
    
    private boolean execute(final PreparedStatementUnit preparedStatementUnit, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        boolean result;
        ExecutorUtils.setThreadLocalData(isExceptionThrown, dataMap);
        AbstractExecutionEvent event = ExecutorUtils.getExecutionEvent(sqlType, preparedStatementUnit.getSqlExecutionUnit(), parameters);
        EventBusInstance.getInstance().post(event);
        try {
            result = preparedStatementUnit.getStatement().execute();
        } catch (final SQLException ex) {
            ExecutorUtils.handleException(event, ex);
            return false;
        }
        event.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        EventBusInstance.getInstance().post(event);
        return result;
    }
}
