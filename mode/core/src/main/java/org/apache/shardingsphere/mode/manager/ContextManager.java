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
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.SchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereView;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereRowData;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlShardingSphereRowDataSwapper;
import org.apache.shardingsphere.mode.manager.switcher.ResourceSwitchManager;
import org.apache.shardingsphere.mode.manager.switcher.SwitchingResource;
import org.apache.shardingsphere.mode.metadata.MetadataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetadataPersistService;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
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
    
    private volatile MetadataContexts metadataContexts;
    
    private final InstanceContext instanceContext;
    
    private final ExecutorEngine executorEngine;
    
    public ContextManager(final MetadataContexts metadataContexts, final InstanceContext instanceContext) {
        this.metadataContexts = metadataContexts;
        this.instanceContext = instanceContext;
        executorEngine = ExecutorEngine.createExecutorEngineWithSize(metadataContexts.getMetadata().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE));
    }
    
    /**
     * Renew Metadata contexts.
     *
     * @param metadataContexts Metadata contexts
     */
    public synchronized void renewMetadataContexts(final MetadataContexts metadataContexts) {
        this.metadataContexts = metadataContexts;
    }
    
    /**
     * Get data source map.
     *
     * @param databaseName database name
     * @return data source map
     */
    public Map<String, DataSource> getDataSourceMap(final String databaseName) {
        return metadataContexts.getMetadata().getDatabase(databaseName).getResourceMetaData().getDataSources();
    }
    
    /**
     * Add database.
     *
     * @param databaseName database name
     */
    public synchronized void addDatabase(final String databaseName) {
        if (metadataContexts.getMetadata().containsDatabase(databaseName)) {
            return;
        }
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), metadataContexts.getMetadata().getProps());
        metadataContexts.getMetadata().addDatabase(databaseName, protocolType);
    }
    
    /**
     * Add database and persist.
     *
     * @param databaseName database name
     */
    public synchronized void addDatabaseAndPersist(final String databaseName) {
        if (metadataContexts.getMetadata().containsDatabase(databaseName)) {
            return;
        }
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), metadataContexts.getMetadata().getProps());
        metadataContexts.getMetadata().addDatabase(databaseName, protocolType);
        metadataContexts.getPersistService().getDatabaseMetadataService().addDatabase(databaseName);
    }
    
    /**
     * Drop database.
     *
     * @param databaseName database name
     */
    public synchronized void dropDatabase(final String databaseName) {
        if (!metadataContexts.getMetadata().containsDatabase(databaseName)) {
            return;
        }
        String actualDatabaseName = metadataContexts.getMetadata().getActualDatabaseName(databaseName);
        metadataContexts.getMetadata().dropDatabase(actualDatabaseName);
    }
    
    /**
     * Drop database and persist.
     *
     * @param databaseName database name
     */
    public synchronized void dropDatabaseAndPersist(final String databaseName) {
        if (!metadataContexts.getMetadata().containsDatabase(databaseName)) {
            return;
        }
        String actualDatabaseName = metadataContexts.getMetadata().getActualDatabaseName(databaseName);
        metadataContexts.getMetadata().dropDatabase(actualDatabaseName);
        metadataContexts.getPersistService().getDatabaseMetadataService().dropDatabase(actualDatabaseName);
    }
    
    /**
     * Add schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void addSchema(final String databaseName, final String schemaName) {
        if (metadataContexts.getMetadata().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        metadataContexts.getMetadata().getDatabase(databaseName).putSchema(schemaName, new ShardingSphereSchema());
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
        if (!metadataContexts.getMetadata().containsDatabase(databaseName) || !metadataContexts.getMetadata().getDatabase(databaseName).containsSchema(schemaName)) {
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
        if (!metadataContexts.getMetadata().containsDatabase(databaseName) || !metadataContexts.getMetadata().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        Optional.ofNullable(toBeChangedTable).ifPresent(optional -> alterTable(databaseName, schemaName, optional));
        Optional.ofNullable(toBeChangedView).ifPresent(optional -> alterView(databaseName, schemaName, optional));
    }
    
    private synchronized void dropTable(final String databaseName, final String schemaName, final String toBeDeletedTableName) {
        metadataContexts.getMetadata().getDatabase(databaseName).getSchema(schemaName).removeTable(toBeDeletedTableName);
        metadataContexts.getMetadata().getDatabase(databaseName).getRuleMetaData().getRules().stream().filter(each -> each instanceof MutableDataNodeRule).findFirst()
                .ifPresent(optional -> ((MutableDataNodeRule) optional).remove(schemaName, toBeDeletedTableName));
    }
    
    private synchronized void dropView(final String databaseName, final String schemaName, final String toBeDeletedViewName) {
        metadataContexts.getMetadata().getDatabase(databaseName).getSchema(schemaName).removeView(toBeDeletedViewName);
        metadataContexts.getMetadata().getDatabase(databaseName).getRuleMetaData().getRules().stream().filter(each -> each instanceof MutableDataNodeRule).findFirst()
                .ifPresent(optional -> ((MutableDataNodeRule) optional).remove(schemaName, toBeDeletedViewName));
    }
    
    private synchronized void alterTable(final String databaseName, final String schemaName, final ShardingSphereTable beBoChangedTable) {
        ShardingSphereDatabase database = metadataContexts.getMetadata().getDatabase(databaseName);
        if (!containsMutableDataNodeRule(database, beBoChangedTable.getName())) {
            database.reloadRules(MutableDataNodeRule.class);
        }
        database.getSchema(schemaName).putTable(beBoChangedTable.getName(), beBoChangedTable);
    }
    
    private synchronized void alterView(final String databaseName, final String schemaName, final ShardingSphereView beBoChangedView) {
        ShardingSphereDatabase database = metadataContexts.getMetadata().getDatabase(databaseName);
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
        ShardingSphereDatabase database = metadataContexts.getMetadata().getDatabase(databaseName);
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
        SwitchingResource switchingResource = new ResourceSwitchManager().create(metadataContexts.getMetadata().getDatabase(databaseName).getResourceMetaData(), toBeAddedDataSourcePropsMap);
        metadataContexts.getMetadata().getDatabases().putAll(createChangedDatabases(databaseName, switchingResource, null));
        metadataContexts.getMetadata().getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(each -> each.addResource(metadataContexts.getMetadata().getDatabase(databaseName)));
        metadataContexts.getMetadata().getDatabase(databaseName).getSchemas().forEach((schemaName, schema) -> metadataContexts.getPersistService().getDatabaseMetadataService()
                .persist(metadataContexts.getMetadata().getActualDatabaseName(databaseName), schemaName, schema));
        metadataContexts.getPersistService().getDataSourceService().append(metadataContexts.getMetadata().getActualDatabaseName(databaseName), toBeAddedDataSourcePropsMap);
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
        SwitchingResource switchingResource = new ResourceSwitchManager().create(metadataContexts.getMetadata().getDatabase(databaseName).getResourceMetaData(), toBeUpdatedDataSourcePropsMap);
        metadataContexts.getMetadata().getDatabases().putAll(createChangedDatabases(databaseName, switchingResource, null));
        metadataContexts.getMetadata().getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(each -> each.addResource(metadataContexts.getMetadata().getDatabase(databaseName)));
        metadataContexts.getMetadata().getDatabases().putAll(newShardingSphereDatabase(metadataContexts.getMetadata().getDatabase(databaseName)));
        metadataContexts.getPersistService().getDataSourceService().append(metadataContexts.getMetadata().getActualDatabaseName(databaseName), toBeUpdatedDataSourcePropsMap);
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
        // TODO should check to be dropped resources are unused here. ContextManager is atomic domain to maintain metadata, not DistSQL handler
        Map<String, DataSourceProperties> dataSourcePropsMap = metadataContexts.getPersistService().getDataSourceService().load(metadataContexts.getMetadata().getActualDatabaseName(databaseName));
        Map<String, DataSourceProperties> toBeDeletedDataSourcePropsMap = getToBeDeletedDataSourcePropsMap(dataSourcePropsMap, toBeDroppedResourceNames);
        SwitchingResource switchingResource =
                new ResourceSwitchManager().createByDropResource(metadataContexts.getMetadata().getDatabase(databaseName).getResourceMetaData(), toBeDeletedDataSourcePropsMap);
        metadataContexts.getMetadata().getDatabases().putAll(renewDatabase(metadataContexts.getMetadata().getDatabase(databaseName), switchingResource));
        MetadataContexts reloadMetadataContexts = createMetadataContexts(databaseName, switchingResource, null);
        alterSchemaMetadata(databaseName, reloadMetadataContexts.getMetadata().getDatabase(databaseName), metadataContexts.getMetadata().getDatabase(databaseName));
        deletedSchemaNames(databaseName, reloadMetadataContexts.getMetadata().getDatabase(databaseName), metadataContexts.getMetadata().getDatabase(databaseName));
        metadataContexts = reloadMetadataContexts;
        Map<String, DataSourceProperties> toBeReversedDataSourcePropsMap = getToBeReversedDataSourcePropsMap(dataSourcePropsMap, toBeDroppedResourceNames);
        metadataContexts.getPersistService().getDataSourceService().persist(metadataContexts.getMetadata().getActualDatabaseName(databaseName), toBeReversedDataSourcePropsMap);
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
    
    private synchronized void alterSchemaMetadata(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        Map<String, ShardingSphereSchema> toBeDeletedTables = SchemaManager.getToBeDeletedTablesBySchemas(reloadDatabase.getSchemas(), currentDatabase.getSchemas());
        Map<String, ShardingSphereSchema> toBeAddedTables = SchemaManager.getToBeAddedTablesBySchemas(reloadDatabase.getSchemas(), currentDatabase.getSchemas());
        toBeAddedTables.forEach((key, value) -> metadataContexts.getPersistService().getDatabaseMetadataService().persist(databaseName, key, value));
        toBeDeletedTables.forEach((key, value) -> metadataContexts.getPersistService().getDatabaseMetadataService().delete(databaseName, key, value));
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
            MetadataContexts reloadMetadataContexts = createMetadataContexts(databaseName, null, ruleConfigs);
            alterSchemaMetadata(databaseName, reloadMetadataContexts.getMetadata().getDatabase(databaseName), metadataContexts.getMetadata().getDatabase(databaseName));
            metadataContexts = reloadMetadataContexts;
            metadataContexts.getMetadata().getDatabases().putAll(newShardingSphereDatabase(metadataContexts.getMetadata().getDatabase(databaseName)));
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
                    new ResourceSwitchManager().createByAlterDataSourceProps(metadataContexts.getMetadata().getDatabase(databaseName).getResourceMetaData(), dataSourcePropsMap);
            metadataContexts.getMetadata().getDatabases().putAll(renewDatabase(metadataContexts.getMetadata().getDatabase(databaseName), switchingResource));
            metadataContexts = createMetadataContexts(databaseName, switchingResource, null);
            metadataContexts.getMetadata().getDatabases().putAll(newShardingSphereDatabase(metadataContexts.getMetadata().getDatabase(databaseName)));
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
            SwitchingResource switchingResource = new ResourceSwitchManager().create(metadataContexts.getMetadata().getDatabase(databaseName).getResourceMetaData(), dataSourcePropsMap);
            metadataContexts = createMetadataContexts(databaseName, switchingResource, ruleConfigs);
            metadataContexts.getMetadata().getDatabases().putAll(newShardingSphereDatabase(metadataContexts.getMetadata().getDatabase(databaseName)));
            switchingResource.closeStaleDataSources();
        } catch (SQLException ex) {
            log.error("Alter database: {} data source and rule configuration failed", databaseName, ex);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private Collection<ResourceHeldRule> getStaleResourceHeldRules(final String databaseName) {
        Collection<ResourceHeldRule> result = new LinkedList<>();
        result.addAll(metadataContexts.getMetadata().getDatabase(databaseName).getRuleMetaData().findRules(ResourceHeldRule.class));
        result.addAll(metadataContexts.getMetadata().getGlobalRuleMetaData().findRules(ResourceHeldRule.class));
        return result;
    }
    
    private MetadataContexts createMetadataContexts(final String databaseName, final SwitchingResource switchingResource, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Map<String, ShardingSphereDatabase> changedDatabases = createChangedDatabases(databaseName, switchingResource, ruleConfigs);
        ConfigurationProperties props = metadataContexts.getMetadata().getProps();
        ShardingSphereRuleMetaData changedGlobalMetadata = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(metadataContexts.getMetadata().getGlobalRuleMetaData().getConfigurations(), changedDatabases, instanceContext, props));
        return newMetadataContexts(new ShardingSphereMetaData(changedDatabases, changedGlobalMetadata, props));
    }
    
    private MetadataContexts createMetadataContexts(final String databaseName, final SwitchingResource switchingResource) throws SQLException {
        MetadataPersistService metadataPersistService = metadataContexts.getPersistService();
        Map<String, ShardingSphereDatabase> changedDatabases = createChangedDatabases(databaseName, switchingResource, metadataPersistService.getDatabaseRulePersistService().load(databaseName));
        ConfigurationProperties props = new ConfigurationProperties(metadataPersistService.getPropsService().load());
        ShardingSphereRuleMetaData changedGlobalMetadata = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(metadataPersistService.getGlobalRuleService().load(), changedDatabases, instanceContext, props));
        return newMetadataContexts(new ShardingSphereMetaData(changedDatabases, changedGlobalMetadata, props));
    }
    
    private Map<String, ShardingSphereDatabase> createChangedDatabases(final String databaseName,
                                                                       final SwitchingResource switchingResource, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        if (null != switchingResource && !switchingResource.getNewDataSources().isEmpty()) {
            metadataContexts.getMetadata().getDatabase(databaseName).getResourceMetaData().getDataSources().putAll(switchingResource.getNewDataSources());
        }
        Collection<RuleConfiguration> toBeCreatedRuleConfigs = null == ruleConfigs
                ? metadataContexts.getMetadata().getDatabase(databaseName).getRuleMetaData().getConfigurations()
                : ruleConfigs;
        DatabaseConfiguration toBeCreatedDatabaseConfig =
                new DataSourceProvidedDatabaseConfiguration(metadataContexts.getMetadata().getDatabase(databaseName).getResourceMetaData().getDataSources(), toBeCreatedRuleConfigs);
        ShardingSphereDatabase changedDatabase = ShardingSphereDatabasesFactory.create(metadataContexts.getMetadata().getActualDatabaseName(databaseName),
                toBeCreatedDatabaseConfig, metadataContexts.getMetadata().getProps(), instanceContext);
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(metadataContexts.getMetadata().getDatabases());
        changedDatabase.getSchemas().putAll(newShardingSphereSchemas(changedDatabase));
        result.put(databaseName.toLowerCase(), changedDatabase);
        return result;
    }
    
    private MetadataContexts newMetadataContexts(final ShardingSphereMetaData metadata) {
        return new MetadataContexts(metadataContexts.getPersistService(), metadata);
    }
    
    private Map<String, ShardingSphereSchema> newShardingSphereSchemas(final ShardingSphereDatabase database) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(database.getSchemas().size(), 1);
        database.getSchemas().forEach((key, value) -> result.put(key, new ShardingSphereSchema(value.getTables(),
                metadataContexts.getPersistService().getDatabaseMetadataService().getViewMetaDataPersistService().load(database.getName(), key))));
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> newShardingSphereDatabase(final ShardingSphereDatabase originalDatabase) {
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1);
        result.put(originalDatabase.getName().toLowerCase(), new ShardingSphereDatabase(originalDatabase.getName(),
                originalDatabase.getProtocolType(), originalDatabase.getResourceMetaData(), originalDatabase.getRuleMetaData(),
                metadataContexts.getPersistService().getDatabaseMetadataService().loadSchemas(originalDatabase.getName())));
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
        Collection<ResourceHeldRule> staleResourceHeldRules = metadataContexts.getMetadata().getGlobalRuleMetaData().findRules(ResourceHeldRule.class);
        staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResource);
        ShardingSphereRuleMetaData toBeChangedGlobalRuleMetadata = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(ruleConfigs, metadataContexts.getMetadata().getDatabases(), instanceContext, metadataContexts.getMetadata().getProps()));
        ShardingSphereMetaData toBeChangedMetadata = new ShardingSphereMetaData(
                metadataContexts.getMetadata().getDatabases(), toBeChangedGlobalRuleMetadata, metadataContexts.getMetadata().getProps());
        metadataContexts = newMetadataContexts(toBeChangedMetadata);
    }
    
    /**
     * Alter properties.
     *
     * @param props properties to be altered
     */
    public synchronized void alterProperties(final Properties props) {
        ShardingSphereMetaData toBeChangedMetadata = new ShardingSphereMetaData(
                metadataContexts.getMetadata().getDatabases(), metadataContexts.getMetadata().getGlobalRuleMetaData(), new ConfigurationProperties(props));
        metadataContexts = newMetadataContexts(toBeChangedMetadata);
    }
    
    private void deletedSchemaNames(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        SchemaManager.getToBeDeletedSchemaNames(reloadDatabase.getSchemas(), currentDatabase.getSchemas()).keySet()
                .forEach(each -> metadataContexts.getPersistService().getDatabaseMetadataService().dropSchema(databaseName, each));
    }
    
    /**
     * Reload database metadata from governance center.
     *
     * @param databaseName to be reloaded database name
     */
    public synchronized void reloadDatabaseMetadata(final String databaseName) {
        try {
            ShardingSphereResourceMetaData currentResourceMetaData = metadataContexts.getMetadata().getDatabase(databaseName).getResourceMetaData();
            Map<String, DataSourceProperties> dataSourceProps = metadataContexts.getPersistService().getDataSourceService().load(databaseName);
            SwitchingResource switchingResource = new ResourceSwitchManager().createByAlterDataSourceProps(currentResourceMetaData, dataSourceProps);
            metadataContexts.getMetadata().getDatabases().putAll(renewDatabase(metadataContexts.getMetadata().getDatabase(databaseName), switchingResource));
            MetadataContexts reloadedMetadataContexts = createMetadataContexts(databaseName, switchingResource);
            deletedSchemaNames(databaseName, reloadedMetadataContexts.getMetadata().getDatabase(databaseName), metadataContexts.getMetadata().getDatabase(databaseName));
            metadataContexts = reloadedMetadataContexts;
            metadataContexts.getMetadata().getDatabases().values().forEach(
                    each -> each.getSchemas().forEach((schemaName, schema) -> metadataContexts.getPersistService().getDatabaseMetadataService().compareAndPersist(each.getName(), schemaName, schema)));
            switchingResource.closeStaleDataSources();
        } catch (final SQLException ex) {
            log.error("Reload database Metadata: {} failed", databaseName, ex);
        }
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
                metadataContexts.getMetadata().getDatabase(databaseName).removeSchema(schemaName);
                metadataContexts.getPersistService().getDatabaseMetadataService().dropSchema(metadataContexts.getMetadata().getActualDatabaseName(databaseName), schemaName);
            } else {
                metadataContexts.getMetadata().getDatabase(databaseName).putSchema(schemaName, reloadedSchema);
                metadataContexts.getPersistService().getDatabaseMetadataService().compareAndPersist(metadataContexts.getMetadata().getActualDatabaseName(databaseName), schemaName, reloadedSchema);
            }
        } catch (final SQLException ex) {
            log.error("Reload Metadata of database: {} schema: {} with data source: {} failed", databaseName, schemaName, dataSourceName, ex);
        }
    }
    
    private ShardingSphereSchema loadSchema(final String databaseName, final String schemaName, final String dataSourceName) throws SQLException {
        ShardingSphereDatabase database = metadataContexts.getMetadata().getDatabase(databaseName);
        database.reloadRules(MutableDataNodeRule.class);
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getProtocolType(), database.getResourceMetaData().getStorageTypes(),
                Collections.singletonMap(dataSourceName, database.getResourceMetaData().getDataSources().get(dataSourceName)),
                database.getRuleMetaData().getRules(), metadataContexts.getMetadata().getProps(), schemaName);
        ShardingSphereSchema result = GenericSchemaBuilder.build(material).get(schemaName);
        result.getViews().putAll(metadataContexts.getPersistService().getDatabaseMetadataService().getViewMetaDataPersistService().load(database.getName(), schemaName));
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
        Map<String, DataSource> dataSourceMap = metadataContexts.getMetadata().getDatabase(databaseName).getResourceMetaData().getDataSources();
        try {
            reloadTable(databaseName, schemaName, tableName, dataSourceMap);
        } catch (final SQLException ex) {
            log.error("Reload table: {} Metadata of database: {} schema: {} failed", tableName, databaseName, schemaName, ex);
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
                dataSourceName, metadataContexts.getMetadata().getDatabase(databaseName).getResourceMetaData().getDataSources().get(dataSourceName));
        try {
            reloadTable(databaseName, schemaName, tableName, dataSourceMap);
        } catch (final SQLException ex) {
            log.error("Reload table: {} Metadata of database: {} schema: {} with data source: {} failed", tableName, databaseName, schemaName, dataSourceName, ex);
        }
    }
    
    private synchronized void reloadTable(final String databaseName, final String schemaName, final String tableName, final Map<String, DataSource> dataSourceMap) throws SQLException {
        ShardingSphereDatabase database = metadataContexts.getMetadata().getDatabase(databaseName);
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getProtocolType(),
                database.getResourceMetaData().getStorageTypes(), dataSourceMap, database.getRuleMetaData().getRules(), metadataContexts.getMetadata().getProps(), schemaName);
        ShardingSphereSchema schema = GenericSchemaBuilder.build(Collections.singletonList(tableName), material).getOrDefault(schemaName, new ShardingSphereSchema());
        if (schema.containsTable(tableName)) {
            alterTable(databaseName, schemaName, schema.getTable(tableName));
        } else {
            dropTable(databaseName, schemaName, tableName);
        }
        metadataContexts.getPersistService().getDatabaseMetadataService().compareAndPersist(database.getName(), schemaName, database.getSchema(schemaName));
    }
    
    /**
     * Add ShardingSphere database data.
     * 
     * @param databaseName database name
     */
    public synchronized void addShardingSphereDatabaseData(final String databaseName) {
        if (metadataContexts.getShardingSphereData().getDatabaseData().containsKey(databaseName)) {
            return;
        }
        metadataContexts.getShardingSphereData().getDatabaseData().put(databaseName, new ShardingSphereDatabaseData());
    }
    
    /**
     * Drop ShardingSphere data database.
     * @param databaseName database name
     */
    public synchronized void dropShardingSphereDatabaseData(final String databaseName) {
        if (!metadataContexts.getShardingSphereData().getDatabaseData().containsKey(databaseName.toLowerCase())) {
            return;
        }
        metadataContexts.getShardingSphereData().getDatabaseData().remove(databaseName);
    }
    
    /**
     * Add ShardingSphere schema data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void addShardingSphereSchemaData(final String databaseName, final String schemaName) {
        if (metadataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().containsKey(schemaName)) {
            return;
        }
        metadataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().put(schemaName, new ShardingSphereSchemaData());
    }
    
    /**
     * Drop ShardingSphere schema data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void dropShardingSphereSchemaData(final String databaseName, final String schemaName) {
        ShardingSphereDatabaseData databaseData = metadataContexts.getShardingSphereData().getDatabaseData().get(databaseName);
        if (null == databaseData || !databaseData.getSchemaData().containsKey(schemaName)) {
            return;
        }
        databaseData.getSchemaData().remove(schemaName);
    }
    
    /**
     * Add ShardingSphere table data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public synchronized void addShardingSphereTableData(final String databaseName, final String schemaName, final String tableName) {
        if (!metadataContexts.getShardingSphereData().getDatabaseData().containsKey(databaseName)
                || !metadataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().containsKey(schemaName)) {
            return;
        }
        if (metadataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().get(schemaName).getTableData().containsKey(tableName)) {
            return;
        }
        ShardingSphereDatabaseData database = metadataContexts.getShardingSphereData().getDatabaseData().get(databaseName);
        database.getSchemaData().get(schemaName).getTableData().put(tableName, new ShardingSphereTableData(tableName));
    }
    
    /**
     * Drop ShardingSphere table data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public synchronized void dropShardingSphereTableData(final String databaseName, final String schemaName, final String tableName) {
        if (!metadataContexts.getShardingSphereData().getDatabaseData().containsKey(databaseName)
                || !metadataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().containsKey(schemaName)) {
            return;
        }
        metadataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().get(schemaName).getTableData().remove(tableName);
    }
    
    /**
     * Alter ShardingSphere row data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param yamlRowData yaml row data
     */
    public synchronized void alterShardingSphereRowData(final String databaseName, final String schemaName, final String tableName, final YamlShardingSphereRowData yamlRowData) {
        if (!metadataContexts.getShardingSphereData().getDatabaseData().containsKey(databaseName)
                || !metadataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().containsKey(schemaName)
                || !metadataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().get(schemaName).getTableData().containsKey(tableName)) {
            return;
        }
        if (!metadataContexts.getMetadata().containsDatabase(databaseName) || !metadataContexts.getMetadata().getDatabase(databaseName).containsSchema(schemaName)
                || !metadataContexts.getMetadata().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        ShardingSphereTableData tableData = metadataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().get(schemaName).getTableData().get(tableName);
        List<ShardingSphereColumn> columns = new ArrayList<>(metadataContexts.getMetadata().getDatabase(databaseName).getSchema(schemaName).getTable(tableName).getColumns().values());
        ShardingSphereRowData rowData = new YamlShardingSphereRowDataSwapper(columns).swapToObject(yamlRowData);
        tableData.getRows().add(rowData);
    }
    
    /**
     * Delete ShardingSphere row data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param uniqueKey row uniqueKey
     */
    public synchronized void deleteShardingSphereRowData(final String databaseName, final String schemaName, final String tableName, final String uniqueKey) {
        if (!metadataContexts.getShardingSphereData().getDatabaseData().containsKey(databaseName)
                || !metadataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().containsKey(schemaName)
                || !metadataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().get(schemaName).getTableData().containsKey(tableName)) {
            return;
        }
        ShardingSphereTableData tableData = metadataContexts.getShardingSphereData().getDatabaseData().get(databaseName).getSchemaData().get(schemaName).getTableData().get(tableName);
        tableData.getRows().removeIf(each -> uniqueKey.equals(each.getUniqueKey()));
    }
    
    @Override
    public void close() {
        executorEngine.close();
        metadataContexts.close();
    }
}
