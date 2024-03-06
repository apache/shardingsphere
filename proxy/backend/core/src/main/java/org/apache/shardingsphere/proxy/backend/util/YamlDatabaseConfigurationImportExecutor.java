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

import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.distsql.handler.engine.update.ral.rule.spi.database.ImportRuleConfigurationProvider;
import org.apache.shardingsphere.distsql.handler.exception.datasource.MissingRequiredDataSourcesException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.kernel.category.DistSQLException;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapCreator;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyDataSourceConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.EncryptRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.MaskRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.ShadowRuleConfigurationImportChecker;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Yaml database configuration import executor.
 */
public final class YamlDatabaseConfigurationImportExecutor {
    
    private final EncryptRuleConfigurationImportChecker encryptRuleConfigImportChecker = new EncryptRuleConfigurationImportChecker();
    
    private final ShadowRuleConfigurationImportChecker shadowRuleConfigImportChecker = new ShadowRuleConfigurationImportChecker();
    
    private final MaskRuleConfigurationImportChecker maskRuleConfigImportChecker = new MaskRuleConfigurationImportChecker();
    
    private final YamlProxyDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlProxyDataSourceConfigurationSwapper();
    
    private final DataSourcePoolPropertiesValidator validateHandler = new DataSourcePoolPropertiesValidator();
    
    /**
     * Import proxy database from yaml configuration.
     *
     * @param yamlConfig yaml proxy database configuration
     */
    public void importDatabaseConfiguration(final YamlProxyDatabaseConfiguration yamlConfig) {
        String databaseName = yamlConfig.getDatabaseName();
        checkDatabase(databaseName);
        checkDataSource(yamlConfig.getDataSources());
        addDatabase(databaseName);
        addResources(databaseName, yamlConfig.getDataSources());
        try {
            addRules(databaseName, yamlConfig.getRules());
        } catch (final DistSQLException ex) {
            dropDatabase(databaseName);
            throw ex;
        }
    }
    
    private void checkDatabase(final String databaseName) {
        ShardingSpherePreconditions.checkNotNull(databaseName, () -> new UnsupportedSQLOperationException("Property `databaseName` in imported config is required"));
        if (ProxyContext.getInstance().databaseExists(databaseName)) {
            ShardingSpherePreconditions.checkState(ProxyContext.getInstance().getContextManager().getDatabase(databaseName).getResourceMetaData().getStorageUnits().isEmpty(),
                    () -> new UnsupportedSQLOperationException(String.format("Database `%s` exists and is not empty，overwrite is not supported", databaseName)));
        }
    }
    
    private void checkDataSource(final Map<String, YamlProxyDataSourceConfiguration> dataSources) {
        ShardingSpherePreconditions.checkState(!dataSources.isEmpty(), () -> new MissingRequiredDataSourcesException("Data source configurations in imported config is required"));
    }
    
    private void addDatabase(final String databaseName) {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        contextManager.getInstanceContext().getModeContextManager().createDatabase(databaseName);
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), contextManager.getMetaDataContexts().getMetaData().getProps());
        contextManager.getMetaDataContexts().getMetaData().addDatabase(databaseName, protocolType, contextManager.getMetaDataContexts().getMetaData().getProps());
    }
    
    private void addResources(final String databaseName, final Map<String, YamlProxyDataSourceConfiguration> yamlDataSourceMap) {
        Map<String, DataSourcePoolProperties> propsMap = new LinkedHashMap<>(yamlDataSourceMap.size(), 1F);
        for (Entry<String, YamlProxyDataSourceConfiguration> entry : yamlDataSourceMap.entrySet()) {
            DataSourceConfiguration dataSourceConfig = dataSourceConfigSwapper.swap(entry.getValue());
            propsMap.put(entry.getKey(), DataSourcePoolPropertiesCreator.create(dataSourceConfig));
        }
        validateHandler.validate(propsMap);
        try {
            ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().registerStorageUnits(databaseName, propsMap);
        } catch (final SQLException ex) {
            throw new InvalidStorageUnitsException(Collections.singleton(ex.getMessage()));
        }
        Map<String, StorageUnit> storageUnits = ProxyContext.getInstance().getContextManager()
                .getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData().getStorageUnits();
        Map<String, StorageNode> toBeAddedStorageNode = StorageUnitNodeMapCreator.create(propsMap);
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            storageUnits.put(entry.getKey(), new StorageUnit(toBeAddedStorageNode.get(entry.getKey()), entry.getValue(), DataSourcePoolCreator.create(entry.getValue())));
        }
    }
    
    private void addRules(final String databaseName, final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        if (null == yamlRuleConfigs || yamlRuleConfigs.isEmpty()) {
            return;
        }
        Collection<RuleConfiguration> allRuleConfigs = new LinkedList<>();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        swapToRuleConfigs(yamlRuleConfigs).values().forEach(each -> addRules(allRuleConfigs, each, database));
        metaDataContexts.getPersistService().getDatabaseRulePersistService().persist(metaDataContexts.getMetaData().getDatabase(databaseName).getName(), allRuleConfigs);
    }
    
    private void addRules(final Collection<RuleConfiguration> allRuleConfigs, final Collection<RuleConfiguration> ruleConfigs, final ShardingSphereDatabase database) {
        RuleConfiguration ruleConfig = ruleConfigs.stream().findFirst().orElse(null);
        if (null == ruleConfig) {
            return;
        }
        InstanceContext instanceContext = ProxyContext.getInstance().getContextManager().getInstanceContext();
        if (ruleConfig instanceof ShardingRuleConfiguration || ruleConfig instanceof ReadwriteSplittingRuleConfiguration) {
            ImportRuleConfigurationProvider importRuleConfigurationProvider = TypedSPILoader.getService(ImportRuleConfigurationProvider.class, ruleConfig.getClass());
            importRuleConfigurationProvider.check(database, ruleConfig);
            allRuleConfigs.add(ruleConfig);
            database.getRuleMetaData().getRules().add(importRuleConfigurationProvider.build(database, ruleConfig, instanceContext));
        } else if (ruleConfig instanceof EncryptRuleConfiguration) {
            ruleConfigs.forEach(each -> addEncryptRuleConfiguration((EncryptRuleConfiguration) each, allRuleConfigs, database));
        } else if (ruleConfig instanceof ShadowRuleConfiguration) {
            ruleConfigs.forEach(each -> addShadowRuleConfiguration((ShadowRuleConfiguration) each, allRuleConfigs, database));
        } else if (ruleConfig instanceof MaskRuleConfiguration) {
            ruleConfigs.forEach(each -> addMaskRuleConfiguration((MaskRuleConfiguration) each, allRuleConfigs, database));
        } else if (ruleConfig instanceof BroadcastRuleConfiguration) {
            ruleConfigs.forEach(each -> addBroadcastRuleConfiguration((BroadcastRuleConfiguration) each, allRuleConfigs, database));
        } else if (ruleConfig instanceof SingleRuleConfiguration) {
            ruleConfigs.forEach(each -> addSingleRuleConfiguration((SingleRuleConfiguration) each, allRuleConfigs, database));
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<Integer, Collection<RuleConfiguration>> swapToRuleConfigs(final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        Map<Integer, Collection<RuleConfiguration>> result = new TreeMap<>(Comparator.reverseOrder());
        for (YamlRuleConfiguration each : yamlRuleConfigs) {
            YamlRuleConfigurationSwapper swapper = OrderedSPILoader.getServicesByClass(YamlRuleConfigurationSwapper.class, Collections.singleton(each.getRuleConfigurationType()))
                    .get(each.getRuleConfigurationType());
            result.computeIfAbsent(swapper.getOrder(), key -> new LinkedList<>());
            result.get(swapper.getOrder()).add((RuleConfiguration) swapper.swapToObject(each));
        }
        return result;
    }
    
    private void addEncryptRuleConfiguration(final EncryptRuleConfiguration encryptRuleConfig, final Collection<RuleConfiguration> allRuleConfigs, final ShardingSphereDatabase database) {
        encryptRuleConfigImportChecker.check(database, encryptRuleConfig);
        allRuleConfigs.add(encryptRuleConfig);
        database.getRuleMetaData().getRules().add(new EncryptRule(database.getName(), encryptRuleConfig));
    }
    
    private void addShadowRuleConfiguration(final ShadowRuleConfiguration shadowRuleConfig, final Collection<RuleConfiguration> allRuleConfigs, final ShardingSphereDatabase database) {
        shadowRuleConfigImportChecker.check(database, shadowRuleConfig);
        allRuleConfigs.add(shadowRuleConfig);
        database.getRuleMetaData().getRules().add(new ShadowRule(shadowRuleConfig));
    }
    
    private void addMaskRuleConfiguration(final MaskRuleConfiguration maskRuleConfig, final Collection<RuleConfiguration> allRuleConfigs, final ShardingSphereDatabase database) {
        maskRuleConfigImportChecker.check(database, maskRuleConfig);
        allRuleConfigs.add(maskRuleConfig);
        database.getRuleMetaData().getRules().add(new MaskRule(maskRuleConfig));
    }
    
    private void addBroadcastRuleConfiguration(final BroadcastRuleConfiguration broadcastRuleConfig, final Collection<RuleConfiguration> allRuleConfigs, final ShardingSphereDatabase database) {
        allRuleConfigs.add(broadcastRuleConfig);
        database.getRuleMetaData().getRules().add(new BroadcastRule(broadcastRuleConfig, database.getName(),
                database.getResourceMetaData().getStorageUnits().entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                database.getRuleMetaData().getRules()));
    }
    
    private void addSingleRuleConfiguration(final SingleRuleConfiguration singleRuleConfig, final Collection<RuleConfiguration> allRuleConfigs, final ShardingSphereDatabase database) {
        allRuleConfigs.add(singleRuleConfig);
        database.getRuleMetaData().getRules().add(
                new SingleRule(singleRuleConfig, database.getName(), database.getProtocolType(),
                        database.getResourceMetaData().getStorageUnits().entrySet().stream()
                                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                        database.getRuleMetaData().getRules()));
    }
    
    private void dropDatabase(final String databaseName) {
        ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().dropDatabase(databaseName);
    }
}
