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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.metrics.api.MetricsPool;
import org.apache.shardingsphere.agent.metrics.api.MetricsWrapper;
import org.apache.shardingsphere.agent.metrics.api.constant.MetricIds;

import java.lang.reflect.Method;

/**
 * Channel handler advice.
 */
@Slf4j
public final class ChannelHandlerAdvice implements InstanceMethodAroundAdvice {
    
    public static final String CHANNEL_READ = "channelRead";
    
    public static final String CHANNEL_ACTIVE = "channelActive";
    
    public static final String CHANNEL_INACTIVE = "channelInactive";
    
    static {
        MetricsPool.create(MetricIds.PROXY_REQUEST);
        MetricsPool.create(MetricIds.PROXY_COLLECTION);
    }
    
    @Override
    public void beforeMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        switch (method.getName()) {
            case CHANNEL_READ:
                MetricsPool.get(MetricIds.PROXY_REQUEST).ifPresent(MetricsWrapper::inc);
                break;
            case CHANNEL_ACTIVE:
                MetricsPool.get(MetricIds.PROXY_COLLECTION).ifPresent(MetricsWrapper::inc);
                break;
            case CHANNEL_INACTIVE:
                MetricsPool.get(MetricIds.PROXY_COLLECTION).ifPresent(MetricsWrapper::dec);
                break;
            default:
                break;
        }
    }
}
