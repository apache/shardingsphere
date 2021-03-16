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

import io.opentracing.mock.MockSpan;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.advice.AbstractJDBCExecutorCallbackAdviceTest;
import org.apache.shardingsphere.agent.plugin.tracing.jaeger.constant.JaegerConstants;
import org.apache.shardingsphere.agent.plugin.tracing.jaeger.collector.JaegerCollector;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class JDBCExecutorCallbackAdviceTest extends AbstractJDBCExecutorCallbackAdviceTest {
    
    @ClassRule
    public static final JaegerCollector COLLECTOR = new JaegerCollector();

    private static final JDBCExecutorCallbackAdvice ADVICE = new JDBCExecutorCallbackAdvice();
    
    @Before
    public void setup() {
        getExtraMap().put(JaegerConstants.ROOT_SPAN, null);
    }
    
    @Test
    public void assertMethod() {
        ADVICE.beforeMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new MethodInvocationResult());
        ADVICE.afterMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new MethodInvocationResult());
        List<MockSpan> spans = COLLECTOR.finishedSpans();
        assertThat(spans.size(), is(1));
        MockSpan span = spans.get(0);
        Map<String, Object> tags = span.tags();
        assertThat(spans.get(0).logEntries().size(), is(0));
        assertThat(span.operationName(), is("/ShardingSphere/executeSQL/"));
        assertThat(tags.get("db.instance"), is("mock.db"));
        assertThat(tags.get("db.type"), is(JaegerConstants.DB_TYPE_VALUE));
        assertThat(tags.get("span.kind"), is("client"));
        assertThat(tags.get("db.statement"), is("select 1"));
    }
    
    @Test
    public void assertExceptionHandle() {
        ADVICE.beforeMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new MethodInvocationResult());
        ADVICE.onThrowing(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new IOException());
        ADVICE.afterMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new MethodInvocationResult());
        List<MockSpan> spans = COLLECTOR.finishedSpans();
        assertThat(spans.size(), is(1));
        MockSpan span = spans.get(0);
        List<MockSpan.LogEntry> entries = span.logEntries();
        Map<String, ?> fields = entries.get(0).fields();
        assertThat(fields.get(JaegerConstants.ErrorLogTagKeys.EVENT), is("error"));
        assertThat(fields.get(JaegerConstants.ErrorLogTagKeys.ERROR_KIND), is("java.io.IOException"));
        Map<String, Object> tags = span.tags();
        assertThat(span.operationName(), is("/ShardingSphere/executeSQL/"));
        assertThat(tags.get("db.instance"), is("mock.db"));
        assertThat(tags.get("db.type"), is(JaegerConstants.DB_TYPE_VALUE));
        assertThat(tags.get("span.kind"), is("client"));
        assertThat(tags.get("db.statement"), is("select 1"));
    }
}
