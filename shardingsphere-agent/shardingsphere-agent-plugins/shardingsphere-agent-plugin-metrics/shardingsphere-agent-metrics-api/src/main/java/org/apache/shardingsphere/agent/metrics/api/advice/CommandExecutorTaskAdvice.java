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

package org.apache.shardingsphere.agent.metrics.api.advice;

import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.metrics.api.MetricsPool;
import org.apache.shardingsphere.agent.metrics.api.MetricsWrapper;
import org.apache.shardingsphere.agent.metrics.api.constant.MetricIds;
import org.apache.shardingsphere.agent.metrics.api.threadlocal.ElapsedTimeThreadLocal;

import java.lang.reflect.Method;

/**
 * Command executor task advice.
 */
public final class CommandExecutorTaskAdvice implements InstanceMethodAroundAdvice {
    
    public static final String COMMAND_EXECUTOR_RUN = "run";
    
    public static final String COMMAND_EXECUTOR_EXCEPTION = "processException";
    
    static {
        MetricsPool.create(MetricIds.PROXY_EXECUTE_ERROR);
        MetricsPool.create(MetricIds.PROXY_EXECUTE_LATENCY_MILLIS);
    }
    
    @Override
    public void beforeMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        if (COMMAND_EXECUTOR_RUN.equals(method.getName())) {
            ElapsedTimeThreadLocal.INSTANCE.set(System.currentTimeMillis());
        }
    }
    
    @Override
    public void afterMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        if (COMMAND_EXECUTOR_RUN.equals(method.getName())) {
            try {
                long elapsedTime = System.currentTimeMillis() - ElapsedTimeThreadLocal.INSTANCE.get();
                MetricsPool.get(MetricIds.PROXY_EXECUTE_LATENCY_MILLIS).ifPresent(optional -> optional.observe(elapsedTime));
            } finally {
                ElapsedTimeThreadLocal.INSTANCE.remove();
            }
        } else if (COMMAND_EXECUTOR_EXCEPTION.equals(method.getName())) {
            MetricsPool.get(MetricIds.PROXY_EXECUTE_ERROR).ifPresent(MetricsWrapper::inc);
        }
    }
}
