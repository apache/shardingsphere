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

package org.apache.shardingsphere.agent.metrics.prometheus.hikari;

import com.zaxxer.hikari.metrics.PoolStats;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hikari PoolStat Collector.
 */
public final class HikariPoolStatCollector extends Collector {
    
    private final Map<String, PoolStats> poolStatsMap = new ConcurrentHashMap<>();
    
    @Override
    public List<MetricFamilySamples> collect() {
        GaugeMetricFamily activeConnections = new GaugeMetricFamily(
                "hikaricp_active_connections",
                "Active connections",
                Arrays.asList("pool"));
        poolStatsMap.forEach((k, v) ->
                activeConnections.addMetric(Collections.singletonList(k), v.getActiveConnections()));
        GaugeMetricFamily idleConnections = new GaugeMetricFamily(
                "hikaricp_idle_connections",
                "Idle connections",
                Arrays.asList("pool"));
        poolStatsMap.forEach((k, v) ->
                idleConnections.addMetric(Collections.singletonList(k), v.getIdleConnections()));
        GaugeMetricFamily pendingThreads = new GaugeMetricFamily(
                "hikaricp_pending_threads",
                "Pending threads",
                Arrays.asList("pool"));
        poolStatsMap.forEach((k, v) ->
                pendingThreads.addMetric(Collections.singletonList(k), v.getPendingThreads()));
        GaugeMetricFamily totalConnections = new GaugeMetricFamily(
                "hikaricp_connections",
                "Total connections",
                Arrays.asList("pool"));
        poolStatsMap.forEach((k, v) ->
                totalConnections.addMetric(Collections.singletonList(k), v.getTotalConnections()));
        GaugeMetricFamily maxConnections = new GaugeMetricFamily(
                "hikaricp_max_connections",
                "Max connections",
                Arrays.asList("pool"));
        poolStatsMap.forEach((k, v) ->
                maxConnections.addMetric(Collections.singletonList(k), v.getMaxConnections()));
        GaugeMetricFamily minConnections = new GaugeMetricFamily(
                "hikaricp_min_connections",
                "Min connections",
                Arrays.asList("pool"));
        poolStatsMap.forEach((k, v) ->
                minConnections.addMetric(Collections.singletonList(k), v.getMinConnections()));
        List<MetricFamilySamples> result = new LinkedList<>();
        result.add(activeConnections);
        result.add(idleConnections);
        result.add(pendingThreads);
        result.add(totalConnections);
        result.add(maxConnections);
        result.add(minConnections);
        return result;
    }
    
    void addPoolStats(final String poolName, final PoolStats poolStats) {
        poolStatsMap.put(poolName, poolStats);
    }
    
    void removePoolStats(final String poolName) {
        poolStatsMap.remove(poolName);
    }
}
