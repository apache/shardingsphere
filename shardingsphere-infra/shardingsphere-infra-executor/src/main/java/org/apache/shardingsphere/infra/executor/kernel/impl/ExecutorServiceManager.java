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

package org.apache.shardingsphere.infra.executor.kernel.impl;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Executor service manager.
 */
@Getter
public final class ExecutorServiceManager {
    
    private static final String DEFAULT_NAME_FORMAT = "%d";
    
    private static final ExecutorService SHUTDOWN_EXECUTOR = Executors.newSingleThreadExecutor(ShardingSphereThreadFactoryBuilder.build("Executor-Engine-Closer"));
    
    private final ListeningExecutorService executorService;
    
    public ExecutorServiceManager(final int executorSize) {
        this(executorSize, DEFAULT_NAME_FORMAT);
    }
    
    public ExecutorServiceManager(final int executorSize, final String nameFormat) {
        executorService = MoreExecutors.listeningDecorator(getExecutorService(executorSize, nameFormat));
        MoreExecutors.addDelayedShutdownHook(executorService, 60, TimeUnit.SECONDS);
    }
    
    private ExecutorService getExecutorService(final int executorSize, final String nameFormat) {
        ThreadFactory threadFactory = ShardingSphereThreadFactoryBuilder.build(nameFormat);
        return 0 == executorSize ? Executors.newCachedThreadPool(threadFactory) : Executors.newFixedThreadPool(executorSize, threadFactory);
    }
    
    /**
     * Close executor service.
     */
    public void close() {
        SHUTDOWN_EXECUTOR.execute(() -> {
            try {
                executorService.shutdown();
                while (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
