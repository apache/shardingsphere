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
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.TracingSQLParserEngineAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.core.constant.AttributeConstants;
import org.apache.shardingsphere.agent.plugin.tracing.opentracing.span.OpenTracingErrorSpan;

import java.lang.reflect.Method;

/**
 * OpenTracing SQL parser engine advice executor.
 */
public final class OpenTracingSQLParserEngineAdvice extends TracingSQLParserEngineAdvice<Span> {
    
    @Override
    protected Object recordSQLParseInfo(final Span parentSpan, final TargetAdviceObject target, final String sql) {
        Scope result = GlobalTracer.get().buildSpan(OPERATION_NAME).asChildOf(parentSpan)
                .withTag(AttributeConstants.COMPONENT, AttributeConstants.COMPONENT_NAME)
                .withTag(AttributeConstants.DB_STATEMENT, sql)
                .withTag(AttributeConstants.SPAN_KIND, AttributeConstants.SPAN_KIND_INTERNAL)
                .startActive(true);
        target.setAttachment(result);
        return result;
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
