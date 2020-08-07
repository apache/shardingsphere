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

package org.apache.shardingsphere.metrics.prometheus;

import org.apache.shardingsphere.metrics.api.MetricsTracker;
import org.apache.shardingsphere.metrics.api.MetricsTrackerFactory;
import org.apache.shardingsphere.metrics.prometheus.impl.counter.RequestTotalCounterMetricsTracker;
import org.apache.shardingsphere.metrics.prometheus.impl.counter.SQLStatementCounterMetricsTracker;
import org.apache.shardingsphere.metrics.prometheus.impl.counter.ShadowHitTotalCounterMetricsTracker;
import org.apache.shardingsphere.metrics.prometheus.impl.counter.ShardingDatasourceCounterMetricsTracker;
import org.apache.shardingsphere.metrics.prometheus.impl.counter.ShardingTableCounterMetricsTracker;
import org.apache.shardingsphere.metrics.prometheus.impl.counter.TransactionCounterMetricsTracker;
import org.apache.shardingsphere.metrics.prometheus.impl.gauge.ChannelCountGaugeMetricsTracker;
import org.apache.shardingsphere.metrics.prometheus.impl.histogram.RequestLatencyHistogramMetricsTracker;
import org.apache.shardingsphere.metrics.prometheus.impl.summary.RequestLatencySummaryMetricsTracker;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Prometheus metrics tracker factory.
 */
public final class PrometheusMetricsTrackerFactory implements MetricsTrackerFactory {
    
    private static final Collection<MetricsTracker> REGISTER = new LinkedList<>();
    
    static {
        REGISTER.add(new RequestTotalCounterMetricsTracker());
        REGISTER.add(new SQLStatementCounterMetricsTracker());
        REGISTER.add(new ChannelCountGaugeMetricsTracker());
        REGISTER.add(new RequestLatencyHistogramMetricsTracker());
        REGISTER.add(new RequestLatencySummaryMetricsTracker());
        REGISTER.add(new ShardingTableCounterMetricsTracker());
        REGISTER.add(new ShardingDatasourceCounterMetricsTracker());
        REGISTER.add(new TransactionCounterMetricsTracker());
        REGISTER.add(new ShadowHitTotalCounterMetricsTracker());
    }
    
    @Override
    public Optional<MetricsTracker> create(final String metricsType, final String metricsLabel) {
        return REGISTER.stream().filter(each -> each.metricsLabel().equals(metricsLabel) && each.metricsType().equals(metricsType)).findFirst();
    }
}

