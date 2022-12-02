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

package org.apache.shardingsphere.test.runner.scheduler;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.runner.ParallelRunningStrategy.ParallelLevel;
import org.apache.shardingsphere.test.runner.executor.ParallelRunnerExecutors;
import org.apache.shardingsphere.test.runner.key.TestKeyProvider;
import org.apache.shardingsphere.test.runner.key.TestKeyProviderFactory;
import org.apache.shardingsphere.test.runner.param.ParameterizedArray;
import org.apache.shardingsphere.test.runner.param.RunnerParameters;
import org.junit.runners.model.RunnerScheduler;

/**
 * Parallel runner scheduler.
 */
@RequiredArgsConstructor
public final class ParallelRunnerScheduler implements RunnerScheduler {
    
    private final ParallelLevel parallelLevel;
    
    private final ParallelRunnerExecutors executors;
    
    @Override
    public void schedule(final Runnable childStatement) {
        ParameterizedArray parameterizedArray = new RunnerParameters(childStatement).getParameterizedArray();
        TestKeyProvider provider = TestKeyProviderFactory.newInstance(parallelLevel);
        executors.getExecutor(parameterizedArray.getDatabaseType()).execute(provider.getKey(parameterizedArray), childStatement);
    }
    
    @Override
    public void finished() {
        executors.finishAll();
    }
}
