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
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricsConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.yaml.loader.YamlMetricConfigurationsLoader;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.yaml.swapper.YamlMetricsConfigurationSwapper;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper.type.CounterWrapper;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper.type.GaugeWrapper;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper.type.HistogramWrapper;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.wrapper.type.SummaryWrapper;

import java.util.List;
import java.util.Map;

/**
 * Prometheus metrics wrapper factory.
 */
public final class PrometheusWrapperFactory implements MetricsWrapperFactory {
    
    private static final MetricsConfiguration METRICS_CONFIG;
    
    static {
        METRICS_CONFIG = YamlMetricsConfigurationSwapper.swap(YamlMetricConfigurationsLoader.load(PrometheusWrapperFactory.class.getResourceAsStream("/META-INF/conf/prometheus-metrics.yaml")));
    }
    
    @Override
    public MetricsWrapper create(final String id) {
        return create(getMetricConfiguration(id));
    }
    
    private MetricsWrapper create(final MetricConfiguration metricConfig) {
        switch (metricConfig.getType().toUpperCase()) {
            case "COUNTER":
                return createCounter(metricConfig);
            case "GAUGE":
                return createGauge(metricConfig);
            case "HISTOGRAM":
                return createHistogram(metricConfig);
            case "SUMMARY":
                return createSummary(metricConfig);
            default:
                throw new UnsupportedOperationException(String.format("Can not support type `%s`.", metricConfig.getType()));
        }
    }
    
    private MetricConfiguration getMetricConfiguration(final String id) {
        return METRICS_CONFIG.get(id);
    }
    
    private MetricsWrapper createCounter(final MetricConfiguration metricConfig) {
        Counter.Builder builder = Counter.build().name(metricConfig.getId()).help(metricConfig.getHelp());
        List<String> labels = metricConfig.getLabels();
        if (!labels.isEmpty()) {
            builder.labelNames(labels.toArray(new String[0]));
        }
        return new CounterWrapper(builder.register());
    }
    
    private MetricsWrapper createGauge(final MetricConfiguration metricConfig) {
        Gauge.Builder builder = Gauge.build().name(metricConfig.getId()).help(metricConfig.getHelp());
        List<String> labels = metricConfig.getLabels();
        if (!labels.isEmpty()) {
            builder.labelNames(labels.toArray(new String[0]));
        }
        return new GaugeWrapper(builder.register());
    }
    
    private MetricsWrapper createHistogram(final MetricConfiguration metricConfig) {
        Histogram.Builder builder = Histogram.build().name(metricConfig.getId()).help(metricConfig.getHelp());
        List<String> labels = metricConfig.getLabels();
        if (!labels.isEmpty()) {
            builder.labelNames(labels.toArray(new String[0]));
        }
        Map<String, Object> props = metricConfig.getProps();
        if (!props.isEmpty()) {
            parseHistogramProperties(builder, props);
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
    
    private MetricsWrapper createSummary(final MetricConfiguration metricConfig) {
        Summary.Builder builder = Summary.build().name(metricConfig.getId()).help(metricConfig.getHelp());
        List<String> labels = metricConfig.getLabels();
        if (!labels.isEmpty()) {
            builder.labelNames(labels.toArray(new String[0]));
        }
        return new SummaryWrapper(builder.register());
    }
    
    /**
     * Create gauge metric family.
     *
     * @param id metric id
     * @return gauge metric family
     */
    public GaugeMetricFamily createGaugeMetricFamily(final String id) {
        MetricConfiguration metricConfig = getMetricConfiguration(id);
        List<String> labels = metricConfig.getLabels();
        return labels.isEmpty() ? new GaugeMetricFamily(metricConfig.getId(), metricConfig.getHelp(), 1d) : new GaugeMetricFamily(metricConfig.getId(), metricConfig.getHelp(), labels);
    }
    
    /**
     * Create gauge metric with value.
     *
     * @param id metric id
     * @param value value
     * @return gauge metric
     */
    public GaugeMetricFamily createGaugeMetric(final String id, final double value) {
        MetricConfiguration metricConfig = getMetricConfiguration(id);
        return new GaugeMetricFamily(metricConfig.getId(), metricConfig.getHelp(), value);
    }
}
