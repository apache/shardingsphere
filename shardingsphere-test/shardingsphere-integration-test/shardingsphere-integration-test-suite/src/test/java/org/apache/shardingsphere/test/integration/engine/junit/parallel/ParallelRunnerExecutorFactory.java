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

package org.apache.shardingsphere.test.integration.engine.junit.parallel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.engine.junit.parallel.annotaion.ParallelLevel;
import org.apache.shardingsphere.test.integration.engine.junit.parallel.impl.CaseParallelRunnerExecutor;
import org.apache.shardingsphere.test.integration.engine.junit.parallel.impl.ScenarioParallelRunnerExecutor;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Parallel runner executor factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParallelRunnerExecutorFactory {
    
    private static final ConcurrentMap<DatabaseType, ParallelRunnerExecutor> EXECUTORS = new ConcurrentHashMap<>();
    
    /**
     * Get parallel runner executor.
     * 
     * @param databaseType database type
     * @param parallelLevel parallel level
     * @return parallel runner executor
     */
    public static ParallelRunnerExecutor getExecutor(final DatabaseType databaseType, final ParallelLevel parallelLevel) {
        if (EXECUTORS.containsKey(databaseType)) {
            return EXECUTORS.get(databaseType);
        }
        EXECUTORS.putIfAbsent(databaseType, newInstance(parallelLevel));
        return EXECUTORS.get(databaseType);
    }
    
    private static ParallelRunnerExecutor newInstance(final ParallelLevel parallelLevel) {
        switch (parallelLevel) {
            case CASE:
                return new CaseParallelRunnerExecutor();
            case SCENARIO:
                return new ScenarioParallelRunnerExecutor();
            default:
                throw new UnsupportedOperationException("Unsupported runtime strategy.");
        }
    }
    
    /**
     * Get all executors.
     * 
     * @return all executors
     */
    public static Collection<ParallelRunnerExecutor> getAllExecutors() {
        return EXECUTORS.values();
    }
}
