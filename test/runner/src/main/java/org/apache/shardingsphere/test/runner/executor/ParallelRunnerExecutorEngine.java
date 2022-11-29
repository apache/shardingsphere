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

package org.apache.shardingsphere.test.runner.executor;

import org.apache.shardingsphere.test.runner.ParallelRunningStrategy.ParallelLevel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parallel runner executor engine.
 * 
 * @param <T> key type which bind to executor
 */
public final class ParallelRunnerExecutorEngine<T> {
    
    private final Map<T, ParallelRunnerExecutor> executors = new ConcurrentHashMap<>();
    
    /**
     * Get executor.
     *
     * @param key key bind to the executor
     * @param parallelLevel parallel level
     * @return got executor
     */
    public ParallelRunnerExecutor getExecutor(final T key, final ParallelLevel parallelLevel) {
        if (executors.containsKey(key)) {
            return executors.get(key);
        }
        ParallelRunnerExecutor newExecutor = ParallelRunnerExecutorFactory.newInstance(parallelLevel);
        if (null != executors.putIfAbsent(key, newExecutor)) {
            newExecutor.finished();
        }
        return executors.get(key);
    }
    
    /**
     * Finish all executors.
     */
    public void finishAllExecutors() {
        executors.values().forEach(ParallelRunnerExecutor::finished);
    }
}
