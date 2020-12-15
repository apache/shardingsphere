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
 *
 */

package org.apache.shardingsphere.agent.metrics.bootstrap;

import java.lang.reflect.Method;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodInvocationResult;
import org.apache.shardingsphere.agent.core.plugin.advice.TargetObject;
import org.apache.shardingsphere.agent.metrics.api.reporter.MetricsReporter;

/**
 * Channel handler advice.
 */
public final class ChannelHandlerAdvice implements MethodAroundAdvice {
    
    private static final String REQUEST_TOTAL = "proxy_request_total";
    
    private static final String COLLECTION_TOTAL = "proxy_connection_total";
    
    static {
        MetricsReporter.registerCounter(REQUEST_TOTAL, "the shardingsphere proxy request total");
        MetricsReporter.registerGauge(COLLECTION_TOTAL, "the shardingsphere proxy connection total");
    }
    
    @Override
    public void beforeMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        collectMetrics(method.getName());
    }
    
    private void collectMetrics(final String methodName) {
        if (MethodNameConstant.CHANNEL_READ.equals(methodName)) {
            MetricsReporter.counterIncrement(REQUEST_TOTAL);
        } else if (MethodNameConstant.CHANNEL_ACTIVE.equals(methodName)) {
            MetricsReporter.gaugeIncrement(COLLECTION_TOTAL);
        } else if (MethodNameConstant.CHANNEL_INACTIVE.equals(methodName)) {
            MetricsReporter.gaugeDecrement(COLLECTION_TOTAL);
        }
    }
}
