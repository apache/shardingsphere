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

package org.apache.shardingsphere.agent.plugin.metrics.core.exporter.impl.proxy;

import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricFamilyMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.MetricsExporter;
import org.apache.shardingsphere.infra.state.instance.InstanceStateContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.util.Collections;
import java.util.Optional;

/**
 * Proxy state exporter.
 */
public final class ProxyStateExporter implements MetricsExporter {
    
    private final MetricConfiguration config = new MetricConfiguration("proxy_state",
            MetricCollectorType.GAUGE_METRIC_FAMILY, "State of ShardingSphere-Proxy. 0 is OK; 1 is CIRCUIT BREAK; 2 is LOCK", Collections.emptyList(), Collections.emptyMap());
    
    @Override
    public Optional<GaugeMetricFamilyMetricsCollector> export(final String pluginType) {
        if (null == ProxyContext.getInstance().getContextManager()) {
            return Optional.empty();
        }
        InstanceStateContext stateContext = ProxyContext.getInstance().getContextManager().getComputeNodeInstanceContext().getInstance().getState();
        GaugeMetricFamilyMetricsCollector result = MetricsCollectorRegistry.get(config, pluginType);
        result.cleanMetrics();
        result.addMetric(Collections.emptyList(), stateContext.getCurrentState().ordinal());
        return Optional.of(result);
    }
}
