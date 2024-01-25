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
import org.apache.shardingsphere.agent.plugin.core.advice.AbstractInstanceMethodAdvice;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * Current connections count advice for ShardingSphere-Proxy.
 */
public final class CurrentConnectionsCountAdvice extends AbstractInstanceMethodAdvice {
    
    private final MetricConfiguration config = new MetricConfiguration("proxy_current_connections",
            MetricCollectorType.GAUGE, "Current connections of ShardingSphere-Proxy", Collections.emptyList(), Collections.emptyMap());
    
    @Override
    public void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args, final String pluginType) {
        switch (method.getName()) {
            case "channelActive":
                MetricsCollectorRegistry.<GaugeMetricsCollector>get(config, pluginType).inc();
                break;
            case "channelInactive":
                MetricsCollectorRegistry.<GaugeMetricsCollector>get(config, pluginType).dec();
                break;
            default:
                break;
        }
    }
}
