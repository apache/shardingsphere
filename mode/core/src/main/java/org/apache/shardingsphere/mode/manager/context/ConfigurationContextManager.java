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

package org.apache.shardingsphere.mode.manager.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.storage.StorageResource;
import org.apache.shardingsphere.infra.datasource.storage.StorageUnit;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.SchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.metadata.factory.ExternalMetaDataFactory;
import org.apache.shardingsphere.metadata.factory.InternalMetaDataFactory;
import org.apache.shardingsphere.metadata.persist.MetaDataBasedPersistService;
import org.apache.shardingsphere.mode.manager.switcher.NewResourceSwitchManager;
import org.apache.shardingsphere.mode.manager.switcher.ResourceSwitchManager;
import org.apache.shardingsphere.mode.manager.switcher.SwitchingResource;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Configuration context manager.
 */
@Slf4j
@RequiredArgsConstructor
public final class ConfigurationContextManager {
    
    private final AtomicReference<MetaDataContexts> metaDataContexts;
    
    private final InstanceContext instanceContext;
    
    /**
     * Register storage unit.
     *
     * @param databaseName database name
     * @param dataSourceProps data source properties
     */
    @SuppressWarnings("rawtypes")
    public synchronized void registerStorageUnit(final String databaseName, final Map<String, DataSourceProperties> dataSourceProps) {
        try {
            Collection<ResourceHeldRule> staleResourceHeldRules = getStaleResourceHeldRules(databaseName);
            staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResource);
            SwitchingResource switchingResource =
                    new NewResourceSwitchManager().registerStorageUnit(metaDataContexts.get().getMetaData().getDatabase(databaseName).getResourceMetaData(), dataSourceProps);
            buildNewMetaDataContext(databaseName, switchingResource);
        } catch (final SQLException ex) {
            log.error("Alter database: {} register storage unit failed", databaseName, ex);
        }
    }
    
    /**
     * Alter storage unit.
     *
     * @param databaseName database name
     * @param storageUnitName storage unit name
     * @param dataSourceProps data source properties
     */
    @SuppressWarnings("rawtypes")
    public synchronized void alterStorageUnit(final String databaseName, final String storageUnitName, final Map<String, DataSourceProperties> dataSourceProps) {
        try {
            Collection<ResourceHeldRule> staleResourceHeldRules = getStaleResourceHeldRules(databaseName);
            staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResource);
            SwitchingResource switchingResource =
                    new NewResourceSwitchManager().alterStorageUnit(metaDataContexts.get().getMetaData().getDatabase(databaseName).getResourceMetaData(), dataSourceProps);
            buildNewMetaDataContext(databaseName, switchingResource);
        } catch (final SQLException ex) {
            log.error("Alter database: {} register storage unit failed", databaseName, ex);
        }
    }
    
    /**
     * UnRegister storage unit.
     *
     * @param databaseName database name
     * @param storageUnitName storage unit name
     */
    @SuppressWarnings("rawtypes")
    public synchronized void unregisterStorageUnit(final String databaseName, final String storageUnitName) {
        try {
            Collection<ResourceHeldRule> staleResourceHeldRules = getStaleResourceHeldRules(databaseName);
            staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResource);
            SwitchingResource switchingResource = new NewResourceSwitchManager().unregisterStorageUnit(metaDataContexts.get().getMetaData().getDatabase(databaseName).getResourceMetaData(),
                    storageUnitName);
            buildNewMetaDataContext(databaseName, switchingResource);
        } catch (final SQLException ex) {
            log.error("Alter database: {} register storage unit failed", databaseName, ex);
        }
    }
    
    private void buildNewMetaDataContext(final String databaseName, final SwitchingResource switchingResource) throws SQLException {
        metaDataContexts.get().getMetaData().getDatabases().putAll(renewDatabase(metaDataContexts.get().getMetaData().getDatabase(databaseName), switchingResource));
        MetaDataContexts reloadMetaDataContexts = createMetaDataContexts(databaseName, false, switchingResource, null);
        reloadMetaDataContexts.getMetaData().getDatabase(databaseName).getSchemas().forEach((schemaName, schema) -> reloadMetaDataContexts.getPersistService().getDatabaseMetaDataService()
                .persist(reloadMetaDataContexts.getMetaData().getDatabase(databaseName).getName(), schemaName, schema));
        Optional.ofNullable(reloadMetaDataContexts.getStatistics().getDatabaseData().get(databaseName))
                .ifPresent(optional -> optional.getSchemaData().forEach((schemaName, schemaData) -> reloadMetaDataContexts.getPersistService().getShardingSphereDataPersistService()
                        .persist(databaseName, schemaName, schemaData, metaDataContexts.get().getMetaData().getDatabases())));
        alterSchemaMetaData(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.get().getMetaData().getDatabase(databaseName));
        metaDataContexts.set(reloadMetaDataContexts);
        metaDataContexts.get().getMetaData().getDatabases().putAll(newShardingSphereDatabase(metaDataContexts.get().getMetaData().getDatabase(databaseName)));
        switchingResource.closeStaleDataSources();
    }
    
    /**
     * Alter data source nodes configuration.
     *
     * @param databaseName database name
     * @param dataSourcePropsMap altered data source properties map
     */
    public synchronized void alterDataSourceNodesConfiguration(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        // TODO Support for registering storage node #25447
    }
    
    /**
     * Register storage node.
     *
     * @param databaseName database name
     * @param dataSourceProps data source properties
     */
    @SuppressWarnings("rawtypes")
    public synchronized void registerStorageNode(final String databaseName, final Map<String, DataSourceProperties> dataSourceProps) {
        // TODO Support for registering storage node #25447
    }
    
    /**
     * Alter storage node.
     *
     * @param databaseName database name
     * @param storageUnitName storage unit name
     * @param dataSourceProps data source properties
     */
    @SuppressWarnings("rawtypes")
    public synchronized void alterStorageNode(final String databaseName, final String storageUnitName, final Map<String, DataSourceProperties> dataSourceProps) {
        // TODO Support for registering storage node #25447
    }
    
    /**
     * Unregister storage node.
     *
     * @param databaseName database name
     * @param storageUnitName storage unit name
     */
    @SuppressWarnings("rawtypes")
    public synchronized void unregisterStorageNode(final String databaseName, final String storageUnitName) {
        // TODO Support for registering storage node #25447
    }
    
    /**
     * Alter rule configuration.
     *
     * @param databaseName database name
     * @param ruleConfigs rule configurations
     */
    @SuppressWarnings("rawtypes")
    public synchronized void alterRuleConfiguration(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) {
        try {
            Collection<ResourceHeldRule> staleResourceHeldRules = getStaleResourceHeldRules(databaseName);
            staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResource);
            MetaDataContexts reloadMetaDataContexts = createMetaDataContexts(databaseName, false, null, ruleConfigs);
            alterSchemaMetaData(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.get().getMetaData().getDatabase(databaseName));
            metaDataContexts.set(reloadMetaDataContexts);
            metaDataContexts.get().getMetaData().getDatabase(databaseName).getSchemas().putAll(newShardingSphereSchemas(metaDataContexts.get().getMetaData().getDatabase(databaseName)));
        } catch (final SQLException ex) {
            log.error("Alter database: {} rule configurations failed", databaseName, ex);
        }
    }
    
    /**
     * Alter rule configuration.
     *
     * @param databaseName database name
     * @param ruleConfig rule configurations
     */
    public synchronized void alterRuleConfiguration(final String databaseName, final RuleConfiguration ruleConfig) {
        try {
            ShardingSphereDatabase database = metaDataContexts.get().getMetaData().getDatabase(databaseName);
            Collection<ShardingSphereRule> rules = new LinkedList<>(database.getRuleMetaData().getRules());
            rules.removeIf(each -> each.getConfiguration().getClass().isAssignableFrom(ruleConfig.getClass()));
            rules.addAll(DatabaseRulesBuilder.build(databaseName, database.getResourceMetaData().getDataSources(), database.getRuleMetaData().getRules(), ruleConfig, instanceContext));
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
    public synchronized void dropRuleConfiguration(final String databaseName, final RuleConfiguration ruleConfig) {
        try {
            ShardingSphereDatabase database = metaDataContexts.get().getMetaData().getDatabase(databaseName);
            Collection<ShardingSphereRule> rules = new LinkedList<>(database.getRuleMetaData().getRules());
            rules.removeIf(each -> each.getConfiguration().getClass().isAssignableFrom(ruleConfig.getClass()));
            if (isNotEmptyConfig(ruleConfig)) {
                rules.addAll(DatabaseRulesBuilder.build(databaseName, database.getResourceMetaData().getDataSources(), database.getRuleMetaData().getRules(), ruleConfig, instanceContext));
            }
            refreshMetadata(databaseName, database, rules);
        } catch (final SQLException ex) {
            log.error("Drop database: {} rule configurations failed", databaseName, ex);
        }
    }
    
    private static boolean isNotEmptyConfig(final RuleConfiguration ruleConfig) {
        return !((DatabaseRuleConfiguration) ruleConfig).isEmpty();
    }
    
    private void refreshMetadata(final String databaseName, final ShardingSphereDatabase database, final Collection<ShardingSphereRule> rules) throws SQLException {
        database.getRuleMetaData().getRules().clear();
        database.getRuleMetaData().getRules().addAll(rules);
        MetaDataContexts reloadMetaDataContexts = createMetaDataContextsByAlterRule(databaseName, database.getRuleMetaData().getConfigurations());
        alterSchemaMetaData(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.get().getMetaData().getDatabase(databaseName));
        metaDataContexts.set(reloadMetaDataContexts);
        metaDataContexts.get().getMetaData().getDatabase(databaseName).getSchemas().putAll(newShardingSphereSchemas(metaDataContexts.get().getMetaData().getDatabase(databaseName)));
    }
    
    private MetaDataContexts createMetaDataContextsByAlterRule(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Map<String, ShardingSphereDatabase> changedDatabases = createChangedDatabases(databaseName, false, null, ruleConfigs);
        return newMetaDataContexts(new ShardingSphereMetaData(changedDatabases, metaDataContexts.get().getMetaData().getGlobalResourceMetaData(),
                metaDataContexts.get().getMetaData().getGlobalRuleMetaData(), metaDataContexts.get().getMetaData().getProps()));
    }
    
    /**
     * Alter data source units configuration.
     *
     * @param databaseName database name
     * @param dataSourcePropsMap altered data source properties map
     */
    @SuppressWarnings("rawtypes")
    public synchronized void alterDataSourceUnitsConfiguration(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        try {
            Collection<ResourceHeldRule> staleResourceHeldRules = getStaleResourceHeldRules(databaseName);
            staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResource);
            SwitchingResource switchingResource =
                    new ResourceSwitchManager().createByAlterDataSourceProps(metaDataContexts.get().getMetaData().getDatabase(databaseName).getResourceMetaData(), dataSourcePropsMap);
            metaDataContexts.get().getMetaData().getDatabases().putAll(renewDatabase(metaDataContexts.get().getMetaData().getDatabase(databaseName), switchingResource));
            // TODO Remove this logic when issue #22887 are finished.
            MetaDataContexts reloadMetaDataContexts = createMetaDataContexts(databaseName, false, switchingResource, null);
            reloadMetaDataContexts.getMetaData().getDatabase(databaseName).getSchemas().forEach((schemaName, schema) -> reloadMetaDataContexts.getPersistService().getDatabaseMetaDataService()
                    .persist(reloadMetaDataContexts.getMetaData().getDatabase(databaseName).getName(), schemaName, schema));
            Optional.ofNullable(reloadMetaDataContexts.getStatistics().getDatabaseData().get(databaseName))
                    .ifPresent(optional -> optional.getSchemaData().forEach((schemaName, schemaData) -> reloadMetaDataContexts.getPersistService().getShardingSphereDataPersistService()
                            .persist(databaseName, schemaName, schemaData, metaDataContexts.get().getMetaData().getDatabases())));
            alterSchemaMetaData(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.get().getMetaData().getDatabase(databaseName));
            metaDataContexts.set(reloadMetaDataContexts);
            metaDataContexts.get().getMetaData().getDatabases().putAll(newShardingSphereDatabase(metaDataContexts.get().getMetaData().getDatabase(databaseName)));
            switchingResource.closeStaleDataSources();
        } catch (final SQLException ex) {
            log.error("Alter database: {} data source configuration failed", databaseName, ex);
        }
    }
    
    /**
     * Alter schema meta data.
     *
     * @param databaseName database name
     * @param reloadDatabase reload database
     * @param currentDatabase current database
     */
    public void alterSchemaMetaData(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        Map<String, ShardingSphereSchema> toBeAlterSchemas = SchemaManager.getToBeDeletedTablesBySchemas(reloadDatabase.getSchemas(), currentDatabase.getSchemas());
        Map<String, ShardingSphereSchema> toBeAddedSchemas = SchemaManager.getToBeAddedTablesBySchemas(reloadDatabase.getSchemas(), currentDatabase.getSchemas());
        toBeAddedSchemas.forEach((key, value) -> metaDataContexts.get().getPersistService().getDatabaseMetaDataService().persist(databaseName, key, value));
        toBeAlterSchemas.forEach((key, value) -> metaDataContexts.get().getPersistService().getDatabaseMetaDataService().delete(databaseName, key, value));
    }
    
    /**
     * Renew ShardingSphere databases.
     *
     * @param database database
     * @param resource resource
     * @return ShardingSphere databases
     */
    public Map<String, ShardingSphereDatabase> renewDatabase(final ShardingSphereDatabase database, final SwitchingResource resource) {
        Map<String, DataSource> newStorageNodes = getNewStorageNodes(database.getResourceMetaData().getStorageNodeMetaData().getDataSources(), resource);
        Map<String, StorageUnit> newStorageUnits = getNewStorageUnits(database.getResourceMetaData().getStorageUnitMetaData().getStorageUnits(), resource);
        StorageResource newStorageResource = new StorageResource(newStorageNodes, newStorageUnits);
        return Collections.singletonMap(database.getName().toLowerCase(),
                new ShardingSphereDatabase(database.getName(), database.getProtocolType(),
                        new ShardingSphereResourceMetaData(database.getName(), newStorageResource, database.getResourceMetaData().getDataSourcePropsMap()),
                        database.getRuleMetaData(), database.getSchemas()));
    }
    
    private Map<String, DataSource> getNewStorageNodes(final Map<String, DataSource> currentStorageNodes, final SwitchingResource resource) {
        Map<String, DataSource> result = new LinkedHashMap<>();
        for (Entry<String, DataSource> entry : currentStorageNodes.entrySet()) {
            if (!resource.getStaleStorageResource().getStorageNodes().containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private Map<String, StorageUnit> getNewStorageUnits(final Map<String, StorageUnit> currentStorageUnits, final SwitchingResource resource) {
        Map<String, StorageUnit> result = new LinkedHashMap<>();
        for (Entry<String, StorageUnit> entry : currentStorageUnits.entrySet()) {
            if (!resource.getStaleStorageResource().getStorageUnits().containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private Collection<ResourceHeldRule> getStaleResourceHeldRules(final String databaseName) {
        Collection<ResourceHeldRule> result = new LinkedList<>();
        result.addAll(metaDataContexts.get().getMetaData().getDatabase(databaseName).getRuleMetaData().findRules(ResourceHeldRule.class));
        result.addAll(metaDataContexts.get().getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class));
        return result;
    }
    
    /**
     * Create meta data contexts.
     *
     * @param databaseName database name
     * @param internalLoadMetaData internal load meta data
     * @param switchingResource switching resource
     * @param ruleConfigs rule configs
     * @return MetaDataContexts meta data contexts
     * @throws SQLException SQL exception
     */
    public MetaDataContexts createMetaDataContexts(final String databaseName, final boolean internalLoadMetaData, final SwitchingResource switchingResource,
                                                   final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Map<String, ShardingSphereDatabase> changedDatabases = createChangedDatabases(databaseName, internalLoadMetaData, switchingResource, ruleConfigs);
        ConfigurationProperties props = metaDataContexts.get().getMetaData().getProps();
        ShardingSphereRuleMetaData changedGlobalMetaData = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(metaDataContexts.get().getMetaData().getGlobalRuleMetaData().getConfigurations(), changedDatabases, props));
        return newMetaDataContexts(new ShardingSphereMetaData(changedDatabases, metaDataContexts.get().getMetaData().getGlobalResourceMetaData(), changedGlobalMetaData, props));
    }
    
    /**
     * Create changed databases.
     *
     * @param databaseName database name
     * @param internalLoadMetaData internal load meta data
     * @param switchingResource switching resource
     * @param ruleConfigs rule configs
     * @return ShardingSphere databases
     * @throws SQLException SQL exception
     */
    public synchronized Map<String, ShardingSphereDatabase> createChangedDatabases(final String databaseName, final boolean internalLoadMetaData,
                                                                                   final SwitchingResource switchingResource, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        ShardingSphereResourceMetaData resourceMetaData = metaDataContexts.get().getMetaData().getDatabase(databaseName).getResourceMetaData();
        if (null != switchingResource && null != switchingResource.getNewStorageResource() && !switchingResource.getNewStorageResource().getStorageNodes().isEmpty()) {
            resourceMetaData.getStorageNodeMetaData().getDataSources().putAll(switchingResource.getNewStorageResource().getStorageNodes());
        }
        if (null != switchingResource && null != switchingResource.getNewStorageResource() && !switchingResource.getNewStorageResource().getStorageUnits().isEmpty()) {
            resourceMetaData.getStorageUnitMetaData().getStorageUnits().putAll(switchingResource.getNewStorageResource().getStorageUnits());
        }
        Collection<RuleConfiguration> toBeCreatedRuleConfigs = null == ruleConfigs
                ? metaDataContexts.get().getMetaData().getDatabase(databaseName).getRuleMetaData().getConfigurations()
                : ruleConfigs;
        StorageResource storageResource = new StorageResource(resourceMetaData.getStorageNodeMetaData().getDataSources(), resourceMetaData.getStorageUnitMetaData().getStorageUnits());
        DatabaseConfiguration toBeCreatedDatabaseConfig = new DataSourceProvidedDatabaseConfiguration(storageResource, toBeCreatedRuleConfigs, resourceMetaData.getDataSourcePropsMap());
        ShardingSphereDatabase changedDatabase = createChangedDatabase(metaDataContexts.get().getMetaData().getDatabase(databaseName).getName(), internalLoadMetaData,
                metaDataContexts.get().getPersistService(), toBeCreatedDatabaseConfig, metaDataContexts.get().getMetaData().getProps(), instanceContext);
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(metaDataContexts.get().getMetaData().getDatabases());
        changedDatabase.getSchemas().putAll(newShardingSphereSchemas(changedDatabase));
        result.put(databaseName.toLowerCase(), changedDatabase);
        return result;
    }
    
    private ShardingSphereDatabase createChangedDatabase(final String databaseName, final boolean internalLoadMetaData, final MetaDataBasedPersistService persistService,
                                                         final DatabaseConfiguration databaseConfig, final ConfigurationProperties props, final InstanceContext instanceContext) throws SQLException {
        return internalLoadMetaData
                ? InternalMetaDataFactory.create(databaseName, persistService, databaseConfig, props, instanceContext)
                : ExternalMetaDataFactory.create(databaseName, databaseConfig, props, instanceContext);
    }
    
    private Map<String, ShardingSphereSchema> newShardingSphereSchemas(final ShardingSphereDatabase database) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(database.getSchemas().size(), 1F);
        database.getSchemas().forEach((key, value) -> result.put(key, new ShardingSphereSchema(value.getTables(),
                metaDataContexts.get().getPersistService().getDatabaseMetaDataService().getViewMetaDataPersistService().load(database.getName(), key))));
        return result;
    }
    
    /**
     * Create new ShardingSphere database.
     *
     * @param originalDatabase original database
     * @return ShardingSphere databases
     */
    public Map<String, ShardingSphereDatabase> newShardingSphereDatabase(final ShardingSphereDatabase originalDatabase) {
        return Collections.singletonMap(originalDatabase.getName().toLowerCase(), new ShardingSphereDatabase(originalDatabase.getName(),
                originalDatabase.getProtocolType(), originalDatabase.getResourceMetaData(), originalDatabase.getRuleMetaData(),
                metaDataContexts.get().getPersistService().getDatabaseMetaDataService().loadSchemas(originalDatabase.getName())));
    }
    
    /**
     * Alter global rule configuration.
     *
     * @param ruleConfigs global rule configuration
     */
    @SuppressWarnings("rawtypes")
    public synchronized void alterGlobalRuleConfiguration(final Collection<RuleConfiguration> ruleConfigs) {
        if (ruleConfigs.isEmpty()) {
            return;
        }
        Collection<ResourceHeldRule> staleResourceHeldRules = metaDataContexts.get().getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class);
        staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResource);
        ShardingSphereRuleMetaData toBeChangedGlobalRuleMetaData = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(ruleConfigs, metaDataContexts.get().getMetaData().getDatabases(), metaDataContexts.get().getMetaData().getProps()));
        ShardingSphereMetaData toBeChangedMetaData = new ShardingSphereMetaData(metaDataContexts.get().getMetaData().getDatabases(), metaDataContexts.get().getMetaData().getGlobalResourceMetaData(),
                toBeChangedGlobalRuleMetaData, metaDataContexts.get().getMetaData().getProps());
        metaDataContexts.set(newMetaDataContexts(toBeChangedMetaData));
    }
    
    /**
     * Alter global rule configuration.
     *
     * @param ruleConfig global rule configuration
     */
    public synchronized void alterGlobalRuleConfiguration(final RuleConfiguration ruleConfig) {
        if (null == ruleConfig) {
            return;
        }
        Collection<ShardingSphereRule> rules = removeSingleGlobalRule(ruleConfig);
        rules.addAll(GlobalRulesBuilder.buildSingleRules(ruleConfig, metaDataContexts.get().getMetaData().getDatabases(), metaDataContexts.get().getMetaData().getProps()));
        metaDataContexts.get().getMetaData().getGlobalRuleMetaData().getRules().clear();
        metaDataContexts.get().getMetaData().getGlobalRuleMetaData().getRules().addAll(rules);
        ShardingSphereMetaData toBeChangedMetaData = new ShardingSphereMetaData(metaDataContexts.get().getMetaData().getDatabases(), metaDataContexts.get().getMetaData().getGlobalResourceMetaData(),
                metaDataContexts.get().getMetaData().getGlobalRuleMetaData(), metaDataContexts.get().getMetaData().getProps());
        metaDataContexts.set(newMetaDataContexts(toBeChangedMetaData));
    }
    
    private Collection<ShardingSphereRule> removeSingleGlobalRule(final RuleConfiguration ruleConfig) {
        Collection<ShardingSphereRule> result = new LinkedList<>(metaDataContexts.get().getMetaData().getGlobalRuleMetaData().getRules());
        result.removeIf(each -> each.getConfiguration().getClass().isAssignableFrom(ruleConfig.getClass()));
        return result;
    }
    
    /**
     * Alter properties.
     *
     * @param props properties to be altered
     */
    public synchronized void alterProperties(final Properties props) {
        ShardingSphereMetaData toBeChangedMetaData = new ShardingSphereMetaData(metaDataContexts.get().getMetaData().getDatabases(), metaDataContexts.get().getMetaData().getGlobalResourceMetaData(),
                metaDataContexts.get().getMetaData().getGlobalRuleMetaData(), new ConfigurationProperties(props));
        metaDataContexts.set(newMetaDataContexts(toBeChangedMetaData));
    }
    
    private MetaDataContexts newMetaDataContexts(final ShardingSphereMetaData metaData) {
        return new MetaDataContexts(metaDataContexts.get().getPersistService(), metaData);
    }
}
