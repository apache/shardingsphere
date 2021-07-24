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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLServerInfo;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.config.persist.ConfigCenter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.database.DatabaseServerInfo;
import org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy;
import org.apache.shardingsphere.proxy.initializer.BootstrapInitializer;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionContexts;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
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
    
    private final ShardingSphereProxy shardingSphereProxy = new ShardingSphereProxy();
    
    @Override
    public final void init(final YamlProxyConfiguration yamlConfig, final int port) throws SQLException {
        ProxyConfiguration proxyConfig = getProxyConfiguration(yamlConfig);
        MetaDataContexts metaDataContexts = decorateMetaDataContexts(createMetaDataContexts(proxyConfig));
        String xaTransactionMangerType = metaDataContexts.getProps().getValue(ConfigurationPropertyKey.XA_TRANSACTION_MANAGER_TYPE);
        TransactionContexts transactionContexts = decorateTransactionContexts(createTransactionContexts(metaDataContexts), xaTransactionMangerType);
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        setDatabaseServerInfo();
        initScalingWorker(yamlConfig);
        shardingSphereProxy.start(port);
    }
    
    private MetaDataContexts createMetaDataContexts(final ProxyConfiguration proxyConfig) throws SQLException {
        Map<String, Map<String, DataSource>> dataSourcesMap = createDataSourcesMap(proxyConfig.getSchemaDataSources());
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(
                dataSourcesMap, proxyConfig.getSchemaRules(), proxyConfig.getGlobalRules(), proxyConfig.getProps());
        return metaDataContextsBuilder.build();
    }
    
    private static Map<String, Map<String, DataSource>> createDataSourcesMap(final Map<String, Map<String, DataSourceParameter>> schemaDataSources) {
        return schemaDataSources.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> createDataSources(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static Map<String, DataSource> createDataSources(final Map<String, DataSourceParameter> dataSourceParameters) {
        Map<String, DataSourceConfiguration> dataSourceConfigMap = DataSourceParameterConverter.getDataSourceConfigurationMap(dataSourceParameters);
        return DataSourceConverter.getDataSourceMap(dataSourceConfigMap);
    }
    
    private TransactionContexts createTransactionContexts(final MetaDataContexts metaDataContexts) {
        Map<String, ShardingTransactionManagerEngine> transactionManagerEngines = new HashMap<>(metaDataContexts.getAllSchemaNames().size(), 1);
        String xaTransactionMangerType = metaDataContexts.getProps().getValue(ConfigurationPropertyKey.XA_TRANSACTION_MANAGER_TYPE);
        for (String each : metaDataContexts.getAllSchemaNames()) {
            ShardingTransactionManagerEngine engine = new ShardingTransactionManagerEngine();
            ShardingSphereResource resource = metaDataContexts.getMetaData(each).getResource();
            engine.init(resource.getDatabaseType(), resource.getDataSources(), xaTransactionMangerType);
            transactionManagerEngines.put(each, engine);
        }
        return new StandardTransactionContexts(transactionManagerEngines);
    }
    
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
    
    protected abstract ProxyConfiguration getProxyConfiguration(YamlProxyConfiguration yamlConfig);
    
    protected abstract MetaDataContexts decorateMetaDataContexts(MetaDataContexts metaDataContexts);
    
    protected abstract TransactionContexts decorateTransactionContexts(TransactionContexts transactionContexts, String xaTransactionMangerType);
    
    protected abstract void initScalingWorker(YamlProxyConfiguration yamlConfig);
    
    protected final void persistConfigurations(final ConfigCenter configCenter, final YamlProxyConfiguration yamlConfig, final boolean overwrite) {
        YamlProxyServerConfiguration serverConfig = yamlConfig.getServerConfiguration();
        Map<String, YamlProxyRuleConfiguration> ruleConfigs = yamlConfig.getRuleConfigurations();
        if (!isEmptyLocalConfiguration(serverConfig, ruleConfigs)) {
            configCenter.persistConfigurations(getDataSourceConfigurationMap(ruleConfigs),
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
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(globalRuleConfigs);
    }
    
    protected final ProxyConfiguration loadProxyConfiguration(final ConfigCenter configCenter) {
        Collection<String> schemaNames = configCenter.getSchemaMetaDataService().loadAllNames();
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = loadDataSourceParametersMap(configCenter, schemaNames);
        Map<String, Collection<RuleConfiguration>> schemaRuleConfigs = loadSchemaRules(configCenter, schemaNames);
        Collection<RuleConfiguration> globalRuleConfigs = configCenter.getGlobalRuleService().load();
        Properties props = configCenter.getPropsService().load();
        return new ProxyConfiguration(schemaDataSources, schemaRuleConfigs, globalRuleConfigs, props);
    }
    
    private Map<String, Map<String, DataSourceParameter>> loadDataSourceParametersMap(final ConfigCenter configCenter, final Collection<String> schemaNames) {
        return schemaNames.stream().collect(Collectors.toMap(each -> each, 
            each -> DataSourceParameterConverter.getDataSourceParameterMap(configCenter.getDataSourceService().load(each)), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private Map<String, Collection<RuleConfiguration>> loadSchemaRules(final ConfigCenter configCenter, final Collection<String> schemaNames) {
        return schemaNames.stream().collect(Collectors.toMap(each -> each, each -> configCenter.getSchemaRuleService().load(each), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
}
