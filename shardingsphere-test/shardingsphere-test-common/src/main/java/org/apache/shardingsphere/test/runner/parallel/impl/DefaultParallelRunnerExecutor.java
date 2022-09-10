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

package org.apache.shardingsphere.test.runner.parallel.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import org.apache.shardingsphere.test.runner.parallel.ParallelRunnerExecutor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * default parallel executor.
 * @param <T> key type bind to parallel executor
 */
public class DefaultParallelRunnerExecutor<T> implements ParallelRunnerExecutor<T> {
    
    @Getter
    private final Collection<Future<?>> taskFeatures = new LinkedList<>();
    
    private final ExecutorService executorService;
    
    public DefaultParallelRunnerExecutor() {
        executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-ParallelTestThread-%d").build());
    }
    
    @Override
    public void execute(final T key, final Runnable childStatement) {
        
    }
    
    @Override
    public void execute(final Runnable childStatement) {
        taskFeatures.add(executorService.submit(childStatement));
    }
    
    @Override
    public void finished() {
        taskFeatures.forEach(each -> {
            try {
                each.get();
            } catch (final InterruptedException | ExecutionException ignored) {
            }
        });
        executorService.shutdownNow();
    }
}
