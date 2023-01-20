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

package org.apache.shardingsphere.agent.plugin.tracing.zipkin.advice;

import brave.Span;
import brave.Tracing;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.TracingCommandExecutorTaskAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant.ZipkinConstants;

import java.lang.reflect.Method;

/**
 * Zipkin command executor task advice executor.
 */
public final class ZipkinCommandExecutorTaskAdvice extends TracingCommandExecutorTaskAdvice<Span> {
    
    @Override
    protected Span createRootSpan(final TargetAdviceObject target, final Method method, final Object[] args) {
        Span result = Tracing.currentTracer().newTrace().name(OPERATION_NAME);
        result.tag(ZipkinConstants.Tags.COMPONENT, ZipkinConstants.COMPONENT_NAME).kind(Span.Kind.CLIENT).tag(ZipkinConstants.Tags.DB_TYPE, ZipkinConstants.DB_TYPE_VALUE).start();
        return result;
    }
    
    @Override
    protected void finishRootSpan(final Span rootSpan, final TargetAdviceObject target, final int connectionSize) {
        rootSpan.tag(ZipkinConstants.Tags.CONNECTION_COUNT, String.valueOf(connectionSize));
        rootSpan.finish();
    }
    
    @Override
    protected void recordException(final Span rootSpan, final TargetAdviceObject target, final Throwable throwable) {
        rootSpan.error(throwable);
    }
}
