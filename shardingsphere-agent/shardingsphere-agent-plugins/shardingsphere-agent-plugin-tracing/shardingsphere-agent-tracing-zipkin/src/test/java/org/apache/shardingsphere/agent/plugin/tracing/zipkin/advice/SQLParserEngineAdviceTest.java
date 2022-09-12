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
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.advice.AbstractSQLParserEngineAdviceTest;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.collector.ZipkinCollector;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant.ZipkinConstants;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

public final class SQLParserEngineAdviceTest extends AbstractSQLParserEngineAdviceTest {
    
    @ClassRule
    public static final ZipkinCollector COLLECTOR = new ZipkinCollector();
    
    private static final String SQL_STMT = "select 1";
    
    private SQLParserEngineAdvice advice;
    
    private Span parentSpan;
    
    @Before
    @SneakyThrows
    public void setup() {
        parentSpan = Tracing.currentTracer().newTrace().name("parent").start();
        ExecutorDataMap.getValue().put(ZipkinConstants.ROOT_SPAN, parentSpan);
        advice = new SQLParserEngineAdvice();
    }
    
    @Test
    public void assertMethod() {
        advice.beforeMethod(getTargetObject(), null, new Object[]{SQL_STMT, true}, new MethodInvocationResult());
        advice.afterMethod(getTargetObject(), null, new Object[]{SQL_STMT, true}, new MethodInvocationResult());
        parentSpan.finish();
        zipkin2.Span span = COLLECTOR.pop();
        assertNotNull(span.parentId());
        Map<String, String> tags = span.tags();
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is(ZipkinConstants.DB_TYPE_VALUE));
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is(ZipkinConstants.COMPONENT_NAME));
    }
    
    @Test
    public void assertExceptionHandle() {
        advice.beforeMethod(getTargetObject(), null, new Object[]{SQL_STMT, true}, new MethodInvocationResult());
        advice.onThrowing(getTargetObject(), null, new Object[]{SQL_STMT, true}, new IOException());
        advice.afterMethod(getTargetObject(), null, new Object[]{SQL_STMT, true}, new MethodInvocationResult());
        parentSpan.finish();
        zipkin2.Span span = COLLECTOR.pop();
        assertNotNull(span.parentId());
        Map<String, String> tags = span.tags();
        assertThat(tags.get("error"), is("IOException"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is(ZipkinConstants.DB_TYPE_VALUE));
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is(ZipkinConstants.COMPONENT_NAME));
    }
}
