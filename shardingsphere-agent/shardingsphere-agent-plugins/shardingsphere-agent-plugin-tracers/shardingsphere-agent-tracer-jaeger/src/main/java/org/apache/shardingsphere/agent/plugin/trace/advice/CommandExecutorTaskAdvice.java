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

package org.apache.shardingsphere.agent.plugin.trace.advice;

import io.opentracing.Scope;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodInvocationResult;
import org.apache.shardingsphere.agent.core.plugin.advice.TargetObject;
import org.apache.shardingsphere.agent.plugin.trace.ShardingErrorSpan;
import org.apache.shardingsphere.agent.plugin.trace.constant.ShardingTags;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;

import java.lang.reflect.Method;

/**
 * Command executor task advice.
 */
public class CommandExecutorTaskAdvice implements MethodAroundAdvice {
    private static final String OPERATION_NAME = "/" + ShardingTags.COMPONENT_NAME + "/rootInvoke/";
    
    private static final String ROOT_SPAN = "_root_span_";
    
    @Override
    public void beforeMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        Scope scope = GlobalTracer.get().buildSpan(OPERATION_NAME)
                .withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME)
                .startActive(true);
        ExecutorDataMap.getValue().put(ROOT_SPAN, scope.span());
    }
    
    @Override
    public void afterMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        GlobalTracer.get().scopeManager().active().close();
        ExecutorDataMap.getValue().remove(ROOT_SPAN);
    }
    
    @Override
    public void onThrowing(final TargetObject target, final Method method, final Object[] args, final Throwable throwable) {
        ShardingErrorSpan.setError(GlobalTracer.get().activeSpan(), throwable);
    }
}
