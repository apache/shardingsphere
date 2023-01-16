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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice.proxy;

import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.api.advice.type.InstanceMethodAdvice;
import org.apache.shardingsphere.agent.plugin.core.recorder.AdviceRecordPointMark;
import org.apache.shardingsphere.agent.plugin.core.recorder.TimeRecorder;
import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsWrapperRegistry;

import java.lang.reflect.Method;

/**
 * Execute latency histogram advance for ShardingSphere-Proxy.
 */
public final class ExecuteLatencyHistogramAdvice implements InstanceMethodAdvice {
    
    private static final String PROXY_EXECUTE_LATENCY_MILLIS_METRIC_KEY = "proxy_execute_latency_millis";
    
    @Override
    public void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args) {
        TimeRecorder.record(new AdviceRecordPointMark(ExecuteLatencyHistogramAdvice.class, method));
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result) {
        MetricsWrapperRegistry.get(PROXY_EXECUTE_LATENCY_MILLIS_METRIC_KEY)
                .observe(TimeRecorder.getElapsedTimeAndClean(new AdviceRecordPointMark(ExecuteLatencyHistogramAdvice.class, method)));
    }
}
