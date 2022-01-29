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

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.schema.impl.DataSourceGeneratedSchemaConfiguration;
import org.apache.shardingsphere.infra.datasource.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.config.PoolConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.ProxyGlobalConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxySchemaConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
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
        Map<String, Map<String, DataSourceConfiguration>> schemaDataSourceConfigs = getDataSourceConfigurationMap(yamlConfig.getSchemaConfigurations());
        Map<String, Collection<RuleConfiguration>> schemaConfigs = getSchemaConfigurations(yamlConfig.getSchemaConfigurations());
        Map<String, DataSourceGeneratedSchemaConfiguration> schemaConfigurations = new LinkedHashMap<>(schemaDataSourceConfigs.size(), 1);
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : schemaDataSourceConfigs.entrySet()) {
            schemaConfigurations.put(entry.getKey(), new DataSourceGeneratedSchemaConfiguration(entry.getValue(), schemaConfigs.get(entry.getKey())));
        }
        Collection<RuleConfiguration> globalRules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(yamlConfig.getServerConfiguration().getRules());
        Properties props = yamlConfig.getServerConfiguration().getProps();
        return new ProxyConfiguration(schemaConfigurations, new ProxyGlobalConfiguration(globalRules, props, yamlConfig.getServerConfiguration().getLabels()));
    }
    
    private Map<String, Map<String, DataSourceConfiguration>> getDataSourceConfigurationMap(final Map<String, YamlProxySchemaConfiguration> yamlSchemaConfigs) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>(yamlSchemaConfigs.size(), 1);
        for (Entry<String, YamlProxySchemaConfiguration> entry : yamlSchemaConfigs.entrySet()) {
            result.put(entry.getKey(), getDataSourceConfigurations(entry.getValue().getDataSources()));
        }
        return result;
    }
    
    private Map<String, DataSourceConfiguration> getDataSourceConfigurations(final Map<String, YamlProxyDataSourceConfiguration> yamlDataSourceConfigs) {
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(yamlDataSourceConfigs.size(), 1);
        for (Entry<String, YamlProxyDataSourceConfiguration> entry : yamlDataSourceConfigs.entrySet()) {
            result.put(entry.getKey(), getDataSourceConfiguration(entry.getValue()));
        }
        return result;
    }
    
    private DataSourceConfiguration getDataSourceConfiguration(final YamlProxyDataSourceConfiguration yamlDataSourceConfig) {
        ConnectionConfiguration connectionConfig = new ConnectionConfiguration(yamlDataSourceConfig.getUrl(), yamlDataSourceConfig.getUsername(), yamlDataSourceConfig.getPassword());
        PoolConfiguration poolConfig = new PoolConfiguration(yamlDataSourceConfig.getConnectionTimeoutMilliseconds(), yamlDataSourceConfig.getIdleTimeoutMilliseconds(),
                yamlDataSourceConfig.getMaxLifetimeMilliseconds(), yamlDataSourceConfig.getMaxPoolSize(), yamlDataSourceConfig.getMinPoolSize(), yamlDataSourceConfig.getReadOnly(),
                yamlDataSourceConfig.getCustomPoolProps());
        return new DataSourceConfiguration(connectionConfig, poolConfig);
    }
    
    private Map<String, Collection<RuleConfiguration>> getSchemaConfigurations(final Map<String, YamlProxySchemaConfiguration> yamlSchemaConfigs) {
        YamlRuleConfigurationSwapperEngine swapperEngine = new YamlRuleConfigurationSwapperEngine();
        return yamlSchemaConfigs.entrySet().stream().collect(Collectors.toMap(Entry::getKey, 
            entry -> swapperEngine.swapToRuleConfigurations(entry.getValue().getRules()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
}
