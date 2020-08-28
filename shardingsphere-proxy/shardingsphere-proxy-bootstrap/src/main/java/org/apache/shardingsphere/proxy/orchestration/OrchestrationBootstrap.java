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

package org.apache.shardingsphere.proxy.orchestration;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.orchestration.core.common.yaml.swapper.OrchestrationConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.core.facade.OrchestrationFacade;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.util.DataSourceConverter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Orchestration bootstrap.
 */
@RequiredArgsConstructor
public final class OrchestrationBootstrap {
    
    private final OrchestrationFacade orchestrationFacade;
    
    /**
     * Initialize orchestration.
     * 
     * @param yamlConfig YAML proxy configuration
     * @return proxy configuration
     */
    public ProxyConfiguration init(final YamlProxyConfiguration yamlConfig) {
        orchestrationFacade.init(new OrchestrationConfigurationYamlSwapper().swapToObject(yamlConfig.getServerConfiguration().getOrchestration()), yamlConfig.getRuleConfigurations().keySet());
        initConfigurations(yamlConfig);
        return loadProxyConfiguration();
    }
    
    private void initConfigurations(final YamlProxyConfiguration yamlConfig) {
        YamlProxyServerConfiguration serverConfig = yamlConfig.getServerConfiguration();
        Map<String, YamlProxyRuleConfiguration> ruleConfigs = yamlConfig.getRuleConfigurations();
        if (isEmptyLocalConfiguration(serverConfig, ruleConfigs)) {
            orchestrationFacade.onlineInstance();
        } else {
            orchestrationFacade.onlineInstance(getDataSourceConfigurationMap(ruleConfigs),
                    getRuleConfigurations(ruleConfigs), new AuthenticationYamlSwapper().swapToObject(serverConfig.getAuthentication()), serverConfig.getProps());
        }
    }
    
    private boolean isEmptyLocalConfiguration(final YamlProxyServerConfiguration serverConfig, final Map<String, YamlProxyRuleConfiguration> ruleConfigs) {
        return ruleConfigs.isEmpty() && null == serverConfig.getAuthentication() && serverConfig.getProps().isEmpty();
    }
    
    private Map<String, Map<String, DataSourceConfiguration>> getDataSourceConfigurationMap(final Map<String, YamlProxyRuleConfiguration> ruleConfigs) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>(ruleConfigs.size(), 1);
        for (Entry<String, YamlProxyRuleConfiguration> entry : ruleConfigs.entrySet()) {
            result.put(entry.getKey(), DataSourceConverter.getDataSourceConfigurationMap(DataSourceConverter.getDataSourceParameterMap2(entry.getValue().getDataSources())));
        }
        return result;
    }
    
    private Map<String, Collection<RuleConfiguration>> getRuleConfigurations(final Map<String, YamlProxyRuleConfiguration> yamlRuleConfigurations) {
        YamlRuleConfigurationSwapperEngine swapperEngine = new YamlRuleConfigurationSwapperEngine();
        return yamlRuleConfigurations.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> swapperEngine.swapToRuleConfigurations(entry.getValue().getRules())));
    }
    
    private ProxyConfiguration loadProxyConfiguration() {
        Collection<String> schemaNames = orchestrationFacade.getConfigCenter().getAllSchemaNames();
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = loadDataSourceParametersMap(schemaNames);
        Map<String, Collection<RuleConfiguration>> schemaRules = loadSchemaRules(schemaNames);
        Authentication authentication = orchestrationFacade.getConfigCenter().loadAuthentication();
        Properties props = orchestrationFacade.getConfigCenter().loadProperties();
        return new ProxyConfiguration(schemaDataSources, schemaRules, authentication, props);
    }
    
    private Map<String, Map<String, DataSourceParameter>> loadDataSourceParametersMap(final Collection<String> schemaNames) {
        Map<String, Map<String, DataSourceParameter>> result = new LinkedHashMap<>(schemaNames.size(), 1);
        for (String each : schemaNames) {
            result.put(each, DataSourceConverter.getDataSourceParameterMap(orchestrationFacade.getConfigCenter().loadDataSourceConfigurations(each)));
        }
        return result;
    }
    
    private Map<String, Collection<RuleConfiguration>> loadSchemaRules(final Collection<String> schemaNames) {
        Map<String, Collection<RuleConfiguration>> result = new LinkedHashMap<>(schemaNames.size(), 1);
        for (String each : schemaNames) {
            result.put(each, orchestrationFacade.getConfigCenter().loadRuleConfigurations(each));
        }
        return result;
    }
}
