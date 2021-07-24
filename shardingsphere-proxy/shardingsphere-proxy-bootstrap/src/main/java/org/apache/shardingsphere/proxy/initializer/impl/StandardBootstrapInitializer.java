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

package org.apache.shardingsphere.proxy.initializer.impl;

import org.apache.shardingsphere.governance.context.transaction.GovernanceTransactionContexts;
import org.apache.shardingsphere.governance.core.yaml.pojo.YamlGovernanceConfiguration;
import org.apache.shardingsphere.governance.core.yaml.swapper.GovernanceConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.config.persist.ConfigCenter;
import org.apache.shardingsphere.infra.config.persist.repository.ConfigCenterRepository;
import org.apache.shardingsphere.infra.config.persist.repository.LocalConfigCenterRepository;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.swapper.YamlProxyConfigurationSwapper;
import org.apache.shardingsphere.scaling.core.api.ScalingWorker;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Standard bootstrap initializer.
 */
public final class StandardBootstrapInitializer extends AbstractBootstrapInitializer {
    
    private volatile ConfigCenter configCenter;
    
    @Override
    protected ProxyConfiguration getProxyConfiguration(final YamlProxyConfiguration yamlConfig) {
        // TODO load from SPI
        ConfigCenterRepository repository = new LocalConfigCenterRepository();
        configCenter = new ConfigCenter(repository);
        initConfigurations(yamlConfig);
        ProxyConfiguration result = loadProxyConfiguration();
        return (result.getSchemaDataSources().isEmpty()) ? new YamlProxyConfigurationSwapper().swap(yamlConfig) : result;
    }
    
    private void initConfigurations(final YamlProxyConfiguration yamlConfig) {
        YamlProxyServerConfiguration serverConfig = yamlConfig.getServerConfiguration();
        Map<String, YamlProxyRuleConfiguration> ruleConfigs = yamlConfig.getRuleConfigurations();
        if (!isEmptyLocalConfiguration(serverConfig, ruleConfigs)) {
            configCenter.persistConfigurations(getDataSourceConfigurationMap(ruleConfigs),
                    getSchemaRuleConfigurations(ruleConfigs), getGlobalRuleConfigurations(serverConfig.getRules()), serverConfig.getProps(), false);
        }
    }
    
    private boolean isEmptyLocalConfiguration(final YamlProxyServerConfiguration serverConfig, final Map<String, YamlProxyRuleConfiguration> ruleConfigs) {
        return ruleConfigs.isEmpty() && serverConfig.getRules().isEmpty() && serverConfig.getProps().isEmpty();
    }
    
    private Map<String, Map<String, DataSourceConfiguration>> getDataSourceConfigurationMap(final Map<String, YamlProxyRuleConfiguration> ruleConfigs) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>(ruleConfigs.size(), 1);
        for (Entry<String, YamlProxyRuleConfiguration> entry : ruleConfigs.entrySet()) {
            result.put(entry.getKey(),
                    DataSourceParameterConverter.getDataSourceConfigurationMap(DataSourceParameterConverter.getDataSourceParameterMapFromYamlConfiguration(entry.getValue().getDataSources())));
        }
        return result;
    }
    
    private Map<String, Collection<RuleConfiguration>> getSchemaRuleConfigurations(final Map<String, YamlProxyRuleConfiguration> yamlRuleConfigs) {
        YamlRuleConfigurationSwapperEngine swapperEngine = new YamlRuleConfigurationSwapperEngine();
        return yamlRuleConfigs.entrySet().stream().collect(Collectors.toMap(Entry::getKey,
            entry -> swapperEngine.swapToRuleConfigurations(entry.getValue().getRules()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private Collection<RuleConfiguration> getGlobalRuleConfigurations(final Collection<YamlRuleConfiguration> globalRuleConfigs) {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(globalRuleConfigs);
    }
    
    private ProxyConfiguration loadProxyConfiguration() {
        Collection<String> schemaNames = configCenter.getSchemaMetaDataService().loadAllNames();
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = loadDataSourceParametersMap(schemaNames);
        Map<String, Collection<RuleConfiguration>> schemaRuleConfigs = loadSchemaRules(schemaNames);
        Properties props = configCenter.getPropsService().load();
        // TODO load global rules from reg center
        Collection<RuleConfiguration> globalRuleConfigs = configCenter.getGlobalRuleService().load();
        return new ProxyConfiguration(schemaDataSources, schemaRuleConfigs, globalRuleConfigs, props);
    }
    
    private Map<String, Map<String, DataSourceParameter>> loadDataSourceParametersMap(final Collection<String> schemaNames) {
        return schemaNames.stream()
            .collect(Collectors.toMap(each -> each, 
                each -> DataSourceParameterConverter.getDataSourceParameterMap(configCenter.getDataSourceService().load(each)),
                (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private Map<String, Collection<RuleConfiguration>> loadSchemaRules(final Collection<String> schemaNames) {
        return schemaNames.stream().collect(
                Collectors.toMap(each -> each, each -> configCenter.getSchemaRuleService().load(each), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    @Override
    protected MetaDataContexts decorateMetaDataContexts(final MetaDataContexts metaDataContexts) {
        return metaDataContexts;
    }
    
    @Override
    protected TransactionContexts decorateTransactionContexts(final TransactionContexts transactionContexts, final String xaTransactionMangerType) {
        return new GovernanceTransactionContexts(transactionContexts, xaTransactionMangerType);
    }
    
    @Override
    protected void initScalingWorker(final YamlProxyConfiguration yamlConfig) {
        Optional<ServerConfiguration> scalingConfig = getScalingConfiguration(yamlConfig);
        scalingConfig.ifPresent(optional -> initScaling(yamlConfig.getServerConfiguration().getGovernance(), optional));
    }
    
    private void initScaling(final YamlGovernanceConfiguration governanceConfig, final ServerConfiguration scalingConfig) {
        scalingConfig.setGovernanceConfig(new GovernanceConfigurationYamlSwapper().swapToObject(governanceConfig));
        ScalingContext.getInstance().init(scalingConfig);
        ScalingWorker.init();
    }
}
