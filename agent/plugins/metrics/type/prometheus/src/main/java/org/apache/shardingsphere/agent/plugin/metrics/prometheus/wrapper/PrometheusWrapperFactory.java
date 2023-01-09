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

package org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsWrapper;
import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsWrapperFactory;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper.type.CounterWrapper;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper.type.GaugeWrapper;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper.type.HistogramWrapper;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper.type.SummaryWrapper;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Prometheus metrics wrapper factory.
 */
public final class PrometheusWrapperFactory implements MetricsWrapperFactory {
    
    private static List<Map<String, Object>> metrics;
    
    static {
        parseMetricsYAML();
    }
    
    @SuppressWarnings("unchecked")
    private static void parseMetricsYAML() {
        InputStream inputStream = PrometheusWrapperFactory.class.getResourceAsStream("/META-INF/conf/prometheus-metrics.yaml");
        Map<String, List<Map<String, Object>>> metricsMap = new Yaml().loadAs(inputStream, LinkedHashMap.class);
        metrics = metricsMap.get("metrics");
    }
    
    @Override
    public Optional<MetricsWrapper> create(final String id) {
        Optional<Map<String, Object>> metricMap = findMetric(id);
        if (!metricMap.isPresent()) {
            return Optional.empty();
        }
        Map<String, Object> metric = metricMap.get();
        return metric.containsKey("type") ? create(metric, (String) metric.get("type")) : Optional.empty();
    }
    
    private Optional<MetricsWrapper> create(final Map<String, Object> metric, final String type) {
        switch (type.toUpperCase()) {
            case "COUNTER":
                return Optional.of(createCounter(metric));
            case "GAUGE":
                return Optional.of(createGauge(metric));
            case "HISTOGRAM":
                return Optional.of(createHistogram(metric));
            case "SUMMARY":
                return Optional.of(createSummary(metric));
            default:
                return Optional.empty();
        }
    }
    
    private Optional<Map<String, Object>> findMetric(final String id) {
        return metrics.stream().filter(each -> id.equals(each.get("id"))).findFirst();
    }
    
    @SuppressWarnings("unchecked")
    private MetricsWrapper createCounter(final Map<String, Object> metric) {
        Counter.Builder builder = Counter.build().name((String) metric.get("name")).help((String) metric.get("help"));
        List<String> metricLabels = (List<String>) metric.get("labels");
        if (null != metricLabels) {
            builder.labelNames(metricLabels.toArray(new String[0]));
        }
        return new CounterWrapper(builder.register());
    }
    
    @SuppressWarnings("unchecked")
    private MetricsWrapper createGauge(final Map<String, Object> metric) {
        Gauge.Builder builder = Gauge.build().name((String) metric.get("name")).help((String) metric.get("help"));
        List<String> metricLabels = (List<String>) metric.get("labels");
        if (null != metricLabels) {
            builder.labelNames(metricLabels.toArray(new String[0]));
        }
        return new GaugeWrapper(builder.register());
    }
    
    @SuppressWarnings("unchecked")
    private MetricsWrapper createHistogram(final Map<String, Object> metric) {
        Histogram.Builder builder = Histogram.build().name((String) metric.get("name")).help((String) metric.get("help"));
        List<String> metricLabels = (List<String>) metric.get("labels");
        if (null != metricLabels) {
            builder.labelNames(metricLabels.toArray(new String[0]));
        }
        Map<String, Object> metricProps = (Map<String, Object>) metric.get("props");
        if (null != metricProps) {
            parseHistogramProperties(builder, metricProps);
        }
        return new HistogramWrapper(builder.register());
    }
    
    @SuppressWarnings("unchecked")
    private void parseHistogramProperties(final Histogram.Builder builder, final Map<String, Object> props) {
        Map<String, Object> buckets = (Map<String, Object>) props.get("buckets");
        if (null == buckets) {
            return;
        }
        if ("exp".equals(buckets.get("type"))) {
            double start = null == buckets.get("start") ? 1 : Double.parseDouble(buckets.get("start").toString());
            double factor = null == buckets.get("factor") ? 1 : Double.parseDouble(buckets.get("factor").toString());
            int count = null == buckets.get("count") ? 1 : (int) buckets.get("count");
            builder.exponentialBuckets(start, factor, count);
        } else if ("linear".equals(buckets.get("type"))) {
            double start = null == buckets.get("start") ? 1 : Double.parseDouble(buckets.get("start").toString());
            double width = null == buckets.get("width") ? 1 : Double.parseDouble(buckets.get("width").toString());
            int count = null == buckets.get("count") ? 1 : (int) buckets.get("count");
            builder.linearBuckets(start, width, count);
        }
    }
    
    @SuppressWarnings("unchecked")
    private MetricsWrapper createSummary(final Map<String, Object> metric) {
        Summary.Builder builder = Summary.build().name((String) metric.get("name")).help((String) metric.get("help"));
        List<String> metricLabels = (List<String>) metric.get("labels");
        if (null != metricLabels) {
            builder.labelNames(metricLabels.toArray(new String[0]));
        }
        return new SummaryWrapper(builder.register());
    }
    
    /**
     * Create gauge metric family.
     *
     * @param id string
     * @return gauge metric family
     */
    public Optional<GaugeMetricFamily> createGaugeMetricFamily(final String id) {
        Optional<Map<String, Object>> metricMap = findMetric(id);
        if (!metricMap.isPresent()) {
            return Optional.empty();
        }
        Map<String, Object> metric = metricMap.get();
        return "GAUGEMETRICFAMILY".equalsIgnoreCase((String) metric.get("type")) ? createGaugeMetricFamily(metric) : Optional.empty();
    }
    
    @SuppressWarnings("unchecked")
    private Optional<GaugeMetricFamily> createGaugeMetricFamily(final Map<String, Object> metric) {
        List<String> metricLabels = (List<String>) metric.get("labels");
        return Optional.of(null == metricLabels
                ? new GaugeMetricFamily((String) metric.get("name"), (String) metric.get("help"), 1d)
                : new GaugeMetricFamily((String) metric.get("name"), (String) metric.get("help"), metricLabels));
    }
}
