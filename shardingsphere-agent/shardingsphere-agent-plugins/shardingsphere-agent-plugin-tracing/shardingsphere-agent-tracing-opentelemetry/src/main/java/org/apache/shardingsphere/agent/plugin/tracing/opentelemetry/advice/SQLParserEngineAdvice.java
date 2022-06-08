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
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;

import java.lang.reflect.Method;

/**
 * SQL parser engine advice.
 */
public class SQLParserEngineAdvice implements InstanceMethodAroundAdvice {
    
    private static final String OPERATION_NAME = "/ShardingSphere/parseSQL/";
    
    @Override
    public void beforeMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        Span root = (Span) ExecutorDataMap.getValue().get(OpenTelemetryConstants.ROOT_SPAN);
        Tracer tracer = GlobalOpenTelemetry.getTracer("shardingsphere-agent");
        SpanBuilder spanBuilder = tracer.spanBuilder(OPERATION_NAME)
                .setAttribute(OpenTelemetryConstants.COMPONENT, OpenTelemetryConstants.COMPONENT_NAME)
                .setAttribute(OpenTelemetryConstants.DB_TYPE, OpenTelemetryConstants.DB_TYPE_VALUE)
                .setAttribute(OpenTelemetryConstants.DB_STATEMENT, String.valueOf(args[0]));
        if (root != null) {
            spanBuilder.setParent(Context.current().with(root));
        }
        target.setAttachment(spanBuilder.startSpan());
    }
    
    @Override
    public void afterMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        ((Span) target.getAttachment()).end();
    }
    
    @Override
    public void onThrowing(final AdviceTargetObject target, final Method method, final Object[] args, final Throwable throwable) {
        ((Span) target.getAttachment()).setStatus(StatusCode.ERROR).recordException(throwable);
    }
}
