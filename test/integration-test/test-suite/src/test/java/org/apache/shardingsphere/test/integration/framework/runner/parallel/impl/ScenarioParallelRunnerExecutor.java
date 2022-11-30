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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.EqualsAndHashCode;
import org.apache.shardingsphere.test.integration.framework.param.model.ITParameterizedArray;
import org.apache.shardingsphere.test.runner.ParallelRunningStrategy.ParallelLevel;
import org.apache.shardingsphere.test.runner.executor.impl.NormalParallelRunnerExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Parallel runner executor with scenario.
 */
public final class ScenarioParallelRunnerExecutor extends NormalParallelRunnerExecutor {
    
    @Override
    protected ExecutorService getExecutorService(final String key) {
        if (getExecutorServices().containsKey(key)) {
            return getExecutorServices().get(key);
        }
        String threadPoolNameFormat = String.join("-", "ScenarioExecutorPool", key, "%d");
        ExecutorService executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat(threadPoolNameFormat).build());
        if (null != getExecutorServices().putIfAbsent(key, executorService)) {
            executorService.shutdownNow();
        }
        return getExecutorServices().get(key);
    }
    
    @Override
    public ParallelLevel getParallelLevel() {
        return ParallelLevel.SCENARIO;
    }
    
    /**
     * Scenario key.
     */
    @EqualsAndHashCode
    public static final class ScenarioKey {
        
        private final String adapter;
        
        private final String scenario;
        
        private final String databaseType;
        
        public ScenarioKey(final ITParameterizedArray parameterizedArray) {
            adapter = parameterizedArray.getAdapter();
            scenario = parameterizedArray.getScenario();
            databaseType = parameterizedArray.getDatabaseType().getType();
        }
        
        @Override
        public String toString() {
            return String.join("-", adapter, scenario, databaseType);
        }
    }
}
