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

package org.apache.shardingsphere.agent.plugin.metrics.prometheus.exoprter.proxy;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricFamilyMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.impl.proxy.ProxyMetaDataInfoExporter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Prometheus proxy meta data information exporter.
 */
public final class PrometheusProxyMetaDataInfoExporter extends Collector {
    
    @Override
    public List<MetricFamilySamples> collect() {
        Optional<GaugeMetricFamilyMetricsCollector> collector = new ProxyMetaDataInfoExporter().export("Prometheus");
        return collector.<List<MetricFamilySamples>>map(optional -> Collections.singletonList((GaugeMetricFamily) optional.getRawMetricFamilyObject())).orElse(Collections.emptyList());
    }
}
