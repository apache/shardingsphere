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
import org.apache.shardingsphere.agent.api.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant.ZipkinConstants;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Command executor task advice.
 */
public final class CommandExecutorTaskAdvice implements InstanceMethodAroundAdvice {
    
    private static final String OPERATION_NAME = "/ShardingSphere/rootInvoke/";
    
    @Override
    public void beforeMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        Span span = Tracing.currentTracer().newTrace().name(OPERATION_NAME);
        span.tag(ZipkinConstants.Tags.COMPONENT, ZipkinConstants.COMPONENT_NAME).kind(Span.Kind.CLIENT)
                .tag(ZipkinConstants.Tags.DB_TYPE, ZipkinConstants.DB_TYPE_VALUE).start();
        ExecutorDataMap.getValue().put(ZipkinConstants.ROOT_SPAN, span);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @Override
    public void afterMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        Field field = CommandExecutorTask.class.getDeclaredField("connectionSession");
        field.setAccessible(true);
        JDBCBackendConnection connection = (JDBCBackendConnection) ((ConnectionSession) field.get(target)).getBackendConnection();
        Span span = (Span) ExecutorDataMap.getValue().remove(ZipkinConstants.ROOT_SPAN);
        span.tag(ZipkinConstants.Tags.CONNECTION_COUNT, String.valueOf(connection.getConnectionSize()));
        span.finish();
    }
    
    @Override
    public void onThrowing(final AdviceTargetObject target, final Method method, final Object[] args, final Throwable throwable) {
        ((Span) ExecutorDataMap.getValue().get(ZipkinConstants.ROOT_SPAN)).error(throwable);
    }
}
