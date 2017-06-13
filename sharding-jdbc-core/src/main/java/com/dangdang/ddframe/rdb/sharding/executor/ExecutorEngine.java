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

import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.executor.event.AbstractExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.DQLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.EventExecutionType;
import com.dangdang.ddframe.rdb.sharding.executor.threadlocal.ExecutorDataMap;
import com.dangdang.ddframe.rdb.sharding.executor.threadlocal.ExecutorExceptionHandler;
import com.dangdang.ddframe.rdb.sharding.executor.type.batch.BatchPreparedStatementUnit;
import com.dangdang.ddframe.rdb.sharding.util.EventBusInstance;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 多线程执行框架.
 * 
 * @author gaohongtao
 */
@Slf4j
public final class ExecutorEngine implements AutoCloseable {
    
    private final ListeningExecutorService executorService;
    
    public ExecutorEngine(final int executorSize) {
        executorService = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(
                executorSize, executorSize, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingJDBC-%d").build()));
        MoreExecutors.addDelayedShutdownHook(executorService, 60, TimeUnit.SECONDS);
    }
    
    /**
     * 多线程执行任务.
     * 
     * <p>
     * 一组任务中, 将第一个任务放在当前线程执行, 其余的任务放到线程池中运行.
     * </p>
     *  
     * @param sqlType SQL类型
     * @param baseStatementUnits 语句对象执行单元集合
     * @param parameters 参数
     * @param executeUnit 执行单元
     * @param <T> 返回值类型
     * @return 执行结果
     */
    public <T> List<T> execute(final SQLType sqlType, final Collection<? extends BaseStatementUnit> baseStatementUnits, final List<Object> parameters, final ExecuteUnit<T> executeUnit) {
        if (baseStatementUnits.isEmpty()) {
            return Collections.emptyList();
        }
        Iterator<? extends BaseStatementUnit> iterator = baseStatementUnits.iterator();
        BaseStatementUnit firstInput = iterator.next();
        ListenableFuture<List<T>> restFutures = asyncExecute(Lists.newArrayList(iterator), executeUnit, sqlType, parameters);
        T firstOutput;
        List<T> restOutputs;
        try {
            firstOutput = syncExecute(firstInput, executeUnit, sqlType, parameters);
            restOutputs = restFutures.get();
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            ExecutorExceptionHandler.handleException(ex);
            return null;
        }
        List<T> result = Lists.newLinkedList(restOutputs);
        result.add(0, firstOutput);
        return result;
    }
    
    private <T> ListenableFuture<List<T>> asyncExecute(final Collection<BaseStatementUnit> baseStatementUnits, final ExecuteUnit<T> executeUnit, final SQLType sqlType, final List<Object> parameters) {
        List<ListenableFuture<T>> result = new ArrayList<>(baseStatementUnits.size());
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        for (final BaseStatementUnit each : baseStatementUnits) {
            result.add(executorService.submit(new Callable<T>() {
                
                @Override
                public T call() throws Exception {
                    return executeInternal(each, executeUnit, sqlType, isExceptionThrown, dataMap, parameters);
                }
            }));
        }
        return Futures.allAsList(result);
    }
    
    private <T> T syncExecute(final BaseStatementUnit baseStatementUnit, final ExecuteUnit<T> executeUnit, final SQLType sqlType, final List<Object> parameters) throws Exception {
        return executeInternal(baseStatementUnit, executeUnit, sqlType, ExecutorExceptionHandler.isExceptionThrown(), ExecutorDataMap.getDataMap(), parameters);
    }
    
    private <T> T executeInternal(final BaseStatementUnit baseStatementUnit, final ExecuteUnit<T> executeUnit, final SQLType sqlType, 
                          final boolean isExceptionThrown, final Map<String, Object> dataMap, final List<Object> parameters) throws Exception {
        synchronized (baseStatementUnit.getStatement().getConnection()) {
            T result;
            ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
            ExecutorDataMap.setDataMap(dataMap);
            AbstractExecutionEvent event;
            if (SQLType.SELECT == sqlType) {
                event = new DQLExecutionEvent(baseStatementUnit.getSqlExecutionUnit().getDataSource(), baseStatementUnit.getSqlExecutionUnit().getSql(), parameters);
            } else {
                event = new DMLExecutionEvent(baseStatementUnit.getSqlExecutionUnit().getDataSource(), baseStatementUnit.getSqlExecutionUnit().getSql(), parameters);
            }
            EventBusInstance.getInstance().post(event);
            try {
                result = executeUnit.execute(baseStatementUnit);
            } catch (final SQLException ex) {
                event.setEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
                event.setException(Optional.of(ex));
                EventBusInstance.getInstance().post(event);
                ExecutorExceptionHandler.handleException(ex);
                return null;
            }
            event.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
            EventBusInstance.getInstance().post(event);
            return result;
        }
    }
    
    /**
     * 多线程执行批量任务.
     *
     * <p>
     * 一组任务中, 将第一个任务放在当前线程执行, 其余的任务放到线程池中运行.
     * </p>
     *
     * @param batchPreparedStatementUnits 批量语句对象执行单元集合
     * @param parameterSets 参数集
     * @return 执行结果
     */
    public int[] executeBatch(final Collection<BatchPreparedStatementUnit> batchPreparedStatementUnits, final List<List<Object>> parameterSets) {
        if (batchPreparedStatementUnits.isEmpty()) {
            return new int[0];
        }
        Iterator<BatchPreparedStatementUnit> iterator = batchPreparedStatementUnits.iterator();
        BatchPreparedStatementUnit firstInput = iterator.next();
        ListenableFuture<List<int[]>> restFutures = asyncExecuteBatch(Lists.newArrayList(iterator), parameterSets);
        int[] firstOutput;
        List<int[]> restOutputs;
        try {
            firstOutput = syncExecuteBatch(firstInput, parameterSets);
            restOutputs = restFutures.get();
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            ExecutorExceptionHandler.handleException(ex);
            return new int[0];
        }
        List<int[]> result = Lists.newLinkedList(restOutputs);
        result.add(0, firstOutput);
        return accumulateBatchResults(batchPreparedStatementUnits, parameterSets, result);
    }
    
    private ListenableFuture<List<int[]>> asyncExecuteBatch(final Collection<BatchPreparedStatementUnit> batchPreparedStatementUnits, final List<List<Object>> parameterSets) {
        List<ListenableFuture<int[]>> result = new ArrayList<>(batchPreparedStatementUnits.size());
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        for (final BatchPreparedStatementUnit each : batchPreparedStatementUnits) {
            result.add(executorService.submit(new Callable<int[]>() {
                
                @Override
                public int[] call() throws Exception {
                    return executeBatchInternal(each, parameterSets, isExceptionThrown, dataMap);
                }
            }));
        }
        return Futures.allAsList(result);
    }
    
    private int[] syncExecuteBatch(final BatchPreparedStatementUnit batchPreparedStatementUnit, final List<List<Object>> parameterSets) throws Exception {
        return executeBatchInternal(batchPreparedStatementUnit, parameterSets, ExecutorExceptionHandler.isExceptionThrown(), ExecutorDataMap.getDataMap());
    }
    
    private int[] executeBatchInternal(
            final BatchPreparedStatementUnit batchPreparedStatementUnit, final List<List<Object>> parameterSets, final boolean isExceptionThrown, final Map<String, Object> dataMap) throws Exception {
        synchronized (batchPreparedStatementUnit.getStatement().getConnection()) {
            int[] result;
            ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
            ExecutorDataMap.setDataMap(dataMap);
            List<AbstractExecutionEvent> events = new LinkedList<>();
            for (List<Object> each : parameterSets) {
                AbstractExecutionEvent event = new DMLExecutionEvent(batchPreparedStatementUnit.getSqlExecutionUnit().getDataSource(), batchPreparedStatementUnit.getSqlExecutionUnit().getSql(), each);
                events.add(event);
                EventBusInstance.getInstance().post(event);
            }
            try {
                result = batchPreparedStatementUnit.getStatement().executeBatch();
            } catch (final SQLException ex) {
                for (AbstractExecutionEvent each : events) {
                    each.setEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
                    each.setException(Optional.of(ex));
                    EventBusInstance.getInstance().post(each);
                    ExecutorExceptionHandler.handleException(ex);
                }
                return new int[0];
            }
            for (AbstractExecutionEvent each : events) {
                each.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
                EventBusInstance.getInstance().post(each);
            }
            return result;
        }
    }
    
    private int[] accumulateBatchResults(final Collection<BatchPreparedStatementUnit> baseStatementUnits, final List<List<Object>> parameterSets, final List<int[]> results) {
        int[] result = new int[parameterSets.size()];
        int count = 0;
        for (BatchPreparedStatementUnit each : baseStatementUnits) {
            for (Map.Entry<Integer, Integer> entry : each.getOuterAndInnerAddBatchCountMap().entrySet()) {
                result[entry.getKey()] += results.get(count)[entry.getValue()];
            }
            count++;
        }
        return result;
    }
    
    @Override
    public void close() {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (final InterruptedException ignored) {
        }
        if (!executorService.isTerminated()) {
            throw new ShardingJdbcException("ExecutorEngine can not been terminated");
        }
    }
}
