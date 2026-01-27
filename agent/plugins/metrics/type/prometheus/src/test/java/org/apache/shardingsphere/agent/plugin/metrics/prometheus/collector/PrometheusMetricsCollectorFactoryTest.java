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

package org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector;

import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.type.PrometheusMetricsCounterCollector;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.type.PrometheusMetricsGaugeCollector;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.type.PrometheusMetricsGaugeMetricFamilyCollector;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.type.PrometheusMetricsHistogramCollector;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.type.PrometheusMetricsSummaryCollector;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class PrometheusMetricsCollectorFactoryTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideMetricCollectorArguments")
    void assertCreateCollector(final String metricName, final MetricCollectorType collectorType, final Class<?> expectedType) {
        MetricConfiguration config = new MetricConfiguration(metricName, collectorType, null, Collections.emptyList(), Collections.emptyMap());
        assertThat(new PrometheusMetricsCollectorFactory().create(config), isA(expectedType));
    }
    
    private static Stream<Arguments> provideMetricCollectorArguments() {
        return Stream.of(
                Arguments.of("test_counter", MetricCollectorType.COUNTER, PrometheusMetricsCounterCollector.class),
                Arguments.of("test_gauge", MetricCollectorType.GAUGE, PrometheusMetricsGaugeCollector.class),
                Arguments.of("test_histogram", MetricCollectorType.HISTOGRAM, PrometheusMetricsHistogramCollector.class),
                Arguments.of("test_summary", MetricCollectorType.SUMMARY, PrometheusMetricsSummaryCollector.class),
                Arguments.of("test_gauge_metric_family", MetricCollectorType.GAUGE_METRIC_FAMILY, PrometheusMetricsGaugeMetricFamilyCollector.class));
    }
}
