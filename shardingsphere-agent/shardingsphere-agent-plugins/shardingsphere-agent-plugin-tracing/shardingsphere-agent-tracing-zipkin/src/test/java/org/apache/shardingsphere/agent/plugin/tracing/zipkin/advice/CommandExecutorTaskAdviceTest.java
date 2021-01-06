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

package org.apache.shardingsphere.agent.plugin.tracing.zipkin.advice;

import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.api.advice.TargetObject;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant.ZipkinConstants;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import zipkin2.Span;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class CommandExecutorTaskAdviceTest extends AdviceBaseTest {
    
    private static Method executeCommandMethod;
    
    private CommandExecutorTaskAdvice advice;
    
    private TargetObject targetObject;
    
    @BeforeClass
    public static void setup() throws NoSuchMethodException {
        prepare("org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask");
        executeCommandMethod = CommandExecutorTask.class.getDeclaredMethod("executeCommand", ChannelHandlerContext.class, PacketPayload.class, BackendConnection.class);
    }
    
    @Before
    @SneakyThrows
    @SuppressWarnings("all")
    public void before() {
        advice = new CommandExecutorTaskAdvice();
        Object executorTask = new CommandExecutorTask(null, new BackendConnection(TransactionType.BASE), null, null);
        targetObject = (TargetObject) executorTask;
    }
    
    @Test
    public void testMethod() {
        advice.beforeMethod(targetObject, executeCommandMethod, new Object[]{}, new MethodInvocationResult());
        advice.afterMethod(targetObject, executeCommandMethod, new Object[]{}, new MethodInvocationResult());
        Span span = SPANS.poll();
        assertNotNull(span);
        Map<String, String> tags = span.tags();
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is(ZipkinConstants.DB_TYPE_VALUE));
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is(ZipkinConstants.COMPONENT_NAME));
        assertThat(tags.get(ZipkinConstants.Tags.CONNECTION_COUNT), is("0"));
        assertThat(span.name(), is("/ShardingSphere/rootInvoke/".toLowerCase()));
    }
    
    @Test
    public void testExceptionHandle() {
        advice.beforeMethod(targetObject, executeCommandMethod, new Object[]{}, new MethodInvocationResult());
        advice.onThrowing(targetObject, executeCommandMethod, new Object[]{}, new IOException());
        advice.afterMethod(targetObject, executeCommandMethod, new Object[]{}, new MethodInvocationResult());
        Span span = SPANS.poll();
        assertNotNull(span);
        Map<String, String> tags = span.tags();
        assertThat(tags.get("error"), is("IOException"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is(ZipkinConstants.DB_TYPE_VALUE));
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is(ZipkinConstants.COMPONENT_NAME));
        assertThat(tags.get(ZipkinConstants.Tags.CONNECTION_COUNT), is("0"));
        assertThat(span.name(), is("/ShardingSphere/rootInvoke/".toLowerCase()));
    }
    
    @After
    public void cleanup() {
        SPANS.clear();
    }
}
