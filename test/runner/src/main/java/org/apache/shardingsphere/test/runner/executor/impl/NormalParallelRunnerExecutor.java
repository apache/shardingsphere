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

package org.apache.shardingsphere.test.runner.executor.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import org.apache.shardingsphere.test.runner.ParallelRunningStrategy.ParallelLevel;
import org.apache.shardingsphere.test.runner.executor.ParallelRunnerExecutor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Normal parallel runner executor.
 */
public class NormalParallelRunnerExecutor implements ParallelRunnerExecutor {
    
    private final Collection<Future<?>> futures = new LinkedList<>();
    
    @Getter
    private final Map<Object, ExecutorService> executorServices = new ConcurrentHashMap<>();
    
    private volatile ExecutorService defaultExecutorService;
    
    @Override
    public <T> void execute(final T key, final Runnable childStatement) {
        futures.add(getExecutorService(key).submit(childStatement));
    }
    
    @Override
    public void execute(final Runnable childStatement) {
        futures.add(getExecutorService().submit(childStatement));
    }
    
    protected <T> ExecutorService getExecutorService(final T key) {
        if (executorServices.containsKey(key)) {
            return executorServices.get(key);
        }
        String threadPoolNameFormat = String.join("-", "ShardingSphere-KeyedParallelTestThread", key.toString(), "%d");
        ExecutorService executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat(threadPoolNameFormat).build());
        if (null != executorServices.putIfAbsent(key, executorService)) {
            executorService.shutdownNow();
        }
        return executorServices.get(key);
    }
    
    private ExecutorService getExecutorService() {
        if (null == defaultExecutorService) {
            synchronized (NormalParallelRunnerExecutor.class) {
                if (null == defaultExecutorService) {
                    defaultExecutorService = Executors.newFixedThreadPool(
                            Runtime.getRuntime().availableProcessors(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-ParallelTestThread-%d").build());
                }
            }
        }
        return defaultExecutorService;
    }
    
    @Override
    public void finished() {
        futures.forEach(each -> {
            try {
                each.get();
            } catch (final InterruptedException | ExecutionException ignored) {
            }
        });
        executorServices.values().forEach(ExecutorService::shutdownNow);
        if (null != defaultExecutorService) {
            defaultExecutorService.shutdownNow();
        }
    }
    
    @Override
    public ParallelLevel getParallelLevel() {
        return ParallelLevel.NORMAL;
    }
}
