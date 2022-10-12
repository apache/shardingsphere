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

package org.apache.shardingsphere.agent.metrics.api.advice;

import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.metrics.api.MetricsPool;
import org.apache.shardingsphere.agent.metrics.api.constant.MetricIds;
import org.apache.shardingsphere.agent.metrics.api.fixture.FixtureWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ChannelHandlerAdviceTest extends MetricsAdviceBaseTest {
    
    private final ChannelHandlerAdvice channelHandlerAdvice = new ChannelHandlerAdvice();
    
    @Mock
    private Method channelRead;
    
    @Mock
    private Method channelActive;
    
    @Mock
    private Method channelInactive;
    
    @Test
    public void assertMethod() {
        when(channelRead.getName()).thenReturn(ChannelHandlerAdvice.CHANNEL_READ);
        when(channelActive.getName()).thenReturn(ChannelHandlerAdvice.CHANNEL_ACTIVE);
        when(channelInactive.getName()).thenReturn(ChannelHandlerAdvice.CHANNEL_INACTIVE);
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        channelHandlerAdvice.beforeMethod(targetObject, channelRead, new Object[]{}, new MethodInvocationResult());
        channelHandlerAdvice.beforeMethod(targetObject, channelActive, new Object[]{}, new MethodInvocationResult());
        channelHandlerAdvice.beforeMethod(targetObject, channelActive, new Object[]{}, new MethodInvocationResult());
        channelHandlerAdvice.beforeMethod(targetObject, channelInactive, new Object[]{}, new MethodInvocationResult());
        FixtureWrapper requestWrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PROXY_REQUEST).get();
        assertTrue(MetricsPool.get(MetricIds.PROXY_REQUEST).isPresent());
        assertThat(requestWrapper.getFixtureValue(), is(1.0));
        FixtureWrapper connectionWrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PROXY_COLLECTION).get();
        assertTrue(MetricsPool.get(MetricIds.PROXY_COLLECTION).isPresent());
        assertThat(connectionWrapper.getFixtureValue(), is(1.0));
    }
}
