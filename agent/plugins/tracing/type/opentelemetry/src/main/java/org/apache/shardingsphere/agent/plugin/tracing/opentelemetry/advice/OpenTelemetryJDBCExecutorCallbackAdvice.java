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
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.core.RootSpanContext;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.TracingJDBCExecutorCallbackAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;

import java.lang.reflect.Method;

/**
 * OpenTelemetry JDBC executor callback advice executor.
 */
public final class OpenTelemetryJDBCExecutorCallbackAdvice extends TracingJDBCExecutorCallbackAdvice<Span> {
    
    @Override
    protected void recordExecuteInfo(final Span rootSpan, final TargetAdviceObject target, final JDBCExecutionUnit executionUnit, final boolean isTrunkThread, final DataSourceMetaData metaData) {
        Tracer tracer = GlobalOpenTelemetry.getTracer("shardingsphere-agent");
        SpanBuilder spanBuilder = tracer.spanBuilder(OPERATION_NAME);
        if (!RootSpanContext.isEmpty()) {
            spanBuilder.setParent(Context.current().with(RootSpanContext.<Span>get()));
        }
        spanBuilder.setAttribute(OpenTelemetryConstants.COMPONENT, OpenTelemetryConstants.COMPONENT_NAME);
        spanBuilder.setAttribute(OpenTelemetryConstants.DB_TYPE, OpenTelemetryConstants.DB_TYPE_VALUE);
        spanBuilder.setAttribute(OpenTelemetryConstants.DB_INSTANCE, executionUnit.getExecutionUnit().getDataSourceName())
                .setAttribute(OpenTelemetryConstants.PEER_HOSTNAME, metaData.getHostname())
                .setAttribute(OpenTelemetryConstants.PEER_PORT, String.valueOf(metaData.getPort()))
                .setAttribute(OpenTelemetryConstants.DB_STATEMENT, executionUnit.getExecutionUnit().getSqlUnit().getSql())
                .setAttribute(OpenTelemetryConstants.DB_BIND_VARIABLES, executionUnit.getExecutionUnit().getSqlUnit().getParameters().toString());
        target.setAttachment(spanBuilder.startSpan());
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result, final String pluginType) {
        ((Span) target.getAttachment()).end();
    }
    
    @Override
    public void onThrowing(final TargetAdviceObject target, final Method method, final Object[] args, final Throwable throwable, final String pluginType) {
        ((Span) target.getAttachment()).setStatus(StatusCode.ERROR).recordException(throwable);
    }
}
