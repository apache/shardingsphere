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

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceGeneratedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.ProxyGlobalConfiguration;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;

import java.util.Collection;
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
     * Swap YAML proxy configuration to proxy configuration.
     *
     * @param yamlConfig YAML proxy configuration
     * @return proxy configuration
     */
    public ProxyConfiguration swap(final YamlProxyConfiguration yamlConfig) {
        Map<String, DatabaseConfiguration> databaseConfigs = swapDatabaseConfigurations(yamlConfig);
        ProxyGlobalConfiguration globalConfig = new ProxyGlobalConfiguration(ruleConfigSwapperEngine.swapToRuleConfigurations(yamlConfig.getServerConfiguration().getRules()),
                yamlConfig.getServerConfiguration().getProps(), yamlConfig.getServerConfiguration().getLabels());
        return new ProxyConfiguration(databaseConfigs, globalConfig);
    }
    
    private Map<String, DatabaseConfiguration> swapDatabaseConfigurations(final YamlProxyConfiguration yamlConfig) {
        Map<String, DatabaseConfiguration> result = new LinkedHashMap<>(yamlConfig.getDatabaseConfigurations().size(), 1);
        for (Entry<String, YamlProxyDatabaseConfiguration> entry : yamlConfig.getDatabaseConfigurations().entrySet()) {
            Map<String, DataSourceConfiguration> databaseDataSourceConfigs =
                    swapDataSourceConfigurations(entry.getValue().getDataSources(), entry.getValue().getDatabaseName());
            Collection<RuleConfiguration> databaseRuleConfigs = ruleConfigSwapperEngine.swapToRuleConfigurations(entry.getValue().getRules());
            result.put(entry.getKey(), new DataSourceGeneratedDatabaseConfiguration(databaseDataSourceConfigs, databaseRuleConfigs));
        }
        return result;
    }
    
    private Map<String, DataSourceConfiguration> swapDataSourceConfigurations(final Map<String, YamlProxyDataSourceConfiguration> yamlConfigs, final String databaseName) {
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(yamlConfigs.size(), 1);
        for (Entry<String, YamlProxyDataSourceConfiguration> entry : yamlConfigs.entrySet()) {
            result.put(entry.getKey(), dataSourceConfigSwapper.swap(entry.getValue()));
        }
        return result;
    }
}
