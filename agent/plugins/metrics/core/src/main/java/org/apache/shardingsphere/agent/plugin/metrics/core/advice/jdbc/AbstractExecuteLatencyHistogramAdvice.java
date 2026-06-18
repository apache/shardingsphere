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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice.jdbc;

import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.core.advice.AbstractInstanceMethodAdvice;
import org.apache.shardingsphere.agent.plugin.core.recorder.MethodTimeRecorder;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.HistogramMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.util.HistogramBucketUtils;

import java.util.Collections;

/**
 * Abstract execute latency histogram advice for ShardingSphere-JDBC.
 */
public abstract class AbstractExecuteLatencyHistogramAdvice extends AbstractInstanceMethodAdvice {
    
    private final MetricConfiguration config = new MetricConfiguration("jdbc_statement_execute_latency_millis", MetricCollectorType.HISTOGRAM,
            "Statement execute latency millis histogram", Collections.singletonMap("buckets", HistogramBucketUtils.getBucketsMap()));
    
    @Override
    public void beforeMethod(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final String pluginType) {
        getMethodTimeRecorder().recordNow(method);
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final Object result, final String pluginType) {
        MetricsCollectorRegistry.<HistogramMetricsCollector>get(config, pluginType).observe(getMethodTimeRecorder().getElapsedTimeAndClean(method));
    }
    
    protected abstract MethodTimeRecorder getMethodTimeRecorder();
}
