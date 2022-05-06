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

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRecognizer;
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
import org.apache.shardingsphere.infra.metadata.schema.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
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
    
    /**
     * Initialize context manager.
     *
     * @param metaDataContexts meta data contexts
     * @param transactionContexts transaction contexts
     * @param instanceContext instance context
     */
    public void init(final MetaDataContexts metaDataContexts, final TransactionContexts transactionContexts, final InstanceContext instanceContext) {
        this.metaDataContexts = metaDataContexts;
        this.transactionContexts = transactionContexts;
        this.instanceContext = instanceContext;
    }
    
    /**
     * Get data source map.
     *
     * @param databaseName database name
     * @return data source map
     */
    public Map<String, DataSource> getDataSourceMap(final String databaseName) {
        return metaDataContexts.getMetaData(databaseName).getResource().getDataSources();
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
     * Add database.
     *
     * @param databaseName database name
     * @throws SQLException SQL exception
     */
    public void addDatabase(final String databaseName) throws SQLException {
        if (metaDataContexts.getMetaDataMap().containsKey(databaseName)) {
            return;
        }
        MetaDataContexts newMetaDataContexts = buildNewMetaDataContext(databaseName);
        FederationDatabaseMetaData databaseMetaData = newMetaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(databaseName);
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().put(databaseName, databaseMetaData);
        metaDataContexts.getOptimizerContext().getPlannerContexts().put(databaseName, OptimizerPlannerContextFactory.create(databaseMetaData));
        metaDataContexts.getMetaDataMap().put(databaseName, newMetaDataContexts.getMetaData(databaseName));
        metaDataContexts.getMetaDataPersistService().ifPresent(this::persistMetaData);
        renewAllTransactionContext();
    }
    
    /**
     * Add schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public void addSchema(final String databaseName, final String schemaName) {
        if (null != metaDataContexts.getMetaData(databaseName).getSchemaByName(schemaName)) {
            return;
        }
        FederationDatabaseMetaData databaseMetaData = metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(databaseName);
        databaseMetaData.putTableMetadata(schemaName, new TableMetaData());
        metaDataContexts.getOptimizerContext().getPlannerContexts().put(databaseName, OptimizerPlannerContextFactory.create(databaseMetaData));
        metaDataContexts.getMetaDataMap().get(databaseName).getSchemas().put(schemaName, new ShardingSphereSchema());
    }
    
    /**
     * Alter database.
     *
     * @param databaseName database name
     * @param schemas schemas
     */
    public void alterDatabase(final String databaseName, final Map<String, ShardingSphereSchema> schemas) {
        ShardingSphereMetaData alteredMetaData = new ShardingSphereMetaData(
                databaseName, metaDataContexts.getMetaData(databaseName).getResource(), metaDataContexts.getMetaData(databaseName).getRuleMetaData(), schemas);
        Map<String, ShardingSphereMetaData> alteredMetaDataMap = new HashMap<>(metaDataContexts.getMetaDataMap());
        alteredMetaDataMap.put(databaseName, alteredMetaData);
        FederationDatabaseMetaData alteredDatabaseMetaData = new FederationDatabaseMetaData(databaseName, schemas);
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().put(databaseName, alteredDatabaseMetaData);
        metaDataContexts.getOptimizerContext().getPlannerContexts().put(databaseName, OptimizerPlannerContextFactory.create(alteredDatabaseMetaData));
        renewMetaDataContexts(rebuildMetaDataContexts(alteredMetaDataMap));
    }
    
    /**
     * Alter database.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param changedTableMetaData changed table meta data
     * @param deletedTable deleted table
     */
    public void alterDatabase(final String databaseName, final String schemaName, final TableMetaData changedTableMetaData, final String deletedTable) {
        if (null != metaDataContexts.getMetaData(databaseName)) {
            Optional.ofNullable(changedTableMetaData).ifPresent(optional -> alterTableSchema(databaseName, schemaName, optional));
            Optional.ofNullable(deletedTable).ifPresent(optional -> deleteTable(databaseName, schemaName, optional));
        }
    }
    
    private void persistMetaData(final MetaDataPersistService metaDataPersistService) {
        metaDataContexts.getMetaDataMap().forEach((databaseName, schemas) -> schemas.getSchemas().forEach((schemaName, tables) -> {
            if (tables.getTables().isEmpty()) {
                metaDataPersistService.getSchemaMetaDataService().persistSchema(databaseName, schemaName);
            } else {
                metaDataPersistService.getSchemaMetaDataService().persistTables(databaseName, schemaName, tables);
            }
        }));
    }
    
    private void alterTableSchema(final String databaseName, final String schemaName, final TableMetaData changedTableMetaData) {
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData(databaseName);
        alterSingleTableDataNodes(databaseName, metaData, changedTableMetaData);
        FederationDatabaseMetaData databaseMetaData = metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(databaseName);
        metaData.getSchemaByName(schemaName).put(changedTableMetaData.getName(), changedTableMetaData);
        databaseMetaData.putTableMetadata(schemaName, changedTableMetaData);
        metaDataContexts.getOptimizerContext().getPlannerContexts().put(databaseName, OptimizerPlannerContextFactory.create(databaseMetaData));
    }
    
    private void alterSingleTableDataNodes(final String databaseName, final ShardingSphereMetaData metaData, final TableMetaData changedTableMetaData) {
        if (!containsInImmutableDataNodeContainedRule(changedTableMetaData.getName(), metaData)) {
            refreshRules(databaseName, metaData);
        }
    }
    
    private void refreshRules(final String databaseName, final ShardingSphereMetaData metaData) {
        Collection<ShardingSphereRule> rules = SchemaRulesBuilder.buildRules(databaseName, new DataSourceProvidedDatabaseConfiguration(metaData.getResource().getDataSources(),
                metaData.getRuleMetaData().getConfigurations()), new ConfigurationProperties(metaDataContexts.getProps().getProps()));
        metaData.getRuleMetaData().getRules().clear();
        metaData.getRuleMetaData().getRules().addAll(rules);
    }
    
    private void deleteTable(final String databaseName, final String schemaName, final String deletedTable) {
        if (null != metaDataContexts.getMetaData(databaseName).getSchemaByName(schemaName)) {
            metaDataContexts.getMetaData(databaseName).getSchemaByName(schemaName).remove(deletedTable);
            FederationDatabaseMetaData databaseMetaData = metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(databaseName);
            databaseMetaData.removeTableMetadata(schemaName, deletedTable);
            metaDataContexts.getOptimizerContext().getPlannerContexts().put(databaseName, OptimizerPlannerContextFactory.create(databaseMetaData));
        }
    }
    
    private boolean containsInImmutableDataNodeContainedRule(final String tableName, final ShardingSphereMetaData schemaMetaData) {
        return schemaMetaData.getRuleMetaData().findRules(DataNodeContainedRule.class).stream()
                .filter(each -> !(each instanceof MutableDataNodeRule)).anyMatch(each -> each.getAllTables().contains(tableName));
    }
    
    /**
     * Delete database.
     *
     * @param databaseName database name
     */
    public void deleteDatabase(final String databaseName) {
        if (metaDataContexts.getMetaDataMap().containsKey(databaseName)) {
            metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().remove(databaseName);
            metaDataContexts.getOptimizerContext().getParserContexts().remove(databaseName);
            metaDataContexts.getOptimizerContext().getPlannerContexts().remove(databaseName);
            ShardingSphereMetaData removeMetaData = metaDataContexts.getMetaDataMap().remove(databaseName);
            closeDataSources(removeMetaData);
            removeAndCloseTransactionEngine(databaseName);
            metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getSchemaMetaDataService().deleteDatabase(databaseName));
        }
    }
    
    /**
     * Drop schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public void dropSchema(final String databaseName, final String schemaName) {
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData(databaseName);
        if (null == metaData || null == metaData.getSchemaByName(schemaName)) {
            return;
        }
        FederationDatabaseMetaData databaseMetaData = metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().get(databaseName);
        databaseMetaData.removeSchemaMetadata(schemaName);
        metaData.getSchemas().remove(schemaName);
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
        metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getDataSourceService().append(databaseName, dataSourcePropsMap));
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
        metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getDataSourceService().append(databaseName, dataSourcePropsMap));
    }
    
    /**
     * Drop resource.
     *
     * @param databaseName database name
     * @param toBeDroppedResourceNames to be dropped resource names
     */
    public void dropResource(final String databaseName, final Collection<String> toBeDroppedResourceNames) {
        toBeDroppedResourceNames.forEach(metaDataContexts.getMetaData(databaseName).getResource().getDataSources()::remove);
        metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getDataSourceService().drop(databaseName, toBeDroppedResourceNames));
    }
    
    /**
     * Alter rule configuration.
     *
     * @param databaseName database name
     * @param ruleConfigs collection of rule configurations
     */
    public void alterRuleConfiguration(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) {
        try {
            MetaDataContexts changedMetaDataContexts = buildChangedMetaDataContext(metaDataContexts.getMetaDataMap().get(databaseName), ruleConfigs);
            metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().putAll(changedMetaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases());
            Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(metaDataContexts.getMetaDataMap());
            metaDataMap.putAll(changedMetaDataContexts.getMetaDataMap());
            renewMetaDataContexts(rebuildMetaDataContexts(metaDataMap));
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
            MetaDataContexts changedMetaDataContext = buildChangedMetaDataContextWithChangedDataSource(metaDataContexts.getMetaDataMap().get(databaseName), dataSourcePropsMap);
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
            MetaDataContexts changedMetaDataContext = buildChangedMetaDataContextWithChangedDataSourceAndRule(metaDataContexts.getMetaDataMap().get(databaseName), dataSourcePropsMap, ruleConfigs);
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
     * @param databaseName database name to be reload
     * @param schemaName schema name to be reload
     */
    public void reloadMetaData(final String databaseName, final String schemaName) {
        try {
            Map<String, ShardingSphereSchema> schemas = loadActualSchema(databaseName, schemaName);
            alterDatabase(databaseName, schemas);
            for (ShardingSphereSchema each : schemas.values()) {
                metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getSchemaMetaDataService().persistTables(databaseName, schemaName, each));
            }
        } catch (final SQLException ex) {
            log.error("Reload database:{} meta data failed", databaseName, ex);
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
            SchemaBuilderMaterials materials = new SchemaBuilderMaterials(
                    metaDataContexts.getMetaData(databaseName).getResource().getDatabaseType(), metaDataContexts.getMetaData(databaseName).getResource().getDataSources(),
                    metaDataContexts.getMetaData(databaseName).getRuleMetaData().getRules(), metaDataContexts.getProps(), schemaName);
            loadTableMetaData(databaseName, schemaName, tableName, materials);
        } catch (final SQLException ex) {
            log.error("Reload table:{} meta data of database:{} failed", tableName, databaseName, ex);
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
            SchemaBuilderMaterials materials = new SchemaBuilderMaterials(
                    metaDataContexts.getMetaData(databaseName).getResource().getDatabaseType(), Collections.singletonMap(dataSourceName,
                            metaDataContexts.getMetaData(databaseName).getResource().getDataSources().get(dataSourceName)),
                    metaDataContexts.getMetaData(databaseName).getRuleMetaData().getRules(), metaDataContexts.getProps(), schemaName);
            loadTableMetaData(databaseName, schemaName, tableName, materials);
        } catch (final SQLException ex) {
            log.error("Reload table:{} meta data of database:{} with data source:{} failed", tableName, databaseName, dataSourceName, ex);
        }
    }
    
    private void loadTableMetaData(final String databaseName, final String schemaName, final String tableName, final SchemaBuilderMaterials materials) throws SQLException {
        SchemaMetaData schemaMetaData = TableMetaDataBuilder.load(Collections.singletonList(tableName), materials).getOrDefault(schemaName, new SchemaMetaData("", Collections.emptyMap()));
        if (schemaMetaData.getTables().containsKey(tableName)) {
            metaDataContexts.getMetaData(databaseName).getSchemaByName(schemaName).put(tableName, schemaMetaData.getTables().get(tableName));
            metaDataContexts.getMetaDataPersistService()
                    .ifPresent(optional -> optional.getSchemaMetaDataService().persistTables(databaseName, schemaName, metaDataContexts.getMetaData(databaseName).getSchemaByName(schemaName)));
        }
    }
    
    private Map<String, ShardingSphereSchema> loadActualSchema(final String databaseName, final String schemaName) throws SQLException {
        Map<String, DataSource> dataSourceMap = metaDataContexts.getMetaData(databaseName).getResource().getDataSources();
        Collection<ShardingSphereRule> rules = metaDataContexts.getMetaDataMap().get(databaseName).getRuleMetaData().getRules();
        DatabaseType databaseType = DatabaseTypeRecognizer.getDatabaseType(dataSourceMap.values());
        return SchemaLoader.load(schemaName, databaseType, dataSourceMap, rules, metaDataContexts.getProps().getProps());
    }
    
    private Collection<DataSource> getPendingClosedDataSources(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        Collection<DataSource> result = new LinkedList<>();
        result.addAll(getDeletedDataSources(metaDataContexts.getMetaData(databaseName), dataSourcePropsMap).values());
        result.addAll(getChangedDataSources(metaDataContexts.getMetaData(databaseName), dataSourcePropsMap).values());
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
    
    private void refreshMetaDataContext(final String databaseName, final Map<String, DataSourceProperties> dataSourceProps) throws SQLException {
        MetaDataContexts changedMetaDataContext = buildChangedMetaDataContextWithAddedDataSource(metaDataContexts.getMetaDataMap().get(databaseName), dataSourceProps);
        metaDataContexts.getMetaDataMap().putAll(changedMetaDataContext.getMetaDataMap());
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().putAll(changedMetaDataContext.getOptimizerContext().getFederationMetaData().getDatabases());
        metaDataContexts.getOptimizerContext().getParserContexts().putAll(changedMetaDataContext.getOptimizerContext().getParserContexts());
        metaDataContexts.getOptimizerContext().getPlannerContexts().putAll(changedMetaDataContext.getOptimizerContext().getPlannerContexts());
        renewTransactionContext(databaseName, metaDataContexts.getMetaData(databaseName).getResource());
    }
    
    private void refreshMetaDataContext(final String databaseName, final MetaDataContexts changedMetaDataContext, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases().putAll(changedMetaDataContext.getOptimizerContext().getFederationMetaData().getDatabases());
        Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(metaDataContexts.getMetaDataMap());
        metaDataMap.putAll(changedMetaDataContext.getMetaDataMap());
        Collection<DataSource> pendingClosedDataSources = getPendingClosedDataSources(databaseName, dataSourcePropsMap);
        renewMetaDataContexts(rebuildMetaDataContexts(metaDataMap));
        renewTransactionContext(databaseName, metaDataContexts.getMetaData(databaseName).getResource());
        closeDataSources(databaseName, pendingClosedDataSources);
    }
    
    private MetaDataContexts buildChangedMetaDataContextWithAddedDataSource(final ShardingSphereMetaData originalMetaData,
                                                                            final Map<String, DataSourceProperties> addedDataSourceProps) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(originalMetaData.getResource().getDataSources());
        dataSourceMap.putAll(DataSourcePoolCreator.create(addedDataSourceProps));
        Properties props = metaDataContexts.getProps().getProps();
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(dataSourceMap, originalMetaData.getRuleMetaData().getConfigurations());
        Optional<MetaDataPersistService> metaDataPersistService = metaDataContexts.getMetaDataPersistService();
        metaDataPersistService.ifPresent(optional -> persistTransactionConfiguration(databaseConfig, optional));
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(metaDataContexts.getGlobalRuleMetaData().getConfigurations(), props);
        metaDataContextsBuilder.addDatabase(originalMetaData.getDatabaseName(), originalMetaData.getResource().getDatabaseType(), databaseConfig, props);
        metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getSchemaMetaDataService()
                .persistTables(originalMetaData.getDatabaseName(), originalMetaData.getDatabaseName(), metaDataContextsBuilder.getSchemaMap(originalMetaData.getDatabaseName())));
        return metaDataContextsBuilder.build(metaDataContexts.getMetaDataPersistService().orElse(null));
    }
    
    private void persistTransactionConfiguration(final DatabaseConfiguration databaseConfig, final MetaDataPersistService metaDataPersistService) {
        Optional<TransactionConfigurationFileGenerator> fileGenerator = TransactionConfigurationFileGeneratorFactory.newInstance(getTransactionRule().getProviderType());
        if (fileGenerator.isPresent()) {
            Properties transactionProps = fileGenerator.get().getTransactionProps(getTransactionRule().getProps(), databaseConfig, instanceContext.getModeConfiguration().getType());
            metaDataPersistService.persistTransactionRule(transactionProps, true);
        }
    }
    
    private MetaDataContexts buildChangedMetaDataContext(final ShardingSphereMetaData originalMetaData, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Properties props = metaDataContexts.getProps().getProps();
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(metaDataContexts.getGlobalRuleMetaData().getConfigurations(), props);
        metaDataContextsBuilder.addDatabase(originalMetaData.getDatabaseName(), originalMetaData.getResource().getDatabaseType(),
                new DataSourceProvidedDatabaseConfiguration(originalMetaData.getResource().getDataSources(), ruleConfigs), props);
        metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getSchemaMetaDataService()
                .persistTables(originalMetaData.getDatabaseName(), originalMetaData.getDatabaseName(), metaDataContextsBuilder.getSchemaMap(originalMetaData.getDatabaseName())));
        return metaDataContextsBuilder.build(metaDataContexts.getMetaDataPersistService().orElse(null));
    }
    
    private MetaDataContexts buildChangedMetaDataContextWithChangedDataSource(final ShardingSphereMetaData originalMetaData,
                                                                              final Map<String, DataSourceProperties> newDataSourceProps) throws SQLException {
        Collection<String> deletedDataSources = getDeletedDataSources(originalMetaData, newDataSourceProps).keySet();
        Map<String, DataSource> changedDataSources = buildChangedDataSources(originalMetaData, newDataSourceProps);
        Properties props = metaDataContexts.getProps().getProps();
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(metaDataContexts.getGlobalRuleMetaData().getConfigurations(), props);
        metaDataContextsBuilder.addDatabase(originalMetaData.getDatabaseName(), originalMetaData.getResource().getDatabaseType(), new DataSourceProvidedDatabaseConfiguration(
                getNewDataSources(originalMetaData.getResource().getDataSources(), getAddedDataSources(originalMetaData, newDataSourceProps), changedDataSources, deletedDataSources),
                originalMetaData.getRuleMetaData().getConfigurations()), props);
        metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getSchemaMetaDataService()
                .persistTables(originalMetaData.getDatabaseName(), originalMetaData.getDatabaseName(), metaDataContextsBuilder.getSchemaMap(originalMetaData.getDatabaseName())));
        return metaDataContextsBuilder.build(metaDataContexts.getMetaDataPersistService().orElse(null));
    }
    
    private MetaDataContexts buildChangedMetaDataContextWithChangedDataSourceAndRule(final ShardingSphereMetaData originalMetaData, final Map<String, DataSourceProperties> newDataSourceProps,
                                                                                     final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Collection<String> deletedDataSources = getDeletedDataSources(originalMetaData, newDataSourceProps).keySet();
        Map<String, DataSource> changedDataSources = buildChangedDataSources(originalMetaData, newDataSourceProps);
        Properties props = metaDataContexts.getProps().getProps();
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(metaDataContexts.getGlobalRuleMetaData().getConfigurations(), props);
        metaDataContextsBuilder.addDatabase(originalMetaData.getDatabaseName(), originalMetaData.getResource().getDatabaseType(),
                new DataSourceProvidedDatabaseConfiguration(getNewDataSources(originalMetaData.getResource().getDataSources(),
                        getAddedDataSources(originalMetaData, newDataSourceProps), changedDataSources, deletedDataSources), ruleConfigs),
                props);
        metaDataContexts.getMetaDataPersistService().ifPresent(optional -> optional.getSchemaMetaDataService()
                .persistTables(originalMetaData.getDatabaseName(), originalMetaData.getDatabaseName(), metaDataContextsBuilder.getSchemaMap(originalMetaData.getDatabaseName())));
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
    
    private void renewTransactionContext(final String databaseName, final ShardingSphereResource resource) {
        ShardingSphereTransactionManagerEngine changedStaleEngine = transactionContexts.getEngines().get(databaseName);
        if (null != changedStaleEngine) {
            closeTransactionEngine(changedStaleEngine);
        }
        transactionContexts.getEngines().put(databaseName, createNewEngine(resource.getDatabaseType(), resource.getDataSources()));
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
    
    private MetaDataContexts buildNewMetaDataContext(final String databaseName) throws SQLException {
        ConfigurationProperties configurationProps = metaDataContexts.getProps();
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(metaDataContexts.getGlobalRuleMetaData().getConfigurations(), configurationProps.getProps());
        metaDataContextsBuilder.addDatabase(databaseName, Strings.isNullOrEmpty(configurationProps.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE))
                ? DatabaseTypeRegistry.getDefaultDatabaseType()
                : DatabaseTypeRegistry.getTrunkDatabaseType(configurationProps.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE)),
                new DataSourceProvidedDatabaseConfiguration(new HashMap<>(), new LinkedList<>()), configurationProps.getProps());
        return metaDataContextsBuilder.build(metaDataContexts.getMetaDataPersistService().orElse(null));
    }
    
    private void closeDataSources(final ShardingSphereMetaData removeMetaData) {
        if (null != removeMetaData.getResource()) {
            removeMetaData.getResource().getDataSources().values().forEach(each -> removeMetaData.getResource().close(each));
        }
    }
    
    private void closeDataSources(final String databaseName, final Collection<DataSource> dataSources) {
        ShardingSphereResource resource = metaDataContexts.getMetaData(databaseName).getResource();
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
        metaDataContexts.close();
    }
}
