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
import org.apache.shardingsphere.agent.plugin.tracing.advice.AbstractSQLParserEngineAdviceTest;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.collector.ZipkinCollector;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant.ZipkinConstants;
import org.apache.shardingsphere.agent.plugin.tracing.core.RootSpanContext;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public final class ZipkinSQLParserEngineAdviceTest extends AbstractSQLParserEngineAdviceTest {
    
    @ClassRule
    public static final ZipkinCollector COLLECTOR = new ZipkinCollector();
    
    private static final String SQL_STATEMENT = "select 1";
    
    private ZipkinSQLParserEngineAdvice advice;
    
    @Before
    public void setup() {
        RootSpanContext.set(Tracing.currentTracer().newTrace().name("parent").start());
        advice = new ZipkinSQLParserEngineAdvice();
    }
    
    @Test
    public void assertMethod() {
        advice.beforeMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, "Zipkin");
        advice.afterMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, null, "Zipkin");
        RootSpanContext.<Span>get().finish();
        zipkin2.Span span = COLLECTOR.pop();
        assertNotNull(span.parentId());
        Map<String, String> tags = span.tags();
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is(ZipkinConstants.DB_TYPE_VALUE));
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is(ZipkinConstants.COMPONENT_NAME));
    }
    
    @Test
    public void assertExceptionHandle() {
        advice.beforeMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, "Zipkin");
        advice.onThrowing(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, new IOException(), "Zipkin");
        advice.afterMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, null, "Zipkin");
        RootSpanContext.<Span>get().finish();
        zipkin2.Span span = COLLECTOR.pop();
        assertNotNull(span.parentId());
        Map<String, String> tags = span.tags();
        assertThat(tags.get("error"), is("IOException"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is(ZipkinConstants.DB_TYPE_VALUE));
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is(ZipkinConstants.COMPONENT_NAME));
    }
}
