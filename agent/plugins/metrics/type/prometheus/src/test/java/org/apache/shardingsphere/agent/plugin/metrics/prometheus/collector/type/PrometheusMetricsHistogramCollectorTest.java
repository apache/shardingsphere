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

import io.prometheus.client.Histogram;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PrometheusMetricsHistogramCollectorTest {
    
    @Test
    void assertObserveWithoutBuckets() throws ReflectiveOperationException {
        PrometheusMetricsHistogramCollector collector = new PrometheusMetricsHistogramCollector(new MetricConfiguration("histogram_default_observe",
                MetricCollectorType.HISTOGRAM, "foo_help", Collections.emptyList(), Collections.emptyMap()));
        collector.observe(1D);
        Histogram histogram = (Histogram) Plugins.getMemberAccessor().get(PrometheusMetricsHistogramCollector.class.getDeclaredField("histogram"), collector);
        assertThat(histogram.collect().get(0).samples.stream().filter(sample -> sample.name.endsWith("_count")).findFirst().get().value, is(1D));
    }
    
    @Test
    void assertAppendPropertiesWithExponentialBuckets() throws ReflectiveOperationException {
        Map<String, Object> buckets = new HashMap<>(4, 1F);
        buckets.put("type", "exp");
        buckets.put("start", 2D);
        buckets.put("factor", 3D);
        buckets.put("count", 2);
        PrometheusMetricsHistogramCollector collector = new PrometheusMetricsHistogramCollector(new MetricConfiguration("histogram_explicit_exp",
                MetricCollectorType.HISTOGRAM, "foo_help", Collections.emptyList(), Collections.singletonMap("buckets", buckets)));
        Histogram histogram = (Histogram) Plugins.getMemberAccessor().get(PrometheusMetricsHistogramCollector.class.getDeclaredField("histogram"), collector);
        assertThat(getBucketLabelValues(histogram), is(Arrays.asList("2.0", "6.0", "+Inf")));
    }
    
    @Test
    void assertAppendPropertiesWithExponentialBucketsDefaults() throws ReflectiveOperationException {
        PrometheusMetricsHistogramCollector collector = new PrometheusMetricsHistogramCollector(new MetricConfiguration("histogram_default_exp",
                MetricCollectorType.HISTOGRAM, "foo_help", Collections.emptyList(), Collections.singletonMap("buckets", Collections.singletonMap("type", "exp"))));
        Histogram histogram = (Histogram) Plugins.getMemberAccessor().get(PrometheusMetricsHistogramCollector.class.getDeclaredField("histogram"), collector);
        assertThat(getBucketLabelValues(histogram), is(Arrays.asList("1.0", "+Inf")));
    }
    
    @Test
    void assertAppendPropertiesWithLinearBuckets() throws ReflectiveOperationException {
        Map<String, Object> buckets = new HashMap<>(4, 1F);
        buckets.put("type", "linear");
        buckets.put("start", 2D);
        buckets.put("width", 2D);
        buckets.put("count", 2);
        PrometheusMetricsHistogramCollector collector = new PrometheusMetricsHistogramCollector(new MetricConfiguration("histogram_explicit_linear",
                MetricCollectorType.HISTOGRAM, "foo_help", Collections.emptyList(), Collections.singletonMap("buckets", buckets)));
        Histogram histogram = (Histogram) Plugins.getMemberAccessor().get(PrometheusMetricsHistogramCollector.class.getDeclaredField("histogram"), collector);
        assertThat(getBucketLabelValues(histogram), is(Arrays.asList("2.0", "4.0", "+Inf")));
    }
    
    @Test
    void assertAppendPropertiesWithLinearBucketsDefaults() throws ReflectiveOperationException {
        PrometheusMetricsHistogramCollector collector = new PrometheusMetricsHistogramCollector(new MetricConfiguration("histogram_default_linear",
                MetricCollectorType.HISTOGRAM, "foo_help", Collections.emptyList(), Collections.singletonMap("buckets", Collections.singletonMap("type", "linear"))));
        Histogram histogram = (Histogram) Plugins.getMemberAccessor().get(PrometheusMetricsHistogramCollector.class.getDeclaredField("histogram"), collector);
        assertThat(getBucketLabelValues(histogram), is(Arrays.asList("1.0", "+Inf")));
    }
    
    @Test
    void assertAppendPropertiesWithUnrecognizedType() throws ReflectiveOperationException {
        PrometheusMetricsHistogramCollector collector = new PrometheusMetricsHistogramCollector(new MetricConfiguration("histogram_unknown_type",
                MetricCollectorType.HISTOGRAM, "foo_help", Collections.emptyList(), Collections.singletonMap("buckets", Collections.singletonMap("type", "custom"))));
        Histogram histogram = (Histogram) Plugins.getMemberAccessor().get(PrometheusMetricsHistogramCollector.class.getDeclaredField("histogram"), collector);
        assertFalse(getBucketLabelValues(histogram).isEmpty());
    }
    
    private List<String> getBucketLabelValues(final Histogram histogram) {
        return histogram.collect().get(0).samples.stream().filter(each -> each.name.endsWith("_bucket")).map(each -> each.labelValues.get(each.labelNames.indexOf("le"))).collect(Collectors.toList());
    }
}
