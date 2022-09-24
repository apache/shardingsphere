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

package org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.advice;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.advice.AbstractJDBCExecutorCallbackAdviceTest;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.collector.OpenTelemetryCollector;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class JDBCExecutorCallbackAdviceTest extends AbstractJDBCExecutorCallbackAdviceTest {
    
    @ClassRule
    public static final OpenTelemetryCollector COLLECTOR = new OpenTelemetryCollector();
    
    private JDBCExecutorCallbackAdvice advice;
    
    @Before
    public void setup() {
        advice = new JDBCExecutorCallbackAdvice();
    }
    
    @Test
    public void assertMethod() {
        advice.beforeMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new MethodInvocationResult());
        advice.afterMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new MethodInvocationResult());
        List<SpanData> spanItems = COLLECTOR.getSpanItems();
        assertThat(spanItems.size(), is(1));
        SpanData spanData = spanItems.get(0);
        assertThat(spanData.getName(), is("/ShardingSphere/executeSQL/"));
        Attributes attributes = spanData.getAttributes();
        assertThat(attributes.get(AttributeKey.stringKey(OpenTelemetryConstants.COMPONENT)), is(OpenTelemetryConstants.COMPONENT_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(OpenTelemetryConstants.DB_TYPE)), is(OpenTelemetryConstants.DB_TYPE_VALUE));
        assertThat(attributes.get(AttributeKey.stringKey(OpenTelemetryConstants.DB_INSTANCE)), is("mock.db"));
        assertThat(attributes.get(AttributeKey.stringKey(OpenTelemetryConstants.DB_STATEMENT)), is("select 1"));
    }
    
    @Test
    public void assertExceptionHandle() {
        advice.beforeMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new MethodInvocationResult());
        advice.onThrowing(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new IOException());
        advice.afterMethod(getTargetObject(), null, new Object[]{getExecutionUnit(), false, getExtraMap()}, new MethodInvocationResult());
        List<SpanData> spanItems = COLLECTOR.getSpanItems();
        assertThat(spanItems.size(), is(1));
        SpanData spanData = spanItems.get(0);
        assertThat(spanData.getName(), is("/ShardingSphere/executeSQL/"));
        assertThat(spanData.getStatus().getStatusCode(), is(StatusCode.ERROR));
        Attributes attributes = spanData.getAttributes();
        assertThat(attributes.get(AttributeKey.stringKey(OpenTelemetryConstants.COMPONENT)), is(OpenTelemetryConstants.COMPONENT_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(OpenTelemetryConstants.DB_TYPE)), is(OpenTelemetryConstants.DB_TYPE_VALUE));
        assertThat(attributes.get(AttributeKey.stringKey(OpenTelemetryConstants.DB_INSTANCE)), is("mock.db"));
        assertThat(attributes.get(AttributeKey.stringKey(OpenTelemetryConstants.DB_STATEMENT)), is("select 1"));
    }
}
