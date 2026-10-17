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
import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.TracingJDBCExecutorCallbackAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.core.constant.AttributeConstants;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * OpenTelemetry JDBC executor callback advice executor.
 */
public final class OpenTelemetryJDBCExecutorCallbackAdvice extends TracingJDBCExecutorCallbackAdvice<Span> {
    
    private static final ThreadLocal<Deque<PendingSpan>> ACTIVE_SPANS = ThreadLocal.withInitial(ArrayDeque::new);
    
    @Override
    protected void recordExecuteInfo(final Span parentSpan, final TargetAdviceObject target, final JDBCExecutionUnit executionUnit, final boolean isTrunkThread,
                                     final ConnectionProperties connectionProps, final DatabaseType databaseType) {
        Tracer tracer = GlobalOpenTelemetry.getTracer(OpenTelemetryConstants.TRACER_NAME);
        SpanBuilder spanBuilder = tracer.spanBuilder(OPERATION_NAME);
        if (null != parentSpan) {
            spanBuilder.setParent(Context.current().with(parentSpan));
        }
        spanBuilder.setAttribute(AttributeConstants.COMPONENT, AttributeConstants.COMPONENT_NAME);
        spanBuilder.setAttribute(AttributeConstants.DB_TYPE, databaseType.getType());
        spanBuilder.setAttribute(AttributeConstants.DB_INSTANCE, executionUnit.getExecutionUnit().getDataSourceName())
                .setAttribute(AttributeConstants.PEER_HOSTNAME, connectionProps.getHostname())
                .setAttribute(AttributeConstants.PEER_PORT, String.valueOf(connectionProps.getPort()))
                .setAttribute(AttributeConstants.DB_STATEMENT, executionUnit.getExecutionUnit().getSqlUnit().getSql())
                .setAttribute(AttributeConstants.DB_BIND_VARIABLES, executionUnit.getExecutionUnit().getSqlUnit().getParameters().toString())
                .setAttribute(AttributeConstants.SPAN_KIND, AttributeConstants.SPAN_KIND_CLIENT);
        ACTIVE_SPANS.get().push(new PendingSpan(spanBuilder.startSpan()));
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final Object result, final String pluginType) {
        PendingSpan pending = ACTIVE_SPANS.get().poll();
        if (null != pending) {
            Span span = pending.getSpan();
            if (null != pending.getThrowable()) {
                span.setStatus(StatusCode.ERROR).recordException(pending.getThrowable());
            } else {
                span.setStatus(StatusCode.OK);
            }
            span.end();
        }
    }
    
    @Override
    public void onThrowing(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final Throwable throwable, final String pluginType) {
        PendingSpan pending = ACTIVE_SPANS.get().peek();
        if (null != pending) {
            pending.setThrowable(throwable);
        }
    }
    
    /**
     * A span started for one invocation, together with the throwable recorded by a preceding
     * {@code onThrowing} call. The advice executor invokes {@code onThrowing} before the
     * {@code finally} block invokes {@code afterMethod}, so {@code afterMethod} is the single
     * exit that ends the span and applies its final status.
     */
    private static final class PendingSpan {
        
        private final Span span;
        
        private Throwable throwable;
        
        PendingSpan(final Span span) {
            this.span = span;
        }
        
        Span getSpan() {
            return span;
        }
        
        Throwable getThrowable() {
            return throwable;
        }
        
        void setThrowable(final Throwable throwable) {
            this.throwable = throwable;
        }
    }
}
