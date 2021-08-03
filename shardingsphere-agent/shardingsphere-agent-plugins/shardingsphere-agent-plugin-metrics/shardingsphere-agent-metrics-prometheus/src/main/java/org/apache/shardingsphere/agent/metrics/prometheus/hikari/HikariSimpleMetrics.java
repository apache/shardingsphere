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
import io.prometheus.client.Histogram;

/**
 * Hikari Metrics.
 */
public final class HikariSimpleMetrics {
    
    private static final Counter CONNECTION_TIMEOUT_COUNTER = Counter.build()
            .name("hikaricp_connection_timeout_total")
            .labelNames("pool")
            .help("Connection timeout total count")
            .create();
    
    private static final Histogram ELAPSED_ACQUIRED =
            createHistogram("hikaricp_connection_acquired_nanos", "Connection acquired time (ns)", 1_000);
    
    private static final Histogram ELAPSED_USAGE =
            createHistogram("hikaricp_connection_usage_millis", "Connection usage (ms)", 1);
    
    private static final Histogram ELAPSED_CREATION =
            createHistogram("hikaricp_connection_creation_millis", "Connection creation (ms)", 1);
    
    private final Counter.Child connectionTimeoutCounterChild;
    
    private final Histogram.Child elapsedAcquiredChild;
    
    private final Histogram.Child elapsedUsageChild;
    
    private final Histogram.Child elapsedCreationChild;
    
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
        elapsedAcquiredChild = ELAPSED_ACQUIRED.labels(poolName);
        elapsedUsageChild = ELAPSED_USAGE.labels(poolName);
        elapsedCreationChild = ELAPSED_CREATION.labels(poolName);
    }
    
    private static Histogram createHistogram(final String name, final String help, final double bucketStart) {
        return Histogram.build()
                .name(name)
                .labelNames("pool")
                .help(help)
                .exponentialBuckets(bucketStart, 2.0, 11)
                .create();
    }
    
    /**
     * register metrics.
     *
     * @param registry the metrics register
     */
    public void register(final CollectorRegistry registry) {
        CONNECTION_TIMEOUT_COUNTER.register(registry);
        ELAPSED_ACQUIRED.register(registry);
        ELAPSED_USAGE.register(registry);
        ELAPSED_CREATION.register(registry);
    }
    
    /**
     * observe metrics.
     *
     * @param type the metrics type
     * @param value the metrics value
     */
    public void observe(final MetricsType type, final double value) {
        switch (type) {
            case CONNECTION_ACQUIRED_NANOS:
                elapsedAcquiredChild.observe(value);
                break;
            case CONNECTION_USAGE_MILLIS:
                elapsedUsageChild.observe(value);
                break;
            case CONNECTION_CREATED_MILLIS:
                elapsedCreationChild.observe(value);
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
        ELAPSED_ACQUIRED.remove(poolName);
        ELAPSED_USAGE.remove(poolName);
        ELAPSED_CREATION.remove(poolName);
    }
}
