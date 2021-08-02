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

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.metrics.api.MetricsPool;
import org.apache.shardingsphere.agent.metrics.api.constant.MetricIds;
import org.apache.shardingsphere.agent.metrics.api.fixture.FixtureWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PacketCodecAdviceTest extends MetricsAdviceBaseTest {
    
    private final PacketCodecAdvice advice = new PacketCodecAdvice();
    
    @Mock
    private Method encode;
    
    @Mock
    private Method decode;
    
    @Mock
    private ByteBuf buf;
    
    @Test
    public void assertResponse() {
        when(encode.getName()).thenReturn(PacketCodecAdvice.METHOD_ENCODE);
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        when(buf.readableBytes()).thenReturn(100);
        advice.afterMethod(targetObject, encode, new Object[]{new Object(), new Object(), buf}, new MethodInvocationResult());
        FixtureWrapper wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PROXY_RESPONSE_BYTES).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(100d));
    }
    
    @Test
    public void assertRequest() {
        when(decode.getName()).thenReturn(PacketCodecAdvice.METHOD_DECODE);
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        when(buf.readableBytes()).thenReturn(200);
        advice.beforeMethod(targetObject, decode, new Object[]{new Object(), buf}, new MethodInvocationResult());
        FixtureWrapper wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PROXY_REQUEST_BYTES).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(200d));
    }
}
