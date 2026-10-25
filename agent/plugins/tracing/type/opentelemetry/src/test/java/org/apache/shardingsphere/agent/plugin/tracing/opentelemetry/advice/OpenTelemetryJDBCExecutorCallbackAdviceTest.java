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
import org.apache.shardingsphere.agent.core.advisor.executor.type.InstanceMethodAdviceExecutor;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        Collection<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertCommonData(spanItems, parentSpan.getSpanContext().getSpanId());
        assertThat(spanItems.iterator().next().getStatus().getStatusCode(), is(StatusCode.OK));
    }
    
    @Test
    void assertMethodWithoutParentSpan() {
        RootSpanContext.set(null);
        OpenTelemetryJDBCExecutorCallbackAdvice advice = new OpenTelemetryJDBCExecutorCallbackAdvice();
        advice.beforeMethod(targetObject, null, new Object[]{executionUnit, false}, "OpenTelemetry");
        advice.afterMethod(targetObject, null, new Object[]{executionUnit, false}, null, "OpenTelemetry");
        Collection<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertCommonData(spanItems, SpanId.getInvalid());
    }
    
    @Test
    void assertExceptionHandle() {
        OpenTelemetryJDBCExecutorCallbackAdvice advice = new OpenTelemetryJDBCExecutorCallbackAdvice();
        advice.beforeMethod(targetObject, null, new Object[]{executionUnit, false}, "OpenTelemetry");
        advice.onThrowing(targetObject, null, new Object[]{executionUnit, false}, new IOException(""), "OpenTelemetry");
        advice.afterMethod(targetObject, null, new Object[]{executionUnit, false}, null, "OpenTelemetry");
        Collection<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertCommonData(spanItems, parentSpan.getSpanContext().getSpanId());
        assertThat(spanItems.iterator().next().getStatus().getStatusCode(), is(StatusCode.ERROR));
    }
    
    @Test
    void assertNestedFailureDoesNotEndOuterSpan() {
        OpenTelemetryJDBCExecutorCallbackAdvice advice = new OpenTelemetryJDBCExecutorCallbackAdvice();
        JDBCExecutionUnit innerExecutionUnit = new JDBCExecutionUnit(new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit("SELECT 2", Collections.emptyList())), null, executionUnit.getStorageResource());
        advice.beforeMethod(targetObject, null, new Object[]{executionUnit, false}, "OpenTelemetry");
        advice.beforeMethod(targetObject, null, new Object[]{innerExecutionUnit, false}, "OpenTelemetry");
        advice.onThrowing(targetObject, null, new Object[]{innerExecutionUnit, false}, new IOException("mock"), "OpenTelemetry");
        advice.afterMethod(targetObject, null, new Object[]{innerExecutionUnit, false}, null, "OpenTelemetry");
        advice.afterMethod(targetObject, null, new Object[]{executionUnit, false}, null, "OpenTelemetry");
        Collection<SpanData> actualSpanItems = testExporter.getFinishedSpanItems();
        assertThat(actualSpanItems.size(), is(2));
        SpanData actualInnerSpan = findSpanByStatement(actualSpanItems, "SELECT 2");
        SpanData actualOuterSpan = findSpanByStatement(actualSpanItems, SQL);
        assertThat(actualInnerSpan.getStatus().getStatusCode(), is(StatusCode.ERROR));
        assertThat(actualOuterSpan.getStatus().getStatusCode(), is(StatusCode.OK));
    }
    
    @Test
    void assertNestedFailedBeforeDoesNotEndOuterSpan() throws Exception {
        OpenTelemetryJDBCExecutorCallbackAdvice advice = new OpenTelemetryJDBCExecutorCallbackAdvice();
        InstanceMethodAdviceExecutor executor = new InstanceMethodAdviceExecutor(Collections.singletonMap("OpenTelemetry", Collections.singleton(advice)));
        JDBCExecutionUnit innerExecutionUnit = new JDBCExecutionUnit(new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit("SELECT 2", Collections.emptyList())), null, executionUnit.getStorageResource());
        Method executeMethod = Object.class.getDeclaredMethod("toString");
        assertThrows(IOException.class, () -> executor.advice(targetObject, executeMethod, new Object[]{executionUnit, false}, () -> {
            // The inner callback target has no resourceMetaData field, so the inner beforeMethod fails. The executor swallows the failure
            // and still invokes the completion hooks of the inner invocation, which must not touch the span of the outer invocation.
            executor.advice(mock(TargetAdviceObject.class), executeMethod, new Object[]{innerExecutionUnit, false}, () -> null);
            throw new IOException("mock");
        }));
        Collection<SpanData> actualSpanItems = testExporter.getFinishedSpanItems();
        assertThat(actualSpanItems.size(), is(1));
        SpanData actualOuterSpan = actualSpanItems.iterator().next();
        assertThat(actualOuterSpan.getAttributes().get(AttributeKey.stringKey(AttributeConstants.DB_STATEMENT)), is(SQL));
        assertThat(actualOuterSpan.getStatus().getStatusCode(), is(StatusCode.ERROR));
        assertThat(actualOuterSpan.getEvents().size(), is(1));
    }
    
    @Test
    @SneakyThrows({ReflectiveOperationException.class, InterruptedException.class})
    void assertPendingSpanIsReclaimedWhenCompletionHooksAreSkipped() {
        OpenTelemetryJDBCExecutorCallbackAdvice advice = new OpenTelemetryJDBCExecutorCallbackAdvice();
        registerSpanWithoutCompletionHooks(advice);
        Field pendingSpansField = OpenTelemetryJDBCExecutorCallbackAdvice.class.getDeclaredField("PENDING_SPANS");
        pendingSpansField.setAccessible(true);
        Map<?, ?> pendingSpans = (Map<?, ?>) pendingSpansField.get(null);
        for (int i = 0; i < 50 && !pendingSpans.isEmpty(); i++) {
            System.gc();
            // Reading the map lets the WeakHashMap expunge entries whose keys were collected.
            TimeUnit.MILLISECONDS.sleep(10L);
        }
        assertThat(pendingSpans.size(), is(0));
    }
    
    private void registerSpanWithoutCompletionHooks(final OpenTelemetryJDBCExecutorCallbackAdvice advice) {
        JDBCExecutionUnit orphanExecutionUnit = new JDBCExecutionUnit(new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit("SELECT 3", Collections.emptyList())), null, executionUnit.getStorageResource());
        advice.beforeMethod(targetObject, null, new Object[]{orphanExecutionUnit, false}, "OpenTelemetry");
        // No onThrowing and no afterMethod follow: the plugin was disabled between the hooks, so the completion hooks are skipped.
    }
    
    @Test
    void assertConcurrentInvocations() throws InterruptedException {
        OpenTelemetryJDBCExecutorCallbackAdvice advice = new OpenTelemetryJDBCExecutorCallbackAdvice();
        JDBCExecutionUnit otherExecutionUnit = new JDBCExecutionUnit(new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit("SELECT 2", Collections.emptyList())), null, executionUnit.getStorageResource());
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
                advice.afterMethod(targetObject, null, new Object[]{otherExecutionUnit, false}, null, "OpenTelemetry");
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                workerError.set(ex);
            }
        });
        threadA.start();
        threadB.start();
        threadA.join(5000L);
        threadB.join(5000L);
        assertNull(workerError.get());
        Collection<SpanData> actualSpanItems = testExporter.getFinishedSpanItems();
        assertThat(actualSpanItems.size(), is(2));
        SpanData actualSpanA = findSpanByStatement(actualSpanItems, SQL);
        SpanData actualSpanB = findSpanByStatement(actualSpanItems, "SELECT 2");
        assertThat(actualSpanA.getStatus().getStatusCode(), is(StatusCode.OK));
        assertThat(actualSpanB.getStatus().getStatusCode(), is(StatusCode.ERROR));
        assertThat(actualSpanA.getParentSpanId(), is(parentSpan.getSpanContext().getSpanId()));
        assertThat(actualSpanB.getParentSpanId(), is(parentSpan.getSpanContext().getSpanId()));
    }
    
    private SpanData findSpanByStatement(final Collection<SpanData> spanItems, final String sql) {
        for (SpanData each : spanItems) {
            if (sql.equals(each.getAttributes().get(AttributeKey.stringKey(AttributeConstants.DB_STATEMENT)))) {
                return each;
            }
        }
        throw new AssertionError("No span found with statement: " + sql);
    }
    
    private void assertCommonData(final Collection<SpanData> spanItems, final String expectedParentSpanId) {
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
