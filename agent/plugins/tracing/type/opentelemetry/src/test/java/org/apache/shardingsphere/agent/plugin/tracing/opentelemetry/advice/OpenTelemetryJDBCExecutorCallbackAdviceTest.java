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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.plugin.tracing.advice.AbstractJDBCExecutorCallbackAdviceTest;
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

@Slf4j
public final class OpenTelemetryJDBCExecutorCallbackAdviceTest extends AbstractJDBCExecutorCallbackAdviceTest {
    
    @ClassRule
    public static final OpenTelemetryCollector COLLECTOR = new OpenTelemetryCollector();
    
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
        OpenTelemetryJDBCExecutorCallbackAdvice advice = new OpenTelemetryJDBCExecutorCallbackAdvice();
        advice.beforeMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, "OpenTelemetry");
        advice.afterMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, null, "OpenTelemetry");
        List<SpanData> spanItems = COLLECTOR.getSpanItems();
        assertThat(spanItems.size(), is(1));
        SpanData spanData = spanItems.get(0);
        assertThat(spanData.getName(), is("/ShardingSphere/executeSQL/"));
        Attributes attributes = spanData.getAttributes();
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.COMPONENT)), is(AttributeConstants.COMPONENT_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_TYPE)), is(getDatabaseType(getDataSourceName())));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_INSTANCE)), is(getDataSourceName()));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_STATEMENT)), is(getSql()));
    }
    
    @Test
    public void assertExceptionHandle() {
        OpenTelemetryJDBCExecutorCallbackAdvice advice = new OpenTelemetryJDBCExecutorCallbackAdvice();
        advice.beforeMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, "OpenTelemetry");
        advice.onThrowing(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new IOException(), "OpenTelemetry");
        List<SpanData> spanItems = COLLECTOR.getSpanItems();
        assertThat(spanItems.size(), is(1));
        SpanData spanData = spanItems.get(0);
        assertThat(spanData.getName(), is("/ShardingSphere/executeSQL/"));
        assertThat(spanData.getStatus().getStatusCode(), is(StatusCode.ERROR));
        Attributes attributes = spanData.getAttributes();
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.COMPONENT)), is(AttributeConstants.COMPONENT_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_TYPE)), is(getDatabaseType(getDataSourceName())));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_INSTANCE)), is(getDataSourceName()));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_STATEMENT)), is(getSql()));
    }
}
