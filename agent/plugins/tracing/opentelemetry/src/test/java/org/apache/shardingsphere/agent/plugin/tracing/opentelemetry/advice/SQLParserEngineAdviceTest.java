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
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.advice.AbstractSQLParserEngineAdviceTest;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.collector.OpenTelemetryCollector;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

public final class SQLParserEngineAdviceTest extends AbstractSQLParserEngineAdviceTest {
    
    @ClassRule
    public static final OpenTelemetryCollector COLLECTOR = new OpenTelemetryCollector();
    
    private static final String SQL_STATEMENT = "select 1";
    
    private SQLParserEngineAdvice advice;
    
    private Span parentSpan;
    
    @Before
    public void setup() {
        parentSpan = GlobalOpenTelemetry.getTracer("shardingsphere-agent")
                .spanBuilder("parent")
                .startSpan();
        ExecutorDataMap.getValue().put(OpenTelemetryConstants.ROOT_SPAN, parentSpan);
        advice = new SQLParserEngineAdvice();
    }
    
    @Test
    public void assertMethod() {
        advice.beforeMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, new MethodInvocationResult());
        advice.afterMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, new MethodInvocationResult());
        parentSpan.end();
        List<SpanData> spanItems = COLLECTOR.getSpanItems();
        assertThat(spanItems.size(), is(2));
        assertThat(spanItems.get(0).getName(), is("/ShardingSphere/parseSQL/"));
        assertNotNull(spanItems.get(0).getParentSpanId(), is(parentSpan.getSpanContext().getSpanId()));
        Attributes attributes = spanItems.get(0).getAttributes();
        assertThat(attributes.get(AttributeKey.stringKey(OpenTelemetryConstants.COMPONENT)), is(OpenTelemetryConstants.COMPONENT_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(OpenTelemetryConstants.DB_TYPE)), is(OpenTelemetryConstants.DB_TYPE_VALUE));
        assertThat(attributes.get(AttributeKey.stringKey(OpenTelemetryConstants.DB_STATEMENT)), is(SQL_STATEMENT));
    }
    
    @Test
    public void assertExceptionHandle() {
        advice.beforeMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, new MethodInvocationResult());
        advice.onThrowing(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, new IOException());
        advice.afterMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, new MethodInvocationResult());
        parentSpan.end();
        List<SpanData> spanItems = COLLECTOR.getSpanItems();
        assertThat(spanItems.size(), is(2));
        assertThat(spanItems.get(0).getName(), is("/ShardingSphere/parseSQL/"));
        assertThat(spanItems.get(0).getStatus().getStatusCode(), is(StatusCode.ERROR));
        assertNotNull(spanItems.get(0).getParentSpanId(), is(parentSpan.getSpanContext().getSpanId()));
        Attributes attributes = spanItems.get(0).getAttributes();
        assertThat(attributes.get(AttributeKey.stringKey(OpenTelemetryConstants.COMPONENT)), is(OpenTelemetryConstants.COMPONENT_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(OpenTelemetryConstants.DB_TYPE)), is(OpenTelemetryConstants.DB_TYPE_VALUE));
        assertThat(attributes.get(AttributeKey.stringKey(OpenTelemetryConstants.DB_STATEMENT)), is(SQL_STATEMENT));
    }
}
