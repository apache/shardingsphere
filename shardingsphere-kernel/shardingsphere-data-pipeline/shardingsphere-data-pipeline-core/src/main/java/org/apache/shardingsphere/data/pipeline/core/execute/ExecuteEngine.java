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

package org.apache.shardingsphere.data.pipeline.core.execute;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.executor.LifecycleExecutor;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;

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
public final class ExecuteEngine {
    
    public static final String THREAD_NAME_FORMAT = "Scaling-execute-%d";
    
    private final ListeningExecutorService executorService;
    
    /**
     * Create task execute engine instance with cached thread pool.
     *
     * @return task execute engine instance
     */
    public static ExecuteEngine newCachedThreadInstance() {
        return new ExecuteEngine(MoreExecutors.listeningDecorator(Executors.newCachedThreadPool(ExecutorThreadFactoryBuilder.build(THREAD_NAME_FORMAT))));
    }
    
    /**
     * Create task execute engine instance with fixed thread pool.
     *
     * @param threadNumber thread number
     * @return task execute engine instance
     */
    public static ExecuteEngine newFixedThreadInstance(final int threadNumber) {
        return new ExecuteEngine(MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(threadNumber, ExecutorThreadFactoryBuilder.build(THREAD_NAME_FORMAT))));
    }
    
    /**
     * Submit a {@code LifecycleExecutor} without callback to execute.
     *
     * @param lifecycleExecutor lifecycle executor
     * @return execute future
     */
    public Future<?> submit(final LifecycleExecutor lifecycleExecutor) {
        return executorService.submit(lifecycleExecutor);
    }
    
    /**
     * Submit a {@code LifecycleExecutor} with callback {@code ExecuteCallback} to execute.
     *
     * @param lifecycleExecutor lifecycle executor
     * @param executeCallback execute callback
     * @return execute future
     */
    public Future<?> submit(final LifecycleExecutor lifecycleExecutor, final ExecuteCallback executeCallback) {
        ListenableFuture<?> result = executorService.submit(lifecycleExecutor);
        Futures.addCallback(result, new ExecuteFutureCallback<>(executeCallback), executorService);
        return result;
    }
    
    /**
     * Submit a collection of {@code LifecycleExecutor} with callback {@code ExecuteCallback} to execute.
     *
     * @param lifecycleExecutors lifecycle executor
     * @param executeCallback execute callback
     * @return execute future of all
     */
    public Future<?> submitAll(final Collection<? extends LifecycleExecutor> lifecycleExecutors, final ExecuteCallback executeCallback) {
        Collection<ListenableFuture<?>> listenableFutures = new ArrayList<>(lifecycleExecutors.size());
        for (LifecycleExecutor each : lifecycleExecutors) {
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
