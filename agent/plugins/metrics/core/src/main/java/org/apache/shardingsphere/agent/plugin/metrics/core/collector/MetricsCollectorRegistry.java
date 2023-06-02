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

package org.apache.shardingsphere.agent.plugin.metrics.core.collector;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.plugin.core.spi.PluginServiceLoader;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Metrics collector registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetricsCollectorRegistry {
    
    private static final Map<String, MetricsCollector> COLLECTORS = new ConcurrentHashMap<>();
    
    /**
     * Get metrics collector.
     *
     * @param metricConfig metric configuration
     * @param pluginType plugin type
     * @param <T> type of metrics collector
     * @return metrics collector
     * @see <a href="https://bugs.openjdk.java.net/browse/JDK-8161372">JDK-8161372</a>
     */
    @SuppressWarnings("unchecked")
    public static <T extends MetricsCollector> T get(final MetricConfiguration metricConfig, final String pluginType) {
        T result = (T) COLLECTORS.get(metricConfig.getId());
        return (T) (null == result
                ? COLLECTORS.computeIfAbsent(metricConfig.getId(), key -> PluginServiceLoader.getServiceLoader(MetricsCollectorFactory.class).getService(pluginType).create(metricConfig))
                : result);
    }
}
