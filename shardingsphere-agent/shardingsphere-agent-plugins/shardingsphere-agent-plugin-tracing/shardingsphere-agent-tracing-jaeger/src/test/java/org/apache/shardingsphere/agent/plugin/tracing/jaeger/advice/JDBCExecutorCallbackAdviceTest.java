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

package org.apache.shardingsphere.agent.plugin.tracing.jaeger.advice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.jaeger.constant.ErrorLogTagKeys;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.internal.util.reflection.FieldReader;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class JDBCExecutorCallbackAdviceTest {
    
    private static final JDBCExecutorCallbackAdvice ADVICE = new JDBCExecutorCallbackAdvice();
    
    private static MockTracer tracer;
    
    private static Method executeMethod;
    
    @BeforeClass
    @SneakyThrows
    public static void setup() {
        if (!GlobalTracer.isRegistered()) {
            GlobalTracer.register(new MockTracer());
        }
        FieldReader fieldReader = new FieldReader(GlobalTracer.get(), GlobalTracer.class.getDeclaredField("tracer"));
        JDBCExecutorCallbackAdviceTest.tracer = (MockTracer) fieldReader.read();
        executeMethod = JDBCExecutorCallback.class.getDeclaredMethod("execute", JDBCExecutionUnit.class, boolean.class, Map.class);
    }
    
    @Before
    public void reset() {
        tracer.reset();
    }
    
    @Test
    public void testMethod() {
        final MockTargetObject targetObject = new MockTargetObject();
        final Map<String, Object> extraMap = Maps.newHashMap();
        extraMap.put("_root_span_", null);
        JDBCExecutionUnit executionUnit = mock(JDBCExecutionUnit.class);
        when(executionUnit.getExecutionUnit()).thenReturn(new ExecutionUnit("mock.db", new SQLUnit("select 1", Lists.newArrayList())));
        ADVICE.beforeMethod(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new MethodInvocationResult());
        ADVICE.afterMethod(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new MethodInvocationResult());
        List<MockSpan> spans = tracer.finishedSpans();
        System.out.println(spans);
        Assert.assertEquals(1, spans.size());
        MockSpan span = spans.get(0);
        Map<String, Object> tags = span.tags();
        Assert.assertEquals(0, span.logEntries().size());
        Assert.assertEquals("/ShardingSphere/executeSQL/", span.operationName());
        Assert.assertEquals("mock.db", tags.get("db.instance"));
        Assert.assertEquals("sql", tags.get("db.type"));
        Assert.assertEquals("client", tags.get("span.kind"));
        Assert.assertEquals("select 1", tags.get("db.statement"));
    }
    
    @Test
    public void testExceptionHandle() {
        final MockTargetObject targetObject = new MockTargetObject();
        final Map<String, Object> extraMap = Maps.newHashMap();
        extraMap.put("_root_span_", null);
        JDBCExecutionUnit executionUnit = mock(JDBCExecutionUnit.class);
        when(executionUnit.getExecutionUnit()).thenReturn(new ExecutionUnit("mock.db", new SQLUnit("select 1", Lists.newArrayList())));
        ADVICE.beforeMethod(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new MethodInvocationResult());
        ADVICE.onThrowing(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new IOException());
        ADVICE.afterMethod(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new MethodInvocationResult());
        List<MockSpan> spans = tracer.finishedSpans();
        Assert.assertEquals(1, spans.size());
        MockSpan span = spans.get(0);
        List<MockSpan.LogEntry> entries = span.logEntries();
        Map<String, ?> fields = entries.get(0).fields();
        Assert.assertEquals("error", fields.get(ErrorLogTagKeys.EVENT));
        Assert.assertEquals("java.io.IOException", fields.get(ErrorLogTagKeys.ERROR_KIND));
        Map<String, Object> tags = span.tags();
        Assert.assertSame("/ShardingSphere/executeSQL/", span.operationName());
        Assert.assertSame("mock.db", tags.get("db.instance"));
        Assert.assertSame("sql", tags.get("db.type"));
        Assert.assertSame("client", tags.get("span.kind"));
        Assert.assertSame("select 1", tags.get("db.statement"));
    }
    
}
