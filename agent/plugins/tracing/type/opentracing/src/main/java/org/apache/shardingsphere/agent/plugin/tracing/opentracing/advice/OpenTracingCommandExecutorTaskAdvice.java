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

package org.apache.shardingsphere.agent.plugin.tracing.opentracing.advice;

import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.TracingCommandExecutorTaskAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.core.constant.AttributeConstants;
import org.apache.shardingsphere.agent.plugin.tracing.opentracing.span.OpenTracingErrorSpan;

import java.lang.reflect.Method;

/**
 * OpenTracing command executor task advice executor.
 */
public final class OpenTracingCommandExecutorTaskAdvice extends TracingCommandExecutorTaskAdvice<Span> {
    
    @Override
    protected Span createRootSpan(final TargetAdviceObject target, final Method method, final Object[] args) {
        return GlobalTracer.get().buildSpan(OPERATION_NAME)
                .withTag(AttributeConstants.COMPONENT, AttributeConstants.COMPONENT_NAME)
                .withTag(AttributeConstants.SPAN_KIND, AttributeConstants.SPAN_KIND_CLIENT)
                .startActive(true).span();
    }
    
    @Override
    protected void finishRootSpan(final Span rootSpan, final TargetAdviceObject target) {
        rootSpan.finish();
    }
    
    @Override
    protected void recordException(final Span rootSpan, final TargetAdviceObject target, final Throwable throwable) {
        OpenTracingErrorSpan.setError(rootSpan, throwable);
    }
}
