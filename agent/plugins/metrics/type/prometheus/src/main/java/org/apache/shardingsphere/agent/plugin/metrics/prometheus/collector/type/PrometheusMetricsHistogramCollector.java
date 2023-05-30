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
import io.prometheus.client.Histogram.Builder;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.HistogramMetricsCollector;

import java.util.Map;

/**
 * Metrics histogram collector of Prometheus.
 */
public final class PrometheusMetricsHistogramCollector implements HistogramMetricsCollector {
    
    private static final String BUCKETS_KEY = "buckets";
    
    private static final String TYPE_KEY = "type";
    
    private static final String EXP_TYPE_KEY = "exp";
    
    private static final String LINEAR_TYPE_KEY = "linear";
    
    private static final String START_KEY = "start";
    
    private static final String COUNT_KEY = "count";
    
    private static final String FACTOR_KEY = "factor";
    
    private static final String WIDTH_KEY = "width";
    
    private final Histogram histogram;
    
    public PrometheusMetricsHistogramCollector(final MetricConfiguration config) {
        Builder builder = Histogram.build().name(config.getId()).help(config.getHelp()).labelNames(config.getLabels().toArray(new String[0]));
        appendProperties(builder, config.getProps());
        histogram = builder.register();
    }
    
    @SuppressWarnings("unchecked")
    private void appendProperties(final Builder builder, final Map<String, Object> props) {
        Map<String, Object> buckets = (Map<String, Object>) props.get(BUCKETS_KEY);
        if (null == buckets) {
            return;
        }
        if (EXP_TYPE_KEY.equals(buckets.get(TYPE_KEY))) {
            double start = null == buckets.get(START_KEY) ? 1 : Double.parseDouble(buckets.get(START_KEY).toString());
            double factor = null == buckets.get(FACTOR_KEY) ? 1 : Double.parseDouble(buckets.get(FACTOR_KEY).toString());
            int count = null == buckets.get(COUNT_KEY) ? 1 : (int) buckets.get(COUNT_KEY);
            builder.exponentialBuckets(start, factor, count);
        } else if (LINEAR_TYPE_KEY.equals(buckets.get(TYPE_KEY))) {
            double start = null == buckets.get(START_KEY) ? 1 : Double.parseDouble(buckets.get(START_KEY).toString());
            double width = null == buckets.get(WIDTH_KEY) ? 1 : Double.parseDouble(buckets.get(WIDTH_KEY).toString());
            int count = null == buckets.get(COUNT_KEY) ? 1 : (int) buckets.get(COUNT_KEY);
            builder.linearBuckets(start, width, count);
        }
    }
    
    @Override
    public void observe(final double value) {
        histogram.observe(value);
    }
}
