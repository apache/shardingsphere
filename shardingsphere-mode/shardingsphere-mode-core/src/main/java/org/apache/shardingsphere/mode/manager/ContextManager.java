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
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
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
        return metaDataContexts.getMetaData().getDatabase(databaseName).getResource().getDataSources();
    }
    
    /**
     * Add database.
     *
     * @param databaseName database name
     * @throws SQLException SQL exception
     */
    public synchronized void addDatabase(final String databaseName) throws SQLException {
        if (metaDataContexts.getMetaData().containsDatabase(databaseName)) {
            return;
        }
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), metaDataContexts.getMetaData().getProps());
        metaDataContexts.getMetaData().addDatabase(databaseName, protocolType);
        metaDataContexts.getOptimizerContext().addDatabase(databaseName, protocolType);
        metaDataContexts.getPersistService().getDatabaseMetaDataService().persistDatabase(databaseName);
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
        metaDataContexts.getOptimizerContext().dropDatabase(actualDatabaseName);
        metaDataContexts.getPersistService().getDatabaseMetaDataService().deleteDatabase(actualDatabaseName);
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
        metaDataContexts.getOptimizerContext().addSchema(databaseName, schemaName);
    }
    
    /**
     * Alter schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeDeletedTableName to be deleted table name
     */
    public synchronized void alterSchema(final String databaseName, final String schemaName, final String toBeDeletedTableName) {
        if (metaDataContexts.getMetaData().containsDatabase(databaseName)) {
            Optional.ofNullable(toBeDeletedTableName).ifPresent(optional -> dropTable(databaseName, schemaName, optional));
        }
    }
    
    /**
     * Alter schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeChangedTable to be changed table
     */
    public synchronized void alterSchema(final String databaseName, final String schemaName, final ShardingSphereTable toBeChangedTable) {
        if (metaDataContexts.getMetaData().containsDatabase(databaseName)) {
            Optional.ofNullable(toBeChangedTable).ifPresent(optional -> alterTable(databaseName, schemaName, optional));
        }
    }
    
    private synchronized void alterTable(final String databaseName, final String schemaName, final ShardingSphereTable beBoChangedTable) {
        alterTable(metaDataContexts.getMetaData().getDatabase(databaseName), schemaName, beBoChangedTable);
        metaDataContexts.getOptimizerContext().alterTable(databaseName, schemaName, beBoChangedTable);
    }
    
    private synchronized void alterTable(final ShardingSphereDatabase database, final String schemaName, final ShardingSphereTable beBoChangedTable) {
        if (containsMutableDataNodeRule(database, schemaName, beBoChangedTable.getName())) {
            database.reloadRules(instanceContext);
        }
        database.getSchema(schemaName).put(beBoChangedTable.getName(), beBoChangedTable);
    }
    
    private boolean containsMutableDataNodeRule(final ShardingSphereDatabase database, final String schemaName, final String tableName) {
        return database.getRuleMetaData().findRules(MutableDataNodeRule.class).stream().anyMatch(each -> each.findSingleTableDataNode(schemaName, tableName).isPresent());
    }
    
    private void dropTable(final String databaseName, final String schemaName, final String toBeDeletedTableName) {
        if (metaDataContexts.getMetaData().getDatabase(databaseName).containsSchema(schemaName)) {
            metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).remove(toBeDeletedTableName);
            metaDataContexts.getOptimizerContext().dropTable(databaseName, schemaName, toBeDeletedTableName);
            // TODO check whether need to reloadRules(single table rule) if table dropped?
        }
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
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabase(databaseName).removeSchemaMetadata(schemaName);
    }
    
    /**
     * Update resources.
     *
     * @param databaseName database name
     * @param toBeUpdatedDataSourcePropsMap to be updated data source properties map
     * @throws SQLException SQL exception
     */
    public synchronized void updateResources(final String databaseName, final Map<String, DataSourceProperties> toBeUpdatedDataSourcePropsMap) throws SQLException {
        SwitchingResource switchingResource = new ResourceSwitchManager().create(metaDataContexts.getMetaData().getDatabase(databaseName).getResource(), toBeUpdatedDataSourcePropsMap);
        metaDataContexts.getMetaData().getDatabases().putAll(createChangedDatabases(databaseName, switchingResource, null));
        metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(each -> each.addResource(metaDataContexts.getMetaData().getDatabase(databaseName)));
        metaDataContexts.getOptimizerContext().alterDatabase(metaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.getMetaData().getGlobalRuleMetaData());
        persistMetaData(metaDataContexts);
        metaDataContexts.getPersistService().getDataSourceService().append(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), toBeUpdatedDataSourcePropsMap);
        switchingResource.closeStaleDataSources();
    }
    
    /**
     * Drop resources.
     *
     * @param databaseName database name
     * @param toBeDroppedResourceNames to be dropped resource names
     */
    public synchronized void dropResources(final String databaseName, final Collection<String> toBeDroppedResourceNames) {
        Map<String, DataSource> dataSourceMap = metaDataContexts.getMetaData().getDatabase(databaseName).getResource().getDataSources();
        // TODO should check to be dropped resources are unused here. ContextManager is atomic domain to maintain metadata, not Dist SQL handler
        for (String each : toBeDroppedResourceNames) {
            dataSourceMap.remove(each);
        }
        metaDataContexts.getPersistService().getDataSourceService().drop(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), toBeDroppedResourceNames);
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
            metaDataContexts = createMetaDataContexts(databaseName, null, ruleConfigs);
            persistMetaData(metaDataContexts);
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
            SwitchingResource switchingResource = new ResourceSwitchManager().create(metaDataContexts.getMetaData().getDatabase(databaseName).getResource(), dataSourcePropsMap);
            metaDataContexts = createMetaDataContexts(databaseName, switchingResource, null);
            persistMetaData(metaDataContexts);
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
            SwitchingResource switchingResource = new ResourceSwitchManager().create(metaDataContexts.getMetaData().getDatabase(databaseName).getResource(), dataSourcePropsMap);
            metaDataContexts = createMetaDataContexts(databaseName, switchingResource, ruleConfigs);
            persistMetaData(metaDataContexts);
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
        ShardingSphereRuleMetaData changedGlobalMetaData = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), changedDatabases, instanceContext));
        return newMetaDataContexts(new ShardingSphereMetaData(changedDatabases, changedGlobalMetaData, metaDataContexts.getMetaData().getProps()),
                OptimizerContextFactory.create(changedDatabases, changedGlobalMetaData));
    }
    
    private Map<String, ShardingSphereDatabase> createChangedDatabases(final String databaseName,
                                                                       final SwitchingResource switchingResource, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Map<String, DataSource> toBeCreatedDataSources = null == switchingResource
                ? metaDataContexts.getMetaData().getDatabase(databaseName).getResource().getDataSources()
                : switchingResource.getNewDataSources();
        Collection<RuleConfiguration> toBeCreatedRuleConfigs = null == ruleConfigs
                ? metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getConfigurations()
                : ruleConfigs;
        DatabaseConfiguration toBeCreatedDatabaseConfig = new DataSourceProvidedDatabaseConfiguration(toBeCreatedDataSources, toBeCreatedRuleConfigs);
        ShardingSphereDatabase changedDatabase = ShardingSphereDatabasesFactory.create(metaDataContexts.getMetaData().getActualDatabaseName(databaseName),
                toBeCreatedDatabaseConfig, metaDataContexts.getMetaData().getProps(), instanceContext);
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(metaDataContexts.getMetaData().getDatabases());
        result.put(databaseName.toLowerCase(), changedDatabase);
        return result;
    }
    
    private MetaDataContexts newMetaDataContexts(final ShardingSphereMetaData metaData, final OptimizerContext optimizerContext) {
        return new MetaDataContexts(metaDataContexts.getPersistService(), metaData, optimizerContext);
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
                GlobalRulesBuilder.buildRules(ruleConfigs, metaDataContexts.getMetaData().getDatabases(), instanceContext));
        ShardingSphereMetaData toBeChangedMetaData = new ShardingSphereMetaData(
                metaDataContexts.getMetaData().getDatabases(), toBeChangedGlobalRuleMetaData, metaDataContexts.getMetaData().getProps());
        metaDataContexts = newMetaDataContexts(toBeChangedMetaData, metaDataContexts.getOptimizerContext());
    }
    
    /**
     * Alter properties.
     *
     * @param props properties to be altered
     */
    public synchronized void alterProperties(final Properties props) {
        ShardingSphereMetaData toBeChangedMetaData = new ShardingSphereMetaData(
                metaDataContexts.getMetaData().getDatabases(), metaDataContexts.getMetaData().getGlobalRuleMetaData(), new ConfigurationProperties(props));
        metaDataContexts = newMetaDataContexts(toBeChangedMetaData, metaDataContexts.getOptimizerContext());
    }
    
    /**
     * Reload database.
     *
     * @param databaseName to be reloaded database name
     */
    public synchronized void reloadDatabase(final String databaseName) {
        try {
            ShardingSphereResource currentResource = metaDataContexts.getMetaData().getDatabase(databaseName).getResource();
            SwitchingResource switchingResource = new SwitchingResource(currentResource, currentResource.getDataSources(), Collections.emptyMap());
            MetaDataContexts reloadedMetaDataContexts = createMetaDataContexts(databaseName, switchingResource, null);
            Map<String, ShardingSphereSchema> toBeDeletedSchemas = getToBeDeletedSchemas(reloadedMetaDataContexts.getMetaData().getDatabase(databaseName));
            metaDataContexts = reloadedMetaDataContexts;
            toBeDeletedSchemas.keySet().forEach(each -> reloadedMetaDataContexts.getPersistService().getDatabaseMetaDataService().deleteSchema(databaseName, each));
            persistMetaData(reloadedMetaDataContexts);
        } catch (final SQLException ex) {
            log.error("Reload database: {} failed", databaseName, ex);
        }
    }
    
    private Map<String, ShardingSphereSchema> getToBeDeletedSchemas(final ShardingSphereDatabase reloadedDatabase) {
        Map<String, ShardingSphereSchema> currentSchemas = metaDataContexts.getMetaData().getDatabase(reloadedDatabase.getName()).getSchemas();
        return currentSchemas.entrySet().stream().filter(entry -> !reloadedDatabase.containsSchema(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private void persistMetaData(final MetaDataContexts metaDataContexts) {
        metaDataContexts.getMetaData().getDatabases().values().forEach(
                each -> each.getSchemas().forEach((schemaName, tables) -> metaDataContexts.getPersistService().getDatabaseMetaDataService().persistMetaData(each.getName(), schemaName, tables)));
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
            if (null == reloadedSchema) {
                metaDataContexts.getMetaData().getDatabase(databaseName).removeSchema(schemaName);
                metaDataContexts.getPersistService().getDatabaseMetaDataService().deleteSchema(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), schemaName);
            } else {
                metaDataContexts.getMetaData().getDatabase(databaseName).putSchema(schemaName, reloadedSchema);
                metaDataContexts.getPersistService().getDatabaseMetaDataService().persistMetaData(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), schemaName, reloadedSchema);
            }
        } catch (final SQLException ex) {
            log.error("Reload meta data of database: {} schema: {} with data source: {} failed", databaseName, schemaName, dataSourceName, ex);
        }
    }
    
    private ShardingSphereSchema loadSchema(final String databaseName, final String schemaName, final String dataSourceName) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        database.reloadRules(MutableDataNodeRule.class, instanceContext);
        GenericSchemaBuilderMaterials materials = new GenericSchemaBuilderMaterials(database.getProtocolType(), database.getResource().getDatabaseType(),
                Collections.singletonMap(dataSourceName, database.getResource().getDataSources().get(dataSourceName)),
                database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
        return GenericSchemaBuilder.build(materials).get(schemaName);
    }
    
    /**
     * Reload table.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName to be reloaded table name
     */
    public void reloadTable(final String databaseName, final String schemaName, final String tableName) {
        Map<String, DataSource> dataSourceMap = metaDataContexts.getMetaData().getDatabase(databaseName).getResource().getDataSources();
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
                dataSourceName, metaDataContexts.getMetaData().getDatabase(databaseName).getResource().getDataSources().get(dataSourceName));
        try {
            reloadTable(databaseName, schemaName, tableName, dataSourceMap);
        } catch (final SQLException ex) {
            log.error("Reload table: {} meta data of database: {} schema: {} with data source: {} failed", tableName, databaseName, schemaName, dataSourceName, ex);
        }
    }
    
    private synchronized void reloadTable(final String databaseName, final String schemaName, final String tableName, final Map<String, DataSource> dataSourceMap) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        GenericSchemaBuilderMaterials materials = new GenericSchemaBuilderMaterials(database.getProtocolType(),
                database.getResource().getDatabaseType(), dataSourceMap, database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
        ShardingSphereSchema schema = GenericSchemaBuilder.build(Collections.singletonList(tableName), materials).getOrDefault(schemaName, new ShardingSphereSchema());
        if (schema.containsTable(tableName)) {
            database.getSchema(schemaName).put(tableName, schema.get(tableName));
            metaDataContexts.getPersistService().getDatabaseMetaDataService().persistMetaData(database.getName(), schemaName, database.getSchema(schemaName));
        }
    }
    
    @Override
    public void close() throws Exception {
        executorEngine.close();
        metaDataContexts.close();
    }
}
