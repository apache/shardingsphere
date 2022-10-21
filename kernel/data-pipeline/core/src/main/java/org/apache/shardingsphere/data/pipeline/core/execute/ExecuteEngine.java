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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.executor.LifecycleExecutor;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executor engine.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecuteEngine {
    
    private static final String THREAD_PREFIX = "pipeline-";
    
    private static final String THREAD_SUFFIX = "-%d";
    
    private final ExecutorService executorService;
    
    /**
     * Create task execute engine instance with cached thread pool.
     *
     * @param threadName thread name
     * @return task execute engine instance
     */
    public static ExecuteEngine newCachedThreadInstance(final String threadName) {
        String threadNameFormat = THREAD_PREFIX + threadName + THREAD_SUFFIX;
        return new ExecuteEngine(Executors.newCachedThreadPool(ExecutorThreadFactoryBuilder.build(threadNameFormat)));
    }
    
    /**
     * Create task execute engine instance with fixed thread pool.
     *
     * @param threadNumber thread number
     * @param threadName thread name
     * @return task execute engine instance
     */
    public static ExecuteEngine newFixedThreadInstance(final int threadNumber, final String threadName) {
        String threadNameFormat = THREAD_PREFIX + threadName + THREAD_SUFFIX;
        return new ExecuteEngine(Executors.newFixedThreadPool(threadNumber, ExecutorThreadFactoryBuilder.build(threadNameFormat)));
    }
    
    /**
     * Submit a {@code LifecycleExecutor} with callback {@code ExecuteCallback} to execute.
     *
     * @param lifecycleExecutor lifecycle executor
     * @param executeCallback execute callback
     * @return execute future
     */
    public CompletableFuture<?> submit(final LifecycleExecutor lifecycleExecutor, final ExecuteCallback executeCallback) {
        return CompletableFuture.runAsync(lifecycleExecutor, executorService).whenCompleteAsync((unused, throwable) -> {
            if (null == throwable) {
                executeCallback.onSuccess();
            } else {
                Throwable cause = throwable.getCause();
                executeCallback.onFailure(null != cause ? cause : throwable);
            }
        }, executorService);
    }
    
    /**
     * Submit a collection of {@code LifecycleExecutor} with callback {@code ExecuteCallback} to execute.
     *
     * @param lifecycleExecutors lifecycle executor
     * @param executeCallback execute callback
     * @return execute future of all
     */
    public CompletableFuture<?> submitAll(final Collection<? extends LifecycleExecutor> lifecycleExecutors, final ExecuteCallback executeCallback) {
        CompletableFuture<?>[] futures = new CompletableFuture[lifecycleExecutors.size()];
        int i = 0;
        for (LifecycleExecutor each : lifecycleExecutors) {
            futures[i++] = CompletableFuture.runAsync(each, executorService);
        }
        return CompletableFuture.allOf(futures).whenCompleteAsync((unused, throwable) -> {
            if (null == throwable) {
                executeCallback.onSuccess();
            } else {
                Throwable cause = throwable.getCause();
                executeCallback.onFailure(null != cause ? cause : throwable);
            }
        }, executorService);
    }
}
