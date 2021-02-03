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

package org.apache.shardingsphere.test.integration.engine.junit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a strategy for scheduling when individual test methods
 * should be run (in serial or parallel).
 * <p>
 * WARNING: still experimental, may go away.
 * </p>
 */
@Slf4j
public class ITRunnerScheduler implements RunnerScheduler {
    
    private static final String DEFAULT_EXECUTOR_KEY = "default";
    
    private final Map<String, ExecutorService> executors = new HashMap<>();
    
    private final Map<String, AtomicInteger> executorCounter = new HashMap<>();
    
    public ITRunnerScheduler() {
        Collection<String> adapters = IntegrationTestEnvironment.getInstance().getAdapters();
        Collection<String> scenarios = IntegrationTestEnvironment.getInstance().getScenarios();
        Set<DatabaseType> databaseTypes = IntegrationTestEnvironment.getInstance().getDatabaseEnvironments().keySet();
        
        executors.put(DEFAULT_EXECUTOR_KEY, getExecutorService(DEFAULT_EXECUTOR_KEY));
        executorCounter.put(DEFAULT_EXECUTOR_KEY, new AtomicInteger(0));
        for (String each : adapters) {
            for (String scenario : scenarios) {
                for (DatabaseType databaseType : databaseTypes) {
                    String executorServiceKey = getExecutorServiceKey(each, scenario, databaseType.getName());
                    executors.put(executorServiceKey, getExecutorService(executorServiceKey));
                    executorCounter.put(executorServiceKey, new AtomicInteger(0));
                }
            }
        }
    }
    
    @SneakyThrows
    @Override
    public void schedule(final Runnable childStatement) {
        // TODO Gets the parameters of the Runnable closure
        Field field = childStatement.getClass().getDeclaredField("val$each");
        field.setAccessible(true);
        ITBlockJUnit4ClassRunnerWithParameters runnerWithParameters = (ITBlockJUnit4ClassRunnerWithParameters) field.get(childStatement);
        String executorServiceKey = getExecutorServiceKey(runnerWithParameters.getParameters(), runnerWithParameters.getTestClass());
        executorCounter.get(executorServiceKey).incrementAndGet();
        executors.get(executorServiceKey).submit(childStatement);
    }
    
    private String getExecutorServiceKey(final String adapter, final String scenario, final String databaseTypeName) {
        return String.join("_", adapter, scenario, databaseTypeName);
    }
    
    private String getExecutorServiceKey(final Object[] parameters, final TestClass testClass) {
        Class<?> realTestClass = testClass.getJavaClass();
        if (realTestClass == null) {
            return DEFAULT_EXECUTOR_KEY;
        }
        int parametersLength = parameters.length;
        if (parametersLength == 7) {
            return String.join("_", String.valueOf(parameters[2]), String.valueOf(parameters[3]), String.valueOf(parameters[4]));
        } else if (parametersLength == 5) {
            return String.join("_", String.valueOf(parameters[1]), String.valueOf(parameters[2]), String.valueOf(parameters[3]));
        }
        return DEFAULT_EXECUTOR_KEY;
    }
    
    @Override
    public void finished() {
        for (Map.Entry<String, AtomicInteger> each : executorCounter.entrySet()) {
            log.info(String.join(" -> ", each.getKey(), String.valueOf(each.getValue().get())));
        }
        executors.values().forEach(executorService -> {
            try {
                executorService.shutdown();
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        });
    }
    
    private ExecutorService getExecutorService(final String executorServiceKey) {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadFactoryBuilder().setNameFormat("ITRunnerScheduler-" + executorServiceKey + "-pool-%d").build());
    }
}
