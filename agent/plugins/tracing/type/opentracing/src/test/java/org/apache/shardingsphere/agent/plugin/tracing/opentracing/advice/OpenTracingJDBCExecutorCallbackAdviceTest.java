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

package org.apache.shardingsphere.agent.plugin.tracing.opentracing.advice;

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.plugin.tracing.advice.AbstractJDBCExecutorCallbackAdviceTest;
import org.apache.shardingsphere.agent.plugin.tracing.core.constant.AttributeConstants;
import org.apache.shardingsphere.agent.plugin.tracing.opentracing.constant.ErrorLogTagKeys;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class OpenTracingJDBCExecutorCallbackAdviceTest extends AbstractJDBCExecutorCallbackAdviceTest {
    
    private static MockTracer tracer;
    
    private static Method executeMethod;
    
    @BeforeAll
    public static void setup() throws ReflectiveOperationException {
        if (!GlobalTracer.isRegistered()) {
            GlobalTracer.register(new MockTracer());
        }
        tracer = (MockTracer) Plugins.getMemberAccessor().get(GlobalTracer.class.getDeclaredField("tracer"), GlobalTracer.get());
        executeMethod = JDBCExecutorCallback.class.getDeclaredMethod("execute", JDBCExecutionUnit.class, boolean.class);
    }
    
    @BeforeEach
    public void reset() {
        tracer.reset();
    }
    
    @Test
    public void assertMethod() {
        OpenTracingJDBCExecutorCallbackAdvice advice = new OpenTracingJDBCExecutorCallbackAdvice();
        advice.beforeMethod(getTargetObject(), executeMethod, new Object[]{getExecutionUnit(), false, Collections.emptyList()}, "OpenTracing");
        advice.afterMethod(getTargetObject(), executeMethod, new Object[]{getExecutionUnit(), false, Collections.emptyList()}, null, "OpenTracing");
        List<MockSpan> spans = tracer.finishedSpans();
        assertThat(spans.size(), is(1));
        MockSpan span = spans.get(0);
        Map<String, Object> tags = span.tags();
        assertTrue(spans.get(0).logEntries().isEmpty());
        assertThat(span.operationName(), is("/ShardingSphere/executeSQL/"));
        assertThat(tags.get(AttributeConstants.COMPONENT), is(AttributeConstants.COMPONENT_NAME));
        assertThat(tags.get(AttributeConstants.DB_INSTANCE), is(getDataSourceName()));
        assertThat(tags.get(AttributeConstants.DB_TYPE), is(getDatabaseType(getDataSourceName())));
        assertThat(tags.get(AttributeConstants.DB_STATEMENT), is(getSql()));
        assertThat(tags.get(AttributeConstants.SPAN_KIND), is(AttributeConstants.SPAN_KIND_CLIENT));
    }
    
    @Test
    public void assertExceptionHandle() {
        Map<String, Object> extraMap = Collections.singletonMap("_root_span_", null);
        OpenTracingJDBCExecutorCallbackAdvice advice = new OpenTracingJDBCExecutorCallbackAdvice();
        advice.beforeMethod(getTargetObject(), executeMethod, new Object[]{getExecutionUnit(), false, extraMap}, "OpenTracing");
        advice.onThrowing(getTargetObject(), executeMethod, new Object[]{getExecutionUnit(), false, extraMap}, new IOException(), "OpenTracing");
        List<MockSpan> spans = tracer.finishedSpans();
        assertThat(spans.size(), is(1));
        MockSpan span = spans.get(0);
        List<MockSpan.LogEntry> entries = span.logEntries();
        Map<String, ?> fields = entries.get(0).fields();
        assertThat(fields.get(ErrorLogTagKeys.EVENT), is("error"));
        assertThat(fields.get(ErrorLogTagKeys.ERROR_KIND), is("java.io.IOException"));
        Map<String, Object> tags = span.tags();
        assertThat(span.operationName(), is("/ShardingSphere/executeSQL/"));
        assertThat(tags.get(AttributeConstants.COMPONENT), is(AttributeConstants.COMPONENT_NAME));
        assertThat(tags.get(AttributeConstants.DB_INSTANCE), is(getDataSourceName()));
        assertThat(tags.get(AttributeConstants.DB_TYPE), is(getDatabaseType(getDataSourceName())));
        assertThat(tags.get(AttributeConstants.DB_STATEMENT), is(getSql()));
        assertThat(tags.get(AttributeConstants.SPAN_KIND), is(AttributeConstants.SPAN_KIND_CLIENT));
    }
}
