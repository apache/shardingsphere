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

package org.apache.shardingsphere.test.integration.engine.junit.parallel.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.engine.junit.parallel.ParallelRunnerExecutor;
import org.apache.shardingsphere.test.integration.engine.param.model.ParameterizedArray;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Parallel runner executor with scenario.
 */
@Slf4j
public final class ScenarioParallelRunnerExecutor implements ParallelRunnerExecutor {
    
    private final ConcurrentMap<ScenarioKey, ScenarioExecutorQueue> scenarioExecutorQueues = new ConcurrentHashMap<>();
    
    private final Lock lock = new ReentrantLock();
    
    private final List<Future<?>> scenarioTaskResults = new LinkedList<>();
    
    @Override
    public void execute(final ParameterizedArray parameterizedArray, final Runnable childStatement) {
        scenarioTaskResults.add(getScenarioExecutorQueue(new ScenarioKey(parameterizedArray.getAdapter(), parameterizedArray.getScenario(), parameterizedArray.getDatabaseType().getName()))
                .submit(childStatement));
    }
    
    private ScenarioExecutorQueue getScenarioExecutorQueue(final ScenarioKey scenarioKey) {
        ScenarioExecutorQueue scenarioExecutorQueue = this.scenarioExecutorQueues.get(scenarioKey);
        if (null != scenarioExecutorQueue) {
            return scenarioExecutorQueue;
        }
        try {
            lock.lock();
            scenarioExecutorQueue = this.scenarioExecutorQueues.get(scenarioKey);
            if (null != scenarioExecutorQueue) {
                return scenarioExecutorQueue;
            }
            scenarioExecutorQueue = new ScenarioExecutorQueue(scenarioKey);
            this.scenarioExecutorQueues.put(scenarioKey, scenarioExecutorQueue);
            return scenarioExecutorQueue;
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void finished() {
        scenarioTaskResults.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException ignore) {
            } catch (ExecutionException ignore) {
            }
        });
        scenarioExecutorQueues.values().forEach(scenarioExecutorQueue -> {
            try {
                scenarioExecutorQueue.close();
            } catch (IOException ignore) {
            }
        });
    }
    
    /**
     * Scenario key.
     */
    @RequiredArgsConstructor
    private static final class ScenarioKey {
        
        private final String adapter;
        
        private final String scenario;
        
        private final String databaseTypeName;
    
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ScenarioKey that = (ScenarioKey) o;
            if (!adapter.equals(that.adapter)) {
                return false;
            }
            if (!scenario.equals(that.scenario)) {
                return false;
            }
            return databaseTypeName.equals(that.databaseTypeName);
        }
    
        @Override
        public int hashCode() {
            int result = adapter.hashCode();
            result = 31 * result + scenario.hashCode();
            result = 31 * result + databaseTypeName.hashCode();
            return result;
        }
    
        @Override
        public String toString() {
            return String.join("-", adapter, scenario, databaseTypeName);
        }
    }
    
    /**
     * Scenario executor queue.
     */
    @Setter
    private static final class ScenarioExecutorQueue implements Closeable {
        
        private final BlockingQueue<Runnable> executorQueue;
    
        private final ExecutorService executorService;
    
        ScenarioExecutorQueue(final ScenarioKey scenarioKey) {
            this.executorQueue = new LinkedBlockingQueue<>();
            this.executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                    this.executorQueue, new ThreadFactoryBuilder().setNameFormat("ScenarioExecutor-" + scenarioKey + "-pool-%d").build());
        }
    
        public Future<?> submit(final Runnable childStatement) {
            return this.executorService.submit(childStatement);
        }
    
        @Override
        public void close() throws IOException {
            this.executorService.shutdownNow();
        }
    }
}
