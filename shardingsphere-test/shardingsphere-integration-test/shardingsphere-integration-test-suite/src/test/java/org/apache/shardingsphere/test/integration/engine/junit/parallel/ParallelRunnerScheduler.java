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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.engine.junit.parallel.annotaion.ParallelLevel;
import org.apache.shardingsphere.test.integration.engine.junit.parallel.impl.CaseParallelRunnerExecutor;
import org.apache.shardingsphere.test.integration.engine.junit.parallel.impl.ScenarioParallelRunnerExecutor;
import org.apache.shardingsphere.test.integration.engine.param.RunnerParameters;
import org.apache.shardingsphere.test.integration.engine.param.model.ParameterizedArray;
import org.junit.runners.model.RunnerScheduler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Parallel runner scheduler.
 */
@RequiredArgsConstructor
public final class ParallelRunnerScheduler implements RunnerScheduler {
    
    private final ParallelLevel parallelLevel;
    
    private final ConcurrentMap<RunnerExecutorKey, ParallelRunnerExecutor> runnerExecutors = new ConcurrentHashMap<>();
    
    private final Lock lock = new ReentrantLock();
    
    @Override
    public void schedule(final Runnable childStatement) {
        Object[] parameters = new RunnerParameters(childStatement).getRunnerParameters();
        ParameterizedArray parameterizedArray = (ParameterizedArray) parameters[0];
        getRunnerExecutor(new RunnerExecutorKey(parameterizedArray.getDatabaseType())).execute(parameterizedArray, childStatement);
    }
    
    private ParallelRunnerExecutor getRunnerExecutor(final RunnerExecutorKey runnerExecutorKey) {
        ParallelRunnerExecutor runnerExecutor = runnerExecutors.get(runnerExecutorKey);
        if (null != runnerExecutor) {
            return runnerExecutor;
        }
        try {
            lock.lock();
            runnerExecutor = runnerExecutors.get(runnerExecutorKey);
            if (null != runnerExecutor) {
                return runnerExecutor;
            }
            runnerExecutor = getRunnerExecutor();
            runnerExecutors.put(runnerExecutorKey, runnerExecutor);
            return runnerExecutor;
        } finally {
            lock.unlock();
        }
    }
    
    private ParallelRunnerExecutor getRunnerExecutor() {
        switch (parallelLevel) {
            case CASE:
                return new CaseParallelRunnerExecutor();
            case SCENARIO:
                return new ScenarioParallelRunnerExecutor();
            default:
                throw new UnsupportedOperationException("Unsupported runtime strategy.");
        }
    }
    
    @Override
    public void finished() {
        runnerExecutors.values().forEach(ParallelRunnerExecutor::finished);
    }
    
    /**
     * Runner executor key.
     */
    @RequiredArgsConstructor
    private static final class RunnerExecutorKey {
        
        private final DatabaseType databaseType;
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RunnerExecutorKey that = (RunnerExecutorKey) o;
            return databaseType.getName().equals(that.databaseType.getName());
        }
        
        @Override
        public int hashCode() {
            return databaseType.hashCode();
        }
    }
}
