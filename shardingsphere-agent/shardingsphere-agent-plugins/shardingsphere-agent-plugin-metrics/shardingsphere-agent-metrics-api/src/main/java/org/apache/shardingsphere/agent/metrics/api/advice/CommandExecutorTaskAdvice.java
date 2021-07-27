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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.metrics.api.constant.MethodNameConstant;
import org.apache.shardingsphere.agent.metrics.api.reporter.MetricsReporter;
import org.apache.shardingsphere.agent.metrics.api.threadlocal.ElapsedTimeThreadLocal;

import java.lang.reflect.Method;

/**
 * Command executor task advice.
 */
@Slf4j
public final class CommandExecutorTaskAdvice implements InstanceMethodAroundAdvice {
    
    private static final String PROXY_EXECUTE_LATENCY_MILLIS = "proxy_execute_latency_millis";
    
    private static final String PROXY_EXECUTE_ERROR_TOTAL = "proxy_execute_error_total";
    
    static {
        MetricsReporter.registerHistogram(PROXY_EXECUTE_LATENCY_MILLIS, "the shardingsphere proxy executor latency millis");
        MetricsReporter.registerCounter(PROXY_EXECUTE_ERROR_TOTAL, "the shardingsphere proxy executor error");
    }
    
    @Override
    public void beforeMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        if (MethodNameConstant.COMMAND_EXECUTOR_RUN.equals(method.getName())) {
            ElapsedTimeThreadLocal.INSTANCE.set(System.currentTimeMillis());
            log.info("run....1");
        }
    }

    @Override
    public void afterMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        if (MethodNameConstant.COMMAND_EXECUTOR_RUN.equals(method.getName())) {
            try {
                long elapsedTime = System.currentTimeMillis() - ElapsedTimeThreadLocal.INSTANCE.get();
                MetricsReporter.recordTime(PROXY_EXECUTE_LATENCY_MILLIS, elapsedTime);
            } finally {
                ElapsedTimeThreadLocal.INSTANCE.remove();
                log.info("run....2");
            }
        } else if (MethodNameConstant.COMMAND_EXECUTOR_EXCEPTION.equals(method.getName())) {
            MetricsReporter.counterIncrement(PROXY_EXECUTE_ERROR_TOTAL);
            log.info("exception....2");
        }
    }
}
