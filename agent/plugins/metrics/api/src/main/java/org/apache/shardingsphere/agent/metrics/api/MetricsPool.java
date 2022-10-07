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

package org.apache.shardingsphere.agent.metrics.api;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Metrics pool.
 */
public final class MetricsPool {
    
    private static final ConcurrentHashMap<String, MetricsWrapper> METRICS_POOL = new ConcurrentHashMap<>();
    
    private static MetricsWrapperFactory wrapperFactory;
    
    /**
     * Set the metrics wrapper factory.
     *
     * @param factory MetricsWrapperFactory
     */
    public static void setMetricsFactory(final MetricsWrapperFactory factory) {
        wrapperFactory = factory;
    }
    
    /**
     * Create metrics wrapper by id.
     *
     * @param id id
     */
    public static void create(final String id) {
        Optional<MetricsWrapper> wrapper = wrapperFactory.create(id);
        wrapper.ifPresent(optional -> METRICS_POOL.put(id, optional));
    }
    
    /**
     * Get the metrics wrapper by id.
     *
     * @param id id
     * @return optional of metrics wrapper
     */
    public static Optional<MetricsWrapper> get(final String id) {
        return Optional.ofNullable(METRICS_POOL.get(id));
    }
}
