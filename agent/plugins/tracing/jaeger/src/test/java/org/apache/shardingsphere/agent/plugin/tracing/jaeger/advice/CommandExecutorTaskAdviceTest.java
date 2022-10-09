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
import io.opentracing.mock.MockSpan.LogEntry;
import io.opentracing.tag.Tags;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.advice.AbstractCommandExecutorTaskAdviceTest;
import org.apache.shardingsphere.agent.plugin.tracing.jaeger.collector.JaegerCollector;
import org.apache.shardingsphere.agent.plugin.tracing.jaeger.constant.JaegerConstants;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class CommandExecutorTaskAdviceTest extends AbstractCommandExecutorTaskAdviceTest {
    
    @ClassRule
    public static final JaegerCollector COLLECTOR = new JaegerCollector();
    
    private static final CommandExecutorTaskAdvice ADVICE = new CommandExecutorTaskAdvice();
    
    private static final Map<String, Object> EXPECTED = new HashMap<>(2, 1);
    
    @BeforeClass
    public static void setup() {
        EXPECTED.put(Tags.COMPONENT.getKey(), JaegerConstants.COMPONENT_NAME);
        EXPECTED.put(JaegerConstants.ShardingSphereTags.CONNECTION_COUNT.getKey(), 0);
    }
    
    @Test
    public void assertMethod() {
        ADVICE.beforeMethod(getTargetObject(), null, new Object[]{}, new MethodInvocationResult());
        ADVICE.afterMethod(getTargetObject(), null, new Object[]{}, new MethodInvocationResult());
        List<MockSpan> spans = COLLECTOR.finishedSpans();
        assertThat(spans.size(), is(1));
        assertTrue(spans.get(0).logEntries().isEmpty());
        assertThat(spans.get(0).operationName(), is("/ShardingSphere/rootInvoke/"));
        assertThat(spans.get(0).tags(), is(EXPECTED));
    }
    
    @Test
    public void assertExceptionHandle() {
        ADVICE.beforeMethod(getTargetObject(), null, new Object[]{}, new MethodInvocationResult());
        ADVICE.onThrowing(getTargetObject(), null, new Object[]{}, new IOException());
        ADVICE.afterMethod(getTargetObject(), null, new Object[]{}, new MethodInvocationResult());
        List<MockSpan> spans = COLLECTOR.finishedSpans();
        assertThat(spans.size(), is(1));
        MockSpan span = spans.get(0);
        assertTrue((boolean) span.tags().get("error"));
        List<LogEntry> entries = span.logEntries();
        assertThat(entries.size(), is(1));
        Map<String, ?> fields = entries.get(0).fields();
        assertThat(fields.get("event"), is("error"));
        assertNull(fields.get("message"));
        assertThat(fields.get("error.kind"), is("java.io.IOException"));
        assertThat(span.operationName(), is("/ShardingSphere/rootInvoke/"));
        Map<Object, Object> map = new HashMap<>(EXPECTED);
        map.put(JaegerConstants.ErrorLogTagKeys.EVENT_ERROR_TYPE, true);
        assertThat(spans.get(0).tags(), is(map));
    }
}
