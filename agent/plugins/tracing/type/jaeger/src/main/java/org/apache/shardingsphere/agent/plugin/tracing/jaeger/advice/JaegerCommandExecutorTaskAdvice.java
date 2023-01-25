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

package org.apache.shardingsphere.agent.plugin.tracing.jaeger.advice;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.TracingCommandExecutorTaskAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.jaeger.constant.JaegerConstants;
import org.apache.shardingsphere.agent.plugin.tracing.jaeger.span.JaegerErrorSpan;

import java.lang.reflect.Method;

/**
 * Command executor task advice executor.
 */
public final class JaegerCommandExecutorTaskAdvice extends TracingCommandExecutorTaskAdvice<Span> {
    
    @Override
    protected Span createRootSpan(final TargetAdviceObject target, final Method method, final Object[] args) {
        return GlobalTracer.get().buildSpan(OPERATION_NAME).withTag(Tags.COMPONENT.getKey(), JaegerConstants.COMPONENT_NAME).startActive(true).span();
    }
    
    @Override
    protected void finishRootSpan(final Span rootSpan, final TargetAdviceObject target, final int connectionSize) {
        Scope scope = GlobalTracer.get().scopeManager().active();
        scope.span().setTag(JaegerConstants.ShardingSphereTags.CONNECTION_COUNT.getKey(), connectionSize);
        scope.close();
    }
    
    @Override
    protected void recordException(final Span rootSpan, final TargetAdviceObject target, final Throwable throwable) {
        JaegerErrorSpan.setError(GlobalTracer.get().activeSpan(), throwable);
    }
}
