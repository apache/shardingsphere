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
 *
 */

package org.apache.shardingsphere.agent.metrics.bootstrap;

import java.lang.reflect.Method;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodInvocationResult;
import org.apache.shardingsphere.agent.core.plugin.advice.TargetObject;
import org.apache.shardingsphere.agent.metrics.api.reporter.MetricsReporter;

/**
 * Command executor task advice.
 */
public final class CommandExecutorTaskAdvice implements MethodAroundAdvice {
    
    private static final String METRICS_NAME = " proxy_execute_latency_millis";
    
    static {
        MetricsReporter.registerHistogram(METRICS_NAME, "the shardingsphere proxy executor latency millis");
    }
    
    @Override
    public void beforeMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        ElapsedTimeThreadLocal.INSTANCE.set(System.currentTimeMillis());
    }

    @Override
    public void afterMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        try {
            long elapsedTime = System.currentTimeMillis() - ElapsedTimeThreadLocal.INSTANCE.get();
            MetricsReporter.recordTime(METRICS_NAME, elapsedTime);
        } finally {
            ElapsedTimeThreadLocal.INSTANCE.remove();
        }
    }
}
