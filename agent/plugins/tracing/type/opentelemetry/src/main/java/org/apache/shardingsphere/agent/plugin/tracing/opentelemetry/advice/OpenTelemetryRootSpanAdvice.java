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
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.TracingRootSpanAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.core.constant.AttributeConstants;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;

import java.lang.reflect.Method;

/**
 * OpenTelemetry root span advice.
 */
public final class OpenTelemetryRootSpanAdvice extends TracingRootSpanAdvice<Span> {
    
    @Override
    protected Span createRootSpan(final TargetAdviceObject target, final Method method, final Object[] args) {
        SpanBuilder spanBuilder = GlobalOpenTelemetry.getTracer(OpenTelemetryConstants.TRACER_NAME)
                .spanBuilder(OPERATION_NAME)
                .setAttribute(AttributeConstants.COMPONENT, AttributeConstants.COMPONENT_NAME)
                .setAttribute(AttributeConstants.SPAN_KIND, AttributeConstants.SPAN_KIND_CLIENT);
        return spanBuilder.startSpan();
    }
    
    @Override
    protected void finishRootSpan(final Span rootSpan, final TargetAdviceObject target) {
        rootSpan.setStatus(StatusCode.OK);
        rootSpan.end();
    }
    
    @Override
    protected void recordException(final Span rootSpan, final TargetAdviceObject target, final Throwable throwable) {
        rootSpan.setStatus(StatusCode.ERROR).recordException(throwable);
        rootSpan.end();
    }
}
