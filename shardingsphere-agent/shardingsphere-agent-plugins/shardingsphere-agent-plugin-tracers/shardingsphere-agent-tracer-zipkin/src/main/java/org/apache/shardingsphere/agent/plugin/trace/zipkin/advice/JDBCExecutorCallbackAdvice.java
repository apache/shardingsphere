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

package org.apache.shardingsphere.agent.plugin.trace.zipkin.advice;

import brave.Span;
import brave.Tracing;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodInvocationResult;
import org.apache.shardingsphere.agent.core.plugin.advice.TargetObject;
import org.apache.shardingsphere.agent.plugin.trace.zipkin.ShardingConstants;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;

import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.util.Map;

/**
 * JDBC executor callback advice.
 */
public class JDBCExecutorCallbackAdvice implements MethodAroundAdvice {
    
    private static final String OPERATION_NAME = "/ShardingSphere/executeSQL/";
    
    @SneakyThrows
    @Override
    public void beforeMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        final Span root = (Span) ((Map<String, Object>) args[2]).get(ShardingConstants.ROOT_SPAN);
        if ((boolean) args[1]) {
            target.setAttachment(Tracing.currentTracer().newChild(root.context()).name(OPERATION_NAME).start());
        } else {
            final JDBCExecutionUnit executionUnit = (JDBCExecutionUnit) args[0];
            final ExecutionUnit unit = executionUnit.getExecutionUnit();
            Method getMetadataMethod = JDBCExecutorCallback.class.getDeclaredMethod("getDataSourceMetaData", DatabaseMetaData.class);
            getMetadataMethod.setAccessible(true);
            DataSourceMetaData metaData = (DataSourceMetaData) getMetadataMethod.invoke(target, new Object[]{executionUnit.getStorageResource().getConnection().getMetaData()});
            Span span = Tracing.currentTracer().nextSpan().name(OPERATION_NAME);
            span.tag(ShardingConstants.Tags.COMPONENT, ShardingConstants.COMPONENT_NAME);
            span.tag(ShardingConstants.Tags.DB_TYPE, ShardingConstants.DB_TYPE_VALUE);
            span.tag(ShardingConstants.Tags.DB_INSTANCE, unit.getDataSourceName());
            span.tag(ShardingConstants.Tags.PEER_HOSTNAME, metaData.getHostName());
            span.tag(ShardingConstants.Tags.PEER_PORT, String.valueOf(metaData.getPort()));
            span.tag(ShardingConstants.Tags.DB_STATEMENT, unit.getSqlUnit().getSql());
            span.tag(ShardingConstants.Tags.DB_BIND_VARIABLES, unit.getSqlUnit().getParameters().toString());
            span.start();
            target.setAttachment(span);
        }
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
