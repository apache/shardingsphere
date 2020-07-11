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

package org.apache.shardingsphere.proxy.config.converter;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.configuration.swapper.MetricsConfigurationYamlSwapper;
import org.apache.shardingsphere.metrics.configuration.yaml.YamlMetricsConfiguration;
import org.apache.shardingsphere.proxy.config.util.DataSourceConverter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Abstract configuration converter.
 */
public abstract class AbstractConfigurationConverter implements ProxyConfigurationConverter {
    
    /**
     * Get rule configurations.
     *
     * @param localRuleConfigs rule configs for YAML
     * @return rule configurations
     */
    protected Map<String, Collection<RuleConfiguration>> getRuleConfigurations(final Map<String, YamlProxyRuleConfiguration> localRuleConfigs) {
        YamlRuleConfigurationSwapperEngine swapperEngine = new YamlRuleConfigurationSwapperEngine();
        return localRuleConfigs.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> swapperEngine.swapToRuleConfigurations(entry.getValue().getRules())));
    }
    
    /**
     * Get data source parameters map.
     *
     * @param localRuleConfigs rule configs for YAML
     * @return data source parameters map
     */
    protected Map<String, Map<String, DataSourceParameter>> getDataSourceParametersMap(final Map<String, YamlProxyRuleConfiguration> localRuleConfigs) {
        return localRuleConfigs.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> DataSourceConverter.getDataSourceParameterMap2(entry.getValue().getDataSources())));
    }
    
    /**
     * Get metrics configuration.
     *
     * @param yamlMetricsConfiguration metrics configuration for YAML
     * @return metrics configuration
     */
    protected MetricsConfiguration getMetricsConfiguration(final YamlMetricsConfiguration yamlMetricsConfiguration) {
        return Optional.ofNullable(yamlMetricsConfiguration).map(new MetricsConfigurationYamlSwapper()::swapToObject).orElse(null);
    }
}
