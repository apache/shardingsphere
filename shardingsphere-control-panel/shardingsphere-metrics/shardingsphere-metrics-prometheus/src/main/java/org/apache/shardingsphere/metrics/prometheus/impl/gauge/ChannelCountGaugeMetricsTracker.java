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

import io.prometheus.client.Gauge;
import org.apache.shardingsphere.metrics.api.GaugeMetricsTracker;
import org.apache.shardingsphere.metrics.enums.MetricsLabelEnum;

/**
 *  Channel count gauge metrics tracker.
 */
public final class ChannelCountGaugeMetricsTracker implements GaugeMetricsTracker {
    
    private static final Gauge CHANNEL_COUNT = Gauge.build().name("channel_count").help("proxy channel count").register();
    
    @Override
    public void increment(final double amount, final String... labelValues) {
        CHANNEL_COUNT.inc(amount);
    }
    
    @Override
    public void decrement(final double amount, final String... labelValues) {
        CHANNEL_COUNT.dec(amount);
    }
    
    @Override
    public String metricsLabel() {
        return MetricsLabelEnum.CHANNEL_COUNT.getName();
    }
}

