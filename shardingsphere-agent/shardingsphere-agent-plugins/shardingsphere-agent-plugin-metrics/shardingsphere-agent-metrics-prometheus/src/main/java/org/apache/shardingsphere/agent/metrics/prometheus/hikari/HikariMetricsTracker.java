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

import com.zaxxer.hikari.metrics.IMetricsTracker;
import com.zaxxer.hikari.metrics.PoolStats;
import io.prometheus.client.CollectorRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hikari metrics tracker.
 */
class HikariMetricsTracker implements IMetricsTracker {
    
    private static final Map<CollectorRegistry, Integer> REGISTRY_STAT_MAP = new ConcurrentHashMap<>();
    
    private static final HikariPoolStatCollector HIKARI_POOL_STAT_COLLECTOR = new HikariPoolStatCollector();
    
    private final String poolName;
    
    private final HikariSimpleMetrics hikariSimpleMetrics;
    
    HikariMetricsTracker(final String poolName, final PoolStats poolStats, final CollectorRegistry collectorRegistry) {
        hikariSimpleMetrics = new HikariSimpleMetrics(poolName);
        this.poolName = poolName;
        HIKARI_POOL_STAT_COLLECTOR.addPoolStats(poolName, poolStats);
        registerMetrics(collectorRegistry);
    }
    
    private void registerMetrics(final CollectorRegistry collectorRegistry) {
        if (null == REGISTRY_STAT_MAP.putIfAbsent(collectorRegistry, 1)) {
            HIKARI_POOL_STAT_COLLECTOR.register(collectorRegistry);
            hikariSimpleMetrics.register(collectorRegistry);
        }
    }
    
    @Override
    public void recordConnectionAcquiredNanos(final long elapsedAcquiredNanos) {
        hikariSimpleMetrics.observe(HikariSimpleMetrics.MetricsType.CONNECTION_ACQUIRED_NANOS, elapsedAcquiredNanos);
    }
    
    @Override
    public void recordConnectionUsageMillis(final long elapsedBorrowedMillis) {
        hikariSimpleMetrics.observe(HikariSimpleMetrics.MetricsType.CONNECTION_USAGE_MILLIS, elapsedBorrowedMillis);
    }
    
    @Override
    public void recordConnectionCreatedMillis(final long connectionCreatedMillis) {
        hikariSimpleMetrics.observe(HikariSimpleMetrics.MetricsType.CONNECTION_CREATED_MILLIS, connectionCreatedMillis);
    }
    
    @Override
    public void recordConnectionTimeout() {
        hikariSimpleMetrics.observe(HikariSimpleMetrics.MetricsType.CONNECTION_TIMEOUT_COUNT, 1);
    }
    
    @Override
    public void close() {
        HIKARI_POOL_STAT_COLLECTOR.removePoolStats(poolName);
        hikariSimpleMetrics.closeMetrics();
    }
}
