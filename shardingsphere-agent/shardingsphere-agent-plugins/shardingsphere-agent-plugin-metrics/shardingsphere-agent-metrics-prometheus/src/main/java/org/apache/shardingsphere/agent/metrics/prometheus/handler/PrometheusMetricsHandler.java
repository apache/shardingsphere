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

package org.apache.shardingsphere.agent.metrics.prometheus.handler;

import com.zaxxer.hikari.HikariDataSource;
import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.metrics.api.constant.MetricIds;
import org.apache.shardingsphere.agent.metrics.prometheus.hikari.HikariMetricsTrackerFactory;

/**
 * Prometheus metrics handler.
 */
@Slf4j
public final class PrometheusMetricsHandler {
    
    /**
     * Handle the delegate metric.
     *
     * @param  id metric id
     * @param object delegate parameter object
     */
    public static void handle(final String id, final Object object) {
        if (MetricIds.HIKARI_SET_METRICS_FACTORY.equals(id)) {
            if (object instanceof HikariDataSource) {
                HikariDataSource dataSource = (HikariDataSource) object;
                dataSource.setMetricsTrackerFactory(HikariMetricsTrackerFactory.getInstance(CollectorRegistry.defaultRegistry));
                log.info("Set metrics factory to {}", dataSource);
            }
        }
    }
}
