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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.executor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Process thread executor group.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProcessThreadExecutorGroup {
    
    private static final ProcessThreadExecutorGroup INSTANCE = new ProcessThreadExecutorGroup();
    
    private final Map<Integer, ExecutorService> executorServices = new ConcurrentHashMap<>();
    
    /**
     * Get process thread executor group.
     *
     * @return connection thread executor group
     */
    public static ProcessThreadExecutorGroup getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get executor service of execution id.
     *
     * @param executionId execution id
     * @param threadNum thread num
     * @return executor service of current execution
     */
    public ExecutorService get(final String executionId, final int threadNum) {
        return executorServices.computeIfAbsent(Math.abs(executionId.hashCode()) % threadNum, this::newSingleThreadExecutorService);
    }
    
    private ExecutorService newSingleThreadExecutorService(final int key) {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), runnable -> new Thread(runnable, String.format("Connection-%d-ThreadExecutor", key)));
    }
}
