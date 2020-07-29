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

import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.swapper.ClusterConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.kernel.context.SchemaContextsAware;
import org.apache.shardingsphere.kernel.context.SchemaContextsBuilder;
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.metrics.configuration.swapper.MetricsConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.core.facade.OrchestrationFacade;
import org.apache.shardingsphere.orchestration.core.common.yaml.config.YamlOrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.core.common.yaml.swapper.OrchestrationConfigurationYamlSwapper;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.ShardingConfiguration;
import org.apache.shardingsphere.proxy.config.converter.AbstractConfigurationConverter;
import org.apache.shardingsphere.proxy.config.util.DataSourceConverter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.orchestration.schema.ProxyOrchestrationSchemaContexts;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * Orchestration configuration converter.
 */
public final class OrchestrationConfigurationConverter extends AbstractConfigurationConverter {
    
    private final OrchestrationFacade orchestrationFacade = OrchestrationFacade.getInstance();
    
    @Override
    public ProxyConfiguration convert(final ShardingConfiguration shardingConfiguration) {
        YamlOrchestrationConfiguration orchestration = shardingConfiguration.getServerConfiguration().getOrchestration();
        Set<String> schemaNames = shardingConfiguration.getRuleConfigurationMap().keySet();
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration();
        orchestrationFacade.init(new OrchestrationConfigurationYamlSwapper().swapToObject(orchestration), schemaNames);
        initOrchestrationConfigurations(shardingConfiguration.getServerConfiguration(), shardingConfiguration.getRuleConfigurationMap(), orchestrationFacade);
        Authentication authentication = orchestrationFacade.getConfigCenter().loadAuthentication();
        Properties properties = orchestrationFacade.getConfigCenter().loadProperties();
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = getDataSourceParametersMap(orchestrationFacade);
        Map<String, Collection<RuleConfiguration>> schemaRules = getSchemaRules(orchestrationFacade);
        ClusterConfiguration clusterConfiguration = orchestrationFacade.getConfigCenter().loadClusterConfiguration();
        proxyConfiguration.setAuthentication(authentication);
        proxyConfiguration.setProps(properties);
        proxyConfiguration.setSchemaDataSources(schemaDataSources);
        proxyConfiguration.setSchemaRules(schemaRules);
        proxyConfiguration.setCluster(clusterConfiguration);
        proxyConfiguration.setMetrics(getMetricsConfiguration(shardingConfiguration.getServerConfiguration().getMetrics()));
        return proxyConfiguration;
    }
    
    @Override
    public SchemaContextsAware contextsAware(final SchemaContextsBuilder builder) throws SQLException {
        return new ProxyOrchestrationSchemaContexts(builder.build());
    }
    
    private void initOrchestrationConfigurations(
            final YamlProxyServerConfiguration serverConfig, final Map<String, YamlProxyRuleConfiguration> ruleConfigs, final OrchestrationFacade orchestrationFacade) {
        if (isEmptyLocalConfiguration(serverConfig, ruleConfigs)) {
            orchestrationFacade.onlineInstance();
        } else {
            orchestrationFacade.onlineInstance(getDataSourceConfigurationMap(ruleConfigs),
                    getRuleConfigurations(ruleConfigs), new AuthenticationYamlSwapper().swapToObject(serverConfig.getAuthentication()), serverConfig.getProps());
        }
        orchestrationFacade.initMetricsConfiguration(Optional.ofNullable(serverConfig.getMetrics()).map(new MetricsConfigurationYamlSwapper()::swapToObject).orElse(null));
        orchestrationFacade.initClusterConfiguration(Optional.ofNullable(serverConfig.getCluster()).map(new ClusterConfigurationYamlSwapper()::swapToObject).orElse(null));
    }
    
    private boolean isEmptyLocalConfiguration(final YamlProxyServerConfiguration serverConfig, final Map<String, YamlProxyRuleConfiguration> ruleConfigs) {
        return ruleConfigs.isEmpty() && null == serverConfig.getAuthentication() && serverConfig.getProps().isEmpty();
    }
    
    private Map<String, Map<String, DataSourceConfiguration>> getDataSourceConfigurationMap(final Map<String, YamlProxyRuleConfiguration> ruleConfigs) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>();
        for (Map.Entry<String, YamlProxyRuleConfiguration> entry : ruleConfigs.entrySet()) {
            result.put(entry.getKey(), DataSourceConverter.getDataSourceConfigurationMap(DataSourceConverter.getDataSourceParameterMap2(entry.getValue().getDataSources())));
        }
        return result;
    }
    
    private Map<String, Map<String, DataSourceParameter>> getDataSourceParametersMap(final OrchestrationFacade orchestrationFacade) {
        Map<String, Map<String, DataSourceParameter>> result = new LinkedHashMap<>();
        for (String each : orchestrationFacade.getConfigCenter().getAllSchemaNames()) {
            result.put(each, DataSourceConverter.getDataSourceParameterMap(orchestrationFacade.getConfigCenter().loadDataSourceConfigurations(each)));
        }
        return result;
    }
    
    private Map<String, Collection<RuleConfiguration>> getSchemaRules(final OrchestrationFacade orchestrationFacade) {
        Map<String, Collection<RuleConfiguration>> result = new LinkedHashMap<>();
        for (String each : orchestrationFacade.getConfigCenter().getAllSchemaNames()) {
            result.put(each, orchestrationFacade.getConfigCenter().loadRuleConfigurations(each));
        }
        return result;
    }
    
    @Override
    public void close() {
        if (null != orchestrationFacade) {
            orchestrationFacade.close();
        }
    }
}
