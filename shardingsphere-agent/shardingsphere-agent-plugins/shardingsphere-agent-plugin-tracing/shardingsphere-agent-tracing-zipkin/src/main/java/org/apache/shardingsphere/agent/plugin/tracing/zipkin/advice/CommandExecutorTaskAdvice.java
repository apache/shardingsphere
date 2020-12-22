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
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodInvocationResult;
import org.apache.shardingsphere.agent.core.plugin.advice.TargetObject;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.ShardingConstants;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;

import java.lang.reflect.Method;

/**
 * Command executor task advice.
 */
public class CommandExecutorTaskAdvice implements MethodAroundAdvice {

    private static final String OPERATION_NAME = "/ShardingSphere/rootInvoke/";

    @Override
    public void beforeMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        Span span = Tracing.currentTracer().newTrace().name(OPERATION_NAME);
        span.tag(ShardingConstants.Tags.COMPONENT, ShardingConstants.COMPONENT_NAME).kind(Span.Kind.CLIENT)
                .tag(ShardingConstants.Tags.DB_STATEMENT, ShardingConstants.DB_TYPE_VALUE).start();
        ExecutorDataMap.getValue().put(ShardingConstants.ROOT_SPAN, span);
    }

    @SneakyThrows
    @Override
    public void afterMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        BackendConnection connection = (BackendConnection) CommandExecutorTask.class.getDeclaredField("backendConnection").get(target);
        Span span = (Span) ExecutorDataMap.getValue().remove(ShardingConstants.ROOT_SPAN);
        span.tag(ShardingConstants.Tags.CONNECTION_COUNT, String.valueOf(connection.getConnectionSize()));
        span.flush();
    }

    @Override
    public void onThrowing(final TargetObject target, final Method method, final Object[] args, final Throwable throwable) {
        ((Span) ExecutorDataMap.getValue().get(ShardingConstants.ROOT_SPAN)).error(throwable);
    }
}
