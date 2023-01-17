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

package org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.business;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.PrometheusCollectorFactory;
import org.apache.shardingsphere.proxy.Bootstrap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Build information collector.
 */
@RequiredArgsConstructor
public final class BuildInfoCollector extends Collector {
    
    private static final String BUILD_INFO_METRIC_KEY = "build_info";
    
    private static final PrometheusCollectorFactory FACTORY = new PrometheusCollectorFactory();
    
    private final boolean isEnhancedForProxy;
    
    @Override
    public List<MetricFamilySamples> collect() {
        GaugeMetricFamily artifactInfo = FACTORY.createGaugeMetricFamily(BUILD_INFO_METRIC_KEY);
        addMetric(artifactInfo, getClass().getPackage());
        if (isEnhancedForProxy) {
            addMetric(artifactInfo, Bootstrap.class.getPackage());
        }
        return Collections.singletonList(artifactInfo);
    }
    
    private void addMetric(final GaugeMetricFamily artifactInfo, final Package pkg) {
        String version = null == pkg.getImplementationVersion() ? "unknown" : pkg.getImplementationVersion();
        String name = null == pkg.getImplementationTitle() ? "unknown" : pkg.getImplementationTitle();
        artifactInfo.addMetric(Arrays.asList(version, name), 1L);
    }
}
