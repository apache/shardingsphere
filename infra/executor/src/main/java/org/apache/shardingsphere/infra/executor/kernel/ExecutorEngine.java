/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.executor.kernel;

import lombok.Getter;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorCallback;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorServiceManager;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnknownSQLException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Executor engine.
 */
@Getter
public final class ExecutorEngine implements AutoCloseable {
    
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    
    private final ExecutorServiceManager executorServiceManager;
    
    private ExecutorEngine(final int executorSize) {
        executorServiceManager = new ExecutorServiceManager(executorSize);
    }
    
    /**
     * Create executor engine with executor size.
     *
     * @param executorSize executor size
     * @return created executor engine
     */
    public static ExecutorEngine createExecutorEngineWithSize(final int executorSize) {
        return new ExecutorEngine(executorSize);
    }
    
    /**
     * Create executor engine with CPU and resources.
     * 
     * @param resourceCount resource count
     * @return created executor engine
     */
    public static ExecutorEngine createExecutorEngineWithCPUAndResources(final int resourceCount) {
        int cpuThreadCount = CPU_CORES * 2 - 1;
        int resourceThreadCount = Math.max(resourceCount, 1);
        return new ExecutorEngine(Math.min(cpuThreadCount, resourceThreadCount));
    }
    
    /**
     * Create executor engine with CPU.
     *
     * @return created executor engine
     */
    public static ExecutorEngine createExecutorEngineWithCPU() {
        int cpuThreadCount = CPU_CORES * 2 - 1;
        return new ExecutorEngine(cpuThreadCount);
    }
    
    /**
     * Execute.
     *
     * @param executionGroupContext execution group context
     * @param callback executor callback
     * @param <I> type of input value
     * @param <O> type of return value
     * @return execute result
     * @throws SQLException throw if execute failure
     */
    public <I, O> List<O> execute(final ExecutionGroupContext<I> executionGroupContext, final ExecutorCallback<I, O> callback) throws SQLException {
        return execute(executionGroupContext, null, callback, false);
    }
    
    /**
     * Execute.
     *
     * @param executionGroupContext execution group context
     * @param firstCallback first executor callback
     * @param callback other executor callback
     * @param serial whether using multi thread execute or not
     * @param <I> type of input value
     * @param <O> type of return value
     * @return execute result
     * @throws SQLException throw if execute failure
     */
    public <I, O> List<O> execute(final ExecutionGroupContext<I> executionGroupContext,
                                  final ExecutorCallback<I, O> firstCallback, final ExecutorCallback<I, O> callback, final boolean serial) throws SQLException {
        if (executionGroupContext.getInputGroups().isEmpty()) {
            return Collections.emptyList();
        }
        return serial ? serialExecute(executionGroupContext.getInputGroups().iterator(), firstCallback, callback)
                : parallelExecute(executionGroupContext.getInputGroups().iterator(), firstCallback, callback);
    }
    
    private <I, O> List<O> serialExecute(final Iterator<ExecutionGroup<I>> executionGroups, final ExecutorCallback<I, O> firstCallback, final ExecutorCallback<I, O> callback) throws SQLException {
        ExecutionGroup<I> firstInputs = executionGroups.next();
        List<O> result = new LinkedList<>(syncExecute(firstInputs, null == firstCallback ? callback : firstCallback));
        while (executionGroups.hasNext()) {
            result.addAll(syncExecute(executionGroups.next(), callback));
        }
        return result;
    }
    
    private <I, O> List<O> parallelExecute(final Iterator<ExecutionGroup<I>> executionGroups, final ExecutorCallback<I, O> firstCallback, final ExecutorCallback<I, O> callback) throws SQLException {
        ExecutionGroup<I> firstInputs = executionGroups.next();
        Collection<Future<Collection<O>>> restResultFutures = asyncExecute(executionGroups, callback);
        return getGroupResults(syncExecute(firstInputs, null == firstCallback ? callback : firstCallback), restResultFutures);
    }
    
    private <I, O> Collection<O> syncExecute(final ExecutionGroup<I> executionGroup, final ExecutorCallback<I, O> callback) throws SQLException {
        return callback.execute(executionGroup.getInputs(), true);
    }
    
    private <I, O> Collection<Future<Collection<O>>> asyncExecute(final Iterator<ExecutionGroup<I>> executionGroups, final ExecutorCallback<I, O> callback) {
        Collection<Future<Collection<O>>> result = new LinkedList<>();
        while (executionGroups.hasNext()) {
            result.add(asyncExecute(executionGroups.next(), callback));
        }
        return result;
    }
    
    private <I, O> Future<Collection<O>> asyncExecute(final ExecutionGroup<I> executionGroup, final ExecutorCallback<I, O> callback) {
        return executorServiceManager.getExecutorService().submit(() -> callback.execute(executionGroup.getInputs(), false));
    }
    
    private <O> List<O> getGroupResults(final Collection<O> firstResults, final Collection<Future<Collection<O>>> restFutures) throws SQLException {
        List<O> result = new LinkedList<>(firstResults);
        for (Future<Collection<O>> each : restFutures) {
            try {
                result.addAll(each.get());
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (final ExecutionException ex) {
                return throwException(ex);
            }
        }
        return result;
    }
    
    private <O> List<O> throwException(final Exception exception) throws SQLException {
        if (exception.getCause() instanceof SQLException) {
            throw (SQLException) exception.getCause();
        }
        throw new UnknownSQLException(exception);
    }
    
    @Override
    public void close() {
        executorServiceManager.close();
    }
}
