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

package org.apache.shardingsphere.metrics.prometheus.impl.gauge;

import org.apache.shardingsphere.metrics.enums.MetricsLabelEnum;
import org.apache.shardingsphere.metrics.enums.MetricsTypeEnum;
import org.apache.shardingsphere.metrics.prometheus.impl.AbstractPrometheusCollectorRegistry;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ChannelCountGaugeMetricsTrackerTest extends AbstractPrometheusCollectorRegistry {
    
    @Test
    public void gauge() {
        String name = "channel_count";
        ChannelCountGaugeMetricsTracker tracker = new ChannelCountGaugeMetricsTracker();
        assertThat(tracker.metricsLabel(), is(MetricsLabelEnum.CHANNEL_COUNT.getName()));
        assertThat(tracker.metricsType(), is(MetricsTypeEnum.GAUGE.name()));
        tracker.increment(2.0);
        Double inc = getCollectorRegistry().getSampleValue(name);
        assertThat(inc, is(2.0));
        tracker.decrement(1.0);
        Double dec = getCollectorRegistry().getSampleValue(name);
        assertThat(dec, is(1.0));
    }
}
