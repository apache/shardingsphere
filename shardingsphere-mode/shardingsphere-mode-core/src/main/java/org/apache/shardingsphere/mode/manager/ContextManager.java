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
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContextFactory;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.SystemSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
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
        return metaDataContexts.getMetaData().getDatabases().get(databaseName).getResource().getDataSources();
    }
    
    /**
     * Add database.
     *
     * @param databaseName database name
     * @throws SQLException SQL exception
     */
    public synchronized void addDatabase(final String databaseName) throws SQLException {
        if (metaDataContexts.getMetaData().getDatabases().containsKey(databaseName)) {
            return;
        }
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), metaDataContexts.getMetaData().getProps());
        metaDataContexts.getMetaData().addDatabase(databaseName, protocolType);
        metaDataContexts.getOptimizerContext().addDatabase(databaseName, protocolType);
        metaDataContexts.getPersistService().ifPresent(optional -> optional.getDatabaseMetaDataService().persistDatabase(databaseName));
    }
    
    /**
     * Drop database.
     *
     * @param databaseName database name
     */
    public void dropDatabase(final String databaseName) {
        if (!metaDataContexts.getMetaData().getDatabases().containsKey(databaseName)) {
            return;
        }
        metaDataContexts.getMetaData().dropDatabase(databaseName);
        metaDataContexts.getOptimizerContext().dropDatabase(databaseName);
        metaDataContexts.getPersistService().ifPresent(optional -> optional.getDatabaseMetaDataService().deleteDatabase(databaseName));
    }
    
    /**
     * Add schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public void addSchema(final String databaseName, final String schemaName) {
        if (metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().containsKey(schemaName)) {
            return;
        }
        metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().put(schemaName, new ShardingSphereSchema());
        metaDataContexts.getOptimizerContext().addSchema(databaseName, schemaName);
    }
    
    /**
     * Alter schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeChangedTable to be changed table
     * @param toBeDeletedTableName to be deleted table name
     */
    public void alterSchema(final String databaseName, final String schemaName, final ShardingSphereTable toBeChangedTable, final String toBeDeletedTableName) {
        if (metaDataContexts.getMetaData().getDatabases().containsKey(databaseName)) {
            Optional.ofNullable(toBeChangedTable).ifPresent(optional -> alterTable(databaseName, schemaName, optional));
            Optional.ofNullable(toBeDeletedTableName).ifPresent(optional -> dropTable(databaseName, schemaName, optional));
        }
    }
    
    private void alterTable(final String databaseName, final String schemaName, final ShardingSphereTable beBoChangedTable) {
        alterTable(metaDataContexts.getMetaData().getDatabases().get(databaseName), schemaName, beBoChangedTable);
        metaDataContexts.getOptimizerContext().alterTable(databaseName, schemaName, beBoChangedTable);
    }
    
    private void alterTable(final ShardingSphereDatabase database, final String schemaName, final ShardingSphereTable beBoChangedTable) {
        if (containsMutableDataNodeRule(database, schemaName, beBoChangedTable.getName())) {
            database.reloadRules(instanceContext);
        }
        database.getSchemas().get(schemaName).put(beBoChangedTable.getName(), beBoChangedTable);
    }
    
    private boolean containsMutableDataNodeRule(final ShardingSphereDatabase database, final String schemaName, final String tableName) {
        return database.getRuleMetaData().findRules(MutableDataNodeRule.class).stream().anyMatch(each -> each.findSingleTableDataNode(schemaName, tableName).isPresent());
    }
    
    private void dropTable(final String databaseName, final String schemaName, final String toBeDeletedTableName) {
        if (metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().containsKey(schemaName)) {
            metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().get(schemaName).remove(toBeDeletedTableName);
            metaDataContexts.getOptimizerContext().dropTable(databaseName, schemaName, toBeDeletedTableName);
        }
    }
    
    /**
     * Drop schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public void dropSchema(final String databaseName, final String schemaName) {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabases().get(databaseName);
        if (null == database || !database.getSchemas().containsKey(schemaName)) {
            return;
        }
        database.getSchemas().remove(schemaName);
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(databaseName).removeSchemaMetadata(schemaName);
    }
    
    /**
     * Add resource.
     *
     * @param databaseName database name
     * @param toBeAddedDataSourcePropsMap data source properties map
     * @throws SQLException SQL exception
     */
    public void addResource(final String databaseName, final Map<String, DataSourceProperties> toBeAddedDataSourcePropsMap) throws SQLException {
        refreshMetaDataContextForAddResource(databaseName, toBeAddedDataSourcePropsMap);
        metaDataContexts.getPersistService().ifPresent(optional -> optional.getDataSourceService().append(databaseName, toBeAddedDataSourcePropsMap));
    }
    
    /**
     * Alter resource.
     *
     * @param databaseName database name
     * @param toBeAlteredDataSourcePropsMap data source properties map
     * @throws SQLException SQL exception
     */
    public void alterResource(final String databaseName, final Map<String, DataSourceProperties> toBeAlteredDataSourcePropsMap) throws SQLException {
        refreshMetaDataContextForAlterResource(databaseName, toBeAlteredDataSourcePropsMap);
        metaDataContexts.getPersistService().ifPresent(optional -> optional.getDataSourceService().append(databaseName, toBeAlteredDataSourcePropsMap));
    }
    
    /**
     * Drop resource.
     *
     * @param databaseName database name
     * @param toBeDroppedResourceNames to be dropped resource names
     */
    public void dropResource(final String databaseName, final Collection<String> toBeDroppedResourceNames) {
        Map<String, DataSource> dataSourceMap = metaDataContexts.getMetaData().getDatabases().get(databaseName).getResource().getDataSources();
        // TODO should check to be dropped resources are unused here. ContextManager is atomic domain to maintain metadata, not Dist SQL handler
        for (String each : toBeDroppedResourceNames) {
            dataSourceMap.remove(each);
        }
        metaDataContexts.getPersistService().ifPresent(optional -> optional.getDataSourceService().drop(databaseName, toBeDroppedResourceNames));
    }
    
    /**
     * Alter rule configuration.
     *
     * @param databaseName database name
     * @param ruleConfigs rule configurations
     */
    @SuppressWarnings("rawtypes")
    public void alterRuleConfiguration(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) {
        try {
            Collection<ResourceHeldRule> staleResourceHeldRules = metaDataContexts.getMetaData().getDatabases().get(databaseName).getRuleMetaData().findRules(ResourceHeldRule.class);
            metaDataContexts = createMetaDataContextsWithAlteredDatabaseRules(databaseName, ruleConfigs);
            persistMetaData(metaDataContexts);
            staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResources);
        } catch (final SQLException ex) {
            log.error("Alter database: {} rule configurations failed", databaseName, ex);
        }
    }
    
    private MetaDataContexts createMetaDataContextsWithAlteredDatabaseRules(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        ShardingSphereDatabase toBeChangedDatabase = metaDataContexts.getMetaData().getDatabases().get(databaseName);
        ConfigurationProperties props = metaDataContexts.getMetaData().getProps();
        Map<String, ShardingSphereDatabase> databases = ShardingSphereDatabasesFactory.create(
                Collections.singletonMap(databaseName, new DataSourceProvidedDatabaseConfiguration(toBeChangedDatabase.getResource().getDataSources(), ruleConfigs)), props, instanceContext);
        ShardingSphereRuleMetaData globalMetaData = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), databases, instanceContext));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, globalMetaData, props);
        return new MetaDataContexts(metaDataContexts.getPersistService().orElse(null), metaData, OptimizerContextFactory.create(databases, globalMetaData));
    }
    
    /**
     * Alter data source configuration.
     *
     * @param databaseName database name
     * @param dataSourcePropsMap altered data source properties map
     */
    public void alterDataSourceConfiguration(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        try {
            MetaDataContexts changedMetaDataContext = createMetaDataContextsWithAlteredResources(databaseName, dataSourcePropsMap);
            persistMetaData(changedMetaDataContext);
            refreshMetaDataContext(databaseName, changedMetaDataContext, dataSourcePropsMap);
        } catch (final SQLException ex) {
            log.error("Alter database:{} data source configuration failed", databaseName, ex);
        }
    }
    
    private MetaDataContexts createMetaDataContextsWithAlteredResources(final String databaseName, final Map<String, DataSourceProperties> dataSourceProps) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabases().get(databaseName);
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(getNewDataSources(database, dataSourceProps), database.getRuleMetaData().getConfigurations());
        ConfigurationProperties props = metaDataContexts.getMetaData().getProps();
        Map<String, ShardingSphereDatabase> databases = ShardingSphereDatabasesFactory.create(Collections.singletonMap(database.getName(), databaseConfig), props, instanceContext);
        ShardingSphereRuleMetaData globalMetaData = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), databases, instanceContext));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, globalMetaData, props);
        return new MetaDataContexts(metaDataContexts.getPersistService().orElse(null), metaData, OptimizerContextFactory.create(databases, globalMetaData));
    }
    
    /**
     * Alter data source and rule configuration.
     *
     * @param databaseName database name
     * @param dataSourcePropsMap data source props map
     * @param ruleConfigs rule configurations
     */
    public void alterDataSourceAndRuleConfiguration(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap, final Collection<RuleConfiguration> ruleConfigs) {
        try {
            MetaDataContexts changedMetaDataContext = buildChangedMetaDataContextWithChangedDataSourceAndRule(
                    metaDataContexts.getMetaData().getDatabases().get(databaseName), dataSourcePropsMap, ruleConfigs);
            refreshMetaDataContext(databaseName, changedMetaDataContext, dataSourcePropsMap);
        } catch (SQLException ex) {
            log.error("Alter database:{} data source and rule configuration failed", databaseName, ex);
        }
    }
    
    /**
     * Alter global rule configuration.
     *
     * @param ruleConfigs global rule configuration
     */
    public void alterGlobalRuleConfiguration(final Collection<RuleConfiguration> ruleConfigs) {
        if (ruleConfigs.isEmpty()) {
            return;
        }
        MetaDataContexts newMetaDataContexts = rebuildMetaDataContexts(
                new ShardingSphereRuleMetaData(GlobalRulesBuilder.buildRules(ruleConfigs, metaDataContexts.getMetaData().getDatabases(), instanceContext)));
        metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResources);
        renewMetaDataContexts(newMetaDataContexts);
    }
    
    /**
     * Alter properties.
     *
     * @param props properties to be altered
     */
    public void alterProperties(final Properties props) {
        renewMetaDataContexts(rebuildMetaDataContexts(new ConfigurationProperties(props)));
    }
    
    /**
     * Reload meta data.
     *
     * @param databaseName database name to be reloaded
     */
    public void reloadMetaData(final String databaseName) {
        try {
            Map<String, ShardingSphereSchema> schemas = loadActualSchema(databaseName);
            deleteSchemas(databaseName, schemas);
            alterSchemas(databaseName, schemas);
            persistMetaData(metaDataContexts);
        } catch (final SQLException ex) {
            log.error("Reload database:{} failed", databaseName, ex);
        }
    }
    
    /**
     * Reload table meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName logic table name
     */
    public void reloadMetaData(final String databaseName, final String schemaName, final String tableName) {
        try {
            ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabases().get(databaseName);
            GenericSchemaBuilderMaterials materials = new GenericSchemaBuilderMaterials(database.getProtocolType(),
                    database.getResource().getDatabaseType(), database.getResource().getDataSources(), database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
            loadTableMetaData(databaseName, schemaName, tableName, materials);
        } catch (final SQLException ex) {
            log.error("Reload table:{} meta data of database:{} schema:{} failed", tableName, databaseName, schemaName, ex);
        }
    }
    
    /**
     * Reload single data source table meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param dataSourceName data source name
     * @param tableName logic table name
     */
    public void reloadMetaData(final String databaseName, final String schemaName, final String dataSourceName, final String tableName) {
        try {
            ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabases().get(databaseName);
            GenericSchemaBuilderMaterials materials = new GenericSchemaBuilderMaterials(database.getProtocolType(), database.getResource().getDatabaseType(),
                    Collections.singletonMap(dataSourceName, database.getResource().getDataSources().get(dataSourceName)),
                    database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
            loadTableMetaData(databaseName, schemaName, tableName, materials);
        } catch (final SQLException ex) {
            log.error("Reload table:{} meta data of database:{} schema:{} with data source:{} failed", tableName, databaseName, schemaName, dataSourceName, ex);
        }
    }
    
    /**
     * Reload table meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param dataSourceName data source name
     */
    public void reloadSchemaMetaData(final String databaseName, final String schemaName, final String dataSourceName) {
        try {
            ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabases().get(databaseName);
            database.reloadRules(instanceContext);
            GenericSchemaBuilderMaterials materials = new GenericSchemaBuilderMaterials(database.getProtocolType(), database.getResource().getDatabaseType(),
                    Collections.singletonMap(dataSourceName, database.getResource().getDataSources().get(dataSourceName)),
                    database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
            loadTableMetaData(databaseName, schemaName, materials);
        } catch (final SQLException ex) {
            log.error("Reload meta data of database:{} schema:{} with data source:{} failed", databaseName, schemaName, dataSourceName, ex);
        }
    }
    
    private void alterSchemas(final String databaseName, final Map<String, ShardingSphereSchema> schemas) {
        ShardingSphereDatabase alteredMetaData = new ShardingSphereDatabase(databaseName, metaDataContexts.getMetaData().getDatabases().get(databaseName).getProtocolType(),
                metaDataContexts.getMetaData().getDatabases().get(databaseName).getResource(), metaDataContexts.getMetaData().getDatabases().get(databaseName).getRuleMetaData(), schemas);
        Map<String, ShardingSphereDatabase> alteredDatabases = new HashMap<>(metaDataContexts.getMetaData().getDatabases());
        alteredDatabases.put(databaseName, alteredMetaData);
        FederationDatabaseMetaData alteredDatabaseMetaData = new FederationDatabaseMetaData(databaseName, schemas);
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().put(databaseName, alteredDatabaseMetaData);
        metaDataContexts.getOptimizerContext().getPlannerContexts().put(databaseName, OptimizerPlannerContextFactory.create(alteredDatabaseMetaData));
        renewMetaDataContexts(
                rebuildMetaDataContexts(new ShardingSphereMetaData(alteredDatabases, metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps())));
    }
    
    private void deleteSchemas(final String databaseName, final Map<String, ShardingSphereSchema> actualSchemas) {
        Map<String, ShardingSphereSchema> originalSchemas = metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas();
        if (originalSchemas.isEmpty()) {
            return;
        }
        originalSchemas.forEach((key, value) -> {
            if (null == actualSchemas.get(key)) {
                metaDataContexts.getPersistService().ifPresent(optional -> optional.getDatabaseMetaDataService().deleteSchema(databaseName, key));
            }
        });
    }
    
    private void loadTableMetaData(final String databaseName, final String schemaName, final GenericSchemaBuilderMaterials materials) throws SQLException {
        Map<String, ShardingSphereSchema> schemaMap = GenericSchemaBuilder.build(materials);
        if (schemaMap.containsKey(schemaName)) {
            metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().put(schemaName, schemaMap.get(schemaName));
            metaDataContexts.getPersistService().ifPresent(optional -> optional.getDatabaseMetaDataService()
                    .persistMetaData(databaseName, schemaName, metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().get(schemaName)));
        }
    }
    
    private void loadTableMetaData(final String databaseName, final String schemaName, final String tableName, final GenericSchemaBuilderMaterials materials) throws SQLException {
        ShardingSphereSchema schema = GenericSchemaBuilder.build(Collections.singletonList(tableName), materials).getOrDefault(schemaName, new ShardingSphereSchema());
        if (schema.containsTable(tableName)) {
            metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().get(schemaName).put(tableName, schema.get(tableName));
            metaDataContexts.getPersistService().ifPresent(optional -> optional.getDatabaseMetaDataService()
                    .persistMetaData(databaseName, schemaName, metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().get(schemaName)));
        }
    }
    
    private Map<String, ShardingSphereSchema> loadActualSchema(final String databaseName) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabases().get(databaseName);
        Map<String, DataSource> dataSourceMap = database.getResource().getDataSources();
        database.reloadRules(instanceContext);
        DatabaseType databaseType = DatabaseTypeEngine.getDatabaseType(dataSourceMap.values());
        Map<String, ShardingSphereSchema> result = new ConcurrentHashMap<>();
        GenericSchemaBuilderMaterials materials = new GenericSchemaBuilderMaterials(database.getProtocolType(),
                databaseType, dataSourceMap, database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), databaseName);
        result.putAll(GenericSchemaBuilder.build(materials));
        result.putAll(SystemSchemaBuilder.build(databaseName, database.getProtocolType()));
        return result;
    }
    
    private Collection<DataSource> getPendingClosedDataSources(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        Collection<DataSource> result = new LinkedList<>();
        result.addAll(getDeletedDataSources(metaDataContexts.getMetaData().getDatabases().get(databaseName), dataSourcePropsMap).values());
        result.addAll(getChangedDataSources(metaDataContexts.getMetaData().getDatabases().get(databaseName), dataSourcePropsMap).values());
        return result;
    }
    
    private Map<String, DataSource> getDeletedDataSources(final ShardingSphereDatabase database, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        return database.getResource().getDataSources().entrySet().stream().filter(entry -> !dataSourcePropsMap.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    private Map<String, DataSource> getChangedDataSources(final ShardingSphereDatabase database, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        Collection<String> changedDataSourceNames = getChangedDataSourceProperties(database, dataSourcePropsMap).keySet();
        return database.getResource().getDataSources().entrySet().stream().filter(entry -> changedDataSourceNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    private Map<String, DataSourceProperties> getChangedDataSourceProperties(final ShardingSphereDatabase database, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        return dataSourcePropsMap.entrySet().stream()
                .filter(entry -> isModifiedDataSource(database.getResource().getDataSources(), entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private boolean isModifiedDataSource(final Map<String, DataSource> originalDataSources, final String dataSourceName, final DataSourceProperties dataSourceProps) {
        return originalDataSources.containsKey(dataSourceName) && !dataSourceProps.equals(DataSourcePropertiesCreator.create(originalDataSources.get(dataSourceName)));
    }
    
    private MetaDataContexts rebuildMetaDataContexts(final ShardingSphereMetaData changedMetaData) {
        return new MetaDataContexts(metaDataContexts.getPersistService().orElse(null), changedMetaData, metaDataContexts.getOptimizerContext());
    }
    
    private MetaDataContexts rebuildMetaDataContexts(final ShardingSphereRuleMetaData globalRuleMetaData) {
        ShardingSphereMetaData changedMetaData = new ShardingSphereMetaData(metaDataContexts.getMetaData().getDatabases(), globalRuleMetaData, metaDataContexts.getMetaData().getProps());
        return new MetaDataContexts(metaDataContexts.getPersistService().orElse(null), changedMetaData, metaDataContexts.getOptimizerContext());
    }
    
    private MetaDataContexts rebuildMetaDataContexts(final ConfigurationProperties props) {
        ShardingSphereMetaData changedMetaData = new ShardingSphereMetaData(metaDataContexts.getMetaData().getDatabases(), metaDataContexts.getMetaData().getGlobalRuleMetaData(), props);
        return new MetaDataContexts(metaDataContexts.getPersistService().orElse(null), changedMetaData, metaDataContexts.getOptimizerContext());
    }
    
    private void refreshMetaDataContextForAddResource(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap) throws SQLException {
        MetaDataContexts changedMetaDataContexts = buildChangedMetaDataContextWithAddedDataSource(databaseName, dataSourcePropsMap);
        refreshMetaDataContext(databaseName, changedMetaDataContexts);
    }
    
    private void refreshMetaDataContextForAlterResource(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap) throws SQLException {
        MetaDataContexts changedMetaDataContexts = buildChangedMetaDataContextWithAlteredDataSource(databaseName, dataSourcePropsMap);
        refreshMetaDataContext(databaseName, changedMetaDataContexts);
    }
    
    private void refreshMetaDataContext(final String databaseName, final MetaDataContexts changedMetaDataContexts) {
        metaDataContexts.getMetaData().getDatabases().putAll(changedMetaDataContexts.getMetaData().getDatabases());
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().putAll(changedMetaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases());
        metaDataContexts.getOptimizerContext().getParserContexts().putAll(changedMetaDataContexts.getOptimizerContext().getParserContexts());
        metaDataContexts.getOptimizerContext().getPlannerContexts().putAll(changedMetaDataContexts.getOptimizerContext().getPlannerContexts());
        metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(each -> each.addResource(metaDataContexts.getMetaData().getDatabases().get(databaseName)));
    }
    
    private void refreshMetaDataContext(final String databaseName, final MetaDataContexts changedMetaDataContexts, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().putAll(changedMetaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases());
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(metaDataContexts.getMetaData().getDatabases());
        databases.putAll(changedMetaDataContexts.getMetaData().getDatabases());
        final Collection<DataSource> pendingClosedDataSources = getPendingClosedDataSources(databaseName, dataSourcePropsMap);
        MetaDataContexts newMetaDataContexts = rebuildMetaDataContexts(
                new ShardingSphereMetaData(databases, metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps()));
        metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResources);
        metaDataContexts.getMetaData().getDatabases().get(databaseName).getRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResources);
        renewMetaDataContexts(newMetaDataContexts);
        pendingClosedDataSources.forEach(metaDataContexts.getMetaData().getDatabases().get(databaseName).getResource()::close);
    }
    
    private MetaDataContexts buildChangedMetaDataContextWithAddedDataSource(final String databaseName, final Map<String, DataSourceProperties> addedDataSourceProps) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabases().get(databaseName);
        Map<String, DataSource> dataSourceMap = new HashMap<>(database.getResource().getDataSources());
        dataSourceMap.putAll(DataSourcePoolCreator.create(addedDataSourceProps));
        ConfigurationProperties props = metaDataContexts.getMetaData().getProps();
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(dataSourceMap, database.getRuleMetaData().getConfigurations());
        Map<String, ShardingSphereDatabase> databases = ShardingSphereDatabasesFactory.create(Collections.singletonMap(database.getName(), databaseConfig), props, instanceContext);
        ShardingSphereRuleMetaData globalMetaData = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), databases, instanceContext));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, globalMetaData, props);
        MetaDataContexts result = new MetaDataContexts(metaDataContexts.getPersistService().orElse(null), metaData, OptimizerContextFactory.create(databases, globalMetaData));
        persistMetaData(result);
        return result;
    }
    
    private MetaDataContexts buildChangedMetaDataContextWithAlteredDataSource(final String databaseName, final Map<String, DataSourceProperties> alteredDataSourceProps) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabases().get(databaseName);
        Map<String, DataSource> pendingClosedDataSources = getChangedDataSources(database, alteredDataSourceProps);
        Map<String, DataSourceProperties> pendingAlteredDataSourceProps = alteredDataSourceProps.entrySet().stream().filter(entry -> pendingClosedDataSources.keySet().contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        pendingClosedDataSources.values().forEach(database.getResource()::close);
        Map<String, DataSource> dataSourceMap = new HashMap<>(database.getResource().getDataSources());
        dataSourceMap.putAll(DataSourcePoolCreator.create(pendingAlteredDataSourceProps));
        ConfigurationProperties props = metaDataContexts.getMetaData().getProps();
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(dataSourceMap, database.getRuleMetaData().getConfigurations());
        Map<String, ShardingSphereDatabase> databases = ShardingSphereDatabasesFactory.create(Collections.singletonMap(database.getName(), databaseConfig), props, instanceContext);
        ShardingSphereRuleMetaData globalMetaData = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), databases, instanceContext));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, globalMetaData, props);
        MetaDataContexts result = new MetaDataContexts(metaDataContexts.getPersistService().orElse(null), metaData, OptimizerContextFactory.create(databases, globalMetaData));
        persistMetaData(result);
        return result;
    }
    
    private MetaDataContexts buildChangedMetaDataContextWithChangedDataSourceAndRule(final ShardingSphereDatabase database, final Map<String, DataSourceProperties> dataSourceProps,
                                                                                     final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(getNewDataSources(database, dataSourceProps), ruleConfigs);
        ConfigurationProperties props = metaDataContexts.getMetaData().getProps();
        Map<String, ShardingSphereDatabase> databases = ShardingSphereDatabasesFactory.create(Collections.singletonMap(database.getName(), databaseConfig), props, instanceContext);
        ShardingSphereRuleMetaData globalMetaData = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), databases, instanceContext));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, globalMetaData, props);
        MetaDataContexts result = new MetaDataContexts(metaDataContexts.getPersistService().orElse(null), metaData, OptimizerContextFactory.create(databases, globalMetaData));
        persistMetaData(result);
        return result;
    }
    
    private Map<String, DataSource> getNewDataSources(final ShardingSphereDatabase database, final Map<String, DataSourceProperties> toBeChangedDataSourceProps) {
        Map<String, DataSource> result = new LinkedHashMap<>(database.getResource().getDataSources());
        result.keySet().removeAll(getDeletedDataSources(database, toBeChangedDataSourceProps).keySet());
        result.putAll(buildToBeChangedDataSources(database, toBeChangedDataSourceProps));
        // TODO close stale data sources
        result.putAll(buildToBeAddedDataSources(database, toBeChangedDataSourceProps));
        return result;
    }
    
    private Map<String, DataSource> buildToBeAddedDataSources(final ShardingSphereDatabase database, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        Map<String, DataSourceProperties> toBeAddedDataSourceProps = dataSourcePropsMap.entrySet().stream()
                .filter(entry -> !database.getResource().getDataSources().containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return DataSourcePoolCreator.create(toBeAddedDataSourceProps);
    }
    
    private Map<String, DataSource> buildToBeChangedDataSources(final ShardingSphereDatabase database, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        return DataSourcePoolCreator.create(getChangedDataSourceProperties(database, dataSourcePropsMap));
    }
    
    private void persistMetaData(final MetaDataContexts metaDataContexts) {
        metaDataContexts.getMetaData().getDatabases().forEach((databaseName, schemas) -> schemas.getSchemas()
                .forEach((schemaName, tables) -> metaDataContexts.getPersistService().ifPresent(optional -> optional.getDatabaseMetaDataService().persistMetaData(databaseName, schemaName, tables))));
    }
    
    @Override
    public void close() throws Exception {
        executorEngine.close();
        metaDataContexts.close();
    }
}
