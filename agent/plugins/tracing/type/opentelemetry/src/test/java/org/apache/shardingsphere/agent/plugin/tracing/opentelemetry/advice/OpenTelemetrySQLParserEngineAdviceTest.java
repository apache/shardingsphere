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
import org.apache.shardingsphere.agent.plugin.tracing.advice.AbstractSQLParserEngineAdviceTest;
import org.apache.shardingsphere.agent.plugin.tracing.core.RootSpanContext;
import org.apache.shardingsphere.agent.plugin.tracing.core.constant.AttributeConstants;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.collector.OpenTelemetryCollector;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public final class OpenTelemetrySQLParserEngineAdviceTest extends AbstractSQLParserEngineAdviceTest {
    
    @ClassRule
    public static final OpenTelemetryCollector COLLECTOR = new OpenTelemetryCollector();
    
    private static final String SQL_STATEMENT = "select 1";
    
    private Span parentSpan;
    
    @Before
    public void setup() {
        parentSpan = GlobalOpenTelemetry.getTracer(OpenTelemetryConstants.TRACER_NAME)
                .spanBuilder("parent")
                .startSpan();
        RootSpanContext.set(parentSpan);
    }
    
    @After
    public void clean() {
        parentSpan.end();
        COLLECTOR.cleanup();
    }
    
    @Test
    public void assertMethod() {
        OpenTelemetrySQLParserEngineAdvice advice = new OpenTelemetrySQLParserEngineAdvice();
        advice.beforeMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, "OpenTelemetry");
        advice.afterMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, null, "OpenTelemetry");
        List<SpanData> spanItems = COLLECTOR.getSpanItems();
        assertThat(spanItems.size(), is(1));
        assertThat(spanItems.get(0).getName(), is("/ShardingSphere/parseSQL/"));
        assertNotNull(spanItems.get(0).getParentSpanId(), is(parentSpan.getSpanContext().getSpanId()));
        Attributes attributes = spanItems.get(0).getAttributes();
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.COMPONENT)), is(AttributeConstants.COMPONENT_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_STATEMENT)), is(SQL_STATEMENT));
    }
    
    @Test
    public void assertExceptionHandle() {
        OpenTelemetrySQLParserEngineAdvice advice = new OpenTelemetrySQLParserEngineAdvice();
        advice.beforeMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, "OpenTelemetry");
        advice.onThrowing(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, new IOException(), "OpenTelemetry");
        advice.afterMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, null, "OpenTelemetry");
        List<SpanData> spanItems = COLLECTOR.getSpanItems();
        assertThat(spanItems.size(), is(1));
        assertThat(spanItems.get(0).getName(), is("/ShardingSphere/parseSQL/"));
        assertThat(spanItems.get(0).getStatus().getStatusCode(), is(StatusCode.ERROR));
        assertNotNull(spanItems.get(0).getParentSpanId(), is(parentSpan.getSpanContext().getSpanId()));
        Attributes attributes = spanItems.get(0).getAttributes();
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.COMPONENT)), is(AttributeConstants.COMPONENT_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_STATEMENT)), is(SQL_STATEMENT));
    }
}
