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

package org.apache.shardingsphere.test.runner;

import org.apache.shardingsphere.test.runner.ParallelRunningStrategy.ParallelLevel;
import org.apache.shardingsphere.test.runner.executor.ParallelRunnerExecutors;
import org.apache.shardingsphere.test.runner.scheduler.ParallelRunnerScheduler;
import org.junit.runners.Parameterized;

/**
 * Parallel parameterized.
 */
public final class ParallelParameterized extends Parameterized {
    
    // CHECKSTYLE:OFF
    public ParallelParameterized(final Class<?> clazz) throws Throwable {
        // CHECKSTYLE:ON
        super(clazz);
        setScheduler(new ParallelRunnerScheduler(getParallelLevel(clazz), new ParallelRunnerExecutors()));
    }
    
    private ParallelLevel getParallelLevel(final Class<?> clazz) {
        ParallelRunningStrategy strategy = clazz.getAnnotation(ParallelRunningStrategy.class);
        return null == strategy ? ParallelLevel.NORMAL : strategy.value();
    }
}
