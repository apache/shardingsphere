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

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.impl.ShardingSphereExecutorService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Executor kernel.
 */
@Getter
public final class ExecutorKernel implements AutoCloseable {
    
    private final ShardingSphereExecutorService executorService;
    
    public ExecutorKernel(final int executorSize) {
        executorService = new ShardingSphereExecutorService(executorSize);
    }
    
    /**
     * Execute.
     *
     * @param inputGroups input groups
     * @param callback executor callback
     * @param <I> type of input value
     * @param <O> type of return value
     * @return execute result
     * @throws SQLException throw if execute failure
     */
    public <I, O> List<O> execute(final Collection<InputGroup<I>> inputGroups, final ExecutorCallback<I, O> callback) throws SQLException {
        return execute(inputGroups, null, callback, false);
    }
    
    /**
     * Execute.
     *
     * @param inputGroups input groups
     * @param firstCallback first executor callback
     * @param callback other executor callback
     * @param serial whether using multi thread execute or not
     * @param <I> type of input value
     * @param <O> type of return value
     * @return execute result
     * @throws SQLException throw if execute failure
     */
    public <I, O> List<O> execute(final Collection<InputGroup<I>> inputGroups,
                                  final ExecutorCallback<I, O> firstCallback, final ExecutorCallback<I, O> callback, final boolean serial) throws SQLException {
        if (inputGroups.isEmpty()) {
            return Collections.emptyList();
        }
        return serial ? serialExecute(inputGroups, firstCallback, callback) : parallelExecute(inputGroups, firstCallback, callback);
    }
    
    private <I, O> List<O> serialExecute(final Collection<InputGroup<I>> inputGroups, final ExecutorCallback<I, O> firstCallback, final ExecutorCallback<I, O> callback) throws SQLException {
        Iterator<InputGroup<I>> inputGroupsIterator = inputGroups.iterator();
        InputGroup<I> firstInputs = inputGroupsIterator.next();
        List<O> result = new LinkedList<>(syncExecute(firstInputs, null == firstCallback ? callback : firstCallback));
        for (InputGroup<I> each : Lists.newArrayList(inputGroupsIterator)) {
            result.addAll(syncExecute(each, callback));
        }
        return result;
    }
    
    private <I, O> List<O> parallelExecute(final Collection<InputGroup<I>> inputGroups, final ExecutorCallback<I, O> firstCallback, final ExecutorCallback<I, O> callback) throws SQLException {
        Iterator<InputGroup<I>> inputGroupsIterator = inputGroups.iterator();
        InputGroup<I> firstInputs = inputGroupsIterator.next();
        Collection<ListenableFuture<Collection<O>>> restResultFutures = asyncExecute(Lists.newArrayList(inputGroupsIterator), callback);
        return getGroupResults(syncExecute(firstInputs, null == firstCallback ? callback : firstCallback), restResultFutures);
    }
    
    private <I, O> Collection<O> syncExecute(final InputGroup<I> inputGroup, final ExecutorCallback<I, O> callback) throws SQLException {
        return callback.execute(inputGroup.getInputs(), true, ExecutorDataMap.getValue());
    }
    
    private <I, O> Collection<ListenableFuture<Collection<O>>> asyncExecute(final List<InputGroup<I>> inputGroups, final ExecutorCallback<I, O> callback) {
        Collection<ListenableFuture<Collection<O>>> result = new LinkedList<>();
        for (InputGroup<I> each : inputGroups) {
            result.add(asyncExecute(each, callback));
        }
        return result;
    }
    
    private <I, O> ListenableFuture<Collection<O>> asyncExecute(final InputGroup<I> inputGroup, final ExecutorCallback<I, O> callback) {
        Map<String, Object> dataMap = ExecutorDataMap.getValue();
        return executorService.getExecutorService().submit(() -> callback.execute(inputGroup.getInputs(), false, dataMap));
    }
    
    private <O> List<O> getGroupResults(final Collection<O> firstResults, final Collection<ListenableFuture<Collection<O>>> restFutures) throws SQLException {
        List<O> result = new LinkedList<>(firstResults);
        for (ListenableFuture<Collection<O>> each : restFutures) {
            try {
                result.addAll(each.get());
            } catch (final InterruptedException | ExecutionException ex) {
                return throwException(ex);
            }
        }
        return result;
    }
    
    private <O> List<O> throwException(final Exception exception) throws SQLException {
        if (exception.getCause() instanceof SQLException) {
            throw (SQLException) exception.getCause();
        }
        throw new ShardingSphereException(exception);
    }
    
    @Override
    public void close() {
        executorService.close();
    }
}
