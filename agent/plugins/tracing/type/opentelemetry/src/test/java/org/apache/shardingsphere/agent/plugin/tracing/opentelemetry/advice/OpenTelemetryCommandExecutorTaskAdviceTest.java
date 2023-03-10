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

import io.netty.util.DefaultAttributeMap;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.AgentExtension;
import org.apache.shardingsphere.agent.plugin.tracing.core.constant.AttributeConstants;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({AgentExtension.class, AutoMockExtension.class})
@StaticMockSettings(ProxyContext.class)
public final class OpenTelemetryCommandExecutorTaskAdviceTest {
    
    private final InMemorySpanExporter testExporter = InMemorySpanExporter.create();
    
    private TargetAdviceObject targetObject;
    
    @BeforeEach
    public void setup() {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(testExporter)).build();
        OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal().getTracer(OpenTelemetryConstants.TRACER_NAME);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(mock(ContextManager.class, RETURNS_DEEP_STUBS));
        ConnectionSession connectionSession = new ConnectionSession(mock(MySQLDatabaseType.class), TransactionType.BASE, new DefaultAttributeMap());
        Object executorTask = new CommandExecutorTask(null, connectionSession, null, null);
        targetObject = (TargetAdviceObject) executorTask;
    }
    
    @AfterEach
    public void clean() {
        GlobalOpenTelemetry.resetForTest();
        testExporter.reset();
    }
    
    @Test
    public void assertMethod() {
        OpenTelemetryCommandExecutorTaskAdvice advice = new OpenTelemetryCommandExecutorTaskAdvice();
        advice.beforeMethod(targetObject, null, new Object[]{}, "OpenTelemetry");
        advice.afterMethod(targetObject, null, new Object[]{}, null, "OpenTelemetry");
        List<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertThat(spanItems.size(), is(1));
        SpanData spanData = spanItems.get(0);
        assertThat(spanData.getName(), is("/ShardingSphere/rootInvoke/"));
        assertThat(spanData.getAttributes().get(AttributeKey.stringKey(AttributeConstants.COMPONENT)), is(AttributeConstants.COMPONENT_NAME));
        assertThat(spanData.getAttributes().get(AttributeKey.stringKey(AttributeConstants.SPAN_KIND)), is(AttributeConstants.SPAN_KIND_CLIENT));
    }
    
    @Test
    public void assertExceptionHandle() {
        OpenTelemetryCommandExecutorTaskAdvice advice = new OpenTelemetryCommandExecutorTaskAdvice();
        advice.beforeMethod(targetObject, null, new Object[]{}, "OpenTelemetry");
        advice.onThrowing(targetObject, null, new Object[]{}, new IOException(), "OpenTelemetry");
        List<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertThat(spanItems.size(), is(1));
        SpanData spanData = spanItems.get(0);
        assertThat(spanData.getName(), is("/ShardingSphere/rootInvoke/"));
        assertThat(spanData.getAttributes().get(AttributeKey.stringKey(AttributeConstants.COMPONENT)), is(AttributeConstants.COMPONENT_NAME));
        assertThat(spanData.getAttributes().get(AttributeKey.stringKey(AttributeConstants.SPAN_KIND)), is(AttributeConstants.SPAN_KIND_CLIENT));
        assertThat(spanData.getStatus().getStatusCode(), is(StatusCode.ERROR));
    }
}
