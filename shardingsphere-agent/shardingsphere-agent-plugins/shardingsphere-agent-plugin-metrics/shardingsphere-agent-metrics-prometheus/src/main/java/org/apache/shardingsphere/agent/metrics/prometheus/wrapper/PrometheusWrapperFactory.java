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
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public final class PrometheusWrapperFactory implements MetricsWrapperFactory {
    
    private static List metrics;
    
    static {
        Yaml yaml = new Yaml();
        InputStream in = PrometheusWrapperFactory.class.getResourceAsStream("/metrics.yaml");
        Map metricsMap = yaml.loadAs(in, LinkedHashMap.class);
        metrics = (List) metricsMap.get("metrics");
    }
    
    @Override
    public Optional<MetricsWrapper> create(final String id) {
        return createById(id);
    }
    
    private Optional<MetricsWrapper> createById(final String id) {
        Map metric = findById(id);
        if (null == metric || null == getType(metric)) {
            return Optional.empty();
        }
        switch (getType(metric).toUpperCase()) {
            case "COUNTER":
                return createCounter(metric);
            case "GAUGE":
                return createGauge(metric);
            case "HISTOGRAM":
                return createHistogram(metric);
            case "SUMMARY":
                return createSummary(metric);
            case "DELEGATE":
                return Optional.of(new DelegateWrapper(id));
            default:
                return Optional.empty();
        }
    }
    
    private Optional<MetricsWrapper> createCounter(final Map metric) {
        Counter.Builder builder = Counter.build()
                .name(getName(metric))
                .help(getHelp(metric));
        if (null != getLabels(metric)) {
            builder.labelNames(getLabels(metric).stream().toArray(String[]::new));
        }
        Counter counter = builder.register();
        CounterWrapper wrapper = new CounterWrapper(counter);
        return Optional.of(wrapper);
    }
    
    private Optional<MetricsWrapper> createGauge(final Map metric) {
        Gauge.Builder builder = Gauge.build()
                .name(getName(metric))
                .help(getHelp(metric));
        if (null != getLabels(metric)) {
            builder.labelNames(getLabels(metric).stream().toArray(String[]::new));
        }
        Gauge gauge = builder.register();
        GaugeWrapper wrapper = new GaugeWrapper(gauge);
        return Optional.of(wrapper);
    }
    
    private Optional<MetricsWrapper> createHistogram(final Map metric) {
        Histogram.Builder builder = Histogram.build()
                .name(getName(metric))
                .help(getHelp(metric));
        if (null != getLabels(metric)) {
            builder.labelNames(getLabels(metric).stream().toArray(String[]::new));
        }
        if (null != getProps(metric)) {
            parserHistogramProps(builder, getProps(metric));
        }
        Histogram histogram = builder.register();
        HistogramWrapper wrapper = new HistogramWrapper(histogram);
        return Optional.of(wrapper);
    }
    
    private Optional<MetricsWrapper> createSummary(final Map metric) {
        Summary.Builder builder = Summary.build()
                .name(getName(metric))
                .help(getHelp(metric));
        if (null != getLabels(metric)) {
            builder.labelNames(getLabels(metric).stream().toArray(String[]::new));
        }
        Summary summary = builder.register();
        SummaryWrapper wrapper = new SummaryWrapper(summary);
        return Optional.of(wrapper);
    }
    
    private void parserHistogramProps(final Histogram.Builder builder, final Map props) {
        if (null == props.get("buckets")) {
            return;
        }    
        Map b = (Map) props.get("buckets");
        if ("exp".equals(b.get("type"))) {
            double start = null == b.get("start") ? 1 : Double.valueOf(String.valueOf(b.get("start")));
            double factor = null == b.get("factor") ? 1 : Double.valueOf(String.valueOf(b.get("factor")));
            int count = null == b.get("count") ? 1 : (int) b.get("count");
            builder.exponentialBuckets(start, factor, count);
        } else if ("linear".equals(b.get("type"))) {
            double start = null == b.get("start") ? 1 : Double.valueOf(String.valueOf(b.get("start")));
            double width = null == b.get("width") ? 1 : Double.valueOf(String.valueOf(b.get("width")));
            int count = null == b.get("count") ? 1 : (int) b.get("count");
            builder.linearBuckets(start, width, count);
        }        
    }
    
    private Map findById(final String id) {
        return (Map) metrics.stream().filter(m -> getId((Map) m).equals(id)).findFirst().get();
    }
    
    private String getId(final Map metric) {
        return (String) metric.get("id");
    }
    
    private String getType(final Map metric) {
        return (String) metric.get("type");
    }
    
    private String getName(final Map metric) {
        return (String) metric.get("name");
    }
    
    private String getHelp(final Map metric) {
        return (String) metric.get("help");
    }
    
    private List<String> getLabels(final Map metric) {
        return (List<String>) metric.get("labels");
    }
    
    private Map getProps(final Map metric) {
        return (Map) metric.get("props");
    }
}
