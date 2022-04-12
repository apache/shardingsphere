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

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.schema.impl.DataSourceGeneratedSchemaConfiguration;
import org.apache.shardingsphere.infra.datasource.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.ProxyGlobalConfiguration;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxySchemaConfiguration;

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
        Map<String, DataSourceGeneratedSchemaConfiguration> schemaConfigs = swapSchemaConfigurations(yamlConfig);
        ProxyGlobalConfiguration globalConfig = new ProxyGlobalConfiguration(ruleConfigSwapperEngine.swapToRuleConfigurations(yamlConfig.getServerConfiguration().getRules()),
                yamlConfig.getServerConfiguration().getProps(), yamlConfig.getServerConfiguration().getLabels());
        return new ProxyConfiguration(schemaConfigs, globalConfig);
    }

    private Map<String, DataSourceGeneratedSchemaConfiguration> swapSchemaConfigurations(final YamlProxyConfiguration yamlConfig) {
        Map<String, DataSourceGeneratedSchemaConfiguration> result = new LinkedHashMap<>(yamlConfig.getSchemaConfigurations().size(), 1);
        boolean isDataSourceAggregation = yamlConfig.getServerConfiguration().getProps().containsKey("data-source-aggregation-enabled")
                && (boolean) yamlConfig.getServerConfiguration().getProps().get("data-source-aggregation-enabled");
        for (Entry<String, YamlProxySchemaConfiguration> entry : yamlConfig.getSchemaConfigurations().entrySet()) {
            Map<String, DataSourceConfiguration> schemaDataSourceConfigs = swapDataSourceConfigurations(entry.getValue().getDataSources(), entry.getValue().getSchemaName(), isDataSourceAggregation);
            Collection<RuleConfiguration> schemaRuleConfigs = ruleConfigSwapperEngine.swapToRuleConfigurations(entry.getValue().getRules());
            result.put(entry.getKey(), new DataSourceGeneratedSchemaConfiguration(schemaDataSourceConfigs, schemaRuleConfigs));
        }
        return result;
    }

    private Map<String, DataSourceConfiguration> swapDataSourceConfigurations(final Map<String, YamlProxyDataSourceConfiguration> yamlConfigs, final String schemaName,
                                                                              final boolean isDataSourceAggregation) {
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(yamlConfigs.size(), 1);
        for (Entry<String, YamlProxyDataSourceConfiguration> entry : yamlConfigs.entrySet()) {
            result.put(entry.getKey(), dataSourceConfigSwapper.swap(entry.getValue(), schemaName, entry.getKey(), isDataSourceAggregation));
        }
        return result;
    }
}
