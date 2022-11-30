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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parallel runner executors.
 */
public final class ParallelRunnerExecutors {
    
    private final Map<String, ParallelRunnerExecutor> executors = new ConcurrentHashMap<>();
    
    /**
     * Get executor.
     *
     * @param key key bind to the executor
     * @return got executor
     */
    public ParallelRunnerExecutor getExecutor(final String key) {
        if (executors.containsKey(key)) {
            return executors.get(key);
        }
        ParallelRunnerExecutor newExecutor = new ParallelRunnerExecutor();
        if (null != executors.putIfAbsent(key, newExecutor)) {
            newExecutor.finished();
        }
        return executors.get(key);
    }
    
    /**
     * Finish all executors.
     */
    public void finishAll() {
        executors.values().forEach(ParallelRunnerExecutor::finished);
    }
}
