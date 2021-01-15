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

import brave.Span;
import brave.Tracing;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant.ZipkinConstants;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class SQLParserEngineAdviceTest extends AdviceBaseTest {
    
    private static final String SQL_STMT = "select 1";
    
    private static Method parseMethod;
    
    private SQLParserEngineAdvice advice;
    
    private AdviceTargetObject targetObject;
    
    private Object attachment;
    
    private Span parentSpan;
    
    @BeforeClass
    @SneakyThrows
    public static void setup() {
        prepare("org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine");
        parseMethod = ShardingSphereSQLParserEngine.class.getDeclaredMethod("parse", String.class, boolean.class);
    }
    
    @Before
    @SneakyThrows
    @SuppressWarnings("all")
    public void before() {
        parentSpan = Tracing.currentTracer().newTrace().name("parent").start();
        ExecutorDataMap.getValue().put(ZipkinConstants.ROOT_SPAN, parentSpan);
        Object parserEngine = mock(ShardingSphereSQLParserEngine.class, invocation -> {
            switch (invocation.getMethod().getName()) {
                case "getAttachment":
                    return attachment;
                case "setAttachment":
                    attachment = invocation.getArguments()[0];
                    return null;
                default:
                    return invocation.callRealMethod();
            }
        });
        targetObject = (AdviceTargetObject) parserEngine;
        advice = new SQLParserEngineAdvice();
    }
    
    @Test
    public void testMethod() {
        advice.beforeMethod(targetObject, parseMethod, new Object[]{SQL_STMT, true}, new MethodInvocationResult());
        advice.afterMethod(targetObject, parseMethod, new Object[]{SQL_STMT, true}, new MethodInvocationResult());
        parentSpan.finish();
        zipkin2.Span span = SPANS.pollFirst();
        assertNotNull(span);
        assertNotNull(span.parentId());
        Map<String, String> tags = span.tags();
        assertNotNull(tags);
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is(ZipkinConstants.DB_TYPE_VALUE));
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is(ZipkinConstants.COMPONENT_NAME));
    }
    
    @Test
    public void testExceptionHandle() {
        advice.beforeMethod(targetObject, parseMethod, new Object[]{SQL_STMT, true}, new MethodInvocationResult());
        advice.onThrowing(targetObject, parseMethod, new Object[]{SQL_STMT, true}, new IOException());
        advice.afterMethod(targetObject, parseMethod, new Object[]{SQL_STMT, true}, new MethodInvocationResult());
        // ensure the parent span(mock) finished
        parentSpan.finish();
        zipkin2.Span span = SPANS.pollFirst();
        assertNotNull(span);
        assertNotNull(span.parentId());
        Map<String, String> tags = span.tags();
        assertNotNull(tags);
        assertThat(tags.get("error"), is("IOException"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is(ZipkinConstants.DB_TYPE_VALUE));
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is(ZipkinConstants.COMPONENT_NAME));
    }
    
    @After
    public void cleanup() {
        SPANS.clear();
    }
}
