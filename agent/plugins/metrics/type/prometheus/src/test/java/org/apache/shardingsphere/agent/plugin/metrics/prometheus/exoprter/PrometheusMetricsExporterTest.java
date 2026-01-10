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

package org.apache.shardingsphere.agent.plugin.metrics.prometheus.exoprter;

import io.prometheus.client.GaugeMetricFamily;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricFamilyMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.MetricsExporter;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrometheusMetricsExporterTest {
    
    @Test
    void assertCollectWithAbsentMetricsExporter() {
        MetricsExporter exporter = mock(MetricsExporter.class);
        when(exporter.export("Prometheus")).thenReturn(Optional.empty());
        assertTrue(new PrometheusMetricsExporter(exporter).collect().isEmpty());
    }
    
    @Test
    void assertCollectWithPresentMetricsExporter() {
        MetricsExporter exporter = mock(MetricsExporter.class);
        GaugeMetricFamilyMetricsCollector metricsCollector = mock(GaugeMetricFamilyMetricsCollector.class);
        GaugeMetricFamily expectedMetricFamily = new GaugeMetricFamily("present_metric", "help", Collections.emptyList());
        when(metricsCollector.getRawMetricFamilyObject()).thenReturn(expectedMetricFamily);
        when(exporter.export("Prometheus")).thenReturn(Optional.of(metricsCollector));
        GaugeMetricFamily actualMetricFamily = (GaugeMetricFamily) new PrometheusMetricsExporter(exporter).collect().get(0);
        assertThat(actualMetricFamily, is(expectedMetricFamily));
    }
    
    @Test
    void assertCollectWithException() {
        MetricsExporter exporter = mock(MetricsExporter.class);
        when(exporter.export("Prometheus")).thenThrow(IllegalStateException.class);
        assertTrue(new PrometheusMetricsExporter(exporter).collect().isEmpty());
    }
}
