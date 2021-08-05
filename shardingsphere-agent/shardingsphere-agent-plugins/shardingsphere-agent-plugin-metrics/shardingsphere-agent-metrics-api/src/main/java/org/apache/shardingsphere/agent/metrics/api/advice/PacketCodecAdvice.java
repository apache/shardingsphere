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
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.metrics.api.MetricsPool;
import org.apache.shardingsphere.agent.metrics.api.constant.MetricIds;

import java.lang.reflect.Method;

/**
 * Packet codec advice.
 */
public final class PacketCodecAdvice implements InstanceMethodAroundAdvice {
    
    public static final String METHOD_ENCODE = "encode";
    
    public static final String METHOD_DECODE = "decode";
    
    static {
        MetricsPool.create(MetricIds.PROXY_REQUEST_BYTES);
        MetricsPool.create(MetricIds.PROXY_RESPONSE_BYTES);
    }
    
    @Override
    public void beforeMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        if (METHOD_DECODE.equals(method.getName())) {
            ByteBuf in = (ByteBuf) args[1];
            MetricsPool.get(MetricIds.PROXY_REQUEST_BYTES).ifPresent(m -> m.observe(in.readableBytes()));
        }
    }
    
    @Override
    public void afterMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        if (METHOD_ENCODE.equals(method.getName())) {
            ByteBuf out = (ByteBuf) args[2];
            MetricsPool.get(MetricIds.PROXY_RESPONSE_BYTES).ifPresent(m -> m.observe(out.readableBytes()));
        }
    }
}
