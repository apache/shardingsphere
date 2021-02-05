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

import lombok.SneakyThrows;
import org.apache.commons.lang.ClassUtils;
import org.apache.shardingsphere.test.integration.engine.it.ParallelIT;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;

import java.lang.reflect.Field;

/**
 * Represents a strategy for scheduling when individual test methods should be run (in serial or parallel).
 * 
 * <p>
 * WARNING: still experimental, may go away.
 * </p>
 */
public final class ITRunnerScheduler implements RunnerScheduler {
    
    private final Field parametersField;
    
    private volatile Field runnerField;
    
    private final ITRunnerExecutor runnerExecutor;
    
    @SneakyThrows
    public ITRunnerScheduler(final Class<?> testClass) {
        parametersField = BlockJUnit4ClassRunnerWithParameters.class.getDeclaredField("parameters");
        parametersField.setAccessible(true);
        if (ClassUtils.isAssignable(testClass, ParallelIT.class)) {
            runnerExecutor = new ITRunnerNonScenariosExecutor();
        } else {
            runnerExecutor = new ITRunnerScenariosExecutor();
        }
    }
    
    @SneakyThrows
    @Override
    public void schedule(final Runnable childStatement) {
        // TODO Gets the parameters of the Runnable closure
        if (null == runnerField) {
            runnerField = childStatement.getClass().getDeclaredField("val$each");
            runnerField.setAccessible(true);
        }
        BlockJUnit4ClassRunnerWithParameters runner = (BlockJUnit4ClassRunnerWithParameters) runnerField.get(childStatement);
        runnerExecutor.execute((Object[]) parametersField.get(runner), childStatement);
    }
    
    @Override
    public void finished() {
        runnerExecutor.finished();
    }
}
