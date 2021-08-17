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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLServerInfo;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.condition.PreConditionRuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.infra.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.swapper.YamlProxyConfigurationSwapper;
import org.apache.shardingsphere.proxy.database.DatabaseServerInfo;
import org.apache.shardingsphere.proxy.initializer.BootstrapInitializer;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Abstract bootstrap initializer.
 */
@Slf4j
public abstract class AbstractBootstrapInitializer implements BootstrapInitializer {
    
    private final ShardingSphereMode mode;
    
    @Getter
    private final DistMetaDataPersistService distMetaDataPersistService;
    
    public AbstractBootstrapInitializer(final ShardingSphereMode mode) {
        this.mode = mode;
        distMetaDataPersistService = mode.getPersistRepository().isPresent() ? new DistMetaDataPersistService(mode.getPersistRepository().get()) : null;
    }
    
    @Override
    public final void init(final YamlProxyConfiguration yamlConfig) throws SQLException {
        ProxyConfiguration proxyConfig = new YamlProxyConfigurationSwapper().swap(yamlConfig);
        ContextManager contextManager = createContextManager(mode, proxyConfig);
        ProxyContext.getInstance().init(contextManager);
        setDatabaseServerInfo();
        initScalingInternal(yamlConfig);
        postInit(yamlConfig);
    }
    
    protected abstract ContextManager createContextManager(ShardingSphereMode mode, ProxyConfiguration proxyConfig) throws SQLException;
    
    private void setDatabaseServerInfo() {
        findBackendDataSource().ifPresent(dataSourceSample -> {
            DatabaseServerInfo databaseServerInfo = new DatabaseServerInfo(dataSourceSample);
            log.info(databaseServerInfo.toString());
            switch (databaseServerInfo.getDatabaseName()) {
                case "MySQL":
                    MySQLServerInfo.setServerVersion(databaseServerInfo.getDatabaseVersion());
                    break;
                case "PostgreSQL":
                    PostgreSQLServerInfo.setServerVersion(databaseServerInfo.getDatabaseVersion());
                    break;
                default:
            }
        });
    }
    
    private Optional<DataSource> findBackendDataSource() {
        for (String each : ProxyContext.getInstance().getAllSchemaNames()) {
            return ProxyContext.getInstance().getMetaData(each).getResource().getDataSources().values().stream().findFirst();
        }
        return Optional.empty();
    }
    
    protected final Optional<ServerConfiguration> getScalingConfiguration(final YamlProxyConfiguration yamlConfig) {
        if (null == yamlConfig.getServerConfiguration().getScaling()) {
            return Optional.empty();
        }
        ServerConfiguration result = new ServerConfiguration();
        result.setBlockQueueSize(yamlConfig.getServerConfiguration().getScaling().getBlockQueueSize());
        result.setWorkerThread(yamlConfig.getServerConfiguration().getScaling().getWorkerThread());
        return Optional.of(result);
    }
    
    private void initScalingInternal(final YamlProxyConfiguration yamlConfig) {
        log.debug("Init scaling");
        initScaling(yamlConfig);
    }
    
    protected abstract void initScaling(YamlProxyConfiguration yamlConfig);
    
    protected final void persistConfigurations(final YamlProxyConfiguration yamlConfig, final boolean overwrite) {
        YamlProxyServerConfiguration serverConfig = yamlConfig.getServerConfiguration();
        Map<String, YamlProxyRuleConfiguration> ruleConfigs = yamlConfig.getRuleConfigurations();
        if (!isEmptyLocalConfiguration(serverConfig, ruleConfigs) && null != distMetaDataPersistService) {
            distMetaDataPersistService.persistConfigurations(getDataSourceConfigurationMap(ruleConfigs),
                    getSchemaRuleConfigurations(ruleConfigs), getGlobalRuleConfigurations(serverConfig.getRules()), serverConfig.getProps(), overwrite);
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
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(globalRuleConfigs).stream().filter(
            each -> !(each instanceof PreConditionRuleConfiguration)).collect(Collectors.toList());
    }
    
    protected final ProxyConfiguration loadProxyConfiguration() {
        Collection<String> schemaNames = distMetaDataPersistService.getSchemaMetaDataService().loadAllNames();
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = loadDataSourceParametersMap(schemaNames);
        Map<String, Collection<RuleConfiguration>> schemaRuleConfigs = loadSchemaRules(schemaNames);
        Collection<RuleConfiguration> globalRuleConfigs = distMetaDataPersistService.getGlobalRuleService().load();
        Properties props = distMetaDataPersistService.getPropsService().load();
        return new ProxyConfiguration(schemaDataSources, schemaRuleConfigs, globalRuleConfigs, props);
    }
    
    private Map<String, Map<String, DataSourceParameter>> loadDataSourceParametersMap(final Collection<String> schemaNames) {
        return schemaNames.stream().collect(Collectors.toMap(each -> each, 
            each -> DataSourceParameterConverter.getDataSourceParameterMap(distMetaDataPersistService.getDataSourceService()
                .load(each)), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private Map<String, Collection<RuleConfiguration>> loadSchemaRules(final Collection<String> schemaNames) {
        return schemaNames.stream().collect(Collectors.toMap(
            each -> each, each -> distMetaDataPersistService.getSchemaRuleService().load(each), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    protected void postInit(final YamlProxyConfiguration yamlConfig) {
    }
}
