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
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

/**
 * Pipeline executor engine.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineExecuteEngine {
    
    private static final String THREAD_PREFIX = "pipeline-";
    
    private static final String THREAD_SUFFIX = "-%d";
    
    private static final ExecutorService CALLBACK_EXECUTOR = Executors.newSingleThreadScheduledExecutor(ExecutorThreadFactoryBuilder.build(THREAD_PREFIX + "callback" + THREAD_SUFFIX));
    
    private final ExecutorService executorService;
    
    /**
     * Create task execute engine instance with cached thread pool.
     *
     * @param threadName thread name
     * @return task execute engine instance
     */
    public static PipelineExecuteEngine newCachedThreadInstance(final String threadName) {
        String threadNameFormat = THREAD_PREFIX + threadName + THREAD_SUFFIX;
        return new PipelineExecuteEngine(Executors.newCachedThreadPool(ExecutorThreadFactoryBuilder.build(threadNameFormat)));
    }
    
    /**
     * Create task execute engine instance with fixed thread pool.
     *
     * @param threadNumber thread number
     * @param threadName thread name
     * @return task execute engine instance
     */
    public static PipelineExecuteEngine newFixedThreadInstance(final int threadNumber, final String threadName) {
        String threadNameFormat = THREAD_PREFIX + threadName + THREAD_SUFFIX;
        return new PipelineExecuteEngine(Executors.newFixedThreadPool(threadNumber, ExecutorThreadFactoryBuilder.build(threadNameFormat)));
    }
    
    /**
     * Submit a {@code LifecycleExecutor} to execute.
     *
     * @param pipelineLifecycleRunnable lifecycle executor
     * @return execute future
     */
    public CompletableFuture<?> submit(final PipelineLifecycleRunnable pipelineLifecycleRunnable) {
        return CompletableFuture.runAsync(pipelineLifecycleRunnable, executorService);
    }
    
    /**
     * Submit a {@code LifecycleExecutor} with callback {@code ExecuteCallback} to execute.
     *
     * @param pipelineLifecycleRunnable lifecycle executor
     * @param executeCallback execute callback
     * @return execute future
     */
    public CompletableFuture<?> submit(final PipelineLifecycleRunnable pipelineLifecycleRunnable, final ExecuteCallback executeCallback) {
        return CompletableFuture.runAsync(pipelineLifecycleRunnable, executorService).whenCompleteAsync((unused, throwable) -> {
            if (null == throwable) {
                executeCallback.onSuccess();
            } else {
                Throwable cause = throwable.getCause();
                executeCallback.onFailure(null == cause ? throwable : cause);
            }
        }, CALLBACK_EXECUTOR);
    }
    
    /**
     * Shutdown.
     */
    public void shutdown() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
            executorService.shutdownNow();
        }
    }
    
    /**
     * Trigger.
     *
     * @param futures futures
     * @param executeCallback execute callback on all the futures
     * @throws PipelineInternalException if there's underlying execution exception
     */
    @SneakyThrows(InterruptedException.class)
    public static void trigger(final Collection<CompletableFuture<?>> futures, final ExecuteCallback executeCallback) {
        BlockingQueue<CompletableFuture<?>> futureQueue = new LinkedBlockingQueue<>();
        for (CompletableFuture<?> each : futures) {
            each.whenCompleteAsync((BiConsumer<Object, Throwable>) (unused, throwable) -> {
                try {
                    futureQueue.put(each);
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }, CALLBACK_EXECUTOR);
        }
        for (int i = 1, count = futures.size(); i <= count; i++) {
            CompletableFuture<?> future = futureQueue.take();
            try {
                future.get();
            } catch (final ExecutionException ex) {
                Throwable cause = ex.getCause();
                executeCallback.onFailure(null != cause ? cause : ex);
                throw new PipelineInternalException(ex);
            }
        }
        executeCallback.onSuccess();
    }
}
