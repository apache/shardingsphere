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
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.core.RootSpanContext;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.TracingSQLParserEngineAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant.ZipkinConstants;

import java.lang.reflect.Method;

/**
 * Zipkin SQL parser engine advice executor.
 */
public final class ZipkinSQLParserEngineAdvice extends TracingSQLParserEngineAdvice<Span> {
    
    private static final String OPERATION_NAME = "/ShardingSphere/parseSQL/";
    
    @Override
    protected Object recordSQLParseInfo(final Span rootSpan, final TargetAdviceObject target, final String sql) {
        Span result = Tracing.currentTracer().newChild(RootSpanContext.<Span>get().context()).name(OPERATION_NAME);
        result.tag(ZipkinConstants.Tags.COMPONENT, ZipkinConstants.COMPONENT_NAME);
        result.tag(ZipkinConstants.Tags.DB_TYPE, ZipkinConstants.DB_TYPE_VALUE);
        result.tag(ZipkinConstants.Tags.DB_STATEMENT, sql);
        result.start();
        return result;
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result, final String pluginType) {
        ((Span) target.getAttachment()).finish();
    }
    
    @Override
    public void onThrowing(final TargetAdviceObject target, final Method method, final Object[] args, final Throwable throwable, final String pluginType) {
        ((Span) target.getAttachment()).error(throwable);
    }
}
