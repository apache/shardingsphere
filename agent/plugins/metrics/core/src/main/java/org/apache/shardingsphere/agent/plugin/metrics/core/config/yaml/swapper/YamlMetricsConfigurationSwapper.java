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

package org.apache.shardingsphere.agent.plugin.metrics.core.config.yaml.swapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricsConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.yaml.entity.YamlMetricsConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * YAML metrics configuration swapper.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlMetricsConfigurationSwapper {
    
    /**
     * Swap from YAML metrics configuration to metrics configuration.
     * 
     * @param yamlConfig YAML metrics configuration
     * @return metrics configuration
     */
    public static MetricsConfiguration swap(final YamlMetricsConfiguration yamlConfig) {
        Collection<MetricConfiguration> metricConfigs = null == yamlConfig.getMetrics()
                ? Collections.emptyList()
                : yamlConfig.getMetrics().stream().map(YamlMetricConfigurationSwapper::swap).collect(Collectors.toList());
        return new MetricsConfiguration(metricConfigs);
    }
}
