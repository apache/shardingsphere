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

package org.apache.shardingsphere.mode.manager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.SchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereView;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
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
import java.util.stream.Collectors;

/**
 * Context manager.
 */
@Getter
@Slf4j
public final class ContextManager implements AutoCloseable {
    
    private volatile MetaDataContexts metaDataContexts;
    
    private final InstanceContext instanceContext;
    
    private final ExecutorEngine executorEngine;
    
    public ContextManager(final MetaDataContexts metaDataContexts, final InstanceContext instanceContext) {
        this.metaDataContexts = metaDataContexts;
        this.instanceContext = instanceContext;
        executorEngine = ExecutorEngine.createExecutorEngineWithSize(metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE));
    }
    
    /**
     * Renew meta data contexts.
     *
     * @param metaDataContexts meta data contexts
     */
    public synchronized void renewMetaDataContexts(final MetaDataContexts metaDataContexts) {
        this.metaDataContexts = metaDataContexts;
    }
    
    /**
     * Get data source map.
     *
     * @param databaseName database name
     * @return data source map
     */
    public Map<String, DataSource> getDataSourceMap(final String databaseName) {
        return metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getDataSources();
    }
    
    /**
     * Add database.
     *
     * @param databaseName database name
     */
    public synchronized void addDatabase(final String databaseName) {
        if (metaDataContexts.getMetaData().containsDatabase(databaseName)) {
            return;
        }
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), metaDataContexts.getMetaData().getProps());
        metaDataContexts.getMetaData().addDatabase(databaseName, protocolType);
    }
    
    /**
     * Add database and persist.
     *
     * @param databaseName database name
     */
    public synchronized void addDatabaseAndPersist(final String databaseName) {
        if (metaDataContexts.getMetaData().containsDatabase(databaseName)) {
            return;
        }
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), metaDataContexts.getMetaData().getProps());
        metaDataContexts.getMetaData().addDatabase(databaseName, protocolType);
        metaDataContexts.getPersistService().getDatabaseMetaDataService().addDatabase(databaseName);
    }
    
    /**
     * Drop database.
     *
     * @param databaseName database name
     */
    public synchronized void dropDatabase(final String databaseName) {
        if (!metaDataContexts.getMetaData().containsDatabase(databaseName)) {
            return;
        }
        String actualDatabaseName = metaDataContexts.getMetaData().getActualDatabaseName(databaseName);
        metaDataContexts.getMetaData().dropDatabase(actualDatabaseName);
    }
    
    /**
     * Drop database and persist.
     *
     * @param databaseName database name
     */
    public synchronized void dropDatabaseAndPersist(final String databaseName) {
        if (!metaDataContexts.getMetaData().containsDatabase(databaseName)) {
            return;
        }
        String actualDatabaseName = metaDataContexts.getMetaData().getActualDatabaseName(databaseName);
        metaDataContexts.getMetaData().dropDatabase(actualDatabaseName);
        metaDataContexts.getPersistService().getDatabaseMetaDataService().dropDatabase(actualDatabaseName);
    }
    
    /**
     * Add schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void addSchema(final String databaseName, final String schemaName) {
        if (metaDataContexts.getMetaData().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        metaDataContexts.getMetaData().getDatabase(databaseName).putSchema(schemaName, new ShardingSphereSchema());
    }
    
    /**
     * Alter schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeDeletedTableName to be deleted table name
     * @param toBeDeletedViewName to be deleted view name
     */
    public synchronized void alterSchema(final String databaseName, final String schemaName, final String toBeDeletedTableName, final String toBeDeletedViewName) {
        if (!metaDataContexts.getMetaData().containsDatabase(databaseName) || !metaDataContexts.getMetaData().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        Optional.ofNullable(toBeDeletedTableName).ifPresent(optional -> dropTable(databaseName, schemaName, optional));
        Optional.ofNullable(toBeDeletedViewName).ifPresent(optional -> dropView(databaseName, schemaName, optional));
    }
    
    /**
     * Alter schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeChangedTable to be changed table
     * @param toBeChangedView to be changed view
     */
    public synchronized void alterSchema(final String databaseName, final String schemaName, final ShardingSphereTable toBeChangedTable, final ShardingSphereView toBeChangedView) {
        if (!metaDataContexts.getMetaData().containsDatabase(databaseName) || !metaDataContexts.getMetaData().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        Optional.ofNullable(toBeChangedTable).ifPresent(optional -> alterTable(databaseName, schemaName, optional));
        Optional.ofNullable(toBeChangedView).ifPresent(optional -> alterView(databaseName, schemaName, optional));
    }
    
    private synchronized void dropTable(final String databaseName, final String schemaName, final String toBeDeletedTableName) {
        metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).removeTable(toBeDeletedTableName);
        metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules().stream().filter(each -> each instanceof MutableDataNodeRule).findFirst()
                .ifPresent(optional -> ((MutableDataNodeRule) optional).remove(schemaName, toBeDeletedTableName));
    }
    
    private synchronized void dropView(final String databaseName, final String schemaName, final String toBeDeletedViewName) {
        metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).removeView(toBeDeletedViewName);
        metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules().stream().filter(each -> each instanceof MutableDataNodeRule).findFirst()
                .ifPresent(optional -> ((MutableDataNodeRule) optional).remove(schemaName, toBeDeletedViewName));
    }
    
    private synchronized void alterTable(final String databaseName, final String schemaName, final ShardingSphereTable beBoChangedTable) {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        if (!containsMutableDataNodeRule(database, beBoChangedTable.getName())) {
            database.reloadRules(MutableDataNodeRule.class);
        }
        database.getSchema(schemaName).putTable(beBoChangedTable.getName(), beBoChangedTable);
    }
    
    private synchronized void alterView(final String databaseName, final String schemaName, final ShardingSphereView beBoChangedView) {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        if (!containsMutableDataNodeRule(database, beBoChangedView.getName())) {
            database.reloadRules(MutableDataNodeRule.class);
        }
        database.getSchema(schemaName).putView(beBoChangedView.getName(), beBoChangedView);
    }
    
    private boolean containsMutableDataNodeRule(final ShardingSphereDatabase database, final String tableName) {
        return database.getRuleMetaData().findRules(DataNodeContainedRule.class).stream()
                .filter(each -> !(each instanceof MutableDataNodeRule)).anyMatch(each -> each.getAllTables().contains(tableName));
    }
    
    /**
     * Drop schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void dropSchema(final String databaseName, final String schemaName) {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        if (null == database || !database.containsSchema(schemaName)) {
            return;
        }
        database.removeSchema(schemaName);
    }
    
    /**
     * Add resources.
     *
     * @param databaseName database name
     * @param toBeAddedDataSourcePropsMap to be added data source properties map
     * @throws SQLException SQL exception
     */
    public synchronized void addResources(final String databaseName, final Map<String, DataSourceProperties> toBeAddedDataSourcePropsMap) throws SQLException {
        SwitchingResource switchingResource = new ResourceSwitchManager().create(metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), toBeAddedDataSourcePropsMap);
        metaDataContexts.getMetaData().getDatabases().putAll(createChangedDatabases(databaseName, switchingResource, null));
        metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(each -> each.addResource(metaDataContexts.getMetaData().getDatabase(databaseName)));
        metaDataContexts.getMetaData().getDatabase(databaseName).getSchemas().forEach((schemaName, schema) -> metaDataContexts.getPersistService().getDatabaseMetaDataService()
                .persist(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), schemaName, schema));
        metaDataContexts.getPersistService().getDataSourceService().append(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), toBeAddedDataSourcePropsMap);
        switchingResource.closeStaleDataSources();
    }
    
    /**
     * Update resources.
     *
     * @param databaseName database name
     * @param toBeUpdatedDataSourcePropsMap to be updated data source properties map
     * @throws SQLException SQL exception
     */
    public synchronized void updateResources(final String databaseName, final Map<String, DataSourceProperties> toBeUpdatedDataSourcePropsMap) throws SQLException {
        SwitchingResource switchingResource = new ResourceSwitchManager().create(metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), toBeUpdatedDataSourcePropsMap);
        metaDataContexts.getMetaData().getDatabases().putAll(createChangedDatabases(databaseName, switchingResource, null));
        metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(each -> each.addResource(metaDataContexts.getMetaData().getDatabase(databaseName)));
        metaDataContexts.getMetaData().getDatabases().putAll(newShardingSphereDatabase(metaDataContexts.getMetaData().getDatabase(databaseName)));
        metaDataContexts.getPersistService().getDataSourceService().append(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), toBeUpdatedDataSourcePropsMap);
        switchingResource.closeStaleDataSources();
    }
    
    /**
     * Drop resources.
     *
     * @param databaseName database name
     * @param toBeDroppedResourceNames to be dropped resource names
     * @throws SQLException SQL exception
     */
    public synchronized void dropResources(final String databaseName, final Collection<String> toBeDroppedResourceNames) throws SQLException {
        // TODO should check to be dropped resources are unused here. ContextManager is atomic domain to maintain metadata, not Dist SQL handler
        Map<String, DataSourceProperties> dataSourcePropsMap = metaDataContexts.getPersistService().getDataSourceService().load(metaDataContexts.getMetaData().getActualDatabaseName(databaseName));
        Map<String, DataSourceProperties> toBeDeletedDataSourcePropsMap = getToBeDeletedDataSourcePropsMap(dataSourcePropsMap, toBeDroppedResourceNames);
        SwitchingResource switchingResource =
                new ResourceSwitchManager().createByDropResource(metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), toBeDeletedDataSourcePropsMap);
        metaDataContexts.getMetaData().getDatabases().putAll(renewDatabase(metaDataContexts.getMetaData().getDatabase(databaseName), switchingResource));
        MetaDataContexts reloadMetaDataContexts = createMetaDataContexts(databaseName, switchingResource, null);
        alterSchemaMetaData(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.getMetaData().getDatabase(databaseName));
        deletedSchemaNames(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.getMetaData().getDatabase(databaseName));
        metaDataContexts = reloadMetaDataContexts;
        Map<String, DataSourceProperties> toBeReversedDataSourcePropsMap = getToBeReversedDataSourcePropsMap(dataSourcePropsMap, toBeDroppedResourceNames);
        metaDataContexts.getPersistService().getDataSourceService().persist(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), toBeReversedDataSourcePropsMap);
        switchingResource.closeStaleDataSources();
    }
    
    private Map<String, ShardingSphereDatabase> renewDatabase(final ShardingSphereDatabase database, final SwitchingResource resource) {
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1);
        Map<String, DataSource> newDataSource =
                database.getResourceMetaData().getDataSources().entrySet().stream().filter(entry -> !resource.getStaleDataSources().containsKey(entry.getKey()))
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        result.put(database.getName().toLowerCase(),
                new ShardingSphereDatabase(database.getName(), database.getProtocolType(), new ShardingSphereResourceMetaData(database.getName(), newDataSource),
                        database.getRuleMetaData(), database.getSchemas()));
        return result;
    }
    
    private Map<String, DataSourceProperties> getToBeDeletedDataSourcePropsMap(final Map<String, DataSourceProperties> dataSourcePropsMap, final Collection<String> toBeDroppedResourceNames) {
        return dataSourcePropsMap.entrySet().stream().filter(entry -> toBeDroppedResourceNames.contains(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private Map<String, DataSourceProperties> getToBeReversedDataSourcePropsMap(final Map<String, DataSourceProperties> dataSourcePropsMap, final Collection<String> toBeDroppedResourceNames) {
        return dataSourcePropsMap.entrySet().stream().filter(entry -> !toBeDroppedResourceNames.contains(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private synchronized void alterSchemaMetaData(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        Map<String, ShardingSphereSchema> toBeDeletedTables = SchemaManager.getToBeDeletedTablesBySchemas(reloadDatabase.getSchemas(), currentDatabase.getSchemas());
        Map<String, ShardingSphereSchema> toBeAddedTables = SchemaManager.getToBeAddedTablesBySchemas(reloadDatabase.getSchemas(), currentDatabase.getSchemas());
        toBeAddedTables.forEach((key, value) -> metaDataContexts.getPersistService().getDatabaseMetaDataService().persist(databaseName, key, value));
        toBeDeletedTables.forEach((key, value) -> metaDataContexts.getPersistService().getDatabaseMetaDataService().delete(databaseName, key, value));
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
            MetaDataContexts reloadMetaDataContexts = createMetaDataContexts(databaseName, null, ruleConfigs);
            alterSchemaMetaData(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.getMetaData().getDatabase(databaseName));
            metaDataContexts = reloadMetaDataContexts;
            metaDataContexts.getMetaData().getDatabases().putAll(newShardingSphereDatabase(metaDataContexts.getMetaData().getDatabase(databaseName)));
        } catch (final SQLException ex) {
            log.error("Alter database: {} rule configurations failed", databaseName, ex);
        }
    }
    
    /**
     * Alter data source configuration.
     *
     * @param databaseName database name
     * @param dataSourcePropsMap altered data source properties map
     */
    @SuppressWarnings("rawtypes")
    public synchronized void alterDataSourceConfiguration(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        try {
            Collection<ResourceHeldRule> staleResourceHeldRules = getStaleResourceHeldRules(databaseName);
            staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResource);
            SwitchingResource switchingResource =
                    new ResourceSwitchManager().createByAlterDataSourceProps(metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), dataSourcePropsMap);
            metaDataContexts.getMetaData().getDatabases().putAll(renewDatabase(metaDataContexts.getMetaData().getDatabase(databaseName), switchingResource));
            metaDataContexts = createMetaDataContexts(databaseName, switchingResource, null);
            metaDataContexts.getMetaData().getDatabases().putAll(newShardingSphereDatabase(metaDataContexts.getMetaData().getDatabase(databaseName)));
            switchingResource.closeStaleDataSources();
        } catch (final SQLException ex) {
            log.error("Alter database: {} data source configuration failed", databaseName, ex);
        }
    }
    
    /**
     * Alter data source and rule configuration.
     *
     * @param databaseName database name
     * @param dataSourcePropsMap data source props map
     * @param ruleConfigs rule configurations
     */
    @SuppressWarnings("rawtypes")
    public synchronized void alterDataSourceAndRuleConfiguration(final String databaseName,
                                                                 final Map<String, DataSourceProperties> dataSourcePropsMap, final Collection<RuleConfiguration> ruleConfigs) {
        try {
            Collection<ResourceHeldRule> staleResourceHeldRules = getStaleResourceHeldRules(databaseName);
            staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResource);
            SwitchingResource switchingResource = new ResourceSwitchManager().create(metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), dataSourcePropsMap);
            metaDataContexts = createMetaDataContexts(databaseName, switchingResource, ruleConfigs);
            metaDataContexts.getMetaData().getDatabases().putAll(newShardingSphereDatabase(metaDataContexts.getMetaData().getDatabase(databaseName)));
            switchingResource.closeStaleDataSources();
        } catch (SQLException ex) {
            log.error("Alter database: {} data source and rule configuration failed", databaseName, ex);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private Collection<ResourceHeldRule> getStaleResourceHeldRules(final String databaseName) {
        Collection<ResourceHeldRule> result = new LinkedList<>();
        result.addAll(metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().findRules(ResourceHeldRule.class));
        result.addAll(metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class));
        return result;
    }
    
    private MetaDataContexts createMetaDataContexts(final String databaseName, final SwitchingResource switchingResource, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Map<String, ShardingSphereDatabase> changedDatabases = createChangedDatabases(databaseName, switchingResource, ruleConfigs);
        ConfigurationProperties props = metaDataContexts.getMetaData().getProps();
        ShardingSphereRuleMetaData changedGlobalMetaData = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), changedDatabases, instanceContext, props));
        return newMetaDataContexts(new ShardingSphereMetaData(changedDatabases, changedGlobalMetaData, props));
    }
    
    private Map<String, ShardingSphereDatabase> createChangedDatabases(final String databaseName,
                                                                       final SwitchingResource switchingResource, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        if (null != switchingResource && !switchingResource.getNewDataSources().isEmpty()) {
            metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getDataSources().putAll(switchingResource.getNewDataSources());
        }
        Collection<RuleConfiguration> toBeCreatedRuleConfigs = null == ruleConfigs
                ? metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getConfigurations()
                : ruleConfigs;
        DatabaseConfiguration toBeCreatedDatabaseConfig =
                new DataSourceProvidedDatabaseConfiguration(metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getDataSources(), toBeCreatedRuleConfigs);
        ShardingSphereDatabase changedDatabase = ShardingSphereDatabasesFactory.create(metaDataContexts.getMetaData().getActualDatabaseName(databaseName),
                toBeCreatedDatabaseConfig, metaDataContexts.getMetaData().getProps(), instanceContext);
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(metaDataContexts.getMetaData().getDatabases());
        changedDatabase.getSchemas().putAll(newShardingSphereSchemas(changedDatabase));
        result.put(databaseName.toLowerCase(), changedDatabase);
        return result;
    }
    
    private MetaDataContexts newMetaDataContexts(final ShardingSphereMetaData metaData) {
        return new MetaDataContexts(metaDataContexts.getPersistService(), metaData);
    }
    
    private Map<String, ShardingSphereSchema> newShardingSphereSchemas(final ShardingSphereDatabase database) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(database.getSchemas().size(), 1);
        database.getSchemas().forEach((key, value) -> result.put(key, new ShardingSphereSchema(value.getTables(),
                metaDataContexts.getPersistService().getDatabaseMetaDataService().getViewMetaDataPersistService().load(database.getName(), key))));
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> newShardingSphereDatabase(final ShardingSphereDatabase originalDatabase) {
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1);
        result.put(originalDatabase.getName().toLowerCase(), new ShardingSphereDatabase(originalDatabase.getName(),
                originalDatabase.getProtocolType(), originalDatabase.getResourceMetaData(), originalDatabase.getRuleMetaData(),
                metaDataContexts.getPersistService().getDatabaseMetaDataService().loadSchemas(originalDatabase.getName())));
        return result;
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
        Collection<ResourceHeldRule> staleResourceHeldRules = metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class);
        staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResource);
        ShardingSphereRuleMetaData toBeChangedGlobalRuleMetaData = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(ruleConfigs, metaDataContexts.getMetaData().getDatabases(), instanceContext, metaDataContexts.getMetaData().getProps()));
        ShardingSphereMetaData toBeChangedMetaData = new ShardingSphereMetaData(
                metaDataContexts.getMetaData().getDatabases(), toBeChangedGlobalRuleMetaData, metaDataContexts.getMetaData().getProps());
        metaDataContexts = newMetaDataContexts(toBeChangedMetaData);
    }
    
    /**
     * Alter properties.
     *
     * @param props properties to be altered
     */
    public synchronized void alterProperties(final Properties props) {
        ShardingSphereMetaData toBeChangedMetaData = new ShardingSphereMetaData(
                metaDataContexts.getMetaData().getDatabases(), metaDataContexts.getMetaData().getGlobalRuleMetaData(), new ConfigurationProperties(props));
        metaDataContexts = newMetaDataContexts(toBeChangedMetaData);
    }
    
    /**
     * Reload database.
     *
     * @param databaseName to be reloaded database name
     */
    public synchronized void reloadDatabase(final String databaseName) {
        try {
            ShardingSphereResourceMetaData currentResourceMetaData = metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData();
            SwitchingResource switchingResource = new SwitchingResource(currentResourceMetaData, currentResourceMetaData.getDataSources(), Collections.emptyMap());
            MetaDataContexts reloadedMetaDataContexts = createMetaDataContexts(databaseName, switchingResource, null);
            deletedSchemaNames(databaseName, reloadedMetaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.getMetaData().getDatabase(databaseName));
            metaDataContexts = reloadedMetaDataContexts;
            metaDataContexts.getMetaData().getDatabases().values().forEach(
                    each -> each.getSchemas().forEach((schemaName, schema) -> metaDataContexts.getPersistService().getDatabaseMetaDataService().compareAndPersist(each.getName(), schemaName, schema)));
        } catch (final SQLException ex) {
            log.error("Reload database: {} failed", databaseName, ex);
        }
    }
    
    private void deletedSchemaNames(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        SchemaManager.getToBeDeletedSchemaNames(reloadDatabase.getSchemas(), currentDatabase.getSchemas()).keySet()
                .forEach(each -> metaDataContexts.getPersistService().getDatabaseMetaDataService().dropSchema(databaseName, each));
    }
    
    /**
     * Reload schema.
     *
     * @param databaseName database name
     * @param schemaName to be reloaded schema name
     * @param dataSourceName data source name
     */
    public synchronized void reloadSchema(final String databaseName, final String schemaName, final String dataSourceName) {
        try {
            ShardingSphereSchema reloadedSchema = loadSchema(databaseName, schemaName, dataSourceName);
            if (reloadedSchema.getTables().isEmpty()) {
                metaDataContexts.getMetaData().getDatabase(databaseName).removeSchema(schemaName);
                metaDataContexts.getPersistService().getDatabaseMetaDataService().dropSchema(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), schemaName);
            } else {
                metaDataContexts.getMetaData().getDatabase(databaseName).putSchema(schemaName, reloadedSchema);
                metaDataContexts.getPersistService().getDatabaseMetaDataService().compareAndPersist(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), schemaName, reloadedSchema);
            }
        } catch (final SQLException ex) {
            log.error("Reload meta data of database: {} schema: {} with data source: {} failed", databaseName, schemaName, dataSourceName, ex);
        }
    }
    
    private ShardingSphereSchema loadSchema(final String databaseName, final String schemaName, final String dataSourceName) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        database.reloadRules(MutableDataNodeRule.class);
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getProtocolType(), database.getResourceMetaData().getStorageTypes(),
                Collections.singletonMap(dataSourceName, database.getResourceMetaData().getDataSources().get(dataSourceName)),
                database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
        ShardingSphereSchema result = GenericSchemaBuilder.build(material).get(schemaName);
        result.getViews().putAll(metaDataContexts.getPersistService().getDatabaseMetaDataService().getViewMetaDataPersistService().load(database.getName(), schemaName));
        return result;
    }
    
    /**
     * Reload table.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName to be reloaded table name
     */
    public void reloadTable(final String databaseName, final String schemaName, final String tableName) {
        Map<String, DataSource> dataSourceMap = metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getDataSources();
        try {
            reloadTable(databaseName, schemaName, tableName, dataSourceMap);
        } catch (final SQLException ex) {
            log.error("Reload table: {} meta data of database: {} schema: {} failed", tableName, databaseName, schemaName, ex);
        }
    }
    
    /**
     * Reload table from single data source.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param dataSourceName data source name
     * @param tableName to be reloaded table name
     */
    public void reloadTable(final String databaseName, final String schemaName, final String dataSourceName, final String tableName) {
        Map<String, DataSource> dataSourceMap = Collections.singletonMap(
                dataSourceName, metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getDataSources().get(dataSourceName));
        try {
            reloadTable(databaseName, schemaName, tableName, dataSourceMap);
        } catch (final SQLException ex) {
            log.error("Reload table: {} meta data of database: {} schema: {} with data source: {} failed", tableName, databaseName, schemaName, dataSourceName, ex);
        }
    }
    
    private synchronized void reloadTable(final String databaseName, final String schemaName, final String tableName, final Map<String, DataSource> dataSourceMap) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getProtocolType(),
                database.getResourceMetaData().getStorageTypes(), dataSourceMap, database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
        ShardingSphereSchema schema = GenericSchemaBuilder.build(Collections.singletonList(tableName), material).getOrDefault(schemaName, new ShardingSphereSchema());
        if (schema.containsTable(tableName)) {
            alterTable(databaseName, schemaName, schema.getTable(tableName));
        } else {
            dropTable(databaseName, schemaName, tableName);
        }
        metaDataContexts.getPersistService().getDatabaseMetaDataService().compareAndPersist(database.getName(), schemaName, database.getSchema(schemaName));
    }
    
    /**
     * Add ShardingSphere database data.
     * 
     * @param databaseName database name
     */
    public synchronized void addShardingSphereDatabaseData(final String databaseName) {
        if (metaDataContexts.getShardingSphereData().getDatabaseData().containsKey(databaseName)) {
            return;
        }
        metaDataContexts.getShardingSphereData().getDatabaseData().put(databaseName, new ShardingSphereDatabaseData());
    }
    
    /**
     * Drop ShardingSphere data database.
     * @param databaseName database name
     */
    public synchronized void dropShardingSphereDatabaseData(final String databaseName) {
        if (!metaDataContexts.getShardingSphereData().getDatabaseData().containsKey(databaseName.toLowerCase())) {
            return;
        }
        metaDataContexts.getShardingSphereData().getDatabaseData().remove(databaseName);
    }
    
    /**
     * Add ShardingSphere schema data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void addShardingSphereSchemaData(final String databaseName, final String schemaName) {
        if (metaDataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().containsKey(schemaName)) {
            return;
        }
        metaDataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().put(schemaName, new ShardingSphereSchemaData());
    }
    
    /**
     * Drop ShardingSphere schema data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void dropShardingSphereSchemaData(final String databaseName, final String schemaName) {
        ShardingSphereDatabaseData databaseData = metaDataContexts.getShardingSphereData().getDatabaseData().get(databaseName);
        if (null == databaseData || !databaseData.getSchemaData().containsKey(schemaName)) {
            return;
        }
        databaseData.getSchemaData().remove(schemaName);
    }
    
    /**
     * Alter ShardingSphere schema data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeDeletedTableName to be deleted table name
     */
    public synchronized void alterSchemaData(final String databaseName, final String schemaName, final String toBeDeletedTableName) {
        if (!metaDataContexts.getShardingSphereData().getDatabaseData().containsKey(databaseName)
                || !metaDataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().containsKey(schemaName)) {
            return;
        }
        Optional.ofNullable(toBeDeletedTableName).ifPresent(optional -> dropTableData(databaseName, schemaName, optional));
    }
    
    /**
     * Alter ShardingSphere schema data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeChangedTable to be changed table
     */
    public synchronized void alterSchemaData(final String databaseName, final String schemaName, final ShardingSphereTableData toBeChangedTable) {
        if (!metaDataContexts.getShardingSphereData().getDatabaseData().containsKey(databaseName)
                || !metaDataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().containsKey(schemaName)) {
            return;
        }
        Optional.ofNullable(toBeChangedTable).ifPresent(optional -> alterTableData(databaseName, schemaName, optional));
    }
    
    private synchronized void alterTableData(final String databaseName, final String schemaName, final ShardingSphereTableData toBeChangedTable) {
        ShardingSphereDatabaseData database = metaDataContexts.getShardingSphereData().getDatabaseData().get(databaseName);
        database.getSchemaData().get(schemaName).getTableData().put(toBeChangedTable.getName(), toBeChangedTable);
    }
    
    private synchronized void dropTableData(final String databaseName, final String schemaName, final String toBeDeletedTableName) {
        metaDataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().get(schemaName).getTableData().remove(toBeDeletedTableName);
    }
    
    @Override
    public void close() {
        executorEngine.close();
        metaDataContexts.close();
    }
}
