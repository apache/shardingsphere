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
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.engine.junit.parallel.annotaion.ParallelRuntimeStrategy;
import org.apache.shardingsphere.test.integration.engine.junit.parallel.impl.CaseParallelRunnerExecutor;
import org.apache.shardingsphere.test.integration.engine.junit.parallel.impl.ScenarioParallelRunnerExecutor;
import org.apache.shardingsphere.test.integration.engine.param.model.ParameterizedArray;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Parallel runner scheduler.
 */
public final class ParallelRunnerScheduler implements RunnerScheduler {
    
    private final Field parametersField;
    
    private final ConcurrentMap<RunnerExecutorKey, ParallelRunnerExecutor> runnerExecutors = new ConcurrentHashMap<>();
    
    private final ParallelRuntimeStrategy runtimeStrategy;
    
    private volatile Field runnerField;
    
    private final Lock lock = new ReentrantLock();
    
    public ParallelRunnerScheduler(final ParallelRuntimeStrategy runtimeStrategy) {
        this.runtimeStrategy = runtimeStrategy;
        parametersField = getParametersField();
    }
    
    @SneakyThrows(NoSuchFieldException.class)
    private Field getParametersField() {
        Field result = BlockJUnit4ClassRunnerWithParameters.class.getDeclaredField("parameters");
        result.setAccessible(true);
        return result;
    }
    
    @Override
    public void schedule(final Runnable childStatement) {
        Object[] parameters = getParameters(childStatement);
        ParameterizedArray parameterizedArray = (ParameterizedArray) parameters[0];
        getRunnerExecutor(new RunnerExecutorKey(parameterizedArray.getDatabaseType())).execute(parameterizedArray, childStatement);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Object[] getParameters(final Runnable childStatement) {
        if (null == runnerField) {
            runnerField = childStatement.getClass().getDeclaredField("val$each");
            runnerField.setAccessible(true);
        }
        return (Object[]) parametersField.get(runnerField.get(childStatement));
    }
    
    private ParallelRunnerExecutor getRunnerExecutor(final RunnerExecutorKey runnerExecutorKey) {
        ParallelRunnerExecutor runnerExecutor = this.runnerExecutors.get(runnerExecutorKey);
        if (null != runnerExecutor) {
            return runnerExecutor;
        }
        try {
            lock.lock();
            runnerExecutor = this.runnerExecutors.get(runnerExecutorKey);
            if (null != runnerExecutor) {
                return runnerExecutor;
            }
            runnerExecutor = getRunnerExecutor();
            this.runnerExecutors.put(runnerExecutorKey, runnerExecutor);
            return runnerExecutor;
        } finally {
            lock.unlock();
        }
    }
    
    private ParallelRunnerExecutor getRunnerExecutor() {
        switch (runtimeStrategy.value()) {
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
        if (null != runnerExecutors) {
            runnerExecutors.values().forEach(ParallelRunnerExecutor::finished);
        }
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
