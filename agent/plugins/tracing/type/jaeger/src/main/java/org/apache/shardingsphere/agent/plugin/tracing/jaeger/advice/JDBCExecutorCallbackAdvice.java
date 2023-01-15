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
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.api.advice.type.InstanceMethodAdvice;
import org.apache.shardingsphere.agent.plugin.core.util.AgentReflectionUtil;
import org.apache.shardingsphere.agent.plugin.tracing.jaeger.constant.JaegerConstants;
import org.apache.shardingsphere.agent.plugin.tracing.jaeger.span.JaegerErrorSpan;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;

import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * JDBC executor callback advice executor.
 */
public final class JDBCExecutorCallbackAdvice implements InstanceMethodAdvice {
    
    private static final String OPERATION_NAME = "/ShardingSphere/executeSQL/";
    
    @Override
    @SneakyThrows({ReflectiveOperationException.class, SQLException.class})
    @SuppressWarnings("unchecked")
    public void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args) {
        Span rootSpan = (Span) ((Map<String, Object>) args[2]).get(JaegerConstants.ROOT_SPAN);
        Tracer.SpanBuilder builder = GlobalTracer.get().buildSpan(OPERATION_NAME);
        if (null != rootSpan) {
            builder = builder.asChildOf(rootSpan);
        }
        JDBCExecutionUnit executionUnit = (JDBCExecutionUnit) args[0];
        Map<String, DatabaseType> storageTypes = AgentReflectionUtil.getFieldValue(target, "storageTypes");
        DataSourceMetaData metaData = AgentReflectionUtil.invokeMethod(
                JDBCExecutorCallback.class.getDeclaredMethod("getDataSourceMetaData", DatabaseMetaData.class, DatabaseType.class),
                target, executionUnit.getStorageResource().getConnection().getMetaData(), storageTypes.get(executionUnit.getExecutionUnit().getDataSourceName()));
        builder.withTag(Tags.COMPONENT.getKey(), JaegerConstants.COMPONENT_NAME)
                .withTag(Tags.DB_TYPE.getKey(), JaegerConstants.DB_TYPE_VALUE)
                .withTag(Tags.DB_INSTANCE.getKey(), executionUnit.getExecutionUnit().getDataSourceName())
                .withTag(Tags.PEER_HOSTNAME.getKey(), metaData.getHostname())
                .withTag(Tags.PEER_PORT.getKey(), metaData.getPort())
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .withTag(Tags.DB_STATEMENT.getKey(), executionUnit.getExecutionUnit().getSqlUnit().getSql())
                .withTag(JaegerConstants.ShardingSphereTags.DB_BIND_VARIABLES.getKey(), executionUnit.getExecutionUnit().getSqlUnit().getParameters().toString());
        target.setAttachment(builder.startActive(true));
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result) {
        ((Scope) target.getAttachment()).close();
    }
    
    @Override
    public void onThrowing(final TargetAdviceObject target, final Method method, final Object[] args, final Throwable throwable) {
        JaegerErrorSpan.setError(GlobalTracer.get().activeSpan(), throwable);
    }
}
