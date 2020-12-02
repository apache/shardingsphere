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
public class MetricsReporter {
    
    private static final MetricsRegister METRICS_REGISTER = MetricsProvider.INSTANCE.newInstance();
    
    /**
     * Register metrics.
     *
     * @param metrics the metrics
     */
    public static void registerMetrics(final Collection<Metric> metrics) {
        for (Metric metric : metrics) {
            switch (metric.getType()) {
                case COUNTER:
                    registerCounter(metric.getName(), getLabelNames(metric.getLabels()), metric.getDoc());
                    break;
                case GAUGE:
                    registerGauge(metric.getName(), getLabelNames(metric.getLabels()), metric.getDoc());
                    break;
                case HISTOGRAM:
                    registerHistogram(metric.getName(), getLabelNames(metric.getLabels()), metric.getDoc());
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
     * @param doc doc
     */
    public static void registerCounter(final String name, final String[] labelNames, final String doc) {
        METRICS_REGISTER.registerCounter(name, labelNames, doc);
    }
    
    /**
     * Register gauge.
     *
     * @param name name
     * @param labelNames label names
     * @param doc doc
     */
    public static void registerGauge(final String name, final String[] labelNames, final String doc) {
        METRICS_REGISTER.registerGauge(name, labelNames, doc);
    }
    
    /**
     * Register histogram.
     *
     * @param name name
     * @param labelNames label names
     * @param doc doc
     */
    public static void registerHistogram(final String name, final String[] labelNames, final String doc) {
        METRICS_REGISTER.registerHistogram(name, labelNames, doc);
    }
    
    /**
     * Counter inc.
     *
     * @param name name
     * @param labelValues label values
     */
    public static void counterInc(final String name, final String[] labelValues) {
        METRICS_REGISTER.counterInc(name, labelValues);
    }
    
    /**
     * Counter inc.
     *
     * @param name name
     * @param labelValues label values
     * @param counter counter
     */
    public static void counterInc(final String name, final String[] labelValues, final long counter) {
        METRICS_REGISTER.counterInc(name, labelValues, counter);
    }
    
    /**
     * Gauge inc.
     *
     * @param name name
     * @param labelValues label values
     */
    public static void gaugeInc(final String name, final String[] labelValues) {
        METRICS_REGISTER.gaugeInc(name, labelValues);
    }
    
    /**
     * Gauge dec.
     *
     * @param name name
     * @param labelValues label values
     */
    public static void gaugeDec(final String name, final String[] labelValues) {
        METRICS_REGISTER.gaugeDec(name, labelValues);
    }
    
    /**
     * Record time.
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
