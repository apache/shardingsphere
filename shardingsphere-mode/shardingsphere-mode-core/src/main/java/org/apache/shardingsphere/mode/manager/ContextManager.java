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
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContextFactory;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.SystemSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.rule.builder.schema.DatabaseRulesBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.InstanceAwareRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;

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
        setInstanceContext();
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
    public void addDatabase(final String databaseName) throws SQLException {
        if (metaDataContexts.getMetaData().getDatabases().containsKey(databaseName)) {
            return;
        }
        MetaDataContexts newMetaDataContexts = createMetaDataContext(databaseName);
        FederationDatabaseMetaData federationDatabaseMetaData = newMetaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(databaseName);
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().put(databaseName, federationDatabaseMetaData);
        metaDataContexts.getOptimizerContext().getPlannerContexts().put(databaseName, OptimizerPlannerContextFactory.create(federationDatabaseMetaData));
        metaDataContexts.getMetaData().getDatabases().put(databaseName, newMetaDataContexts.getMetaData().getDatabases().get(databaseName));
        persistMetaData(metaDataContexts);
        metaDataContexts.getMetaData().getGlobalRuleMetaData()
                .findRules(ResourceHeldRule.class).forEach(each -> each.addResource(newMetaDataContexts.getMetaData().getDatabases().get(databaseName)));
        metaDataContexts.getMetaData().getDatabases().get(databaseName).getRuleMetaData()
                .findRules(ResourceHeldRule.class).forEach(each -> each.addResource(newMetaDataContexts.getMetaData().getDatabases().get(databaseName)));
    }
    
    private MetaDataContexts createMetaDataContext(final String databaseName) throws SQLException {
        MetaDataContextsBuilder builder = new MetaDataContextsBuilder(
                Collections.singletonMap(databaseName, new DataSourceProvidedDatabaseConfiguration(new HashMap<>(), new LinkedList<>())),
                metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), metaDataContexts.getMetaData().getProps());
        return builder.build(metaDataContexts.getPersistService().orElse(null));
    }
    
    /**
     * Add schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public void addSchema(final String databaseName, final String schemaName) {
        if (null != metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().get(schemaName)) {
            return;
        }
        FederationDatabaseMetaData federationDatabaseMetaData = metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(databaseName);
        federationDatabaseMetaData.putTable(schemaName, new ShardingSphereTable());
        metaDataContexts.getOptimizerContext().getPlannerContexts().put(databaseName, OptimizerPlannerContextFactory.create(federationDatabaseMetaData));
        metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().put(schemaName, new ShardingSphereSchema());
    }
    
    /**
     * Alter schemas.
     *
     * @param databaseName database name
     * @param schemas schemas
     */
    public void alterSchemas(final String databaseName, final Map<String, ShardingSphereSchema> schemas) {
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
    
    /**
     * Alter schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param changedTableMetaData changed table meta data
     * @param deletedTable deleted table
     */
    public void alterSchema(final String databaseName, final String schemaName, final ShardingSphereTable changedTableMetaData, final String deletedTable) {
        if (null != metaDataContexts.getMetaData().getDatabases().get(databaseName)) {
            Optional.ofNullable(changedTableMetaData).ifPresent(optional -> alterTableSchema(databaseName, schemaName, optional));
            Optional.ofNullable(deletedTable).ifPresent(optional -> deleteTable(databaseName, schemaName, optional));
        }
    }
    
    private void persistMetaData(final MetaDataContexts metaDataContexts) {
        metaDataContexts.getMetaData().getDatabases().forEach((databaseName, schemas) -> schemas.getSchemas()
                .forEach((schemaName, tables) -> metaDataContexts.getPersistService().ifPresent(optional -> optional.getSchemaMetaDataService().persistMetaData(databaseName, schemaName, tables))));
    }
    
    private void alterTableSchema(final String databaseName, final String schemaName, final ShardingSphereTable changedTableMetaData) {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabases().get(databaseName);
        alterSingleTableDataNodes(databaseName, database, changedTableMetaData);
        FederationDatabaseMetaData federationDatabaseMetaData = metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(databaseName);
        database.getSchemas().get(schemaName).put(changedTableMetaData.getName(), changedTableMetaData);
        federationDatabaseMetaData.putTable(schemaName, changedTableMetaData);
        metaDataContexts.getOptimizerContext().getPlannerContexts().put(databaseName, OptimizerPlannerContextFactory.create(federationDatabaseMetaData));
    }
    
    private void alterSingleTableDataNodes(final String databaseName, final ShardingSphereDatabase database, final ShardingSphereTable changedTableMetaData) {
        if (!containsInImmutableDataNodeContainedRule(changedTableMetaData.getName(), database)) {
            refreshRules(databaseName, database);
        }
    }
    
    private void refreshRules(final String databaseName, final ShardingSphereDatabase database) {
        Collection<ShardingSphereRule> databaseRules = DatabaseRulesBuilder.build(databaseName, new DataSourceProvidedDatabaseConfiguration(database.getResource().getDataSources(),
                database.getRuleMetaData().getConfigurations()), new ConfigurationProperties(metaDataContexts.getMetaData().getProps().getProps()));
        database.getRuleMetaData().getRules().clear();
        database.getRuleMetaData().getRules().addAll(databaseRules);
    }
    
    private void deleteTable(final String databaseName, final String schemaName, final String deletedTable) {
        if (null != metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().get(schemaName)) {
            metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().get(schemaName).remove(deletedTable);
            FederationDatabaseMetaData databaseMetaData = metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(databaseName);
            databaseMetaData.removeTableMetadata(schemaName, deletedTable);
            metaDataContexts.getOptimizerContext().getPlannerContexts().put(databaseName, OptimizerPlannerContextFactory.create(databaseMetaData));
        }
    }
    
    private boolean containsInImmutableDataNodeContainedRule(final String tableName, final ShardingSphereDatabase schemaMetaData) {
        return schemaMetaData.getRuleMetaData().findRules(DataNodeContainedRule.class).stream()
                .filter(each -> !(each instanceof MutableDataNodeRule)).anyMatch(each -> each.getAllTables().contains(tableName));
    }
    
    /**
     * Delete database.
     *
     * @param databaseName database name
     */
    public void deleteDatabase(final String databaseName) {
        if (metaDataContexts.getMetaData().getDatabases().containsKey(databaseName)) {
            metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().remove(databaseName);
            metaDataContexts.getOptimizerContext().getParserContexts().remove(databaseName);
            metaDataContexts.getOptimizerContext().getPlannerContexts().remove(databaseName);
            ShardingSphereDatabase removeMetaData = metaDataContexts.getMetaData().getDatabases().remove(databaseName);
            closeDataSources(removeMetaData);
            removeAndCloseResource(databaseName, removeMetaData);
            metaDataContexts.getPersistService().ifPresent(optional -> optional.getSchemaMetaDataService().deleteDatabase(databaseName));
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
        if (null == database || null == database.getSchemas().get(schemaName)) {
            return;
        }
        FederationDatabaseMetaData federationDatabaseMetaData = metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(databaseName);
        federationDatabaseMetaData.removeSchemaMetadata(schemaName);
        database.getSchemas().remove(schemaName);
    }
    
    /**
     * Add resource.
     *
     * @param databaseName database name
     * @param dataSourcePropsMap data source properties map
     * @throws SQLException SQL exception
     */
    public void addResource(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap) throws SQLException {
        refreshMetaDataContext(databaseName, dataSourcePropsMap);
        metaDataContexts.getPersistService().ifPresent(optional -> optional.getDataSourceService().append(databaseName, dataSourcePropsMap));
    }
    
    /**
     * Alter resource.
     *
     * @param databaseName database name
     * @param dataSourcePropsMap data source properties map
     * @throws SQLException SQL exception
     */
    public void alterResource(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap) throws SQLException {
        refreshMetaDataContext(databaseName, dataSourcePropsMap);
        metaDataContexts.getPersistService().ifPresent(optional -> optional.getDataSourceService().append(databaseName, dataSourcePropsMap));
    }
    
    /**
     * Drop resource.
     *
     * @param databaseName database name
     * @param toBeDroppedResourceNames to be dropped resource names
     */
    public void dropResource(final String databaseName, final Collection<String> toBeDroppedResourceNames) {
        toBeDroppedResourceNames.forEach(metaDataContexts.getMetaData().getDatabases().get(databaseName).getResource().getDataSources()::remove);
        metaDataContexts.getPersistService().ifPresent(optional -> optional.getDataSourceService().drop(databaseName, toBeDroppedResourceNames));
    }
    
    /**
     * Alter rule configuration.
     *
     * @param databaseName database name
     * @param ruleConfigs rule configurations
     */
    public void alterRuleConfiguration(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) {
        try {
            MetaDataContexts changedMetaDataContexts = buildChangedMetaDataContext(metaDataContexts.getMetaData().getDatabases().get(databaseName), ruleConfigs);
            metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().putAll(changedMetaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases());
            metaDataContexts.getMetaData().getDatabases().get(databaseName).getRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResources);
            metaDataContexts.getMetaData().getDatabases().putAll(changedMetaDataContexts.getMetaData().getDatabases());
            setInstanceContext();
        } catch (final SQLException ex) {
            log.error("Alter database:{} rule configuration failed", databaseName, ex);
        }
    }
    
    /**
     * Alter data source configuration.
     *
     * @param databaseName database name
     * @param dataSourcePropsMap altered data source properties map
     */
    public void alterDataSourceConfiguration(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        try {
            MetaDataContexts changedMetaDataContext = buildChangedMetaDataContextWithChangedDataSource(metaDataContexts.getMetaData().getDatabases().get(databaseName), dataSourcePropsMap);
            refreshMetaDataContext(databaseName, changedMetaDataContext, dataSourcePropsMap);
            setInstanceContext();
        } catch (final SQLException ex) {
            log.error("Alter database:{} data source configuration failed", databaseName, ex);
        }
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
            setInstanceContext();
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
                new ShardingSphereRuleMetaData(GlobalRulesBuilder.buildRules(ruleConfigs, metaDataContexts.getMetaData().getDatabases())));
        metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResources);
        renewMetaDataContexts(newMetaDataContexts);
        setInstanceContext();
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
            refreshRules(databaseName, database);
            GenericSchemaBuilderMaterials materials = new GenericSchemaBuilderMaterials(database.getProtocolType(), database.getResource().getDatabaseType(),
                    Collections.singletonMap(dataSourceName, database.getResource().getDataSources().get(dataSourceName)),
                    database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
            loadTableMetaData(databaseName, schemaName, materials);
        } catch (final SQLException ex) {
            log.error("Reload meta data of database:{} schema:{} with data source:{} failed", databaseName, schemaName, dataSourceName, ex);
        }
    }
    
    private void deleteSchemas(final String databaseName, final Map<String, ShardingSphereSchema> actualSchemas) {
        Map<String, ShardingSphereSchema> originalSchemas = metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas();
        if (originalSchemas.isEmpty()) {
            return;
        }
        originalSchemas.forEach((key, value) -> {
            if (null == actualSchemas.get(key)) {
                metaDataContexts.getPersistService().ifPresent(optional -> optional.getSchemaMetaDataService().deleteSchema(databaseName, key));
            }
        });
    }
    
    private void loadTableMetaData(final String databaseName, final String schemaName, final GenericSchemaBuilderMaterials materials) throws SQLException {
        Map<String, ShardingSphereSchema> schemaMap = GenericSchemaBuilder.build(materials);
        if (schemaMap.containsKey(schemaName)) {
            metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().put(schemaName, schemaMap.get(schemaName));
            metaDataContexts.getPersistService().ifPresent(optional -> optional.getSchemaMetaDataService()
                    .persistMetaData(databaseName, schemaName, metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().get(schemaName)));
        }
    }
    
    private void loadTableMetaData(final String databaseName, final String schemaName, final String tableName, final GenericSchemaBuilderMaterials materials) throws SQLException {
        ShardingSphereSchema schema = GenericSchemaBuilder.build(Collections.singletonList(tableName), materials).getOrDefault(schemaName, new ShardingSphereSchema());
        if (schema.containsTable(tableName)) {
            metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().get(schemaName).put(tableName, schema.get(tableName));
            metaDataContexts.getPersistService().ifPresent(optional -> optional.getSchemaMetaDataService()
                    .persistMetaData(databaseName, schemaName, metaDataContexts.getMetaData().getDatabases().get(databaseName).getSchemas().get(schemaName)));
        }
    }
    
    private Map<String, ShardingSphereSchema> loadActualSchema(final String databaseName) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabases().get(databaseName);
        Map<String, DataSource> dataSourceMap = database.getResource().getDataSources();
        refreshRules(databaseName, database);
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
    
    private Map<String, DataSource> getDeletedDataSources(final ShardingSphereDatabase originalMetaData, final Map<String, DataSourceProperties> newDataSourcePropsMap) {
        return originalMetaData.getResource().getDataSources().entrySet().stream().filter(entry -> !newDataSourcePropsMap.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    private Map<String, DataSource> getChangedDataSources(final ShardingSphereDatabase originalMetaData, final Map<String, DataSourceProperties> newDataSourcePropsMap) {
        Collection<String> changedDataSourceNames = getChangedDataSourceConfiguration(originalMetaData, newDataSourcePropsMap).keySet();
        return originalMetaData.getResource().getDataSources().entrySet().stream().filter(entry -> changedDataSourceNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    private Map<String, DataSourceProperties> getChangedDataSourceConfiguration(final ShardingSphereDatabase originalMetaData,
                                                                                final Map<String, DataSourceProperties> dataSourcePropsMap) {
        return dataSourcePropsMap.entrySet().stream()
                .filter(entry -> isModifiedDataSource(originalMetaData.getResource().getDataSources(), entry.getKey(), entry.getValue()))
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
    
    private void refreshMetaDataContext(final String databaseName, final Map<String, DataSourceProperties> dataSourceProps) throws SQLException {
        MetaDataContexts changedMetaDataContext = buildChangedMetaDataContextWithAddedDataSource(metaDataContexts.getMetaData().getDatabases().get(databaseName), dataSourceProps);
        metaDataContexts.getMetaData().getDatabases().putAll(changedMetaDataContext.getMetaData().getDatabases());
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().putAll(changedMetaDataContext.getOptimizerContext().getFederationMetaData().getDatabases());
        metaDataContexts.getOptimizerContext().getParserContexts().putAll(changedMetaDataContext.getOptimizerContext().getParserContexts());
        metaDataContexts.getOptimizerContext().getPlannerContexts().putAll(changedMetaDataContext.getOptimizerContext().getPlannerContexts());
        metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(each -> each.addResource(metaDataContexts.getMetaData().getDatabases().get(databaseName)));
    }
    
    private void refreshMetaDataContext(final String databaseName, final MetaDataContexts changedMetaDataContext, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().putAll(changedMetaDataContext.getOptimizerContext().getFederationMetaData().getDatabases());
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(metaDataContexts.getMetaData().getDatabases());
        databases.putAll(changedMetaDataContext.getMetaData().getDatabases());
        final Collection<DataSource> pendingClosedDataSources = getPendingClosedDataSources(databaseName, dataSourcePropsMap);
        MetaDataContexts newMetaDataContexts = rebuildMetaDataContexts(
                new ShardingSphereMetaData(databases, metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps()));
        metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResources);
        metaDataContexts.getMetaData().getDatabases().get(databaseName).getRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResources);
        renewMetaDataContexts(newMetaDataContexts);
        closeDataSources(databaseName, pendingClosedDataSources);
    }
    
    private MetaDataContexts buildChangedMetaDataContextWithAddedDataSource(final ShardingSphereDatabase originalDatabase,
                                                                            final Map<String, DataSourceProperties> addedDataSourceProps) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(originalDatabase.getResource().getDataSources());
        dataSourceMap.putAll(DataSourcePoolCreator.create(addedDataSourceProps));
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(dataSourceMap, originalDatabase.getRuleMetaData().getConfigurations());
        MetaDataContextsBuilder builder = new MetaDataContextsBuilder(Collections.singletonMap(originalDatabase.getName(), databaseConfig),
                metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), metaDataContexts.getMetaData().getProps());
        MetaDataContexts result = builder.build(metaDataContexts.getPersistService().orElse(null));
        persistMetaData(result);
        return result;
    }
    
    private MetaDataContexts buildChangedMetaDataContext(final ShardingSphereDatabase originalDatabase, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        MetaDataContextsBuilder builder = new MetaDataContextsBuilder(
                Collections.singletonMap(originalDatabase.getName(), new DataSourceProvidedDatabaseConfiguration(originalDatabase.getResource().getDataSources(), ruleConfigs)),
                metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), metaDataContexts.getMetaData().getProps());
        MetaDataContexts result = builder.build(metaDataContexts.getPersistService().orElse(null));
        persistMetaData(result);
        return result;
    }
    
    private MetaDataContexts buildChangedMetaDataContextWithChangedDataSource(final ShardingSphereDatabase originalDatabase,
                                                                              final Map<String, DataSourceProperties> newDataSourceProps) throws SQLException {
        Collection<String> deletedDataSources = getDeletedDataSources(originalDatabase, newDataSourceProps).keySet();
        Map<String, DataSource> changedDataSources = buildChangedDataSources(originalDatabase, newDataSourceProps);
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(
                getNewDataSources(originalDatabase.getResource().getDataSources(), getAddedDataSources(originalDatabase, newDataSourceProps), changedDataSources, deletedDataSources),
                originalDatabase.getRuleMetaData().getConfigurations());
        MetaDataContextsBuilder builder = new MetaDataContextsBuilder(Collections.singletonMap(originalDatabase.getName(), databaseConfig),
                metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), metaDataContexts.getMetaData().getProps());
        MetaDataContexts result = builder.build(metaDataContexts.getPersistService().orElse(null));
        persistMetaData(result);
        return result;
    }
    
    private MetaDataContexts buildChangedMetaDataContextWithChangedDataSourceAndRule(final ShardingSphereDatabase originalDatabase, final Map<String, DataSourceProperties> newDataSourceProps,
                                                                                     final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Collection<String> deletedDataSources = getDeletedDataSources(originalDatabase, newDataSourceProps).keySet();
        Map<String, DataSource> changedDataSources = buildChangedDataSources(originalDatabase, newDataSourceProps);
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(getNewDataSources(originalDatabase.getResource().getDataSources(),
                getAddedDataSources(originalDatabase, newDataSourceProps), changedDataSources, deletedDataSources), ruleConfigs);
        MetaDataContextsBuilder builder = new MetaDataContextsBuilder(Collections.singletonMap(originalDatabase.getName(), databaseConfig),
                metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), metaDataContexts.getMetaData().getProps());
        MetaDataContexts result = builder.build(metaDataContexts.getPersistService().orElse(null));
        persistMetaData(result);
        return result;
    }
    
    private Map<String, DataSource> getNewDataSources(final Map<String, DataSource> originalDataSources,
                                                      final Map<String, DataSource> addedDataSources, final Map<String, DataSource> changedDataSources, final Collection<String> deletedDataSources) {
        Map<String, DataSource> result = new LinkedHashMap<>(originalDataSources);
        result.keySet().removeAll(deletedDataSources);
        result.putAll(changedDataSources);
        result.putAll(addedDataSources);
        return result;
    }
    
    private Map<String, DataSource> getAddedDataSources(final ShardingSphereDatabase originalDatabase, final Map<String, DataSourceProperties> newDataSourcePropsMap) {
        return DataSourcePoolCreator.create(newDataSourcePropsMap.entrySet().stream()
                .filter(entry -> !originalDatabase.getResource().getDataSources().containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
    }
    
    private Map<String, DataSource> buildChangedDataSources(final ShardingSphereDatabase originalDatabase, final Map<String, DataSourceProperties> newDataSourcePropsMap) {
        return DataSourcePoolCreator.create(getChangedDataSourceConfiguration(originalDatabase, newDataSourcePropsMap));
    }
    
    private void closeDataSources(final ShardingSphereDatabase removeMetaData) {
        if (null != removeMetaData.getResource()) {
            removeMetaData.getResource().getDataSources().values().forEach(each -> removeMetaData.getResource().close(each));
        }
    }
    
    private void closeDataSources(final String databaseName, final Collection<DataSource> dataSources) {
        ShardingSphereResource resource = metaDataContexts.getMetaData().getDatabases().get(databaseName).getResource();
        dataSources.forEach(resource::close);
    }
    
    private void removeAndCloseResource(final String databaseName, final ShardingSphereDatabase removeMetaData) {
        metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules().stream()
                .filter(each -> each instanceof ResourceHeldRule).map(each -> (ResourceHeldRule<?>) each).forEach(each -> each.closeStaleResource(databaseName));
        removeMetaData.getRuleMetaData().getRules().stream()
                .filter(each -> each instanceof ResourceHeldRule).map(each -> (ResourceHeldRule<?>) each).forEach(each -> each.closeStaleResource(databaseName));
    }
    
    private void setInstanceContext() {
        metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(InstanceAwareRule.class).forEach(each -> each.setInstanceContext(instanceContext));
        metaDataContexts.getMetaData().getDatabases().forEach((key, value) -> value.getRuleMetaData().findRules(InstanceAwareRule.class).forEach(each -> each.setInstanceContext(instanceContext)));
    }
    
    @Override
    public void close() throws Exception {
        executorEngine.close();
        metaDataContexts.close();
    }
}
