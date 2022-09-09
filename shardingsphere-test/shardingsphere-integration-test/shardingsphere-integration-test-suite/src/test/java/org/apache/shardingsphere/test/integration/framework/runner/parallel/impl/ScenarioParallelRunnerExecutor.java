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

package org.apache.shardingsphere.test.integration.framework.runner.parallel.impl;

import lombok.EqualsAndHashCode;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorServiceManager;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;
import org.apache.shardingsphere.test.runner.parallel.impl.DefaultParallelRunnerExecutor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Parallel runner executor with scenario.
 */
public final class ScenarioParallelRunnerExecutor extends DefaultParallelRunnerExecutor<ParameterizedArray> {
    
    private final Map<ScenarioKey, ExecutorServiceManager> executorServiceManagers = new ConcurrentHashMap<>();

    @Override
    public void execute(final ParameterizedArray parameterizedArray, final Runnable childStatement) {
        taskFeatures.add(getExecutorService(new ScenarioKey(parameterizedArray)).getExecutorService().submit(childStatement));
    }
    
    private ExecutorServiceManager getExecutorService(final ScenarioKey scenarioKey) {
        if (executorServiceManagers.containsKey(scenarioKey)) {
            return executorServiceManagers.get(scenarioKey);
        }
        String threadPoolNameFormat = String.join("-", "ScenarioExecutorPool", scenarioKey.toString(), "%d");
        ExecutorServiceManager newExecutorServiceManager = new ExecutorServiceManager(1, threadPoolNameFormat);
        if (null != executorServiceManagers.putIfAbsent(scenarioKey, newExecutorServiceManager)) {
            newExecutorServiceManager.close();
        }
        return executorServiceManagers.get(scenarioKey);
    }
    
    @Override
    public void finished() {
        taskFeatures.forEach(each -> {
            try {
                each.get();
            } catch (final InterruptedException | ExecutionException ignored) {
            }
        });
        executorServiceManagers.values().forEach(each -> each.getExecutorService().shutdownNow());
    }
    
    /**
     * Scenario key.
     */
    @EqualsAndHashCode
    private static final class ScenarioKey {
        
        private final String adapter;
        
        private final String scenario;
        
        private final String databaseTypeName;
        
        private ScenarioKey(final ParameterizedArray parameterizedArray) {
            adapter = parameterizedArray.getAdapter();
            scenario = parameterizedArray.getScenario();
            databaseTypeName = parameterizedArray.getDatabaseType().getType();
        }
        
        @Override
        public String toString() {
            return String.join("-", adapter, scenario, databaseTypeName);
        }
    }
}
