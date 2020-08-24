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

package org.apache.shardingsphere.proxy.config.yaml.swapper;

import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.swapper.ClusterConfigurationYamlSwapper;
import org.apache.shardingsphere.cluster.configuration.yaml.YamlClusterConfiguration;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.configuration.swapper.MetricsConfigurationYamlSwapper;
import org.apache.shardingsphere.metrics.configuration.yaml.YamlMetricsConfiguration;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.util.DataSourceConverter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * YAML proxy configuration swapper.
 */
public final class YamlProxyConfigurationSwapper {
    
    /**
     * Swap YAML proxy configuration to proxy configuration.
     * 
     * @param yamlConfig YAML proxy configuration
     * @return proxy configuration
     */
    public ProxyConfiguration swap(final YamlProxyConfiguration yamlConfig) {
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = getDataSourceParametersMap(yamlConfig.getRuleConfigurations());
        Map<String, Collection<RuleConfiguration>> schemaRules = getRuleConfigurations(yamlConfig.getRuleConfigurations());
        Authentication authentication = new AuthenticationYamlSwapper().swapToObject(yamlConfig.getServerConfiguration().getAuthentication());
        ClusterConfiguration clusterConfig = getClusterConfiguration(yamlConfig.getServerConfiguration().getCluster());
        MetricsConfiguration metricsConfig = getMetricsConfiguration(yamlConfig.getServerConfiguration().getMetrics());
        Properties props = yamlConfig.getServerConfiguration().getProps();
        return new ProxyConfiguration(schemaDataSources, schemaRules, authentication, clusterConfig, metricsConfig, props);
    }
    
    private Map<String, Collection<RuleConfiguration>> getRuleConfigurations(final Map<String, YamlProxyRuleConfiguration> yamlRuleConfigurations) {
        YamlRuleConfigurationSwapperEngine swapperEngine = new YamlRuleConfigurationSwapperEngine();
        return yamlRuleConfigurations.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> swapperEngine.swapToRuleConfigurations(entry.getValue().getRules())));
    }
    
    private Map<String, Map<String, DataSourceParameter>> getDataSourceParametersMap(final Map<String, YamlProxyRuleConfiguration> yamlRuleConfigurations) {
        return yamlRuleConfigurations.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> DataSourceConverter.getDataSourceParameterMap2(entry.getValue().getDataSources())));
    }
    
    private ClusterConfiguration getClusterConfiguration(final YamlClusterConfiguration yamlClusterConfiguration) {
        return Optional.ofNullable(yamlClusterConfiguration).map(new ClusterConfigurationYamlSwapper()::swapToObject).orElse(null);
    }
    
    private MetricsConfiguration getMetricsConfiguration(final YamlMetricsConfiguration yamlMetricsConfiguration) {
        return Optional.ofNullable(yamlMetricsConfiguration).map(new MetricsConfigurationYamlSwapper()::swapToObject).orElse(null);
    }
}
