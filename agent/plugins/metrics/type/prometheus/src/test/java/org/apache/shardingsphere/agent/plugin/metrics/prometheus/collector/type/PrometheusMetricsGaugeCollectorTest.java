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

package org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.type;

import io.prometheus.client.Gauge;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PrometheusMetricsGaugeCollectorTest {
    
    @Test
    void assertInc() throws ReflectiveOperationException {
        PrometheusMetricsGaugeCollector collector = new PrometheusMetricsGaugeCollector(new MetricConfiguration("gauge_inc",
                MetricCollectorType.GAUGE, "foo_help", Collections.emptyList(), Collections.emptyMap()));
        collector.inc();
        Gauge gauge = (Gauge) Plugins.getMemberAccessor().get(PrometheusMetricsGaugeCollector.class.getDeclaredField("gauge"), collector);
        assertThat(gauge.get(), is(1D));
    }
    
    @Test
    void assertIncWithLabels() throws ReflectiveOperationException {
        PrometheusMetricsGaugeCollector collector = new PrometheusMetricsGaugeCollector(new MetricConfiguration("gauge_inc_label",
                MetricCollectorType.GAUGE, "foo_help", Collections.singletonList("type"), Collections.emptyMap()));
        collector.inc("bar");
        Gauge gauge = (Gauge) Plugins.getMemberAccessor().get(PrometheusMetricsGaugeCollector.class.getDeclaredField("gauge"), collector);
        assertThat(gauge.labels("bar").get(), is(1D));
    }
    
    @Test
    void assertDec() throws ReflectiveOperationException {
        PrometheusMetricsGaugeCollector collector = new PrometheusMetricsGaugeCollector(new MetricConfiguration("gauge_dec",
                MetricCollectorType.GAUGE, "foo_help", Collections.emptyList(), Collections.emptyMap()));
        collector.dec();
        Gauge gauge = (Gauge) Plugins.getMemberAccessor().get(PrometheusMetricsGaugeCollector.class.getDeclaredField("gauge"), collector);
        assertThat(gauge.get(), is(-1D));
    }
    
    @Test
    void assertDecWithLabels() throws ReflectiveOperationException {
        PrometheusMetricsGaugeCollector collector = new PrometheusMetricsGaugeCollector(new MetricConfiguration("gauge_dec_label",
                MetricCollectorType.GAUGE, "foo_help", Collections.singletonList("type"), Collections.emptyMap()));
        collector.dec("bar");
        Gauge gauge = (Gauge) Plugins.getMemberAccessor().get(PrometheusMetricsGaugeCollector.class.getDeclaredField("gauge"), collector);
        assertThat(gauge.labels("bar").get(), is(-1D));
    }
}
