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

package org.apache.shardingsphere.agent.metrics.prometheus.wrapper;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import org.apache.shardingsphere.agent.metrics.api.MetricsWrapper;
import org.apache.shardingsphere.agent.metrics.api.MetricsWrapperFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Prometheus metrics wrapper factory.
 */
public class PrometheusWrapperFactory implements MetricsWrapperFactory {
    
    private static List<Map<String, Object>> metrics;
    
    static {
        parseMetricsYaml();
    }
    
    @SuppressWarnings("unchecked")
    private static void parseMetricsYaml() {
        InputStream inputStream = PrometheusWrapperFactory.class.getResourceAsStream("/prometheus/metrics.yaml");
        Map<String, List<Map<String, Object>>> metricsMap = new Yaml().loadAs(inputStream, LinkedHashMap.class);
        metrics = metricsMap.get("metrics");
    }
    
    /**
     * Create metrics wrapper.
     *
     * @param id id
     * @return metrics wrapper
     */
    @Override
    public Optional<MetricsWrapper> create(final String id) {
        return createById(id);
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
        if (null == getMetricType(metric)) {
            return Optional.empty();
        }
        if ("GAUGEMETRICFAMILY".equalsIgnoreCase(getMetricType(metric))) {
            return createGaugeMetricFamily(metric);
        }
        return Optional.empty();
    }
    
    private Optional<GaugeMetricFamily> createGaugeMetricFamily(final Map<String, Object> metric) {
        return Optional.of(null == getMetricLabels(metric)
                ? new GaugeMetricFamily(getMetricName(metric), getMetricHelpMessage(metric), 1)
                : new GaugeMetricFamily(getMetricName(metric), getMetricHelpMessage(metric), getMetricLabels(metric)));
    }
    
    private Optional<MetricsWrapper> createById(final String id) {
        Optional<Map<String, Object>> metricMap = findMetric(id);
        if (!metricMap.isPresent()) {
            return Optional.empty();
        }
        Map<String, Object> metric = metricMap.get();
        if (null == getMetricType(metric)) {
            return Optional.empty();
        }
        switch (getMetricType(metric).toUpperCase()) {
            case "COUNTER":
                return createCounter(metric);
            case "GAUGE":
                return createGauge(metric);
            case "HISTOGRAM":
                return createHistogram(metric);
            case "SUMMARY":
                return createSummary(metric);
            default:
                return Optional.empty();
        }
    }
    
    private Optional<Map<String, Object>> findMetric(final String id) {
        return metrics.stream().filter(optional -> id.equals(getMetricId(optional))).findFirst();
    }
    
    private Optional<MetricsWrapper> createCounter(final Map<String, Object> metric) {
        Counter.Builder builder = Counter.build().name(getMetricName(metric)).help(getMetricHelpMessage(metric));
        if (null != getMetricLabels(metric)) {
            builder.labelNames(getMetricLabels(metric).toArray(new String[0]));
        }
        return Optional.of(new CounterWrapper(builder.register()));
    }
    
    private Optional<MetricsWrapper> createGauge(final Map<String, Object> metric) {
        Gauge.Builder builder = Gauge.build().name(getMetricName(metric)).help(getMetricHelpMessage(metric));
        if (null != getMetricLabels(metric)) {
            builder.labelNames(getMetricLabels(metric).toArray(new String[0]));
        }
        return Optional.of(new GaugeWrapper(builder.register()));
    }
    
    private Optional<MetricsWrapper> createHistogram(final Map<String, Object> metric) {
        Histogram.Builder builder = Histogram.build().name(getMetricName(metric)).help(getMetricHelpMessage(metric));
        if (null != getMetricLabels(metric)) {
            builder.labelNames(getMetricLabels(metric).toArray(new String[0]));
        }
        if (null != getMetricProperties(metric)) {
            parserHistogramProperties(builder, getMetricProperties(metric));
        }
        return Optional.of(new HistogramWrapper(builder.register()));
    }
    
    @SuppressWarnings("unchecked")
    private void parserHistogramProperties(final Histogram.Builder builder, final Map<String, Object> props) {
        if (null == props.get("buckets")) {
            return;
        }
        Map<String, Object> buckets = (Map<String, Object>) props.get("buckets");
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
    
    private Optional<MetricsWrapper> createSummary(final Map<String, Object> metric) {
        Summary.Builder builder = Summary.build().name(getMetricName(metric)).help(getMetricHelpMessage(metric));
        if (null != getMetricLabels(metric)) {
            builder.labelNames(getMetricLabels(metric).toArray(new String[0]));
        }
        return Optional.of(new SummaryWrapper(builder.register()));
    }
    
    private String getMetricId(final Map<String, Object> metric) {
        return (String) metric.get("id");
    }
    
    private String getMetricType(final Map<String, Object> metric) {
        return (String) metric.get("type");
    }
    
    private String getMetricName(final Map<String, Object> metric) {
        return (String) metric.get("name");
    }
    
    private String getMetricHelpMessage(final Map<String, Object> metric) {
        return (String) metric.get("help");
    }
    
    @SuppressWarnings("unchecked")
    private List<String> getMetricLabels(final Map<String, Object> metric) {
        return (List<String>) metric.get("labels");
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMetricProperties(final Map<String, Object> metric) {
        return (Map<String, Object>) metric.get("props");
    }
}
