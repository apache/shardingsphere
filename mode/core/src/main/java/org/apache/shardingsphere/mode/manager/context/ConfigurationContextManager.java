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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.metadata.factory.ExternalMetaDataFactory;
import org.apache.shardingsphere.metadata.factory.InternalMetaDataFactory;
import org.apache.shardingsphere.metadata.persist.MetaDataBasedPersistService;
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
import java.util.stream.Collectors;

/**
 * Configuration context manager.
 */
@RequiredArgsConstructor
@Slf4j
public final class ConfigurationContextManager {
    
    private final AtomicReference<MetaDataContexts> metaDataContexts;
    
    private final InstanceContext instanceContext;
    
    /**
     * Register storage unit.
     *
     * @param databaseName database name
     * @param propsMap data source pool properties map
     */
    public synchronized void registerStorageUnit(final String databaseName, final Map<String, DataSourcePoolProperties> propsMap) {
        try {
            closeStaleRules(databaseName);
            SwitchingResource switchingResource =
                    new ResourceSwitchManager().registerStorageUnit(metaDataContexts.get().getMetaData().getDatabase(databaseName).getResourceMetaData(), propsMap);
            buildNewMetaDataContext(databaseName, switchingResource);
        } catch (final SQLException ex) {
            log.error("Alter database: {} register storage unit failed", databaseName, ex);
        }
    }
    
    /**
     * Alter storage unit.
     *
     * @param databaseName database name
     * @param propsMap data source pool properties map
     */
    public synchronized void alterStorageUnit(final String databaseName, final Map<String, DataSourcePoolProperties> propsMap) {
        try {
            closeStaleRules(databaseName);
            SwitchingResource switchingResource =
                    new ResourceSwitchManager().alterStorageUnit(metaDataContexts.get().getMetaData().getDatabase(databaseName).getResourceMetaData(), propsMap);
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
    public synchronized void unregisterStorageUnit(final String databaseName, final String storageUnitName) {
        try {
            closeStaleRules(databaseName);
            SwitchingResource switchingResource = new ResourceSwitchManager().unregisterStorageUnit(metaDataContexts.get().getMetaData().getDatabase(databaseName).getResourceMetaData(),
                    Collections.singletonList(storageUnitName));
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
     * Alter rule configuration.
     *
     * @param databaseName database name
     * @param ruleConfigs rule configurations
     */
    public synchronized void alterRuleConfiguration(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) {
        try {
            // TODO consider rename this method to alterDatabaseRuleConfiguration
            closeStaleRules(databaseName);
            MetaDataContexts reloadMetaDataContexts = createMetaDataContextsWhenRuleChanged(databaseName, false, null, ruleConfigs);
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
            rules.addAll(DatabaseRulesBuilder.build(databaseName, database.getProtocolType(),
                    database.getResourceMetaData().getStorageUnits().entrySet().stream()
                            .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                    database.getRuleMetaData().getRules(), ruleConfig, instanceContext));
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
                rules.addAll(DatabaseRulesBuilder.build(databaseName, database.getProtocolType(),
                        database.getResourceMetaData().getStorageUnits().entrySet().stream()
                                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSource(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new)),
                        database.getRuleMetaData().getRules(), ruleConfig, instanceContext));
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
     * Alter schema meta data.
     *
     * @param databaseName database name
     * @param reloadDatabase reload database
     * @param currentDatabase current database
     */
    public void alterSchemaMetaData(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        Map<String, ShardingSphereSchema> toBeAlterSchemas = GenericSchemaManager.getToBeDeletedTablesBySchemas(reloadDatabase.getSchemas(), currentDatabase.getSchemas());
        Map<String, ShardingSphereSchema> toBeAddedSchemas = GenericSchemaManager.getToBeAddedTablesBySchemas(reloadDatabase.getSchemas(), currentDatabase.getSchemas());
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
        Map<StorageNode, DataSource> newStorageNodes = getNewStorageNodes(database.getResourceMetaData().getDataSources(), resource);
        Map<String, StorageUnit> newStorageUnits = getNewStorageUnits(database.getResourceMetaData().getStorageUnits(), resource);
        return Collections.singletonMap(database.getName().toLowerCase(), new ShardingSphereDatabase(database.getName(), database.getProtocolType(),
                new ResourceMetaData(newStorageNodes, newStorageUnits), database.getRuleMetaData(), database.getSchemas()));
    }
    
    private Map<StorageNode, DataSource> getNewStorageNodes(final Map<StorageNode, DataSource> currentStorageNodes, final SwitchingResource resource) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(currentStorageNodes.size(), 1F);
        for (Entry<StorageNode, DataSource> entry : currentStorageNodes.entrySet()) {
            if (!resource.getStaleDataSources().containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private Map<String, StorageUnit> getNewStorageUnits(final Map<String, StorageUnit> currentStorageUnits, final SwitchingResource resource) {
        Map<String, StorageUnit> result = new LinkedHashMap<>(currentStorageUnits.size(), 1F);
        for (Entry<String, StorageUnit> entry : currentStorageUnits.entrySet()) {
            if (!resource.getStaleStorageUnitNames().contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    @SneakyThrows(Exception.class)
    private void closeStaleRules(final String databaseName) {
        for (ShardingSphereRule each : metaDataContexts.get().getMetaData().getDatabase(databaseName).getRuleMetaData().getRules()) {
            if (each instanceof AutoCloseable) {
                ((AutoCloseable) each).close();
            }
        }
    }
    
    /**
     * Create meta data contexts when rule configuration changed.
     *
     * @param databaseName database name
     * @param internalLoadMetaData internal load meta data
     * @param switchingResource switching resource
     * @param ruleConfigs rule configs
     * @return MetaDataContexts meta data contexts
     * @throws SQLException SQL exception
     */
    public MetaDataContexts createMetaDataContextsWhenRuleChanged(final String databaseName, final boolean internalLoadMetaData, final SwitchingResource switchingResource,
                                                                  final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        return newMetaDataContexts(new ShardingSphereMetaData(createChangedDatabases(databaseName, internalLoadMetaData, switchingResource, ruleConfigs),
                metaDataContexts.get().getMetaData().getGlobalResourceMetaData(), metaDataContexts.get().getMetaData().getGlobalRuleMetaData(),
                metaDataContexts.get().getMetaData().getProps()));
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
        RuleMetaData changedGlobalMetaData = new RuleMetaData(
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
        Collection<RuleConfiguration> toBeCreatedRuleConfigs = null == ruleConfigs
                ? metaDataContexts.get().getMetaData().getDatabase(databaseName).getRuleMetaData().getConfigurations()
                : ruleConfigs;
        DatabaseConfiguration toBeCreatedDatabaseConfig = getDatabaseConfiguration(
                metaDataContexts.get().getMetaData().getDatabase(databaseName).getResourceMetaData(), switchingResource, toBeCreatedRuleConfigs);
        ShardingSphereDatabase changedDatabase = createChangedDatabase(metaDataContexts.get().getMetaData().getDatabase(databaseName).getName(), internalLoadMetaData,
                metaDataContexts.get().getPersistService(), toBeCreatedDatabaseConfig, metaDataContexts.get().getMetaData().getProps(), instanceContext);
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
    
    private ShardingSphereDatabase createChangedDatabase(final String databaseName, final boolean internalLoadMetaData, final MetaDataBasedPersistService persistService,
                                                         final DatabaseConfiguration databaseConfig, final ConfigurationProperties props, final InstanceContext instanceContext) throws SQLException {
        return internalLoadMetaData
                ? InternalMetaDataFactory.create(databaseName, persistService, databaseConfig, props, instanceContext)
                : ExternalMetaDataFactory.create(databaseName, databaseConfig, props, instanceContext);
    }
    
    private Map<String, ShardingSphereSchema> newShardingSphereSchemas(final ShardingSphereDatabase database) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(database.getSchemas().size(), 1F);
        database.getSchemas().forEach((key, value) -> result.put(key, new ShardingSphereSchema(value.getTables(), value.getViews())));
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
    public synchronized void alterGlobalRuleConfiguration(final Collection<RuleConfiguration> ruleConfigs) {
        if (ruleConfigs.isEmpty()) {
            return;
        }
        closeStaleGlobalRules();
        RuleMetaData toBeChangedGlobalRuleMetaData = new RuleMetaData(
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
    
    @SneakyThrows(Exception.class)
    private void closeStaleGlobalRules() {
        for (ShardingSphereRule each : metaDataContexts.get().getMetaData().getGlobalRuleMetaData().getRules()) {
            if (each instanceof AutoCloseable) {
                ((AutoCloseable) each).close();
            }
        }
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
