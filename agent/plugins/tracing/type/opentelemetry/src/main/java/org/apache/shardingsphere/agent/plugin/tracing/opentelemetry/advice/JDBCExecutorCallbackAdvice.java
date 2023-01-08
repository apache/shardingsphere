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
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.api.advice.type.InstanceMethodAdvice;
import org.apache.shardingsphere.agent.plugin.core.util.AgentReflectionUtil;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;

import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.util.Map;

/**
 * JDBC executor callback advice executor.
 */
public class JDBCExecutorCallbackAdvice implements InstanceMethodAdvice {
    
    private static final String OPERATION_NAME = "/ShardingSphere/executeSQL/";
    
    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args) {
        Span root = (Span) ((Map<String, Object>) args[2]).get(OpenTelemetryConstants.ROOT_SPAN);
        Tracer tracer = GlobalOpenTelemetry.getTracer("shardingsphere-agent");
        SpanBuilder spanBuilder = tracer.spanBuilder(OPERATION_NAME);
        if (null != root) {
            spanBuilder.setParent(Context.current().with(root));
        }
        spanBuilder.setAttribute(OpenTelemetryConstants.COMPONENT, OpenTelemetryConstants.COMPONENT_NAME);
        spanBuilder.setAttribute(OpenTelemetryConstants.DB_TYPE, OpenTelemetryConstants.DB_TYPE_VALUE);
        JDBCExecutionUnit executionUnit = (JDBCExecutionUnit) args[0];
        Map<String, DatabaseType> storageTypes = AgentReflectionUtil.getFieldValue(target, "storageTypes");
        DataSourceMetaData metaData = AgentReflectionUtil.invokeMethod(JDBCExecutorCallback.class.getDeclaredMethod("getDataSourceMetaData", DatabaseMetaData.class, DatabaseType.class),
                target, executionUnit.getStorageResource().getConnection().getMetaData(), storageTypes.get(executionUnit.getExecutionUnit().getDataSourceName()));
        spanBuilder.setAttribute(OpenTelemetryConstants.DB_INSTANCE, executionUnit.getExecutionUnit().getDataSourceName())
                .setAttribute(OpenTelemetryConstants.PEER_HOSTNAME, metaData.getHostname())
                .setAttribute(OpenTelemetryConstants.PEER_PORT, String.valueOf(metaData.getPort()))
                .setAttribute(OpenTelemetryConstants.DB_STATEMENT, executionUnit.getExecutionUnit().getSqlUnit().getSql())
                .setAttribute(OpenTelemetryConstants.DB_BIND_VARIABLES, executionUnit.getExecutionUnit().getSqlUnit().getParameters().toString());
        target.setAttachment(spanBuilder.startSpan());
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result) {
        ((Span) target.getAttachment()).end();
    }
    
    @Override
    public void onThrowing(final TargetAdviceObject target, final Method method, final Object[] args, final Throwable throwable) {
        ((Span) target.getAttachment()).setStatus(StatusCode.ERROR).recordException(throwable);
    }
}
