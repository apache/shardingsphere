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
import org.apache.shardingsphere.agent.metrics.api.constant.MethodNameConstant;
import org.apache.shardingsphere.agent.metrics.api.util.ReflectiveUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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
    @SuppressWarnings("unchecked")
    public void assertMethod() {
        when(channelRead.getName()).thenReturn(MethodNameConstant.CHANNEL_READ);
        when(channelActive.getName()).thenReturn(MethodNameConstant.CHANNEL_ACTIVE);
        when(channelInactive.getName()).thenReturn(MethodNameConstant.CHANNEL_INACTIVE);
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        channelHandlerAdvice.beforeMethod(targetObject, channelRead, new Object[]{}, new MethodInvocationResult());
        channelHandlerAdvice.beforeMethod(targetObject, channelActive, new Object[]{}, new MethodInvocationResult());
        channelHandlerAdvice.beforeMethod(targetObject, channelActive, new Object[]{}, new MethodInvocationResult());
        channelHandlerAdvice.beforeMethod(targetObject, channelInactive, new Object[]{}, new MethodInvocationResult());
        Map<String, DoubleAdder> doubleAdderMap = (Map<String, DoubleAdder>) ReflectiveUtil.getFieldValue(getFixturemetricsregister(), "COUNTER_MAP");
        DoubleAdder doubleAdder = doubleAdderMap.get("proxy_request_total");
        assertNotNull(doubleAdder);
        assertThat(doubleAdder.intValue(), is(1));
        Map<String, AtomicInteger> atomicIntegerMap = (Map<String, AtomicInteger>) ReflectiveUtil.getFieldValue(getFixturemetricsregister(), "GAUGE_MAP");
        assertThat(atomicIntegerMap.size(), is(1));
        AtomicInteger atomicInteger = atomicIntegerMap.get("proxy_connection_total");
        assertNotNull(atomicInteger);
        assertThat(atomicInteger.intValue(), is(1));
    }
}
