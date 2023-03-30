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
import org.apache.shardingsphere.agent.test.AgentExtension;
import org.apache.shardingsphere.agent.test.fixture.AdviceTargetSetting;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.configuration.plugins.Plugins;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AgentExtension.class)
@AdviceTargetSetting("org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback")
class OpenTelemetryJDBCExecutorCallbackAdviceTest {
    
    public static final String DATA_SOURCE_NAME = "mock.db";
    
    public static final String SQL = "SELECT 1";
    
    private static final String DB_TYPE = "MySQL";
    
    private final InMemorySpanExporter testExporter = InMemorySpanExporter.create();
    
    private Span parentSpan;
    
    private TargetAdviceObject targetObject;
    
    private JDBCExecutionUnit executionUnit;
    
    private Map<String, DatabaseType> storageTypes;
    
    @BeforeEach
    void setup() {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(testExporter)).build();
        OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal().getTracer(OpenTelemetryConstants.TRACER_NAME);
        parentSpan = GlobalOpenTelemetry.getTracer(OpenTelemetryConstants.TRACER_NAME).spanBuilder("parent").startSpan();
        RootSpanContext.set(parentSpan);
        prepare();
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @SneakyThrows({ReflectiveOperationException.class, SQLException.class})
    public void prepare() {
        Statement statement = mock(Statement.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getURL()).thenReturn("mock_url");
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(statement.getConnection()).thenReturn(connection);
        executionUnit = new JDBCExecutionUnit(new ExecutionUnit(DATA_SOURCE_NAME, new SQLUnit(SQL, Collections.emptyList())), null, statement);
        JDBCExecutorCallback jdbcExecutorCallback = new JDBCExecutorCallback<Object>(new MySQLDatabaseType(), Collections.singletonMap("ds", new MySQLDatabaseType()),
                new MySQLSelectStatement(), true) {
            
            @Override
            protected Object executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
                throw new SQLException();
            }
            
            @Override
            protected Optional<Object> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
                return Optional.empty();
            }
        };
        Map<String, DataSourceMetaData> cachedDatasourceMetaData = (Map<String, DataSourceMetaData>) Plugins.getMemberAccessor()
                .get(JDBCExecutorCallback.class.getDeclaredField("CACHED_DATASOURCE_METADATA"), jdbcExecutorCallback);
        cachedDatasourceMetaData.put("mock_url", mock(DataSourceMetaData.class));
        storageTypes = Collections.singletonMap(DATA_SOURCE_NAME, new MySQLDatabaseType());
        Plugins.getMemberAccessor().set(JDBCExecutorCallback.class.getDeclaredField("storageTypes"), jdbcExecutorCallback, storageTypes);
        targetObject = (TargetAdviceObject) jdbcExecutorCallback;
    }
    
    @AfterEach
    void clean() {
        parentSpan.end();
        GlobalOpenTelemetry.resetForTest();
        testExporter.reset();
    }
    
    @Test
    void assertMethod() {
        OpenTelemetryJDBCExecutorCallbackAdvice advice = new OpenTelemetryJDBCExecutorCallbackAdvice();
        advice.beforeMethod(targetObject, null, new Object[]{executionUnit, false}, "OpenTelemetry");
        advice.afterMethod(targetObject, null, new Object[]{executionUnit, false}, null, "OpenTelemetry");
        List<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertCommonData(spanItems);
        assertThat(spanItems.iterator().next().getStatus().getStatusCode(), is(StatusCode.OK));
    }
    
    @Test
    void assertExceptionHandle() {
        OpenTelemetryJDBCExecutorCallbackAdvice advice = new OpenTelemetryJDBCExecutorCallbackAdvice();
        advice.beforeMethod(targetObject, null, new Object[]{executionUnit, false}, "OpenTelemetry");
        advice.onThrowing(targetObject, null, new Object[]{executionUnit, false}, new IOException(), "OpenTelemetry");
        List<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertCommonData(spanItems);
        assertThat(spanItems.iterator().next().getStatus().getStatusCode(), is(StatusCode.ERROR));
    }
    
    private void assertCommonData(final List<SpanData> spanItems) {
        assertThat(spanItems.size(), is(1));
        SpanData spanData = spanItems.get(0);
        assertThat(spanData.getName(), is("/ShardingSphere/executeSQL/"));
        Attributes attributes = spanData.getAttributes();
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.COMPONENT)), is(AttributeConstants.COMPONENT_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_TYPE)), is(DB_TYPE));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_INSTANCE)), is(DATA_SOURCE_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_STATEMENT)), is(SQL));
    }
}
