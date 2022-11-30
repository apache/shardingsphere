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

package org.apache.shardingsphere.test.integration.framework.runner.parallel;

import org.apache.shardingsphere.test.integration.framework.param.model.ITParameterizedArray;
import org.apache.shardingsphere.test.runner.ParallelRunningStrategy.ParallelLevel;
import org.apache.shardingsphere.test.runner.executor.ParallelRunnerExecutors;
import org.apache.shardingsphere.test.runner.param.RunnerParameters;
import org.apache.shardingsphere.test.runner.scheduler.ParallelRunnerScheduler;

/**
 * Parameterized parallel runner scheduler.
 */
public final class ParameterizedParallelRunnerScheduler extends ParallelRunnerScheduler {
    
    public ParameterizedParallelRunnerScheduler(final ParallelLevel parallelLevel, final ParallelRunnerExecutors executorEngine) {
        super(parallelLevel, executorEngine);
    }
    
    @Override
    public void schedule(final Runnable childStatement) {
        ITParameterizedArray parameterizedArray = (ITParameterizedArray) new RunnerParameters(childStatement).getParameterizedArray();
        String key = String.join("-", parameterizedArray.getAdapter(), parameterizedArray.getScenario(), parameterizedArray.getDatabaseType().getType());
        getExecutorEngine().getExecutor(parameterizedArray.getDatabaseType().getType()).execute(ParallelLevel.SCENARIO == getParallelLevel() ? key : "", childStatement);
    }
}
