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

package org.apache.shardingsphere.agent.plugin.metrics.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Metrics wrapper registry.
 */
public final class MetricsWrapperRegistry {
    
    private static final Map<String, MetricsWrapper> METRICS_WRAPPERS = new ConcurrentHashMap<>();
    
    private static MetricsWrapperFactory metricsWrapperFactory;
    
    /**
     * Set metrics wrapper factory.
     *
     * @param metricsWrapperFactory metrics wrapper factory
     */
    public static void setMetricsFactory(final MetricsWrapperFactory metricsWrapperFactory) {
        MetricsWrapperRegistry.metricsWrapperFactory = metricsWrapperFactory;
    }
    
    /**
     * Get metrics wrapper.
     *
     * @param id metric ID
     * @return metrics wrapper
     * @see <a href="https://bugs.openjdk.java.net/browse/JDK-8161372">JDK-8161372</a>
     */
    public static MetricsWrapper get(final String id) {
        MetricsWrapper result = METRICS_WRAPPERS.get(id);
        return null == result ? METRICS_WRAPPERS.computeIfAbsent(id, metricsWrapperFactory::create) : result;
    }
}
