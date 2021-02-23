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

/**
 * Parallel runner scheduler.
 */
@RequiredArgsConstructor
public final class ParallelRunnerScheduler implements RunnerScheduler {
    
    private final ParallelLevel parallelLevel;
    
    private final ConcurrentMap<DatabaseType, ParallelRunnerExecutor> runnerExecutors = new ConcurrentHashMap<>();
    
    @Override
    public void schedule(final Runnable childStatement) {
        ParameterizedArray parameterizedArray = new RunnerParameters(childStatement).getParameterizedArray();
        getRunnerExecutor(parameterizedArray.getDatabaseType()).execute(parameterizedArray, childStatement);
    }
    
    private ParallelRunnerExecutor getRunnerExecutor(final DatabaseType databaseType) {
        if (runnerExecutors.containsKey(databaseType)) {
            return runnerExecutors.get(databaseType);
        }
        runnerExecutors.putIfAbsent(databaseType, getRunnerExecutor());
        return runnerExecutors.get(databaseType);
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
}
