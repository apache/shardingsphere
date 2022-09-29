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

package org.apache.shardingsphere.test.runner.parallel;

import org.apache.shardingsphere.test.runner.parallel.annotaion.ParallelLevel;
import org.apache.shardingsphere.test.runner.parallel.impl.DefaultParallelRunnerExecutor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default parallel runner executor factory.
 */
public class DefaultParallelRunnerExecutorFactory<T> implements ParallelRunnerExecutorFactory<T> {
    
    private final Map<T, ParallelRunnerExecutor> executors = new ConcurrentHashMap<>();
    
    private volatile ParallelRunnerExecutor defaultExecutor;
    
    @Override
    public ParallelRunnerExecutor getExecutor(final T key, final ParallelLevel parallelLevel) {
        if (executors.containsKey(key)) {
            return executors.get(key);
        }
        ParallelRunnerExecutor newExecutor = newInstance(parallelLevel);
        if (null != executors.putIfAbsent(key, newExecutor)) {
            newExecutor.finished();
        }
        return executors.get(key);
    }
    
    /**
     * Get parallel runner executor.
     *
     * @param parallelLevel parallel level
     * @return parallel runner executor
     */
    public ParallelRunnerExecutor getExecutor(final ParallelLevel parallelLevel) {
        if (null == defaultExecutor) {
            synchronized (DefaultParallelRunnerExecutorFactory.class) {
                if (null == defaultExecutor) {
                    defaultExecutor = new DefaultParallelRunnerExecutor();
                }
            }
        }
        return defaultExecutor;
    }
    
    /**
     * Create executor instance by parallel level.
     *
     * @param parallelLevel parallel level
     * @return executor by parallel level
     */
    public ParallelRunnerExecutor newInstance(final ParallelLevel parallelLevel) {
        return new DefaultParallelRunnerExecutor();
    }
    
    /**
     * Get all executors.
     *
     * @return all executors
     */
    public Collection<ParallelRunnerExecutor> getAllExecutors() {
        Collection<ParallelRunnerExecutor> result = new LinkedList<>(executors.values());
        if (null != defaultExecutor) {
            result.add(defaultExecutor);
        }
        return result;
    }
}
