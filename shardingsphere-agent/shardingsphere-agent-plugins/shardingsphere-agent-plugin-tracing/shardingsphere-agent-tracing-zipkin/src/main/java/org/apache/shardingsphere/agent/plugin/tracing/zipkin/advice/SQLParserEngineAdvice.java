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
import brave.propagation.TraceContext;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodInvocationResult;
import org.apache.shardingsphere.agent.core.plugin.advice.TargetObject;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.ShardingConstants;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;

import java.lang.reflect.Method;

/**
 * SQL parser engine advice.
 */
public class SQLParserEngineAdvice implements MethodAroundAdvice {
    
    private static final String OPERATION_NAME = "/ShardingSphere/parseSQL/";
    
    @Override
    public void beforeMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        TraceContext parentContext = ((Span) ExecutorDataMap.getValue().get(ShardingConstants.ROOT_SPAN)).context();
        Span span = Tracing.currentTracer().newChild(parentContext).name(OPERATION_NAME);
        span.tag(ShardingConstants.Tags.COMPONENT, ShardingConstants.COMPONENT_NAME);
        span.start();
        target.setAttachment(span);
    }
    
    @Override
    public void afterMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        ((Span) target.getAttachment()).finish();
    }
    
    @Override
    public void onThrowing(final TargetObject target, final Method method, final Object[] args, final Throwable throwable) {
        ((Span) target.getAttachment()).error(throwable);
    }
}
