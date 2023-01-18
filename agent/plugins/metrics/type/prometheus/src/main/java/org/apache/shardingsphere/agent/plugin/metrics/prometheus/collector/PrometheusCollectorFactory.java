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

import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorFactory;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.type.PrometheusCounterCollector;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.type.PrometheusGaugeCollector;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.type.PrometheusGaugeMetricFamilyCollector;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.type.PrometheusHistogramCollector;
import org.apache.shardingsphere.agent.plugin.metrics.prometheus.collector.type.PrometheusSummaryCollector;

/**
 * Prometheus metrics collector factory.
 */
public final class PrometheusCollectorFactory implements MetricsCollectorFactory {
    
    @Override
    public MetricsCollector create(final MetricConfiguration metricConfig) {
        switch (metricConfig.getType()) {
            case COUNTER:
                return new PrometheusCounterCollector(metricConfig);
            case GAUGE:
                return new PrometheusGaugeCollector(metricConfig);
            case HISTOGRAM:
                return new PrometheusHistogramCollector(metricConfig);
            case SUMMARY:
                return new PrometheusSummaryCollector(metricConfig);
            case GAUGE_METRIC_FAMILY:
                return new PrometheusGaugeMetricFamilyCollector(metricConfig);
            default:
                throw new UnsupportedOperationException(String.format("Can not support type `%s`.", metricConfig.getType()));
        }
    }
    
    @Override
    public String getType() {
        return "Prometheus";
    }
}
