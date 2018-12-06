/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.util;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.shardingsphere.core.executor.ShardingThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Util class for creating ListeningExecutorService.
 *
 * @author wuxu
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ListeningExecutorServiceUtil {
    
    private static final ExecutorService SHUTDOWN_EXECUTOR = Executors.newSingleThreadExecutor(ShardingThreadFactoryBuilder.build("Executor-Engine-Closer"));
    
    /**
     * Get listening executor service.
     *
     * @param executorSize thread count of the thread pool
     * @return instance of listening executor service
     */
    public static ListeningExecutorService createAndGet(final int executorSize) {
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
                0 == executorSize ? Executors.newCachedThreadPool(ShardingThreadFactoryBuilder.build()) : Executors.newFixedThreadPool(executorSize, ShardingThreadFactoryBuilder.build()));
        MoreExecutors.addDelayedShutdownHook(executorService, 60, TimeUnit.SECONDS);
        return executorService;
    }
    
    /**
     * Close executor service.
     *
     * @param executorService a executor service to be closed
     */
    public static void close(final ListeningExecutorService executorService) {
        SHUTDOWN_EXECUTOR.execute(new Runnable() {
            
            @Override
            public void run() {
                try {
                    executorService.shutdown();
                    while (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
