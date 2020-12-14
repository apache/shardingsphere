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

package org.apache.shardingsphere.scaling.core.execute.engine;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.scaling.core.execute.executor.ScalingExecutor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Scaling executor engine.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TaskExecuteEngine {
    
    public static final String THREAD_NAME_FORMAT = "ShardingScaling-execute-%d";
    
    private final ListeningExecutorService executorService;
    
    /**
     * Create task execute engine instance with cached thread pool.
     *
     * @return task execute engine instance
     */
    public static TaskExecuteEngine newCachedThreadInstance() {
        return new TaskExecuteEngine(MoreExecutors.listeningDecorator(Executors.newCachedThreadPool(ExecutorThreadFactoryBuilder.build(THREAD_NAME_FORMAT))));
    }
    
    /**
     * Create task execute engine instance with fixed thread pool.
     *
     * @param threadNumber thread number
     * @return task execute engine instance
     */
    public static TaskExecuteEngine newFixedThreadInstance(final int threadNumber) {
        return new TaskExecuteEngine(MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(threadNumber, ExecutorThreadFactoryBuilder.build(THREAD_NAME_FORMAT))));
    }
    
    /**
     * Submit a {@code ScalingExecutor} without callback to execute.
     *
     * @param scalingExecutor scaling executor
     * @return execute future
     */
    public Future<?> submit(final ScalingExecutor scalingExecutor) {
        return executorService.submit(scalingExecutor);
    }
    
    /**
     * Submit a {@code ScalingExecutor} with callback {@code ExecuteCallback} to execute.
     *
     * @param scalingExecutor scaling executor
     * @param executeCallback execute callback
     * @return execute future
     */
    public Future<?> submit(final ScalingExecutor scalingExecutor, final ExecuteCallback executeCallback) {
        ListenableFuture<?> result = executorService.submit(scalingExecutor);
        Futures.addCallback(result, new ExecuteFutureCallback<>(executeCallback), executorService);
        return result;
    }
    
    /**
     * Submit a collection of {@code ScalingExecutor} with callback {@code ExecuteCallback} to execute.
     *
     * @param scalingExecutors scaling executor
     * @param executeCallback execute callback
     * @return execute future of all
     */
    public Future<?> submitAll(final Collection<? extends ScalingExecutor> scalingExecutors, final ExecuteCallback executeCallback) {
        Collection<ListenableFuture<?>> listenableFutures = new ArrayList<>(scalingExecutors.size());
        for (ScalingExecutor each : scalingExecutors) {
            ListenableFuture<?> listenableFuture = executorService.submit(each);
            listenableFutures.add(listenableFuture);
        }
        ListenableFuture<List<Object>> result = Futures.allAsList(listenableFutures);
        Futures.addCallback(result, new ExecuteFutureCallback<Collection<?>>(executeCallback), executorService);
        return result;
    }
    
    @RequiredArgsConstructor
    private static class ExecuteFutureCallback<V> implements FutureCallback<V> {
        
        private final ExecuteCallback executeCallback;
        
        @Override
        public void onSuccess(final V result) {
            executeCallback.onSuccess();
        }
        
        @Override
        @ParametersAreNonnullByDefault
        public void onFailure(final Throwable throwable) {
            executeCallback.onFailure(throwable);
        }
    }
}
