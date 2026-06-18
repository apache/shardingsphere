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

package org.apache.shardingsphere.agent.plugin.metrics.core.exporter.impl;

import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricFamilyMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.MetricsExporter;
import org.apache.shardingsphere.infra.version.ShardingSphereVersion;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * Build information exporter.
 */
public final class BuildInfoExporter implements MetricsExporter {
    
    private final MetricConfiguration config = new MetricConfiguration("build_info",
            MetricCollectorType.GAUGE_METRIC_FAMILY, "Build information", Arrays.asList("name", "version"), Collections.emptyMap());
    
    @Override
    public Optional<GaugeMetricFamilyMetricsCollector> export(final String pluginType) {
        GaugeMetricFamilyMetricsCollector result = MetricsCollectorRegistry.get(config, pluginType);
        result.cleanMetrics();
        result.addMetric(Arrays.asList("ShardingSphere", ShardingSphereVersion.VERSION), 1D);
        return Optional.of(result);
    }
}
