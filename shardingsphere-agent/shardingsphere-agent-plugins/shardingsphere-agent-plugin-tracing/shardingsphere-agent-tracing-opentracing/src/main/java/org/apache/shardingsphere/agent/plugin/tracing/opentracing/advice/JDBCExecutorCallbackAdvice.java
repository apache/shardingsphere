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
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.opentracing.constant.ShardingSphereTags;
import org.apache.shardingsphere.agent.plugin.tracing.opentracing.span.OpenTracingErrorSpan;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * JDBC executor callback advice.
 */
public final class JDBCExecutorCallbackAdvice implements InstanceMethodAroundAdvice {
    
    private static final String OPERATION_NAME = "/ShardingSphere/executeSQL/";
    
    @Override
    @SuppressWarnings("unchecked")
    public void beforeMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        Span root = (Span) ((Map<String, Object>) args[2]).get("ot_root_span_");
        Tracer.SpanBuilder builder = GlobalTracer.get().buildSpan(OPERATION_NAME);
        if ((boolean) args[1]) {
            builder.asChildOf(root);
        } else {
            JDBCExecutionUnit executionUnit = (JDBCExecutionUnit) args[0];
            ExecutionUnit unit = executionUnit.getExecutionUnit();
            builder.withTag(Tags.COMPONENT.getKey(), ShardingSphereTags.COMPONENT_NAME)
                    .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                    .withTag(Tags.DB_TYPE.getKey(), "sql")
                    .withTag(Tags.DB_INSTANCE.getKey(), unit.getDataSourceName())
                    .withTag(Tags.DB_STATEMENT.getKey(), unit.getSqlUnit().getSql())
                    .withTag(ShardingSphereTags.DB_BIND_VARIABLES.getKey(), unit.getSqlUnit().getParameters().toString());
        }
        target.setAttachment(builder.startActive(true));
    }
    
    @Override
    public void afterMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        ((Scope) target.getAttachment()).close();
    }
    
    @Override
    public void onThrowing(final AdviceTargetObject target, final Method method, final Object[] args, final Throwable throwable) {
        OpenTracingErrorSpan.setError(GlobalTracer.get().activeSpan(), throwable);
    }
}
