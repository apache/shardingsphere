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

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.DefaultAttributeMap;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockSpan.LogEntry;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.tracing.AgentExtension;
import org.apache.shardingsphere.agent.plugin.tracing.core.constant.AttributeConstants;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.configuration.plugins.Plugins;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({AgentExtension.class, AutoMockExtension.class})
@StaticMockSettings(ProxyContext.class)
public final class OpenTracingCommandExecutorTaskAdviceTest {
    
    private static final OpenTracingCommandExecutorTaskAdvice ADVICE = new OpenTracingCommandExecutorTaskAdvice();
    
    private static MockTracer tracer;
    
    private static Method executeCommandMethod;
    
    private TargetAdviceObject targetObject;
    
    @BeforeAll
    public static void setup() throws ReflectiveOperationException {
        if (!GlobalTracer.isRegistered()) {
            GlobalTracer.register(new MockTracer());
        }
        tracer = (MockTracer) Plugins.getMemberAccessor().get(GlobalTracer.class.getDeclaredField("tracer"), GlobalTracer.get());
        executeCommandMethod = CommandExecutorTask.class.getDeclaredMethod("executeCommand", ChannelHandlerContext.class, PacketPayload.class);
    }
    
    @BeforeEach
    public void reset() {
        tracer.reset();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(mock(ContextManager.class, RETURNS_DEEP_STUBS));
        ConnectionSession connectionSession = new ConnectionSession(mock(MySQLDatabaseType.class), TransactionType.BASE, new DefaultAttributeMap());
        Object executorTask = new CommandExecutorTask(null, connectionSession, null, null);
        targetObject = (TargetAdviceObject) executorTask;
    }
    
    @Test
    public void assertMethod() {
        ADVICE.beforeMethod(targetObject, executeCommandMethod, new Object[]{}, "OpenTracing");
        ADVICE.afterMethod(targetObject, executeCommandMethod, new Object[]{}, null, "OpenTracing");
        List<MockSpan> spans = tracer.finishedSpans();
        assertThat(spans.size(), is(1));
        assertTrue(spans.get(0).logEntries().isEmpty());
        assertThat(spans.get(0).operationName(), is("/ShardingSphere/rootInvoke/"));
        assertThat(spans.get(0).tags().get(AttributeConstants.COMPONENT), is(AttributeConstants.COMPONENT_NAME));
        assertThat(spans.get(0).tags().get(AttributeConstants.SPAN_KIND), is(AttributeConstants.SPAN_KIND_CLIENT));
    }
    
    @Test
    public void assertExceptionHandle() {
        ADVICE.beforeMethod(targetObject, executeCommandMethod, new Object[]{}, "OpenTracing");
        ADVICE.onThrowing(targetObject, executeCommandMethod, new Object[]{}, new IOException(), "OpenTracing");
        List<MockSpan> spans = tracer.finishedSpans();
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
    }
}
