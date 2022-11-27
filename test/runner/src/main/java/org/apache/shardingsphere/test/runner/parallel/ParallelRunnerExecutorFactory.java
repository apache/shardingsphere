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
import java.util.Collection;

/**
 * Parallel Runner Executor factory.
 * @param <T> key type which bind to executor
 */
public interface ParallelRunnerExecutorFactory<T> {
    
    /**
     * Get executor factory by key and parallel level.
     *
     * @param key key bind to the factory
     * @param parallelLevel parallel level
     * @return executor by key and parallel level
     */
    ParallelRunnerExecutor getExecutor(T key, ParallelLevel parallelLevel);
    
    /**
     * Get factory by parallel level.
     *
     * @param parallelLevel parallel level
     * @return executor by parallel level
     */
    ParallelRunnerExecutor getExecutor(ParallelLevel parallelLevel);
    
    /**
     * Get all executors.
     *
     * @return all executors
     */
    Collection<ParallelRunnerExecutor> getAllExecutors();
}
