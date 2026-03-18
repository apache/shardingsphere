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

package org.apache.shardingsphere.proxy.backend.util;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.distsql.handler.validate.DistSQLDataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationCheckEngine;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.MissingRequiredDatabaseException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapCreator;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRuleBuilder;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyDataSourceConfigurationSwapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Yaml database configuration import executor.
 */
@RequiredArgsConstructor
public final class YamlDatabaseConfigurationImportExecutor {
    
    private final YamlProxyDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlProxyDataSourceConfigurationSwapper();
    
    private final DistSQLDataSourcePoolPropertiesValidator validateHandler = new DistSQLDataSourcePoolPropertiesValidator();
    
    private final ContextManager contextManager;
    
    /**
     * Import proxy database from yaml configuration.
     *
     * @param yamlConfig yaml proxy database configuration
     */
    public void importDatabaseConfiguration(final YamlProxyDatabaseConfiguration yamlConfig) {
        String databaseName = yamlConfig.getDatabaseName();
        checkDatabase(databaseName);
        checkDataSources(databaseName, yamlConfig.getDataSources());
        addDatabase(databaseName);
        try {
            importDataSources(databaseName, yamlConfig.getDataSources());
            importRules(databaseName, yamlConfig.getRules());
        } catch (final ShardingSphereSQLException ex) {
            dropDatabase(contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName));
            throw ex;
        }
    }
    
    private void checkDataSources(final String databaseName, final Map<String, YamlProxyDataSourceConfiguration> dataSources) {
        ShardingSpherePreconditions.checkNotEmpty(dataSources, () -> new EmptyStorageUnitException(databaseName));
    }
    
    private void checkDatabase(final String databaseName) {
        ShardingSpherePreconditions.checkNotEmpty(databaseName, MissingRequiredDatabaseException::new);
        ShardingSpherePreconditions.checkState(!contextManager.getMetaDataContexts().getMetaData().containsDatabase(databaseName), () -> new DatabaseCreateExistsException(databaseName));
    }
    
    private void addDatabase(final String databaseName) {
        contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService().createDatabase(databaseName);
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), contextManager.getMetaDataContexts().getMetaData().getProps());
        contextManager.getMetaDataContexts().getMetaData().addDatabase(databaseName, protocolType, contextManager.getMetaDataContexts().getMetaData().getProps());
    }
    
    private void importDataSources(final String databaseName, final Map<String, YamlProxyDataSourceConfiguration> yamlDataSourceMap) {
        Map<String, DataSourcePoolProperties> propsMap = new LinkedHashMap<>(yamlDataSourceMap.size(), 1F);
        for (Entry<String, YamlProxyDataSourceConfiguration> entry : yamlDataSourceMap.entrySet()) {
            DataSourceConfiguration dataSourceConfig = dataSourceConfigSwapper.swap(entry.getValue());
            propsMap.put(entry.getKey(), DataSourcePoolPropertiesCreator.create(dataSourceConfig));
        }
        validateHandler.validate(propsMap);
        contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService().registerStorageUnits(databaseName, propsMap);
        Map<String, StorageUnit> storageUnits = contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData().getStorageUnits();
        boolean isInstanceConnectionEnabled = contextManager.getMetaDataContexts().getMetaData().getTemporaryProps().<Boolean>getValue(TemporaryConfigurationPropertyKey.INSTANCE_CONNECTION_ENABLED);
        Map<String, StorageNode> toBeAddedStorageNode = StorageUnitNodeMapCreator.create(propsMap, isInstanceConnectionEnabled);
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            storageUnits.put(entry.getKey(), new StorageUnit(toBeAddedStorageNode.get(entry.getKey()), entry.getValue(), DataSourcePoolCreator.create(entry.getValue())));
        }
    }
    
    private void importRules(final String databaseName, final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        if (null == yamlRuleConfigs || yamlRuleConfigs.isEmpty()) {
            return;
        }
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>();
        MetaDataContexts metaDataContexts = contextManager.getMetaDataContexts();
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        swapToRuleConfigs(yamlRuleConfigs).values().forEach(each -> addRule(ruleConfigs, each, database));
        contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseRuleService().persist(metaDataContexts.getMetaData().getDatabase(databaseName).getName(), ruleConfigs);
    }
    
    private void addRule(final Collection<RuleConfiguration> ruleConfigs, final RuleConfiguration ruleConfig, final ShardingSphereDatabase database) {
        DatabaseRuleConfigurationCheckEngine.check(ruleConfig, database);
        ruleConfigs.add(ruleConfig);
        database.getRuleMetaData().getRules().add(buildRule(ruleConfig, database));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private ShardingSphereRule buildRule(final RuleConfiguration ruleConfig, final ShardingSphereDatabase database) {
        DatabaseRuleBuilder ruleBuilder = OrderedSPILoader.getServices(DatabaseRuleBuilder.class, Collections.singleton(ruleConfig)).get(ruleConfig);
        ComputeNodeInstanceContext computeNodeInstanceContext = contextManager.getComputeNodeInstanceContext();
        return ruleBuilder.build(ruleConfig, database.getName(), database.getProtocolType(), database.getResourceMetaData(), database.getRuleMetaData().getRules(), computeNodeInstanceContext);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<Integer, RuleConfiguration> swapToRuleConfigs(final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        Map<Integer, RuleConfiguration> result = new TreeMap<>(Comparator.reverseOrder());
        for (YamlRuleConfiguration each : yamlRuleConfigs) {
            YamlRuleConfigurationSwapper swapper = OrderedSPILoader.getServicesByClass(YamlRuleConfigurationSwapper.class, Collections.singleton(each.getRuleConfigurationType()))
                    .get(each.getRuleConfigurationType());
            result.put(swapper.getOrder(), (RuleConfiguration) swapper.swapToObject(each));
        }
        return result;
    }
    
    private void dropDatabase(final ShardingSphereDatabase database) {
        contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService().dropDatabase(database);
    }
}
