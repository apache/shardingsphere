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

package org.apache.shardingsphere.agent.metrics.api.reporter;

import java.util.Collection;
import java.util.List;
import org.apache.shardingsphere.agent.metrics.api.MetricsProvider;
import org.apache.shardingsphere.agent.metrics.api.MetricsRegister;
import org.apache.shardingsphere.agent.metrics.api.entity.Metric;

/**
 * Metrics reporter.
 */
public final class MetricsReporter {
    
    private static final MetricsRegister METRICS_REGISTER = MetricsProvider.INSTANCE.newInstance();
    
    /**
     * Register metrics.
     *
     * @param metrics metric collection
     */
    public static void registerMetrics(final Collection<Metric> metrics) {
        for (Metric metric : metrics) {
            switch (metric.getType()) {
                case COUNTER:
                    registerCounter(metric.getName(), getLabelNames(metric.getLabels()), metric.getDocument());
                    break;
                case GAUGE:
                    registerGauge(metric.getName(), getLabelNames(metric.getLabels()), metric.getDocument());
                    break;
                case HISTOGRAM:
                    registerHistogram(metric.getName(), getLabelNames(metric.getLabels()), metric.getDocument());
                    break;
                default:
                    throw new RuntimeException("we not support metric registration for type: " + metric.getType());
            }
        }
    }
    
    /**
     * Register counter.
     *
     * @param name name
     * @param labelNames label names
     * @param document document for counter
     */
    public static void registerCounter(final String name, final String[] labelNames, final String document) {
        METRICS_REGISTER.registerCounter(name, labelNames, document);
    }
    
    /**
     * Register gauge.
     *
     * @param name name
     * @param labelNames label names
     * @param document document for gauge
     */
    public static void registerGauge(final String name, final String[] labelNames, final String document) {
        METRICS_REGISTER.registerGauge(name, labelNames, document);
    }
    
    /**
     * Register histogram.
     *
     * @param name name
     * @param labelNames label names
     * @param document document for histogram
     */
    public static void registerHistogram(final String name, final String[] labelNames, final String document) {
        METRICS_REGISTER.registerHistogram(name, labelNames, document);
    }
    
    /**
     * Counter increment.
     *
     * @param name name
     * @param labelValues label values
     */
    public static void counterIncrement(final String name, final String[] labelValues) {
        METRICS_REGISTER.counterIncrement(name, labelValues);
    }
    
    /**
     * Counter increment by count.
     *
     * @param name name
     * @param labelValues label values
     * @param count count
     */
    public static void counterIncrement(final String name, final String[] labelValues, final long count) {
        METRICS_REGISTER.counterIncrement(name, labelValues, count);
    }
    
    /**
     * Gauge increment.
     *
     * @param name name
     * @param labelValues label values
     */
    public static void gaugeIncrement(final String name, final String[] labelValues) {
        METRICS_REGISTER.gaugeIncrement(name, labelValues);
    }
    
    /**
     * Gauge decrement.
     *
     * @param name name
     * @param labelValues label values
     */
    public static void gaugeDecrement(final String name, final String[] labelValues) {
        METRICS_REGISTER.gaugeDecrement(name, labelValues);
    }
    
    /**
     * Record time by duration.
     *
     * @param name name
     * @param labelValues label values
     * @param duration duration
     */
    public static void recordTime(final String name, final String[] labelValues, final long duration) {
        METRICS_REGISTER.recordTime(name, labelValues, duration);
    }
    
    private static String[] getLabelNames(final List<String> labels) {
        return labels.toArray(new String[0]);
    }
}
