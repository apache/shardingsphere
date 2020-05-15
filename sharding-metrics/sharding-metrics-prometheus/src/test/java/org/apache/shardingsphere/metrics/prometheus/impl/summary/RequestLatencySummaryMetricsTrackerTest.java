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

import org.apache.shardingsphere.metrics.api.SummaryMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.enums.MetricsLabelEnum;
import org.apache.shardingsphere.metrics.enums.MetricsTypeEnum;
import org.apache.shardingsphere.metrics.prometheus.impl.AbstractPrometheusCollectorRegistry;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class RequestLatencySummaryMetricsTrackerTest extends AbstractPrometheusCollectorRegistry {
    
    @Test
    public void summary() {
        RequestLatencySummaryMetricsTracker tracker = new RequestLatencySummaryMetricsTracker();
        assertThat(tracker.metricsLabel(), is(MetricsLabelEnum.REQUEST_LATENCY.getName()));
        assertThat(tracker.metricsType(), is(MetricsTypeEnum.SUMMARY.name()));
        assertThat(tracker.metricsType(), not(MetricsTypeEnum.HISTOGRAM.name()));
        SummaryMetricsTrackerDelegate trackerDelegate = tracker.startTimer();
        trackerDelegate.observeDuration();
        String metricName = "requests_latency_summary_millis";
        Double count = getCollectorRegistry().getSampleValue(metricName + "_count");
        assertNotNull(count);
        Double sum = getCollectorRegistry().getSampleValue(metricName + "_sum");
        assertNotNull(sum);
    }
}

