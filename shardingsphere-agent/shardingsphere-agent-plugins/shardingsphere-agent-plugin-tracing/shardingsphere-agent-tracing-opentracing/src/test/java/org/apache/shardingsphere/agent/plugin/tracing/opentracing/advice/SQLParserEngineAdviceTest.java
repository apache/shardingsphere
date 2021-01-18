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
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.opentracing.constant.ErrorLogTagKeys;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.internal.util.reflection.FieldReader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SQLParserEngineAdviceTest {
    
    private static final SQLParserEngineAdvice ADVICE = new SQLParserEngineAdvice();
    
    private static MockTracer tracer;
    
    private static Method parserMethod;
    
    @BeforeClass
    public static void setup() throws NoSuchMethodException, NoSuchFieldException {
        if (!GlobalTracer.isRegistered()) {
            GlobalTracer.register(new MockTracer());
        }
        FieldReader fieldReader = new FieldReader(GlobalTracer.get(), GlobalTracer.class.getDeclaredField("tracer"));
        tracer = (MockTracer) fieldReader.read();
        parserMethod = ShardingSphereSQLParserEngine.class.getDeclaredMethod("parse", String.class, boolean.class);
    }
    
    @Before
    public void reset() {
        tracer.reset();
    }
    
    @Test
    public void assertMethod() {
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        ADVICE.beforeMethod(targetObject, parserMethod, new Object[]{"select 1"}, new MethodInvocationResult());
        ADVICE.afterMethod(targetObject, parserMethod, new Object[]{}, new MethodInvocationResult());
        List<MockSpan> spans = tracer.finishedSpans();
        assertThat(spans.size(), is(1));
        assertThat(spans.get(0).logEntries().size(), is(0));
        assertThat(spans.get(0).operationName(), is("/ShardingSphere/parseSQL/"));
    }
    
    @Test
    public void assertExceptionHandle() {
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        ADVICE.beforeMethod(targetObject, parserMethod, new Object[]{"select 1"}, new MethodInvocationResult());
        ADVICE.onThrowing(targetObject, parserMethod, new Object[]{}, new IOException());
        ADVICE.afterMethod(targetObject, parserMethod, new Object[]{}, new MethodInvocationResult());
        List<MockSpan> spans = tracer.finishedSpans();
        assertThat(spans.size(), is(1));
        MockSpan span = spans.get(0);
        assertThat(span.tags().get("error"), is(true));
        List<MockSpan.LogEntry> entries = span.logEntries();
        assertThat(entries.size(), is(1));
        Map<String, ?> fields = entries.get(0).fields();
        assertThat(fields.get(ErrorLogTagKeys.EVENT), is("error"));
        assertThat(fields.get(ErrorLogTagKeys.ERROR_KIND), is("java.io.IOException"));
        assertThat(span.operationName(), is("/ShardingSphere/parseSQL/"));
    }
}
