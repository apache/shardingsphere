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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricFamilyMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.MetricsExporter;
import org.apache.shardingsphere.proxy.Bootstrap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * JDK build information collector.
 */
@RequiredArgsConstructor
public final class JDKBuildInfoExporter implements MetricsExporter {
    
    private final MetricConfiguration config = new MetricConfiguration("jdk_build_info",
            MetricCollectorType.GAUGE_METRIC_FAMILY, "JDK build information", Arrays.asList("version", "name"), Collections.emptyMap());
    
    @Override
    public Optional<GaugeMetricFamilyMetricsCollector> export(final String pluginType) {
        GaugeMetricFamilyMetricsCollector result = MetricsCollectorRegistry.get(config, pluginType);
        addJDKBuildInfo(result, getClass().getPackage());
        try {
            Class.forName(Bootstrap.class.getCanonicalName());
            addJDKBuildInfo(result, Bootstrap.class.getPackage());
        } catch (final ClassNotFoundException ignored) {
        }
        return Optional.of(result);
    }
    
    private void addJDKBuildInfo(final GaugeMetricFamilyMetricsCollector collector, final Package pkg) {
        String version = null == pkg.getImplementationVersion() ? "unknown" : pkg.getImplementationVersion();
        String name = null == pkg.getImplementationTitle() ? "unknown" : pkg.getImplementationTitle();
        collector.addMetric(Arrays.asList(version, name), 1d);
    }
}
