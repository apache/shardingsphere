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
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.core.RootSpanContext;
import org.apache.shardingsphere.agent.plugin.tracing.core.constant.AttributeConstants;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.fixture.JDBCExecutorCallbackFixture;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenTelemetryJDBCExecutorCallbackAdviceTest {
    
    public static final String DATA_SOURCE_NAME = "mock.db";
    
    public static final String SQL = "SELECT 1";
    
    private static final String DB_TYPE = "SQL92";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final InMemorySpanExporter testExporter = InMemorySpanExporter.create();
    
    private Span parentSpan;
    
    private TargetAdviceObject targetObject;
    
    private JDBCExecutionUnit executionUnit;
    
    @BeforeEach
    void setup() {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(testExporter)).build();
        OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal().getTracer(OpenTelemetryConstants.TRACER_NAME);
        parentSpan = GlobalOpenTelemetry.getTracer(OpenTelemetryConstants.TRACER_NAME).spanBuilder("parent").startSpan();
        RootSpanContext.set(parentSpan);
        prepare();
    }
    
    @SuppressWarnings("rawtypes")
    @SneakyThrows({ReflectiveOperationException.class, SQLException.class})
    private void prepare() {
        Statement statement = mock(Statement.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getURL()).thenReturn("mock_url");
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(statement.getConnection()).thenReturn(connection);
        executionUnit = new JDBCExecutionUnit(new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit(SQL, Collections.emptyList())), null, statement);
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        when(resourceMetaData.getStorageUnits().get(DATA_SOURCE_NAME).getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "SQL92"));
        when(resourceMetaData.getStorageUnits().get(DATA_SOURCE_NAME).getConnectionProperties()).thenReturn(mock(ConnectionProperties.class));
        JDBCExecutorCallback jdbcExecutorCallback = new JDBCExecutorCallbackFixture(
                TypedSPILoader.getService(DatabaseType.class, "SQL92"), resourceMetaData, SelectStatement.builder().databaseType(databaseType).build(), true);
        Plugins.getMemberAccessor().set(JDBCExecutorCallback.class.getDeclaredField("resourceMetaData"), jdbcExecutorCallback, resourceMetaData);
        targetObject = (TargetAdviceObject) jdbcExecutorCallback;
    }
    
    @AfterEach
    void clean() {
        GlobalOpenTelemetry.resetForTest();
        parentSpan.end();
        testExporter.reset();
    }
    
    @Test
    void assertMethod() {
        OpenTelemetryJDBCExecutorCallbackAdvice advice = new OpenTelemetryJDBCExecutorCallbackAdvice();
        advice.beforeMethod(targetObject, null, new Object[]{executionUnit, false}, "OpenTelemetry");
        advice.afterMethod(targetObject, null, new Object[]{executionUnit, false}, null, "OpenTelemetry");
        List<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertCommonData(spanItems, parentSpan.getSpanContext().getSpanId());
        assertThat(spanItems.iterator().next().getStatus().getStatusCode(), is(StatusCode.OK));
    }
    
    @Test
    void assertMethodWithoutParentSpan() {
        RootSpanContext.set(null);
        OpenTelemetryJDBCExecutorCallbackAdvice advice = new OpenTelemetryJDBCExecutorCallbackAdvice();
        advice.beforeMethod(targetObject, null, new Object[]{executionUnit, false}, "OpenTelemetry");
        advice.afterMethod(targetObject, null, new Object[]{executionUnit, false}, null, "OpenTelemetry");
        List<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertCommonData(spanItems, SpanId.getInvalid());
    }
    
    @Test
    void assertExceptionHandle() {
        OpenTelemetryJDBCExecutorCallbackAdvice advice = new OpenTelemetryJDBCExecutorCallbackAdvice();
        advice.beforeMethod(targetObject, null, new Object[]{executionUnit, false}, "OpenTelemetry");
        advice.onThrowing(targetObject, null, new Object[]{executionUnit, false}, new IOException(""), "OpenTelemetry");
        List<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertCommonData(spanItems, parentSpan.getSpanContext().getSpanId());
        assertThat(spanItems.iterator().next().getStatus().getStatusCode(), is(StatusCode.ERROR));
    }
    
    @Test
    void assertConcurrentInvocations() throws InterruptedException {
        OpenTelemetryJDBCExecutorCallbackAdvice advice = new OpenTelemetryJDBCExecutorCallbackAdvice();
        JDBCExecutionUnit otherExecutionUnit = new JDBCExecutionUnit(
                new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit("SELECT 2", Collections.emptyList())), null, executionUnit.getStorageResource());
        CountDownLatch invocationAStarted = new CountDownLatch(1);
        CountDownLatch invocationBStarted = new CountDownLatch(1);
        AtomicReference<Throwable> workerError = new AtomicReference<>();
        Thread threadA = new Thread(() -> {
            try {
                advice.beforeMethod(targetObject, null, new Object[]{executionUnit, false}, "OpenTelemetry");
                invocationAStarted.countDown();
                if (!invocationBStarted.await(5L, TimeUnit.SECONDS)) {
                    workerError.set(new AssertionError("timed out waiting for invocation B to start"));
                    return;
                }
                advice.afterMethod(targetObject, null, new Object[]{executionUnit, false}, null, "OpenTelemetry");
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                workerError.set(ex);
            }
        });
        Thread threadB = new Thread(() -> {
            try {
                if (!invocationAStarted.await(5L, TimeUnit.SECONDS)) {
                    workerError.set(new AssertionError("timed out waiting for invocation A to start"));
                    return;
                }
                advice.beforeMethod(targetObject, null, new Object[]{otherExecutionUnit, false}, "OpenTelemetry");
                invocationBStarted.countDown();
                advice.onThrowing(targetObject, null, new Object[]{otherExecutionUnit, false}, new IOException("mock"), "OpenTelemetry");
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                workerError.set(ex);
            }
        });
        threadA.start();
        threadB.start();
        threadA.join(5000L);
        threadB.join(5000L);
        assertThat(workerError.get(), is(nullValue()));
        List<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertThat(spanItems.size(), is(2));
        SpanData spanA = findSpanByStatement(spanItems, SQL);
        SpanData spanB = findSpanByStatement(spanItems, "SELECT 2");
        assertThat(spanA.getStatus().getStatusCode(), is(StatusCode.OK));
        assertThat(spanB.getStatus().getStatusCode(), is(StatusCode.ERROR));
        assertThat(spanA.getParentSpanId(), is(parentSpan.getSpanContext().getSpanId()));
        assertThat(spanB.getParentSpanId(), is(parentSpan.getSpanContext().getSpanId()));
    }
    
    private SpanData findSpanByStatement(final List<SpanData> spanItems, final String sql) {
        for (SpanData each : spanItems) {
            if (sql.equals(each.getAttributes().get(AttributeKey.stringKey(AttributeConstants.DB_STATEMENT)))) {
                return each;
            }
        }
        throw new AssertionError("No span found with statement: " + sql);
    }
    
    private void assertCommonData(final List<SpanData> spanItems, final String expectedParentSpanId) {
        assertThat(spanItems.size(), is(1));
        SpanData spanData = spanItems.iterator().next();
        assertThat(spanData.getName(), is("/ShardingSphere/executeSQL/"));
        assertThat(spanData.getParentSpanId(), is(expectedParentSpanId));
        Attributes attributes = spanData.getAttributes();
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.COMPONENT)), is(AttributeConstants.COMPONENT_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_TYPE)), is(DB_TYPE));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_INSTANCE)), is(DATA_SOURCE_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_STATEMENT)), is(SQL));
    }
}
