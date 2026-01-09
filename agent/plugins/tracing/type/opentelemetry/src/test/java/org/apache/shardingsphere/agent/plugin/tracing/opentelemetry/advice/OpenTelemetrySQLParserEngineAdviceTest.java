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
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.apache.shardingsphere.agent.plugin.tracing.core.RootSpanContext;
import org.apache.shardingsphere.agent.plugin.tracing.core.constant.AttributeConstants;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.fixture.TargetAdviceObjectFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class OpenTelemetrySQLParserEngineAdviceTest {
    
    private static final String SQL = "SELECT 1";
    
    private final InMemorySpanExporter testExporter = InMemorySpanExporter.create();
    
    private Span parentSpan;
    
    @BeforeEach
    void setup() {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(testExporter)).build();
        OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal().getTracer(OpenTelemetryConstants.TRACER_NAME);
        parentSpan = GlobalOpenTelemetry.getTracer(OpenTelemetryConstants.TRACER_NAME).spanBuilder("parent").startSpan();
        RootSpanContext.set(parentSpan);
    }
    
    @AfterEach
    void clean() {
        GlobalOpenTelemetry.resetForTest();
        parentSpan.end();
        testExporter.reset();
    }
    
    @Test
    void assertMethod() {
        TargetAdviceObjectFixture adviceObjectFixture = new TargetAdviceObjectFixture();
        OpenTelemetrySQLParserEngineAdvice advice = new OpenTelemetrySQLParserEngineAdvice();
        advice.beforeMethod(adviceObjectFixture, null, new Object[]{SQL, true}, "OpenTelemetry");
        advice.afterMethod(adviceObjectFixture, null, new Object[]{SQL, true}, null, "OpenTelemetry");
        List<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertCommonData(spanItems, parentSpan.getSpanContext().getSpanId());
        assertThat(spanItems.iterator().next().getStatus().getStatusCode(), is(StatusCode.OK));
    }
    
    @Test
    void assertMethodWithoutParentSpan() {
        RootSpanContext.set(null);
        TargetAdviceObjectFixture adviceObjectFixture = new TargetAdviceObjectFixture();
        OpenTelemetrySQLParserEngineAdvice advice = new OpenTelemetrySQLParserEngineAdvice();
        advice.beforeMethod(adviceObjectFixture, null, new Object[]{SQL, true}, "OpenTelemetry");
        advice.afterMethod(adviceObjectFixture, null, new Object[]{SQL, true}, null, "OpenTelemetry");
        List<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertCommonData(spanItems, SpanId.getInvalid());
        assertThat(spanItems.iterator().next().getStatus().getStatusCode(), is(StatusCode.OK));
    }
    
    @Test
    void assertExceptionHandle() {
        TargetAdviceObjectFixture adviceObjectFixture = new TargetAdviceObjectFixture();
        OpenTelemetrySQLParserEngineAdvice advice = new OpenTelemetrySQLParserEngineAdvice();
        advice.beforeMethod(adviceObjectFixture, null, new Object[]{SQL, true}, "OpenTelemetry");
        advice.onThrowing(adviceObjectFixture, null, new Object[]{SQL, true}, new IOException(""), "OpenTelemetry");
        List<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertCommonData(spanItems, parentSpan.getSpanContext().getSpanId());
        assertThat(spanItems.iterator().next().getStatus().getStatusCode(), is(StatusCode.ERROR));
    }
    
    private void assertCommonData(final List<SpanData> spanItems, final String expectedParentSpanId) {
        assertThat(spanItems.size(), is(1));
        SpanData spanData = spanItems.iterator().next();
        assertThat(spanData.getName(), is("/ShardingSphere/parseSQL/"));
        assertThat(spanData.getParentSpanId(), is(expectedParentSpanId));
        Attributes attributes = spanItems.iterator().next().getAttributes();
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.COMPONENT)), is(AttributeConstants.COMPONENT_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_STATEMENT)), is(SQL));
    }
}
