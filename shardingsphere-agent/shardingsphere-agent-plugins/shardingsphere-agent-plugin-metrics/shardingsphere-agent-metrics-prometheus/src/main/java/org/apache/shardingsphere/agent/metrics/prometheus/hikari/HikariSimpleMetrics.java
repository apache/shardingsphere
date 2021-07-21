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

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;

import java.util.concurrent.TimeUnit;

/**
 * Hikari Metrics.
 */
public final class HikariSimpleMetrics {
    
    private static final Counter CONNECTION_TIMEOUT_COUNTER = Counter.build()
            .name("hikaricp_connection_timeout_total")
            .labelNames("pool")
            .help("Connection timeout total count")
            .create();
    
    private static final Summary ELAPSED_ACQUIRED_SUMMARY =
            createSummary("hikaricp_connection_acquired_nanos", "Connection acquired time (ns)");
    
    private static final Summary ELAPSED_USAGE_SUMMARY =
            createSummary("hikaricp_connection_usage_millis", "Connection usage (ms)");
    
    private static final Summary ELAPSED_CREATION_SUMMARY =
            createSummary("hikaricp_connection_creation_millis", "Connection creation (ms)");
    
    private final Counter.Child connectionTimeoutCounterChild;
    
    private final Summary.Child elapsedAcquiredSummaryChild;
    
    private final Summary.Child elapsedUsageSummaryChild;
    
    private final Summary.Child elapsedCreationSummaryChild;
    
    private final String poolName;
    
    public enum MetricsType {
        CONNECTION_ACQUIRED_NANOS,
        CONNECTION_USAGE_MILLIS,
        CONNECTION_CREATED_MILLIS,
        CONNECTION_TIMEOUT_COUNT
    }
    
    public HikariSimpleMetrics(final String poolName) {
        this.poolName = poolName;
        connectionTimeoutCounterChild = CONNECTION_TIMEOUT_COUNTER.labels(poolName);
        elapsedAcquiredSummaryChild = ELAPSED_ACQUIRED_SUMMARY.labels(poolName);
        elapsedUsageSummaryChild = ELAPSED_USAGE_SUMMARY.labels(poolName);
        elapsedCreationSummaryChild = ELAPSED_CREATION_SUMMARY.labels(poolName);
    }
    
    private static Summary createSummary(final String name, final String help) {
        return Summary.build()
                .name(name)
                .labelNames("pool")
                .help(help)
                .quantile(0.5, 0.05)
                .quantile(0.95, 0.01)
                .quantile(0.99, 0.001)
                .maxAgeSeconds(TimeUnit.MINUTES.toSeconds(5))
                .ageBuckets(5)
                .create();
    }
    
    /**
     * register metrics.
     *
     * @param  registry the metrics register
     */
    public void register(final CollectorRegistry registry) {
        CONNECTION_TIMEOUT_COUNTER.register(registry);
        ELAPSED_ACQUIRED_SUMMARY.register(registry);
        ELAPSED_USAGE_SUMMARY.register(registry);
        ELAPSED_CREATION_SUMMARY.register(registry);
    }
    
    /**
     * observe metrics.
     *
     * @param  type the metrics type
     * @param  value the metrics value
     */
    public void observe(final MetricsType type, final double value) {
        switch (type) {
            case CONNECTION_ACQUIRED_NANOS:
                elapsedAcquiredSummaryChild.observe(value);
                break;
            case CONNECTION_USAGE_MILLIS:
                elapsedUsageSummaryChild.observe(value);
                break;
            case CONNECTION_CREATED_MILLIS:
                elapsedCreationSummaryChild.observe(value);
                break;
            case CONNECTION_TIMEOUT_COUNT:
                connectionTimeoutCounterChild.inc();
                break;
            default:
                break;
        }
    }
    
    /**
     * close metrics.
     */
    public void closeMetrics() {
        CONNECTION_TIMEOUT_COUNTER.remove(poolName);
        ELAPSED_ACQUIRED_SUMMARY.remove(poolName);
        ELAPSED_USAGE_SUMMARY.remove(poolName);
        ELAPSED_CREATION_SUMMARY.remove(poolName);
    }
}
