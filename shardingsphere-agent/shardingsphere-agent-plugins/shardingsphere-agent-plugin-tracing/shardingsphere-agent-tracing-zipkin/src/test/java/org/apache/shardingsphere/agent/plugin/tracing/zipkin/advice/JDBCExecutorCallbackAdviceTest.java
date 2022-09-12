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

import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.advice.AbstractJDBCExecutorCallbackAdviceTest;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.collector.ZipkinCollector;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant.ZipkinConstants;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import zipkin2.Span;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;

public final class JDBCExecutorCallbackAdviceTest extends AbstractJDBCExecutorCallbackAdviceTest {
    
    @ClassRule
    public static final ZipkinCollector COLLECTOR = new ZipkinCollector();
    
    private JDBCExecutorCallbackAdvice advice;
    
    @Before
    public void setup() {
        getExtraMap().put(ZipkinConstants.ROOT_SPAN, null);
        advice = new JDBCExecutorCallbackAdvice();
    }
    
    @Test
    public void assertMethod() {
        advice.beforeMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new MethodInvocationResult());
        advice.afterMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new MethodInvocationResult());
        Span span = COLLECTOR.pop();
        assertThat(span.name(), is("/ShardingSphere/executeSQL/".toLowerCase()));
        Map<String, String> tags = span.tags();
        assertFalse(tags == null || tags.isEmpty());
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is("shardingsphere"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_INSTANCE), is("mock.db"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_STATEMENT), is("select 1"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is("shardingsphere-proxy"));
        assertThat(tags.get(ZipkinConstants.Tags.PEER_HOSTNAME), is("mock.host"));
        assertThat(tags.get(ZipkinConstants.Tags.PEER_PORT), is("1000"));
    }
    
    @Test
    public void assertExceptionHandle() {
        advice.beforeMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new MethodInvocationResult());
        advice.onThrowing(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new IOException());
        advice.afterMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new MethodInvocationResult());
        Span span = COLLECTOR.pop();
        assertThat(span.name(), is("/ShardingSphere/executeSQL/".toLowerCase()));
        Map<String, String> tags = span.tags();
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is("shardingsphere"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_INSTANCE), is("mock.db"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_STATEMENT), is("select 1"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is("shardingsphere-proxy"));
        assertThat(tags.get(ZipkinConstants.Tags.PEER_HOSTNAME), is("mock.host"));
        assertThat(tags.get(ZipkinConstants.Tags.PEER_PORT), is("1000"));
        assertThat(tags.get("error"), is("IOException"));
    }
}
