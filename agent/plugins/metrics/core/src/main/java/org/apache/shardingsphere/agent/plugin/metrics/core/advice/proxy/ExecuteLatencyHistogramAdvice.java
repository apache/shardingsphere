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
import org.apache.shardingsphere.agent.plugin.core.recorder.MethodTimeRecorder;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.HistogramMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Execute latency histogram advance for ShardingSphere-Proxy.
 */
public final class ExecuteLatencyHistogramAdvice implements InstanceMethodAdvice {
    
    private final MetricConfiguration config = new MetricConfiguration("proxy_execute_latency_millis",
            MetricCollectorType.HISTOGRAM, "Execute latency millis histogram of ShardingSphere-Proxy", Collections.emptyList(), Collections.singletonMap("buckets", getBuckets()));
    
    private final MethodTimeRecorder methodTimeRecorder = new MethodTimeRecorder(ExecuteLatencyHistogramAdvice.class);
    
    private Map<String, Object> getBuckets() {
        Map<String, Object> result = new HashMap<>(4, 1F);
        result.put("type", "exp");
        result.put("start", 1);
        result.put("factor", 2);
        result.put("count", 13);
        return result;
    }
    
    @Override
    public void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args, final String pluginType) {
        if (args[2] instanceof QueryCommandExecutor) {
            methodTimeRecorder.recordNow(method);
        }
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result, final String pluginType) {
        if (args[2] instanceof QueryCommandExecutor) {
            MetricsCollectorRegistry.<HistogramMetricsCollector>get(config, pluginType).observe(methodTimeRecorder.getElapsedTimeAndClean(method));
        }
    }
}
