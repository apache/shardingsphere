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

package org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.advice;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;

import java.lang.reflect.Method;

/**
 * Command executor task advice.
 */
public class CommandExecutorTaskAdvice implements InstanceMethodAroundAdvice {
    
    private static final String OPERATION_NAME = "/ShardingSphere/rootInvoke/";
    
    @Override
    public void beforeMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        SpanBuilder spanBuilder = GlobalOpenTelemetry.getTracer("shardingsphere-agent")
                .spanBuilder(OPERATION_NAME)
                .setAttribute(OpenTelemetryConstants.COMPONENT, OpenTelemetryConstants.COMPONENT_NAME)
                .setSpanKind(SpanKind.CLIENT);
        Span span = spanBuilder.startSpan();
        target.setAttachment(span);
        ExecutorDataMap.getValue().put(OpenTelemetryConstants.ROOT_SPAN, span);
    }
    
    @Override
    public void afterMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        ((Span) target.getAttachment()).end();
        ExecutorDataMap.getValue().remove(OpenTelemetryConstants.ROOT_SPAN);
    }
    
    @Override
    public void onThrowing(final AdviceTargetObject target, final Method method, final Object[] args, final Throwable throwable) {
        ((Span) target.getAttachment()).setStatus(StatusCode.ERROR).recordException(throwable);
    }
}
