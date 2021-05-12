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

package org.apache.shardingsphere.proxy.frontend.executor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Connection thread executor group.
 *
 * <p>
 * Manage the thread for each backend connection invoking.
 * This ensure XA transaction framework processed by current thread id.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectionThreadExecutorGroup {
    
    private static final ConnectionThreadExecutorGroup INSTANCE = new ConnectionThreadExecutorGroup();
    
    private final Map<Integer, ExecutorService> executorServices = new ConcurrentHashMap<>();
    
    /**
     * Get connection thread executor group.
     *
     * @return connection thread executor group
     */
    public static ConnectionThreadExecutorGroup getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection.
     *
     * @param connectionId connection id
     */
    public void register(final int connectionId) {
        executorServices.put(connectionId, Executors.newSingleThreadExecutor());
    }
    
    /**
     * Get executor service of connection.
     *
     * @param connectionId connection id
     * @return executor service of current connection
     */
    public ExecutorService get(final int connectionId) {
        return executorServices.get(connectionId);
    }
    
    /**
     * Unregister connection and await termination.
     *
     * @param connectionId connection id
     */
    public void unregisterAndAwaitTermination(final int connectionId) {
        ExecutorService executorService = executorServices.remove(connectionId);
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
