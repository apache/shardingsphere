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

package org.apache.shardingsphere.metrics.prometheus.impl.summary;

import io.prometheus.client.Summary;
import org.apache.shardingsphere.metrics.api.SummaryMetricsTracker;
import org.apache.shardingsphere.metrics.api.SummaryMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.enums.MetricsLabelEnum;

import java.util.concurrent.TimeUnit;

/**
 * Request latency summary metrics tracker.
 */
public final class RequestLatencySummaryMetricsTracker implements SummaryMetricsTracker {
    
    private static final Summary REQUEST_LATENCY = Summary.build()
            .name("requests_latency_summary_millis").help("Requests Latency Summary Millis (ms)")
            .quantile(0.5, 0.05)
            .quantile(0.95, 0.01)
            .quantile(0.99, 0.001)
            .maxAgeSeconds(TimeUnit.MINUTES.toSeconds(5))
            .ageBuckets(5)
            .register();
    
    @Override
    public SummaryMetricsTrackerDelegate startTimer(final String... labelValues) {
        Summary.Timer timer = REQUEST_LATENCY.startTimer();
        return new PrometheusSummaryMetricsTrackerDelegate(timer);
    }
    
    @Override
    public String metricsLabel() {
        return MetricsLabelEnum.REQUEST_LATENCY.getName();
    }
}

