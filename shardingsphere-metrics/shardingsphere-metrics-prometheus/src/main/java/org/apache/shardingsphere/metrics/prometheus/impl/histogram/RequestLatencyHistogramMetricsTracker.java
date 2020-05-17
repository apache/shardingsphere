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

package org.apache.shardingsphere.metrics.prometheus.impl.histogram;

import io.prometheus.client.Histogram;
import org.apache.shardingsphere.metrics.api.HistogramMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.HistogramMetricsTracker;
import org.apache.shardingsphere.metrics.enums.MetricsLabelEnum;

/**
 * Request latency histogram metrics tracker.
 */
public final class RequestLatencyHistogramMetricsTracker implements HistogramMetricsTracker {
    
    private static final Histogram REQUEST_LATENCY = Histogram.build()
            .name("requests_latency_histogram_millis").help("Requests Latency Histogram Millis (ms)")
            .register();
    
    @Override
    public HistogramMetricsTrackerDelegate startTimer(final String... labelValues) {
        Histogram.Timer timer = REQUEST_LATENCY.startTimer();
        return new PrometheusHistogramMetricsTrackerDelegate(timer);
    }
    
    @Override
    public String metricsLabel() {
        return MetricsLabelEnum.REQUEST_LATENCY.getName();
    }
}

