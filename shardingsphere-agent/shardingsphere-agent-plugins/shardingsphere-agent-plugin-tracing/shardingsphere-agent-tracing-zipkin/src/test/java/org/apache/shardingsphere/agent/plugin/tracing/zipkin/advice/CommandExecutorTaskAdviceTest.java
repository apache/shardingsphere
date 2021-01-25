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
import org.apache.shardingsphere.agent.plugin.tracing.advice.AbstractCommandExecutorTaskAdviceTest;
import org.apache.shardingsphere.agent.plugin.tracing.rule.ZipkinCollector;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant.ZipkinConstants;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import zipkin2.Span;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class CommandExecutorTaskAdviceTest extends AbstractCommandExecutorTaskAdviceTest {
    
    @ClassRule
    public static ZipkinCollector collector = new ZipkinCollector();
    
    private CommandExecutorTaskAdvice advice;
    
    @Before
    public void setup() {
        advice = new CommandExecutorTaskAdvice();
    }
    
    @Test
    public void assertMethod() {
        advice.beforeMethod(getTargetObject(), null, new Object[]{}, new MethodInvocationResult());
        advice.afterMethod(getTargetObject(), null, new Object[]{}, new MethodInvocationResult());
        Span span = collector.pop();
        assertNotNull(span);
        Map<String, String> tags = span.tags();
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is(ZipkinConstants.DB_TYPE_VALUE));
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is(ZipkinConstants.COMPONENT_NAME));
        assertThat(tags.get(ZipkinConstants.Tags.CONNECTION_COUNT), is("0"));
        assertThat(span.name(), is("/ShardingSphere/rootInvoke/".toLowerCase()));
    }
    
    @Test
    public void assertExceptionHandle() {
        advice.beforeMethod(getTargetObject(), null, new Object[]{}, new MethodInvocationResult());
        advice.onThrowing(getTargetObject(), null, new Object[]{}, new IOException());
        advice.afterMethod(getTargetObject(), null, new Object[]{}, new MethodInvocationResult());
        Span span = collector.pop();
        assertNotNull(span);
        Map<String, String> tags = span.tags();
        assertThat(tags.get("error"), is("IOException"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is(ZipkinConstants.DB_TYPE_VALUE));
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is(ZipkinConstants.COMPONENT_NAME));
        assertThat(tags.get(ZipkinConstants.Tags.CONNECTION_COUNT), is("0"));
        assertThat(span.name(), is("/ShardingSphere/rootInvoke/".toLowerCase()));
    }
}
