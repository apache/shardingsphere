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

package org.apache.shardingsphere.mode.metadata.manager;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.PartialRuleUpdateSupported;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.metadata.factory.ExternalMetaDataFactory;
import org.apache.shardingsphere.metadata.factory.InternalMetaDataFactory;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Database rule configuration manager.
 */
@Slf4j
public final class DatabaseRuleConfigurationManager {
    
    private final AtomicReference<MetaDataContexts> metaDataContexts;
    
    private final ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private final MetaDataPersistService metaDataPersistService;
    
    public DatabaseRuleConfigurationManager(final AtomicReference<MetaDataContexts> metaDataContexts, final ComputeNodeInstanceContext computeNodeInstanceContext,
                                            final PersistRepository repository) {
        this.metaDataContexts = metaDataContexts;
        this.computeNodeInstanceContext = computeNodeInstanceContext;
        metaDataPersistService = new MetaDataPersistService(repository);
    }
    
    /**
     * Alter rule configuration.
     *
     * @param databaseName database name
     * @param ruleConfig rule configurations
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public synchronized void alterRuleConfiguration(final String databaseName, final RuleConfiguration ruleConfig) {
        ShardingSphereDatabase database = metaDataContexts.get().getMetaData().getDatabase(databaseName);
        Collection<ShardingSphereRule> rules = new LinkedList<>(database.getRuleMetaData().getRules());
        Optional<ShardingSphereRule> toBeChangedRule = rules.stream().filter(each -> each.getConfiguration().getClass().equals(ruleConfig.getClass())).findFirst();
        if (toBeChangedRule.isPresent() && toBeChangedRule.get() instanceof PartialRuleUpdateSupported && ((PartialRuleUpdateSupported) toBeChangedRule.get()).partialUpdate(ruleConfig)) {
            ((PartialRuleUpdateSupported) toBeChangedRule.get()).updateConfiguration(ruleConfig);
            return;
        }
        try {
            rules.removeIf(each -> each.getConfiguration().getClass().isAssignableFrom(ruleConfig.getClass()));
            rules.addAll(DatabaseRulesBuilder.build(databaseName, database.getProtocolType(), database.getRuleMetaData().getRules(),
                    ruleConfig, computeNodeInstanceContext, database.getResourceMetaData()));
            refreshMetadata(databaseName, database, rules);
        } catch (final SQLException ex) {
            log.error("Alter database: {} rule configurations failed", databaseName, ex);
        }
    }
    
    /**
     * Drop rule configuration.
     *
     * @param databaseName database name
     * @param ruleConfig rule configurations
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public synchronized void dropRuleConfiguration(final String databaseName, final RuleConfiguration ruleConfig) {
        ShardingSphereDatabase database = metaDataContexts.get().getMetaData().getDatabase(databaseName);
        Collection<ShardingSphereRule> rules = new LinkedList<>(database.getRuleMetaData().getRules());
        Optional<ShardingSphereRule> toBeChangedRule = rules.stream().filter(each -> each.getConfiguration().getClass().equals(ruleConfig.getClass())).findFirst();
        if (toBeChangedRule.isPresent() && toBeChangedRule.get() instanceof PartialRuleUpdateSupported && ((PartialRuleUpdateSupported) toBeChangedRule.get()).partialUpdate(ruleConfig)) {
            ((PartialRuleUpdateSupported) toBeChangedRule.get()).updateConfiguration(ruleConfig);
            return;
        }
        try {
            rules.removeIf(each -> each.getConfiguration().getClass().isAssignableFrom(ruleConfig.getClass()));
            if (!((DatabaseRuleConfiguration) ruleConfig).isEmpty()) {
                rules.addAll(DatabaseRulesBuilder.build(databaseName, database.getProtocolType(), database.getRuleMetaData().getRules(),
                        ruleConfig, computeNodeInstanceContext, database.getResourceMetaData()));
            }
            refreshMetadata(databaseName, database, rules);
        } catch (final SQLException ex) {
            log.error("Drop database: {} rule configurations failed", databaseName, ex);
        }
    }
    
    private void refreshMetadata(final String databaseName, final ShardingSphereDatabase database, final Collection<ShardingSphereRule> rules) throws SQLException {
        database.getRuleMetaData().getRules().clear();
        database.getRuleMetaData().getRules().addAll(rules);
        MetaDataContexts reloadMetaDataContexts = createMetaDataContextsByAlterRule(databaseName, database.getRuleMetaData().getConfigurations());
        metaDataContexts.set(reloadMetaDataContexts);
        metaDataContexts.get().getMetaData().getDatabase(databaseName).getSchemas().putAll(buildShardingSphereSchemas(metaDataContexts.get().getMetaData().getDatabase(databaseName)));
    }
    
    private MetaDataContexts createMetaDataContextsByAlterRule(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Map<String, ShardingSphereDatabase> changedDatabases = createChangedDatabases(databaseName, false, null, ruleConfigs);
        return newMetaDataContexts(new ShardingSphereMetaData(changedDatabases, metaDataContexts.get().getMetaData().getGlobalResourceMetaData(),
                metaDataContexts.get().getMetaData().getGlobalRuleMetaData(), metaDataContexts.get().getMetaData().getProps()));
    }
    
    private ResourceMetaData getEffectiveResourceMetaData(final ShardingSphereDatabase database, final SwitchingResource resource) {
        Map<StorageNode, DataSource> storageNodes = getStorageNodes(database.getResourceMetaData().getDataSources(), resource);
        Map<String, StorageUnit> storageUnits = getStorageUnits(database.getResourceMetaData().getStorageUnits(), resource);
        return new ResourceMetaData(storageNodes, storageUnits);
    }
    
    private Map<StorageNode, DataSource> getStorageNodes(final Map<StorageNode, DataSource> currentStorageNodes, final SwitchingResource resource) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(currentStorageNodes.size(), 1F);
        for (Entry<StorageNode, DataSource> entry : currentStorageNodes.entrySet()) {
            if (null == resource || !resource.getStaleDataSources().containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private Map<String, StorageUnit> getStorageUnits(final Map<String, StorageUnit> currentStorageUnits, final SwitchingResource resource) {
        Map<String, StorageUnit> result = new LinkedHashMap<>(currentStorageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : currentStorageUnits.entrySet()) {
            if (null == resource || !resource.getStaleStorageUnitNames().contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private synchronized Map<String, ShardingSphereDatabase> createChangedDatabases(final String databaseName, final boolean internalLoadMetaData,
                                                                                    final SwitchingResource switchingResource, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        ResourceMetaData effectiveResourceMetaData = getEffectiveResourceMetaData(metaDataContexts.get().getMetaData().getDatabase(databaseName), switchingResource);
        Collection<RuleConfiguration> toBeCreatedRuleConfigs = null == ruleConfigs
                ? metaDataContexts.get().getMetaData().getDatabase(databaseName).getRuleMetaData().getConfigurations()
                : ruleConfigs;
        DatabaseConfiguration toBeCreatedDatabaseConfig = getDatabaseConfiguration(effectiveResourceMetaData, switchingResource, toBeCreatedRuleConfigs);
        ShardingSphereDatabase changedDatabase = createChangedDatabase(metaDataContexts.get().getMetaData().getDatabase(databaseName).getName(), internalLoadMetaData,
                metaDataPersistService, toBeCreatedDatabaseConfig, metaDataContexts.get().getMetaData().getProps(), computeNodeInstanceContext);
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(metaDataContexts.get().getMetaData().getDatabases());
        result.put(databaseName.toLowerCase(), changedDatabase);
        return result;
    }
    
    private DatabaseConfiguration getDatabaseConfiguration(final ResourceMetaData resourceMetaData, final SwitchingResource switchingResource,
                                                           final Collection<RuleConfiguration> toBeCreatedRuleConfigs) {
        Map<String, DataSourcePoolProperties> propsMap = null == switchingResource
                ? resourceMetaData.getStorageUnits().entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new))
                : switchingResource.getMergedDataSourcePoolPropertiesMap();
        return new DataSourceProvidedDatabaseConfiguration(getMergedStorageNodeDataSources(resourceMetaData, switchingResource), toBeCreatedRuleConfigs, propsMap);
    }
    
    private Map<StorageNode, DataSource> getMergedStorageNodeDataSources(final ResourceMetaData currentResourceMetaData, final SwitchingResource switchingResource) {
        Map<StorageNode, DataSource> result = currentResourceMetaData.getDataSources();
        if (null != switchingResource && !switchingResource.getNewDataSources().isEmpty()) {
            result.putAll(switchingResource.getNewDataSources());
        }
        return result;
    }
    
    private ShardingSphereDatabase createChangedDatabase(final String databaseName, final boolean internalLoadMetaData, final MetaDataPersistService persistService,
                                                         final DatabaseConfiguration databaseConfig, final ConfigurationProperties props,
                                                         final ComputeNodeInstanceContext computeNodeInstanceContext) throws SQLException {
        return internalLoadMetaData
                ? InternalMetaDataFactory.create(databaseName, persistService, databaseConfig, props, computeNodeInstanceContext)
                : ExternalMetaDataFactory.create(databaseName, databaseConfig, props, computeNodeInstanceContext);
    }
    
    private Map<String, ShardingSphereSchema> buildShardingSphereSchemas(final ShardingSphereDatabase database) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(database.getSchemas().size(), 1F);
        database.getSchemas().forEach((key, value) -> result.put(key, new ShardingSphereSchema(value.getTables(), value.getViews())));
        return result;
    }
    
    private MetaDataContexts newMetaDataContexts(final ShardingSphereMetaData metaData) {
        return MetaDataContextsFactory.create(metaDataPersistService, metaData);
    }
}
