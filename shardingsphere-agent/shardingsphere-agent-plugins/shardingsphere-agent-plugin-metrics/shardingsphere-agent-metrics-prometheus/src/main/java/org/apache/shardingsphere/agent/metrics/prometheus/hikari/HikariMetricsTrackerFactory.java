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
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.metrics.PoolStats;
import io.prometheus.client.CollectorRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Hikari metrics tracker factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HikariMetricsTrackerFactory implements MetricsTrackerFactory {
    
    private static volatile HikariMetricsTrackerFactory instance;
    
    private CollectorRegistry collectorRegistry;
    
    private HikariMetricsTrackerFactory(final CollectorRegistry collectorRegistry) {
        this.collectorRegistry = collectorRegistry;
    }
    
    /**
     * Get the factory with the specific registry.
     *
     * @param  collectorRegistry the metrics registry
     * @return default metrics tracker factory
     */
    public static HikariMetricsTrackerFactory getInstance(final CollectorRegistry collectorRegistry) {
        if (null == instance) {
            instance = new HikariMetricsTrackerFactory(collectorRegistry);
        } 
        return instance;
    }
    
    /**
     * Create the metric tracker.
     *
     * @param  poolName the hikariCP pool name
     * @param  poolStats the hikariCP pool state
     * @return the created metrics tracker
     */
    @Override
    public IMetricsTracker create(final String poolName, final PoolStats poolStats) {
        return new HikariMetricsTracker(poolName, poolStats, this.collectorRegistry);
    }
}
