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

package org.apache.shardingsphere.agent.plugin.tracing.opentracing.advice;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.TracingJDBCExecutorCallbackAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.core.constant.AttributeConstants;
import org.apache.shardingsphere.agent.plugin.tracing.opentracing.span.OpenTracingErrorSpan;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;

import java.lang.reflect.Method;

/**
 * OpenTracing JDBC executor callback advice executor.
 */
public final class OpenTracingJDBCExecutorCallbackAdvice extends TracingJDBCExecutorCallbackAdvice<Span> {
    
    @Override
    protected void recordExecuteInfo(final Span parentSpan, final TargetAdviceObject target, final JDBCExecutionUnit executionUnit, final boolean isTrunkThread, final DataSourceMetaData metaData,
                                     final String databaseType) {
        Tracer.SpanBuilder builder = GlobalTracer.get().buildSpan(OPERATION_NAME);
        builder.asChildOf(parentSpan)
                .withTag(AttributeConstants.COMPONENT, AttributeConstants.COMPONENT_NAME)
                .withTag(AttributeConstants.DB_TYPE, databaseType)
                .withTag(AttributeConstants.DB_INSTANCE, executionUnit.getExecutionUnit().getDataSourceName())
                .withTag(AttributeConstants.DB_STATEMENT, executionUnit.getExecutionUnit().getSqlUnit().getSql())
                .withTag(AttributeConstants.DB_BIND_VARIABLES, executionUnit.getExecutionUnit().getSqlUnit().getParameters().toString())
                .withTag(AttributeConstants.SPAN_KIND, AttributeConstants.SPAN_KIND_CLIENT);
        target.setAttachment(builder.startActive(true));
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result, final String pluginType) {
        ((Scope) target.getAttachment()).close();
    }
    
    @Override
    public void onThrowing(final TargetAdviceObject target, final Method method, final Object[] args, final Throwable throwable, final String pluginType) {
        OpenTracingErrorSpan.setError(GlobalTracer.get().activeSpan(), throwable);
    }
}
