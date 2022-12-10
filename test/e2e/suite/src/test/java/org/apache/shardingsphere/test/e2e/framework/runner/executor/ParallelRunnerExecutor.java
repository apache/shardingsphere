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

package org.apache.shardingsphere.test.e2e.framework.runner.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Parallel runner executor.
 */
public final class ParallelRunnerExecutor {
    
    private final Collection<Future<?>> futures = new CopyOnWriteArrayList<>();
    
    private final Map<Object, ExecutorService> executorServices = new ConcurrentHashMap<>();
    
    /**
     * Execute child statement.
     *
     * @param key executor key
     * @param childStatement child statement
     */
    public void execute(final String key, final Runnable childStatement) {
        futures.add(getExecutorService(key).submit(childStatement));
    }
    
    private ExecutorService getExecutorService(final String key) {
        if (executorServices.containsKey(key)) {
            return executorServices.get(key);
        }
        String threadPoolNameFormat = String.join("-", "TestThread", key, "%d");
        ExecutorService executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat(threadPoolNameFormat).build());
        if (null != executorServices.putIfAbsent(key, executorService)) {
            executorService.shutdownNow();
        }
        return executorServices.get(key);
    }
    
    /**
     * Finish tasks.
     */
    public void finished() {
        futures.forEach(each -> {
            try {
                each.get();
            } catch (final InterruptedException | ExecutionException ignored) {
            }
        });
        executorServices.values().forEach(ExecutorService::shutdownNow);
    }
}
