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
import org.apache.shardingsphere.infra.metadata.database.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.SystemSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.rule.builder.schema.DatabaseRulesBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.rule.builder.DefaultTransactionRuleConfigurationBuilder;
import org.apache.shardingsphere.transaction.spi.TransactionConfigurationFileGenerator;
import org.apache.shardingsphere.transaction.spi.TransactionConfigurationFileGeneratorFactory;

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
    
    private volatile TransactionContexts transactionContexts;
    
    private final InstanceContext instanceContext;
    
    private final ExecutorEngine executorEngine;
    
    public ContextManager(final MetaDataContexts metaDataContexts, final TransactionContexts transactionContexts, final InstanceContext instanceContext) {
        this.metaDataContexts = metaDataContexts;
        this.transactionContexts = transactionContexts;
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
     * Renew transaction contexts.
     *
     * @param transactionContexts transaction contexts
     */
    public synchronized void renewTransactionContexts(final TransactionContexts transactionContexts) {
        this.transactionContexts = transactionContexts;
    }
    
    /**
     * Get data source map.
     *
     * @param databaseName database name
     * @return data source map
     */
    public Map<String, DataSource> getDataSourceMap(final String databaseName) {
        return metaDataContexts.getDatabase(databaseName).getResource().getDataSources();
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
        metaDataContexts.getMetaData().getDatabases().put(databaseName, newMetaDataContexts.getDatabase(databaseName));
        persistMetaData(metaDataContexts);
        renewAllTransactionContext();
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
        if (null != metaDataContexts.getDatabase(databaseName).getSchemas().get(schemaName)) {
            return;
        }
        FederationDatabaseMetaData federationDatabaseMetaData = metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(databaseName);
        federationDatabaseMetaData.putTableMetadata(schemaName, new TableMetaData());
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
        ShardingSphereDatabase alteredMetaData = new ShardingSphereDatabase(databaseName, metaDataContexts.getDatabase(databaseName).getProtocolType(),
                metaDataContexts.getDatabase(databaseName).getResource(), metaDataContexts.getDatabase(databaseName).getRuleMetaData(), schemas);
        Map<String, ShardingSphereDatabase> alteredDatabaseMap = new HashMap<>(metaDataContexts.getMetaData().getDatabases());
        alteredDatabaseMap.put(databaseName, alteredMetaData);
        FederationDatabaseMetaData alteredDatabaseMetaData = new FederationDatabaseMetaData(databaseName, schemas);
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().put(databaseName, alteredDatabaseMetaData);
        metaDataContexts.getOptimizerContext().getPlannerContexts().put(databaseName, OptimizerPlannerContextFactory.create(alteredDatabaseMetaData));
        renewMetaDataContexts(
                rebuildMetaDataContexts(new ShardingSphereMetaData(alteredDatabaseMap, metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps())));
    }
    
    /**
     * Alter schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param changedTableMetaData changed table meta data
     * @param deletedTable deleted table
     */
    public void alterSchema(final String databaseName, final String schemaName, final TableMetaData changedTableMetaData, final String deletedTable) {
        if (null != metaDataContexts.getDatabase(databaseName)) {
            Optional.ofNullable(changedTableMetaData).ifPresent(optional -> alterTableSchema(databaseName, schemaName, optional));
            Optional.ofNullable(deletedTable).ifPresent(optional -> deleteTable(databaseName, schemaName, optional));
        }
    }
    
    private void persistMetaData(final MetaDataContexts metaDataContexts) {
        metaDataContexts.getMetaData().getDatabases().forEach((databaseName, schemas) -> schemas.getSchemas()
                .forEach((schemaName, tables) -> metaDataContexts.getPersistService().ifPresent(optional -> optional.getSchemaMetaDataService().persistMetaData(databaseName, schemaName, tables))));
    }
    
    private void alterTableSchema(final String databaseName, final String schemaName, final TableMetaData changedTableMetaData) {
        ShardingSphereDatabase database = metaDataContexts.getDatabase(databaseName);
        alterSingleTableDataNodes(databaseName, database, changedTableMetaData);
        FederationDatabaseMetaData federationDatabaseMetaData = metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(databaseName);
        database.getSchemas().get(schemaName).put(changedTableMetaData.getName(), changedTableMetaData);
        federationDatabaseMetaData.putTableMetadata(schemaName, changedTableMetaData);
        metaDataContexts.getOptimizerContext().getPlannerContexts().put(databaseName, OptimizerPlannerContextFactory.create(federationDatabaseMetaData));
    }
    
    private void alterSingleTableDataNodes(final String databaseName, final ShardingSphereDatabase database, final TableMetaData changedTableMetaData) {
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
        if (null != metaDataContexts.getDatabase(databaseName).getSchemas().get(schemaName)) {
            metaDataContexts.getDatabase(databaseName).getSchemas().get(schemaName).remove(deletedTable);
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
            removeAndCloseTransactionEngine(databaseName);
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
        ShardingSphereDatabase database = metaDataContexts.getDatabase(databaseName);
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
        toBeDroppedResourceNames.forEach(metaDataContexts.getDatabase(databaseName).getResource().getDataSources()::remove);
        metaDataContexts.getPersistService().ifPresent(optional -> optional.getDataSourceService().drop(databaseName, toBeDroppedResourceNames));
    }
    
    /**
     * Alter rule configuration.
     *
     * @param databaseName database name
     * @param ruleConfigs collection of rule configurations
     */
    public void alterRuleConfiguration(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) {
        try {
            MetaDataContexts changedMetaDataContexts = buildChangedMetaDataContext(metaDataContexts.getMetaData().getDatabases().get(databaseName), ruleConfigs);
            metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().putAll(changedMetaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases());
            renewMetaDataContexts(rebuildMetaDataContexts(changedMetaDataContexts.getMetaData()));
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
        if (!ruleConfigs.isEmpty()) {
            boolean needRenewTransaction = isNeedRenewTransactionContext(ruleConfigs);
            ShardingSphereRuleMetaData newGlobalRuleMetaData = new ShardingSphereRuleMetaData(
                    ruleConfigs, GlobalRulesBuilder.buildRules(ruleConfigs, metaDataContexts.getMetaData().getDatabases()));
            renewMetaDataContexts(rebuildMetaDataContexts(newGlobalRuleMetaData));
            if (needRenewTransaction) {
                renewAllTransactionContext();
            }
        }
    }
    
    private boolean isNeedRenewTransactionContext(final Collection<RuleConfiguration> ruleConfigs) {
        Optional<RuleConfiguration> newConfig = ruleConfigs.stream().filter(each -> each instanceof TransactionRuleConfiguration).findFirst();
        Optional<TransactionRuleConfiguration> oldConfig = metaDataContexts.getMetaData().getGlobalRuleMetaData().findSingleRuleConfiguration(TransactionRuleConfiguration.class);
        return newConfig.isPresent() && oldConfig.isPresent() && !newConfig.get().equals(oldConfig.get());
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
            GenericSchemaBuilderMaterials materials = new GenericSchemaBuilderMaterials(metaDataContexts.getDatabase(databaseName).getProtocolType(),
                    metaDataContexts.getDatabase(databaseName).getResource().getDatabaseType(), metaDataContexts.getDatabase(databaseName).getResource().getDataSources(),
                    metaDataContexts.getDatabase(databaseName).getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
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
     * @param tableName logic table name
     * @param dataSourceName data source name
     */
    public void reloadMetaData(final String databaseName, final String schemaName, final String tableName, final String dataSourceName) {
        try {
            GenericSchemaBuilderMaterials materials = new GenericSchemaBuilderMaterials(metaDataContexts.getDatabase(databaseName).getProtocolType(),
                    metaDataContexts.getDatabase(databaseName).getResource().getDatabaseType(), Collections.singletonMap(dataSourceName,
                            metaDataContexts.getDatabase(databaseName).getResource().getDataSources().get(dataSourceName)),
                    metaDataContexts.getDatabase(databaseName).getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
            loadTableMetaData(databaseName, schemaName, tableName, materials);
        } catch (final SQLException ex) {
            log.error("Reload table:{} meta data of database:{} schema:{} with data source:{} failed", tableName, databaseName, schemaName, dataSourceName, ex);
        }
    }
    
    private void deleteSchemas(final String databaseName, final Map<String, ShardingSphereSchema> actualSchemas) {
        Map<String, ShardingSphereSchema> originalSchemas = metaDataContexts.getDatabase(databaseName).getSchemas();
        if (originalSchemas.isEmpty()) {
            return;
        }
        originalSchemas.forEach((key, value) -> {
            if (null == actualSchemas.get(key)) {
                metaDataContexts.getPersistService().ifPresent(optional -> optional.getSchemaMetaDataService().deleteSchema(databaseName, key));
            }
        });
    }
    
    private void loadTableMetaData(final String databaseName, final String schemaName, final String tableName, final GenericSchemaBuilderMaterials materials) throws SQLException {
        ShardingSphereSchema schema = GenericSchemaBuilder.build(Collections.singletonList(tableName), materials).getOrDefault(schemaName, new ShardingSphereSchema());
        if (schema.getTables().containsKey(tableName)) {
            metaDataContexts.getDatabase(databaseName).getSchemas().get(schemaName).put(tableName, schema.getTables().get(tableName));
            metaDataContexts.getPersistService().ifPresent(optional -> optional.getSchemaMetaDataService()
                    .persistMetaData(databaseName, schemaName, metaDataContexts.getDatabase(databaseName).getSchemas().get(schemaName)));
        }
    }
    
    private Map<String, ShardingSphereSchema> loadActualSchema(final String databaseName) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.getDatabase(databaseName);
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
        result.addAll(getDeletedDataSources(metaDataContexts.getDatabase(databaseName), dataSourcePropsMap).values());
        result.addAll(getChangedDataSources(metaDataContexts.getDatabase(databaseName), dataSourcePropsMap).values());
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
        renewTransactionContext(databaseName, metaDataContexts.getDatabase(databaseName).getResource());
    }
    
    private void refreshMetaDataContext(final String databaseName, final MetaDataContexts changedMetaDataContext, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().putAll(changedMetaDataContext.getOptimizerContext().getFederationMetaData().getDatabases());
        Map<String, ShardingSphereDatabase> databaseMap = new HashMap<>(metaDataContexts.getMetaData().getDatabases());
        databaseMap.putAll(changedMetaDataContext.getMetaData().getDatabases());
        Collection<DataSource> pendingClosedDataSources = getPendingClosedDataSources(databaseName, dataSourcePropsMap);
        renewMetaDataContexts(rebuildMetaDataContexts(new ShardingSphereMetaData(databaseMap, metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps())));
        renewTransactionContext(databaseName, metaDataContexts.getDatabase(databaseName).getResource());
        closeDataSources(databaseName, pendingClosedDataSources);
    }
    
    private MetaDataContexts buildChangedMetaDataContextWithAddedDataSource(final ShardingSphereDatabase originalDatabase,
                                                                            final Map<String, DataSourceProperties> addedDataSourceProps) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(originalDatabase.getResource().getDataSources());
        dataSourceMap.putAll(DataSourcePoolCreator.create(addedDataSourceProps));
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(dataSourceMap, originalDatabase.getRuleMetaData().getConfigurations());
        Optional<MetaDataPersistService> metaDataPersistService = metaDataContexts.getPersistService();
        metaDataPersistService.ifPresent(optional -> persistTransactionConfiguration(databaseConfig, optional));
        MetaDataContextsBuilder builder = new MetaDataContextsBuilder(Collections.singletonMap(originalDatabase.getName(), databaseConfig),
                metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), metaDataContexts.getMetaData().getProps());
        MetaDataContexts result = builder.build(metaDataContexts.getPersistService().orElse(null));
        persistMetaData(result);
        return result;
    }
    
    private void persistTransactionConfiguration(final DatabaseConfiguration databaseConfig, final MetaDataPersistService metaDataPersistService) {
        Optional<TransactionConfigurationFileGenerator> fileGenerator = TransactionConfigurationFileGeneratorFactory.findInstance(getTransactionRule().getProviderType());
        fileGenerator.ifPresent(optional -> metaDataPersistService.persistTransactionRule(
                optional.getTransactionProps(getTransactionRule().getProps(), databaseConfig, instanceContext.getModeConfiguration().getType()), true));
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
    
    /**
     * Reload all transaction context.
     */
    public void renewAllTransactionContext() {
        for (Entry<String, ShardingSphereDatabase> entry : metaDataContexts.getMetaData().getDatabases().entrySet()) {
            renewTransactionContext(entry.getKey(), entry.getValue().getResource());
        }
    }
    
    private void renewTransactionContext(final String databaseName, final ShardingSphereResource resource) {
        ShardingSphereTransactionManagerEngine changedStaleEngine = transactionContexts.getEngines().get(databaseName);
        if (null != changedStaleEngine) {
            closeTransactionEngine(changedStaleEngine);
        }
        transactionContexts.getEngines().put(databaseName, createNewEngine(resource.getDatabaseType(), resource.getDataSources()));
    }
    
    private ShardingSphereTransactionManagerEngine createNewEngine(final DatabaseType databaseType, final Map<String, DataSource> dataSources) {
        Optional<TransactionConfigurationFileGenerator> fileGenerator = TransactionConfigurationFileGeneratorFactory.findInstance(getTransactionRule().getProviderType());
        fileGenerator.ifPresent(optional -> optional.generateFile(getTransactionRule().getProps(), instanceContext));
        ShardingSphereTransactionManagerEngine result = new ShardingSphereTransactionManagerEngine();
        result.init(databaseType, dataSources, getTransactionRule());
        return result;
    }
    
    private TransactionRule getTransactionRule() {
        Optional<TransactionRule> transactionRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().findSingleRule(TransactionRule.class);
        return transactionRule.orElseGet(() -> new TransactionRule(new DefaultTransactionRuleConfigurationBuilder().build()));
    }
    
    private void closeDataSources(final ShardingSphereDatabase removeMetaData) {
        if (null != removeMetaData.getResource()) {
            removeMetaData.getResource().getDataSources().values().forEach(each -> removeMetaData.getResource().close(each));
        }
    }
    
    private void closeDataSources(final String databaseName, final Collection<DataSource> dataSources) {
        ShardingSphereResource resource = metaDataContexts.getDatabase(databaseName).getResource();
        dataSources.forEach(resource::close);
    }
    
    private void removeAndCloseTransactionEngine(final String databaseName) {
        ShardingSphereTransactionManagerEngine staleEngine = transactionContexts.getEngines().remove(databaseName);
        closeTransactionEngine(staleEngine);
    }
    
    private void closeTransactionEngine(final ShardingSphereTransactionManagerEngine staleEngine) {
        if (null != staleEngine) {
            try {
                staleEngine.close();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("Close transaction engine failed", ex);
            }
        }
    }
    
    @Override
    public void close() throws Exception {
        executorEngine.close();
        metaDataContexts.close();
    }
}
