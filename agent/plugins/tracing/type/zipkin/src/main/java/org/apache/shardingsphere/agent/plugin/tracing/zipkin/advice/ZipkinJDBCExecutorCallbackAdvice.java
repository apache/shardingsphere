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
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.TracingJDBCExecutorCallbackAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant.ZipkinConstants;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;

import java.lang.reflect.Method;

/**
 * Zipkin JDBC executor callback advice executor.
 */
public final class ZipkinJDBCExecutorCallbackAdvice extends TracingJDBCExecutorCallbackAdvice<Span> {
    
    @Override
    protected void recordExecuteInfo(final Span rootSpan, final TargetAdviceObject target, final JDBCExecutionUnit executionUnit, final boolean isTrunkThread, final DataSourceMetaData metaData) {
        Span span = null == rootSpan ? Tracing.currentTracer().nextSpan().name(OPERATION_NAME) : Tracing.currentTracer().newChild(rootSpan.context()).name(OPERATION_NAME);
        span.tag(ZipkinConstants.Tags.COMPONENT, ZipkinConstants.COMPONENT_NAME);
        span.tag(ZipkinConstants.Tags.DB_TYPE, ZipkinConstants.DB_TYPE_VALUE);
        span.tag(ZipkinConstants.Tags.DB_INSTANCE, executionUnit.getExecutionUnit().getDataSourceName());
        span.tag(ZipkinConstants.Tags.PEER_HOSTNAME, metaData.getHostname());
        span.tag(ZipkinConstants.Tags.PEER_PORT, String.valueOf(metaData.getPort()));
        span.tag(ZipkinConstants.Tags.DB_STATEMENT, executionUnit.getExecutionUnit().getSqlUnit().getSql());
        span.tag(ZipkinConstants.Tags.DB_BIND_VARIABLES, executionUnit.getExecutionUnit().getSqlUnit().getParameters().toString());
        span.start();
        target.setAttachment(span);
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
