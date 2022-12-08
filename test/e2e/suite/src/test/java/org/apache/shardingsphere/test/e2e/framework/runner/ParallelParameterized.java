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

package org.apache.shardingsphere.test.e2e.framework.runner;

import org.apache.shardingsphere.test.e2e.env.runtime.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.cluster.ClusterEnvironment;
import org.apache.shardingsphere.test.e2e.framework.runner.executor.ParallelRunnerExecutor;
import org.apache.shardingsphere.test.e2e.framework.runner.scheduler.ParallelRunnerScheduler;
import org.junit.runners.Parameterized;

/**
 * Parallel parameterized.
 */
public final class ParallelParameterized extends Parameterized {
    
    // CHECKSTYLE:OFF
    public ParallelParameterized(final Class<?> clazz) throws Throwable {
        // CHECKSTYLE:ON
        super(clazz);
        if (ClusterEnvironment.Type.DOCKER != IntegrationTestEnvironment.getInstance().getClusterEnvironment().getType()) {
            ParallelRunningStrategy runningStrategy = clazz.getAnnotation(ParallelRunningStrategy.class);
            if (null != runningStrategy) {
                setScheduler(new ParallelRunnerScheduler(runningStrategy.value(), new ParallelRunnerExecutor()));
            }
        }
    }
}
