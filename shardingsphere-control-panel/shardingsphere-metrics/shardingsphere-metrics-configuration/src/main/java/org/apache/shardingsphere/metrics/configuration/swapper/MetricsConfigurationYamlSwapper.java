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

package org.apache.shardingsphere.metrics.configuration.swapper;

import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.configuration.yaml.YamlMetricsConfiguration;

/**
 * Metrics configuration YAML swapper.
 */
public final class MetricsConfigurationYamlSwapper implements YamlSwapper<YamlMetricsConfiguration, MetricsConfiguration> {
    
    @Override
    public YamlMetricsConfiguration swapToYamlConfiguration(final MetricsConfiguration metricsConfiguration) {
        YamlMetricsConfiguration configuration = new YamlMetricsConfiguration();
        configuration.setHost(metricsConfiguration.getHost());
        configuration.setName(metricsConfiguration.getMetricsName());
        configuration.setPort(metricsConfiguration.getPort());
        configuration.setAsync(metricsConfiguration.getAsync());
        configuration.setEnable(metricsConfiguration.getEnable());
        configuration.setThreadCount(metricsConfiguration.getThreadCount());
        configuration.setProps(metricsConfiguration.getProps());
        return configuration;
    }
    
    @Override
    public MetricsConfiguration swapToObject(final YamlMetricsConfiguration yamlConfig) {
        return new MetricsConfiguration(yamlConfig.getName(), yamlConfig.getHost(),
                0 == yamlConfig.getPort() ? MetricsConfiguration.DEFAULT_PORT : yamlConfig.getPort(),
                null == yamlConfig.getAsync() ? true : yamlConfig.getAsync(),
                null == yamlConfig.getEnable() ? true : yamlConfig.getEnable(),
                0 == yamlConfig.getThreadCount() ? Runtime.getRuntime().availableProcessors() << 1 : yamlConfig.getThreadCount(),
                yamlConfig.getProps());
    }
}

