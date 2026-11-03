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

package org.apache.shardingsphere.proxy.backend.config.yaml.swapper;

import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlModeConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoadResult;
import org.apache.shardingsphere.proxy.backend.config.ProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyServerConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML proxy configuration swapper.
 */
public final class YamlProxyConfigurationSwapper {
    
    private final YamlProxyDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlProxyDataSourceConfigurationSwapper();
    
    private final YamlRuleConfigurationSwapperEngine ruleConfigSwapperEngine = new YamlRuleConfigurationSwapperEngine();
    
    /**
     * Swap YAML server configuration and converted databases to proxy configuration input.
     *
     * @param yamlConfig YAML server configuration
     * @param databaseConfigs converted database configurations
     * @return proxy configuration input
     */
    public ProxyConfigurationLoadResult swap(final YamlProxyServerConfiguration yamlConfig, final Map<String, ProxyDatabaseConfiguration> databaseConfigs) {
        return new ProxyConfigurationLoadResult(null == yamlConfig.getMode() ? null : new YamlModeConfigurationSwapper().swapToObject(yamlConfig.getMode()),
                swapDataSourceConfigurations(yamlConfig.getDataSources()), ruleConfigSwapperEngine.swapToRuleConfigurations(yamlConfig.getRules()), yamlConfig.getProps(), databaseConfigs);
    }
    
    /**
     * Swap YAML database configuration to proxy database configuration input.
     *
     * @param yamlConfig YAML database configuration
     * @return proxy database configuration input
     */
    public ProxyDatabaseConfiguration swap(final YamlProxyDatabaseConfiguration yamlConfig) {
        return new ProxyDatabaseConfiguration(yamlConfig.getDatabaseName(), swapDataSourceConfigurations(yamlConfig.getDataSources()),
                ruleConfigSwapperEngine.swapToRuleConfigurations(yamlConfig.getRules()));
    }
    
    private Map<String, DataSourceConfiguration> swapDataSourceConfigurations(final Map<String, YamlProxyDataSourceConfiguration> yamlConfigs) {
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(yamlConfigs.size(), 1F);
        for (Entry<String, YamlProxyDataSourceConfiguration> entry : yamlConfigs.entrySet()) {
            result.put(entry.getKey(), dataSourceConfigSwapper.swap(entry.getValue()));
        }
        return result;
    }
}
