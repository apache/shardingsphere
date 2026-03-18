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

import io.prometheus.client.GaugeMetricFamily;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class PrometheusMetricsGaugeMetricFamilyCollectorTest {
    
    @Test
    void assertCreate() throws ReflectiveOperationException {
        PrometheusMetricsGaugeMetricFamilyCollector collector = new PrometheusMetricsGaugeMetricFamilyCollector(new MetricConfiguration("foo_gauge_metric_family",
                MetricCollectorType.GAUGE_METRIC_FAMILY, "foo_help", Collections.emptyList(), Collections.emptyMap()));
        collector.addMetric(Collections.emptyList(), 1D);
        assertThat(Plugins.getMemberAccessor().get(PrometheusMetricsGaugeMetricFamilyCollector.class.getDeclaredField("gaugeMetricFamily"), collector), isA(GaugeMetricFamily.class));
    }
}
