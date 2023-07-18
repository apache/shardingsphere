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
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.ProxyGlobalConfiguration;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyServerConfiguration;

import javax.sql.DataSource;
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
        Map<String, DatabaseConfiguration> databaseConfigs = swapDatabaseConfigurations(yamlConfig.getDatabaseConfigurations());
        ProxyGlobalConfiguration globalConfig = swapGlobalConfiguration(yamlConfig.getServerConfiguration());
        return new ProxyConfiguration(databaseConfigs, globalConfig);
    }
    
    private ProxyGlobalConfiguration swapGlobalConfiguration(final YamlProxyServerConfiguration yamlConfig) {
        Map<String, DataSource> dataSources = swapDataSources(yamlConfig.getDataSources());
        Collection<RuleConfiguration> ruleConfigs = ruleConfigSwapperEngine.swapToRuleConfigurations(yamlConfig.getRules());
        return new ProxyGlobalConfiguration(dataSources, ruleConfigs, yamlConfig.getProps(), yamlConfig.getLabels());
    }
    
    private Map<String, DataSource> swapDataSources(final Map<String, YamlProxyDataSourceConfiguration> yamlDataSourceConfigs) {
        Map<String, DataSourceConfiguration> dataSourceConfigs = swapDataSourceConfigurations(yamlDataSourceConfigs);
        return DataSourcePoolCreator.create(DataSourcePropertiesCreator.createFromConfiguration(dataSourceConfigs));
    }
    
    private Map<String, DatabaseConfiguration> swapDatabaseConfigurations(final Map<String, YamlProxyDatabaseConfiguration> databaseConfigurations) {
        Map<String, DatabaseConfiguration> result = new LinkedHashMap<>(databaseConfigurations.size(), 1F);
        for (Entry<String, YamlProxyDatabaseConfiguration> entry : databaseConfigurations.entrySet()) {
            Map<String, DataSourceConfiguration> databaseDataSourceConfigs = swapDataSourceConfigurations(entry.getValue().getDataSources());
            Collection<RuleConfiguration> databaseRuleConfigs = ruleConfigSwapperEngine.swapToRuleConfigurations(entry.getValue().getRules());
            result.put(entry.getKey(), new DataSourceGeneratedDatabaseConfiguration(databaseDataSourceConfigs, databaseRuleConfigs));
        }
        return result;
    }
    
    private Map<String, DataSourceConfiguration> swapDataSourceConfigurations(final Map<String, YamlProxyDataSourceConfiguration> yamlConfigs) {
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(yamlConfigs.size(), 1F);
        for (Entry<String, YamlProxyDataSourceConfiguration> entry : yamlConfigs.entrySet()) {
            result.put(entry.getKey(), dataSourceConfigSwapper.swap(entry.getValue()));
        }
        return result;
    }
}
