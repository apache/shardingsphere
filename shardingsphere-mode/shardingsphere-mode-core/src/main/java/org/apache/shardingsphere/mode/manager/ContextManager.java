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

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.schema.SchemaConfiguration;
import org.apache.shardingsphere.infra.config.schema.impl.DataSourceProvidedSchemaConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContextFactory;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.mode.lock.LockContext;
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
import java.util.stream.Collectors;

/**
 * Context manager.
 */
@Getter
@Slf4j
public final class ContextManager implements AutoCloseable {
    
    private volatile MetaDataContexts metaDataContexts = new MetaDataContexts(null);
    
    private volatile TransactionContexts transactionContexts = new TransactionContexts();
    
    private volatile InstanceContext instanceContext;
    
    private volatile LockContext lockContext;
    
    /**
     * Initialize context manager.
     *
     * @param metaDataContexts meta data contexts
     * @param transactionContexts transaction contexts
     * @param instanceContext instance context
     * @param lockContext lock context
     */
    public void init(final MetaDataContexts metaDataContexts, final TransactionContexts transactionContexts, final InstanceContext instanceContext, final LockContext lockContext) {
        this.metaDataContexts = metaDataContexts;
        this.transactionContexts = transactionContexts;
        this.instanceContext = instanceContext;
        this.lockContext = lockContext;
    }
    
    /**
     * Get data source map.
     * 
     * @param schemaName schema name
     * @return data source map
     */
    public Map<String, DataSource> getDataSourceMap(final String schemaName) {
        return metaDataContexts.getMetaData(schemaName).getResource().getDataSources();
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
     * Add schema.
     * 
     * @param schemaName schema name
     * @throws SQLException SQL exception
     */
    public void addSchema(final String schemaName) throws SQLException {
        if (metaDataContexts.getMetaDataMap().containsKey(schemaName)) {
            return;
        }
        MetaDataContexts newMetaDataContexts = buildNewMetaDataContext(schemaName);
        FederationDatabaseMetaData databaseMetaData = newMetaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(schemaName);
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().put(schemaName, databaseMetaData);
        metaDataContexts.getOptimizerContext().getPlannerContexts().put(schemaName, OptimizerPlannerContextFactory.create(databaseMetaData));
        metaDataContexts.getMetaDataMap().put(schemaName, newMetaDataContexts.getMetaData(schemaName));
        metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getSchemaMetaDataService().persist(schemaName));
        renewAllTransactionContext();
    }
    
    /**
     * Alter schema.
     *
     * @param schemaName schema name
     * @param schemas schemas
     */
    public void alterSchema(final String schemaName, final Map<String, ShardingSphereSchema> schemas) {
        ShardingSphereMetaData alteredMetaData = new ShardingSphereMetaData(
                schemaName, metaDataContexts.getMetaData(schemaName).getResource(), metaDataContexts.getMetaData(schemaName).getRuleMetaData(), schemas);
        Map<String, ShardingSphereMetaData> alteredMetaDataMap = new HashMap<>(metaDataContexts.getMetaDataMap());
        alteredMetaDataMap.put(schemaName, alteredMetaData);
        FederationDatabaseMetaData alteredDatabaseMetaData = new FederationDatabaseMetaData(schemaName, schemas);
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().put(schemaName, alteredDatabaseMetaData);
        metaDataContexts.getOptimizerContext().getPlannerContexts().put(schemaName, OptimizerPlannerContextFactory.create(alteredDatabaseMetaData));
        renewMetaDataContexts(rebuildMetaDataContexts(alteredMetaDataMap));
    }
    
    /**
     * Alter schema.
     *
     * @param schemaName schema name
     * @param changedTableMetaData changed table meta data
     * @param deletedTable deleted table
     */
    public void alterSchema(final String schemaName, final TableMetaData changedTableMetaData, final String deletedTable) {
        Optional.ofNullable(changedTableMetaData).ifPresent(optional -> alterTableSchema(schemaName, optional));
        Optional.ofNullable(deletedTable).ifPresent(optional -> deleteTableSchema(schemaName, optional));
    }
    
    private void alterTableSchema(final String schemaName, final TableMetaData changedTableMetaData) {
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData(schemaName);
        alterSingleTableDataNodes(schemaName, metaData, changedTableMetaData);
        FederationDatabaseMetaData databaseMetaData = metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(schemaName);
        metaData.getSchema().put(changedTableMetaData.getName(), changedTableMetaData);
        databaseMetaData.put(changedTableMetaData);
        metaDataContexts.getOptimizerContext().getPlannerContexts().put(schemaName, OptimizerPlannerContextFactory.create(databaseMetaData));
    }
    
    private void alterSingleTableDataNodes(final String schemaName, final ShardingSphereMetaData metaData, final TableMetaData changedTableMetaData) {
        if (!containsInDataNodeContainedRule(changedTableMetaData.getName(), metaData)) {
            refreshRules(schemaName, metaData);
        }
    }
    
    private void refreshRules(final String schemaName, final ShardingSphereMetaData metaData) {
        Collection<ShardingSphereRule> rules = SchemaRulesBuilder.buildRules(schemaName, new DataSourceProvidedSchemaConfiguration(metaData.getResource().getDataSources(),
                metaData.getRuleMetaData().getConfigurations()), new ConfigurationProperties(metaDataContexts.getProps().getProps()));
        metaData.getRuleMetaData().getRules().clear();
        metaData.getRuleMetaData().getRules().addAll(rules);
    }
    
    private void deleteTableSchema(final String schemaName, final String deletedTable) {
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData(schemaName);
        FederationDatabaseMetaData databaseMetaData = metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(schemaName);
        metaData.getSchema().remove(deletedTable);
        databaseMetaData.remove(deletedTable);
        metaDataContexts.getOptimizerContext().getPlannerContexts().put(schemaName, OptimizerPlannerContextFactory.create(databaseMetaData));
    }
    
    private boolean containsInDataNodeContainedRule(final String tableName, final ShardingSphereMetaData schemaMetaData) {
        return schemaMetaData.getRuleMetaData().findRules(DataNodeContainedRule.class).stream().anyMatch(each -> each.getAllTables().contains(tableName));
    }
    
    /**
     * Delete schema.
     * 
     * @param schemaName schema name
     */
    public void deleteSchema(final String schemaName) {
        if (metaDataContexts.getMetaDataMap().containsKey(schemaName)) {
            metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().remove(schemaName);
            metaDataContexts.getOptimizerContext().getParserContexts().remove(schemaName);
            metaDataContexts.getOptimizerContext().getPlannerContexts().remove(schemaName);
            ShardingSphereMetaData removeMetaData = metaDataContexts.getMetaDataMap().remove(schemaName);
            closeDataSources(removeMetaData);
            removeAndCloseTransactionEngine(schemaName);
            metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getSchemaMetaDataService().delete(schemaName));
        }
    }
    
    /**
     * Add resource.
     *
     * @param schemaName schema name
     * @param dataSourcePropsMap data source properties map
     * @throws SQLException SQL exception
     */
    public void addResource(final String schemaName, final Map<String, DataSourceProperties> dataSourcePropsMap) throws SQLException {
        refreshMetaDataContext(schemaName, dataSourcePropsMap);
        metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getDataSourceService().append(schemaName, dataSourcePropsMap));
    }
    
    /**
     * Alter resource.
     *
     * @param schemaName schema name
     * @param dataSourcePropsMap data source properties map
     * @throws SQLException SQL exception
     */
    public void alterResource(final String schemaName, final Map<String, DataSourceProperties> dataSourcePropsMap) throws SQLException {
        refreshMetaDataContext(schemaName, dataSourcePropsMap);
        metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getDataSourceService().append(schemaName, dataSourcePropsMap));
    }
    
    /**
     * Drop resource.
     *
     * @param schemaName schema name
     * @param toBeDroppedResourceNames to be dropped resource names
     */
    public void dropResource(final String schemaName, final Collection<String> toBeDroppedResourceNames) {
        toBeDroppedResourceNames.forEach(metaDataContexts.getMetaData(schemaName).getResource().getDataSources()::remove);
        metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getDataSourceService().drop(schemaName, toBeDroppedResourceNames));
    }
    
    /**
     * Alter rule configuration.
     * 
     * @param schemaName schema name
     * @param ruleConfigs collection of rule configurations
     */
    public void alterRuleConfiguration(final String schemaName, final Collection<RuleConfiguration> ruleConfigs) {
        try {
            MetaDataContexts changedMetaDataContexts = buildChangedMetaDataContext(metaDataContexts.getMetaDataMap().get(schemaName), ruleConfigs);
            metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().putAll(changedMetaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases());
            Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(metaDataContexts.getMetaDataMap());
            metaDataMap.putAll(changedMetaDataContexts.getMetaDataMap());
            renewMetaDataContexts(rebuildMetaDataContexts(metaDataMap));
        } catch (final SQLException ex) {
            log.error("Alter schema:{} rule configuration failed", schemaName, ex);
        }
    }
    
    /**
     * Alter data source configuration.
     * 
     * @param schemaName schema name
     * @param dataSourcePropsMap altered data source properties map
     */
    public void alterDataSourceConfiguration(final String schemaName, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        try {
            MetaDataContexts changedMetaDataContext = buildChangedMetaDataContextWithChangedDataSource(metaDataContexts.getMetaDataMap().get(schemaName), dataSourcePropsMap);
            refreshMetaDataContext(schemaName, changedMetaDataContext, dataSourcePropsMap);
        } catch (final SQLException ex) {
            log.error("Alter schema:{} data source configuration failed", schemaName, ex);
        }
    }
    
    /**
     * Alter data source and rule configuration.
     * 
     * @param schemaName schema name
     * @param dataSourcePropsMap data source props map
     * @param ruleConfigs rule configurations
     */
    public void alterDataSourceAndRuleConfiguration(final String schemaName, final Map<String, DataSourceProperties> dataSourcePropsMap, final Collection<RuleConfiguration> ruleConfigs) {
        try {
            MetaDataContexts changedMetaDataContext = buildChangedMetaDataContextWithChangedDataSourceAndRule(metaDataContexts.getMetaDataMap().get(schemaName), dataSourcePropsMap, ruleConfigs);
            refreshMetaDataContext(schemaName, changedMetaDataContext, dataSourcePropsMap);
        } catch (SQLException ex) {
            log.error("Alter schema:{} data source and rule configuration failed", schemaName, ex);
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
            ShardingSphereRuleMetaData newGlobalRuleMetaData = new ShardingSphereRuleMetaData(ruleConfigs, GlobalRulesBuilder.buildRules(ruleConfigs, metaDataContexts.getMetaDataMap()));
            renewMetaDataContexts(rebuildMetaDataContexts(newGlobalRuleMetaData));
            if (needRenewTransaction) {
                renewAllTransactionContext();
            }
        }
    }
    
    private boolean isNeedRenewTransactionContext(final Collection<RuleConfiguration> ruleConfigs) {
        Optional<RuleConfiguration> newConfig = ruleConfigs.stream().filter(each -> each instanceof TransactionRuleConfiguration).findFirst();
        Optional<TransactionRuleConfiguration> oldConfig = metaDataContexts.getGlobalRuleMetaData().findSingleRuleConfiguration(TransactionRuleConfiguration.class);
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
     * @param schemaName schema name to be reload
     */
    public void reloadMetaData(final String schemaName) {
        try {
            ShardingSphereSchema schema = loadActualSchema(schemaName);
            alterSchema(schemaName, Collections.singletonMap(schemaName, schema));
            metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getSchemaMetaDataService().persist(schemaName, schemaName, schema));
        } catch (final SQLException ex) {
            log.error("Reload schema:{} meta data failed", schemaName, ex);
        }
    }
    
    /**
     * Reload table meta data.
     *
     * @param schemaName schema name
     * @param tableName logic table name
     */
    public void reloadMetaData(final String schemaName, final String tableName) {
        try {
            SchemaBuilderMaterials materials = new SchemaBuilderMaterials(
                    metaDataContexts.getMetaData(schemaName).getResource().getDatabaseType(), metaDataContexts.getMetaData(schemaName).getResource().getDataSources(),
                    metaDataContexts.getMetaData(schemaName).getRuleMetaData().getRules(), metaDataContexts.getProps());
            loadTableMetaData(schemaName, tableName, materials);
        } catch (final SQLException ex) {
            log.error("Reload table:{} meta data of schema:{} failed", tableName, schemaName, ex);
        }
    }
    
    /**
     * Reload single data source table meta data.
     *
     * @param schemaName schema name
     * @param tableName logic table name
     * @param dataSourceName data source name
     */
    public void reloadMetaData(final String schemaName, final String tableName, final String dataSourceName) {
        try {
            SchemaBuilderMaterials materials = new SchemaBuilderMaterials(
                    metaDataContexts.getMetaData(schemaName).getResource().getDatabaseType(), Collections.singletonMap(dataSourceName, 
                    metaDataContexts.getMetaData(schemaName).getResource().getDataSources().get(dataSourceName)),
                    metaDataContexts.getMetaData(schemaName).getRuleMetaData().getRules(), metaDataContexts.getProps());
            loadTableMetaData(schemaName, tableName, materials);
        } catch (final SQLException ex) {
            log.error("Reload table:{} meta data of schema:{} with data source:{} failed", tableName, schemaName, dataSourceName, ex);
        }
    }
    
    private void loadTableMetaData(final String schemaName, final String tableName, final SchemaBuilderMaterials materials) throws SQLException {
        TableMetaData tableMetaData = TableMetaDataBuilder.load(Collections.singletonList(tableName), materials).getOrDefault(tableName, new TableMetaData());
        if (!tableMetaData.getColumns().isEmpty()) {
            metaDataContexts.getMetaData(schemaName).getSchema().put(tableName, tableMetaData);
            metaDataContexts.getMetaDataPersistService().ifPresent(optional ->
                    optional.getSchemaMetaDataService().persist(schemaName, schemaName, metaDataContexts.getMetaData(schemaName).getSchema()));
        }
    }
    
    private ShardingSphereSchema loadActualSchema(final String schemaName) throws SQLException {
        Map<String, DataSource> dataSourceMap = metaDataContexts.getMetaData(schemaName).getResource().getDataSources();
        Collection<ShardingSphereRule> rules = metaDataContexts.getMetaDataMap().get(schemaName).getRuleMetaData().getRules();
        return SchemaLoader.load(dataSourceMap, rules, metaDataContexts.getProps().getProps());
    }
    
    private Collection<DataSource> getPendingClosedDataSources(final String schemaName, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        Collection<DataSource> result = new LinkedList<>();
        result.addAll(getDeletedDataSources(metaDataContexts.getMetaData(schemaName), dataSourcePropsMap).values());
        result.addAll(getChangedDataSources(metaDataContexts.getMetaData(schemaName), dataSourcePropsMap).values());
        return result;
    }
    
    private Map<String, DataSource> getDeletedDataSources(final ShardingSphereMetaData originalMetaData, final Map<String, DataSourceProperties> newDataSourcePropsMap) {
        return originalMetaData.getResource().getDataSources().entrySet().stream().filter(entry -> !newDataSourcePropsMap.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    private Map<String, DataSource> getChangedDataSources(final ShardingSphereMetaData originalMetaData, final Map<String, DataSourceProperties> newDataSourcePropsMap) {
        Collection<String> changedDataSourceNames = getChangedDataSourceConfiguration(originalMetaData, newDataSourcePropsMap).keySet();
        return originalMetaData.getResource().getDataSources().entrySet().stream().filter(entry -> changedDataSourceNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    private Map<String, DataSourceProperties> getChangedDataSourceConfiguration(final ShardingSphereMetaData originalMetaData,
                                                                                final Map<String, DataSourceProperties> dataSourcePropsMap) {
        return dataSourcePropsMap.entrySet().stream()
                .filter(entry -> isModifiedDataSource(originalMetaData.getResource().getDataSources(), entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private boolean isModifiedDataSource(final Map<String, DataSource> originalDataSources, final String dataSourceName, final DataSourceProperties dataSourceProps) {
        return originalDataSources.containsKey(dataSourceName) && !dataSourceProps.equals(DataSourcePropertiesCreator.create(originalDataSources.get(dataSourceName)));
    }
    
    private MetaDataContexts rebuildMetaDataContexts(final Map<String, ShardingSphereMetaData> schemaMetaData) {
        return new MetaDataContexts(metaDataContexts.getMetaDataPersistService().orElse(null),
                schemaMetaData, metaDataContexts.getGlobalRuleMetaData(), metaDataContexts.getExecutorEngine(),
                metaDataContexts.getOptimizerContext(), metaDataContexts.getProps());
    }
    
    private MetaDataContexts rebuildMetaDataContexts(final ShardingSphereRuleMetaData globalRuleMetaData) {
        return new MetaDataContexts(metaDataContexts.getMetaDataPersistService().orElse(null),
                metaDataContexts.getMetaDataMap(), globalRuleMetaData, metaDataContexts.getExecutorEngine(), metaDataContexts.getOptimizerContext(), metaDataContexts.getProps());
    }
    
    private MetaDataContexts rebuildMetaDataContexts(final ConfigurationProperties props) {
        return new MetaDataContexts(metaDataContexts.getMetaDataPersistService().orElse(null),
                metaDataContexts.getMetaDataMap(), metaDataContexts.getGlobalRuleMetaData(), metaDataContexts.getExecutorEngine(), metaDataContexts.getOptimizerContext(), props);
    }
    
    private void refreshMetaDataContext(final String schemaName, final Map<String, DataSourceProperties> dataSourceProps) throws SQLException {
        MetaDataContexts changedMetaDataContext = buildChangedMetaDataContextWithAddedDataSource(metaDataContexts.getMetaDataMap().get(schemaName), dataSourceProps);
        metaDataContexts.getMetaDataMap().putAll(changedMetaDataContext.getMetaDataMap());
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().putAll(changedMetaDataContext.getOptimizerContext().getFederationMetaData().getDatabases());
        metaDataContexts.getOptimizerContext().getParserContexts().putAll(changedMetaDataContext.getOptimizerContext().getParserContexts());
        metaDataContexts.getOptimizerContext().getPlannerContexts().putAll(changedMetaDataContext.getOptimizerContext().getPlannerContexts());
        renewTransactionContext(schemaName, metaDataContexts.getMetaData(schemaName).getResource());
    }
    
    private void refreshMetaDataContext(final String schemaName, final MetaDataContexts changedMetaDataContext, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().putAll(changedMetaDataContext.getOptimizerContext().getFederationMetaData().getDatabases());
        Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(metaDataContexts.getMetaDataMap());
        metaDataMap.putAll(changedMetaDataContext.getMetaDataMap());
        Collection<DataSource> pendingClosedDataSources = getPendingClosedDataSources(schemaName, dataSourcePropsMap);
        renewMetaDataContexts(rebuildMetaDataContexts(metaDataMap));
        renewTransactionContext(schemaName, metaDataContexts.getMetaData(schemaName).getResource());
        closeDataSources(schemaName, pendingClosedDataSources);
    }
    
    private MetaDataContexts buildChangedMetaDataContextWithAddedDataSource(final ShardingSphereMetaData originalMetaData, 
                                                                            final Map<String, DataSourceProperties> addedDataSourceProps) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(originalMetaData.getResource().getDataSources());
        dataSourceMap.putAll(DataSourcePoolCreator.create(addedDataSourceProps));
        Properties props = metaDataContexts.getProps().getProps();
        SchemaConfiguration schemaConfiguration = new DataSourceProvidedSchemaConfiguration(dataSourceMap, originalMetaData.getRuleMetaData().getConfigurations());
        Optional<MetaDataPersistService> metaDataPersistService = metaDataContexts.getMetaDataPersistService();
        metaDataPersistService.ifPresent(optional -> persistTransactionConfiguration(schemaConfiguration, optional));
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(metaDataContexts.getGlobalRuleMetaData().getConfigurations(), props);
        metaDataContextsBuilder.addSchema(originalMetaData.getName(), originalMetaData.getResource().getDatabaseType(), schemaConfiguration, props);
        metaDataContexts.getMetaDataPersistService().ifPresent(
            optional -> optional.getSchemaMetaDataService().persist(originalMetaData.getName(), originalMetaData.getName(), metaDataContextsBuilder.getSchemaMap(originalMetaData.getName())));
        return metaDataContextsBuilder.build(metaDataContexts.getMetaDataPersistService().orElse(null));
    }
    
    private void persistTransactionConfiguration(final SchemaConfiguration schemaConfiguration, final MetaDataPersistService metaDataPersistService) {
        Optional<TransactionConfigurationFileGenerator> fileGenerator = TransactionConfigurationFileGeneratorFactory.newInstance(getTransactionRule().getProviderType());
        if (fileGenerator.isPresent()) {
            Properties transactionProps = fileGenerator.get().getTransactionProps(getTransactionRule().getProps(), schemaConfiguration, instanceContext.getModeConfiguration().getType());
            metaDataPersistService.persistTransactionRule(transactionProps, true);
        }
    }
    
    private MetaDataContexts buildChangedMetaDataContext(final ShardingSphereMetaData originalMetaData, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Properties props = metaDataContexts.getProps().getProps();
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(metaDataContexts.getGlobalRuleMetaData().getConfigurations(), props);
        metaDataContextsBuilder.addSchema(originalMetaData.getName(), originalMetaData.getResource().getDatabaseType(), 
                new DataSourceProvidedSchemaConfiguration(originalMetaData.getResource().getDataSources(), ruleConfigs), props);
        metaDataContexts.getMetaDataPersistService().ifPresent(
            optional -> optional.getSchemaMetaDataService().persist(originalMetaData.getName(), originalMetaData.getName(), metaDataContextsBuilder.getSchemaMap(originalMetaData.getName())));
        return metaDataContextsBuilder.build(metaDataContexts.getMetaDataPersistService().orElse(null));
    }
    
    private MetaDataContexts buildChangedMetaDataContextWithChangedDataSource(final ShardingSphereMetaData originalMetaData, 
                                                                              final Map<String, DataSourceProperties> newDataSourceProps) throws SQLException {
        Collection<String> deletedDataSources = getDeletedDataSources(originalMetaData, newDataSourceProps).keySet();
        Map<String, DataSource> changedDataSources = buildChangedDataSources(originalMetaData, newDataSourceProps);
        Properties props = metaDataContexts.getProps().getProps();
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(metaDataContexts.getGlobalRuleMetaData().getConfigurations(), props);
        metaDataContextsBuilder.addSchema(originalMetaData.getName(), originalMetaData.getResource().getDatabaseType(), new DataSourceProvidedSchemaConfiguration(
                getNewDataSources(originalMetaData.getResource().getDataSources(), getAddedDataSources(originalMetaData, newDataSourceProps), changedDataSources, deletedDataSources),
                originalMetaData.getRuleMetaData().getConfigurations()), props);
        metaDataContexts.getMetaDataPersistService().ifPresent(
            optional -> optional.getSchemaMetaDataService().persist(originalMetaData.getName(), originalMetaData.getName(), metaDataContextsBuilder.getSchemaMap(originalMetaData.getName())));
        return metaDataContextsBuilder.build(metaDataContexts.getMetaDataPersistService().orElse(null));
    }
    
    private MetaDataContexts buildChangedMetaDataContextWithChangedDataSourceAndRule(final ShardingSphereMetaData originalMetaData, final Map<String, DataSourceProperties> newDataSourceProps, 
                                                                                     final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Collection<String> deletedDataSources = getDeletedDataSources(originalMetaData, newDataSourceProps).keySet();
        Map<String, DataSource> changedDataSources = buildChangedDataSources(originalMetaData, newDataSourceProps);
        Properties props = metaDataContexts.getProps().getProps();
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(metaDataContexts.getGlobalRuleMetaData().getConfigurations(), props);
        metaDataContextsBuilder.addSchema(originalMetaData.getName(), originalMetaData.getResource().getDatabaseType(), 
                new DataSourceProvidedSchemaConfiguration(getNewDataSources(originalMetaData.getResource().getDataSources(), 
                getAddedDataSources(originalMetaData, newDataSourceProps), changedDataSources, deletedDataSources), ruleConfigs), props);
        metaDataContexts.getMetaDataPersistService().ifPresent(
            optional -> optional.getSchemaMetaDataService().persist(originalMetaData.getName(), originalMetaData.getName(), metaDataContextsBuilder.getSchemaMap(originalMetaData.getName())));
        return metaDataContextsBuilder.build(metaDataContexts.getMetaDataPersistService().orElse(null));
    }
    
    private Map<String, DataSource> getNewDataSources(final Map<String, DataSource> originalDataSources,
                                                      final Map<String, DataSource> addedDataSources, final Map<String, DataSource> changedDataSources, final Collection<String> deletedDataSources) {
        Map<String, DataSource> result = new LinkedHashMap<>(originalDataSources);
        result.keySet().removeAll(deletedDataSources);
        result.putAll(changedDataSources);
        result.putAll(addedDataSources);
        return result;
    }
    
    private Map<String, DataSource> getAddedDataSources(final ShardingSphereMetaData originalMetaData, final Map<String, DataSourceProperties> newDataSourcePropsMap) {
        return DataSourcePoolCreator.create(Maps.filterKeys(newDataSourcePropsMap, each -> !originalMetaData.getResource().getDataSources().containsKey(each)));
    }
    
    private Map<String, DataSource> buildChangedDataSources(final ShardingSphereMetaData originalMetaData, final Map<String, DataSourceProperties> newDataSourcePropsMap) {
        return DataSourcePoolCreator.create(getChangedDataSourceConfiguration(originalMetaData, newDataSourcePropsMap));
    }
    
    /**
     * Reload all transaction context.
     */
    public void renewAllTransactionContext() {
        for (Entry<String, ShardingSphereMetaData> entry : metaDataContexts.getMetaDataMap().entrySet()) {
            renewTransactionContext(entry.getKey(), entry.getValue().getResource());
        }
    }
    
    private void renewTransactionContext(final String schemaName, final ShardingSphereResource resource) {
        ShardingSphereTransactionManagerEngine changedStaleEngine = transactionContexts.getEngines().get(schemaName);
        if (null != changedStaleEngine) {
            closeTransactionEngine(changedStaleEngine);
        }
        transactionContexts.getEngines().put(schemaName, createNewEngine(resource.getDatabaseType(), resource.getDataSources()));
    }
    
    private ShardingSphereTransactionManagerEngine createNewEngine(final DatabaseType databaseType, final Map<String, DataSource> dataSources) {
        Optional<TransactionConfigurationFileGenerator> fileGenerator = TransactionConfigurationFileGeneratorFactory.newInstance(getTransactionRule().getProviderType());
        fileGenerator.ifPresent(optional -> optional.generateFile(getTransactionRule().getProps(), instanceContext));
        ShardingSphereTransactionManagerEngine result = new ShardingSphereTransactionManagerEngine();
        result.init(databaseType, dataSources, getTransactionRule());
        return result;
    }
    
    private TransactionRule getTransactionRule() {
        Optional<TransactionRule> transactionRule = metaDataContexts.getGlobalRuleMetaData().getRules().stream()
                .filter(each -> each instanceof TransactionRule).map(each -> (TransactionRule) each).findFirst();
        return transactionRule.orElseGet(() -> new TransactionRule(new DefaultTransactionRuleConfigurationBuilder().build()));
    }
    
    private MetaDataContexts buildNewMetaDataContext(final String schemaName) throws SQLException {
        Properties props = metaDataContexts.getProps().getProps();
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(metaDataContexts.getGlobalRuleMetaData().getConfigurations(), props);
        metaDataContextsBuilder.addSchema(schemaName, DatabaseTypeRegistry.getDefaultDatabaseType(), new DataSourceProvidedSchemaConfiguration(new HashMap<>(), new LinkedList<>()), props);
        return metaDataContextsBuilder.build(metaDataContexts.getMetaDataPersistService().orElse(null));
    }
    
    private void closeDataSources(final ShardingSphereMetaData removeMetaData) {
        if (null != removeMetaData.getResource()) {
            removeMetaData.getResource().getDataSources().values().forEach(each -> removeMetaData.getResource().close(each));
        }
    }
    
    private void closeDataSources(final String schemaName, final Collection<DataSource> dataSources) {
        ShardingSphereResource resource = metaDataContexts.getMetaData(schemaName).getResource();
        dataSources.forEach(resource::close);
    }
    
    private void removeAndCloseTransactionEngine(final String schemaName) {
        ShardingSphereTransactionManagerEngine staleEngine = transactionContexts.getEngines().remove(schemaName);
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
        metaDataContexts.close();
    }
}
