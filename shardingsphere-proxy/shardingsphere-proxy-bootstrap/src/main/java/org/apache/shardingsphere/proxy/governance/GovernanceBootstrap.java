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

package org.apache.shardingsphere.proxy.governance;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.governance.core.facade.GovernanceFacade;
import org.apache.shardingsphere.governance.core.yaml.swapper.GovernanceConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.auth.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.context.schema.DataSourceParameter;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Governance bootstrap.
 */
@RequiredArgsConstructor
public final class GovernanceBootstrap {
    
    private final GovernanceFacade governanceFacade;
    
    /**
     * Initialize governance.
     * 
     * @param yamlConfig YAML proxy configuration
     * @return proxy configuration
     */
    public ProxyConfiguration init(final YamlProxyConfiguration yamlConfig) {
        governanceFacade.init(new GovernanceConfigurationYamlSwapper().swapToObject(yamlConfig.getServerConfiguration().getGovernance()), yamlConfig.getRuleConfigurations().keySet());
        initConfigurations(yamlConfig);
        return loadProxyConfiguration();
    }
    
    private void initConfigurations(final YamlProxyConfiguration yamlConfig) {
        YamlProxyServerConfiguration serverConfig = yamlConfig.getServerConfiguration();
        Map<String, YamlProxyRuleConfiguration> ruleConfigs = yamlConfig.getRuleConfigurations();
        if (isEmptyLocalConfiguration(serverConfig, ruleConfigs)) {
            governanceFacade.onlineInstance();
        } else {
            governanceFacade.onlineInstance(
                    getDataSourceConfigurationMap(ruleConfigs), getRuleConfigurations(ruleConfigs), getAuthentication(serverConfig.getAuthentication()), serverConfig.getProps());
        }
    }
    
    private boolean isEmptyLocalConfiguration(final YamlProxyServerConfiguration serverConfig, final Map<String, YamlProxyRuleConfiguration> ruleConfigs) {
        return ruleConfigs.isEmpty() && null == serverConfig.getAuthentication() && serverConfig.getProps().isEmpty();
    }
    
    private Map<String, Map<String, DataSourceConfiguration>> getDataSourceConfigurationMap(final Map<String, YamlProxyRuleConfiguration> ruleConfigs) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>(ruleConfigs.size(), 1);
        for (Entry<String, YamlProxyRuleConfiguration> entry : ruleConfigs.entrySet()) {
            result.put(entry.getKey(), 
                    DataSourceParameterConverter.getDataSourceConfigurationMap(DataSourceParameterConverter.getDataSourceParameterMapFromYamlConfiguration(entry.getValue().getDataSources())));
        }
        return result;
    }
    
    private Map<String, Collection<RuleConfiguration>> getRuleConfigurations(final Map<String, YamlProxyRuleConfiguration> yamlRuleConfigurations) {
        YamlRuleConfigurationSwapperEngine swapperEngine = new YamlRuleConfigurationSwapperEngine();
        return yamlRuleConfigurations.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> swapperEngine.swapToRuleConfigurations(entry.getValue().getRules())));
    }
    
    private Authentication getAuthentication(final YamlAuthenticationConfiguration authConfig) {
        return new AuthenticationYamlSwapper().swapToObject(authConfig);
    }
    
    private ProxyConfiguration loadProxyConfiguration() {
        Collection<String> schemaNames = governanceFacade.getConfigCenter().getAllSchemaNames();
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = loadDataSourceParametersMap(schemaNames);
        Map<String, Collection<RuleConfiguration>> schemaRules = loadSchemaRules(schemaNames);
        Authentication authentication = governanceFacade.getConfigCenter().loadAuthentication();
        Properties props = governanceFacade.getConfigCenter().loadProperties();
        return new ProxyConfiguration(schemaDataSources, schemaRules, authentication, props);
    }
    
    private Map<String, Map<String, DataSourceParameter>> loadDataSourceParametersMap(final Collection<String> schemaNames) {
        Map<String, Map<String, DataSourceParameter>> result = new LinkedHashMap<>(schemaNames.size(), 1);
        for (String each : schemaNames) {
            result.put(each, DataSourceParameterConverter.getDataSourceParameterMap(governanceFacade.getConfigCenter().loadDataSourceConfigurations(each)));
        }
        return result;
    }
    
    private Map<String, Collection<RuleConfiguration>> loadSchemaRules(final Collection<String> schemaNames) {
        Map<String, Collection<RuleConfiguration>> result = new LinkedHashMap<>(schemaNames.size(), 1);
        for (String each : schemaNames) {
            result.put(each, governanceFacade.getConfigCenter().loadRuleConfigurations(each));
        }
        return result;
    }
}
