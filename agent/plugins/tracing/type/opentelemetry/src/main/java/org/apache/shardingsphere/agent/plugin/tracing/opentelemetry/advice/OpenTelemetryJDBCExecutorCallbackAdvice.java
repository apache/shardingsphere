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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.TracingJDBCExecutorCallbackAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.core.constant.AttributeConstants;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpenTelemetry JDBC executor callback advice executor.
 */
public final class OpenTelemetryJDBCExecutorCallbackAdvice extends TracingJDBCExecutorCallbackAdvice<Span> {
    
    /**
     * Spans started for an invocation whose completion hooks have not run yet, keyed by the execution unit of that invocation.
     * The per-invocation key keeps concurrent and nested callbacks isolated and leaves nothing to consume for a completion hook
     * whose {@code beforeMethod} failed before registering a span. An entry is released by the completion hooks of its own invocation,
     * or by {@link #endAllPendingSpans()} when the plugin stops.
     */
    private static final Map<JDBCExecutionUnit, PendingSpan> PENDING_SPANS = new ConcurrentHashMap<>();
    
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
        PENDING_SPANS.put(executionUnit, new PendingSpan(spanBuilder.startSpan()));
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final Object result, final String pluginType) {
        PendingSpan pending = PENDING_SPANS.remove((JDBCExecutionUnit) args[0]);
        if (null != pending) {
            endSpan(pending);
        }
    }
    
    private static void endSpan(final PendingSpan pending) {
        Span span = pending.getSpan();
        if (null != pending.getThrowable()) {
            span.setStatus(StatusCode.ERROR).recordException(pending.getThrowable());
        } else {
            span.setStatus(StatusCode.OK);
        }
        span.end();
    }
    
    @Override
    public void onThrowing(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final Throwable throwable, final String pluginType) {
        PendingSpan pending = PENDING_SPANS.get((JDBCExecutionUnit) args[0]);
        if (null != pending) {
            pending.setThrowable(throwable);
        }
    }
    
    /**
     * End and release every span that is still pending when the plugin stops, so that no span outlives the tracing it was started for.
     */
    public static void endAllPendingSpans() {
        for (Entry<JDBCExecutionUnit, PendingSpan> entry : PENDING_SPANS.entrySet()) {
            PendingSpan pending = PENDING_SPANS.remove(entry.getKey());
            if (null != pending) {
                endSpan(pending);
            }
        }
    }
    
    /**
     * A span started for one invocation, together with the throwable recorded by a preceding {@code onThrowing} call. The advice executor
     * invokes {@code onThrowing} before the {@code finally} block invokes {@code afterMethod}, so {@code afterMethod} is the single exit that
     * ends the span and applies its final status.
     */
    @RequiredArgsConstructor
    @Getter
    private static final class PendingSpan {
        
        private final Span span;
        
        @Setter
        private Throwable throwable;
    }
}
