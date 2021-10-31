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

package org.apache.shardingsphere.mode.manager.cluster.coordinator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.QualifiedSchema;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilder;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.infra.rule.identifier.type.StatusContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.authority.event.AuthorityChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.schema.SchemaChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.PrimaryStateChangedEvent;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.rule.builder.DefaultTransactionRuleConfigurationBuilder;

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
 * Cluster context manager coordinator.
 */
@Slf4j
public final class ClusterContextManagerCoordinator {
    
    private final MetaDataPersistService metaDataPersistService;
    
    private final ContextManager contextManager;
    
    public ClusterContextManagerCoordinator(final MetaDataPersistService metaDataPersistService, final ContextManager contextManager) {
        this.metaDataPersistService = metaDataPersistService;
        this.contextManager = contextManager;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Renew to persist meta data.
     *
     * @param event schema added event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent event) throws SQLException {
        persistSchema(event.getSchemaName());
        contextManager.addSchema(event.getSchemaName());
    }
    
    /**
     * Renew to delete schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent event) {
        contextManager.deleteSchema(event.getSchemaName());
    }
    
    /**
     * Renew properties.
     *
     * @param event properties changed event
     */
    @Subscribe
    public synchronized void renew(final PropertiesChangedEvent event) {
        ConfigurationProperties props = new ConfigurationProperties(event.getProps());
        contextManager.renewMetaDataContexts(rebuildMetaDataContexts(props));
    }
    
    /**
     * Renew authority.
     *
     * @param event authority changed event
     */
    @Subscribe
    public synchronized void renew(final AuthorityChangedEvent event) {
        Optional<AuthorityRule> rule = contextManager.getMetaDataContexts().getGlobalRuleMetaData().getRules().stream()
                .filter(each -> each instanceof AuthorityRule).findAny().map(each -> (AuthorityRule) each);
        rule.ifPresent(optional -> optional.refresh(contextManager.getMetaDataContexts().getMetaDataMap(), event.getUsers()));
    }
    
    /**
     * Renew meta data of the schema.
     *
     * @param event meta data changed event
     */
    @Subscribe
    public synchronized void renew(final SchemaChangedEvent event) {
        String schemaName = event.getSchemaName();
        Collection<TableMetaData> tableMetaDataList = event.getSchema().getTables().values();
        ShardingSphereMetaData kernelMetaData = new ShardingSphereMetaData(schemaName, contextManager.getMetaDataContexts().getMetaData(schemaName).getResource(),
                contextManager.getMetaDataContexts().getMetaData(schemaName).getRuleMetaData(), SchemaBuilder.buildKernelSchema(tableMetaDataList,
                contextManager.getMetaDataContexts().getMetaData(schemaName).getRuleMetaData().getRules()));
        Map<String, ShardingSphereMetaData> kernelMetaDataMap = new HashMap<>(contextManager.getMetaDataContexts().getMetaDataMap());
        kernelMetaDataMap.put(schemaName, kernelMetaData);
        contextManager.getMetaDataContexts().getOptimizerContext().getMetaData().getSchemas().put(schemaName,
                new FederationSchemaMetaData(schemaName, SchemaBuilder.buildFederationSchema(tableMetaDataList,
                        contextManager.getMetaDataContexts().getMetaData(schemaName).getRuleMetaData().getRules()).getTables()));
        contextManager.renewMetaDataContexts(rebuildMetaDataContexts(kernelMetaDataMap));
    }
    
    /**
     * Renew rule configurations.
     *
     * @param event rule configurations changed event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsChangedEvent event) throws SQLException {
        String schemaName = event.getSchemaName();
        MetaDataContexts changedMetaDataContexts = buildChangedMetaDataContext(contextManager.getMetaDataContexts().getMetaDataMap().get(schemaName), event.getRuleConfigurations());
        contextManager.getMetaDataContexts().getOptimizerContext().getMetaData().getSchemas().putAll(changedMetaDataContexts.getOptimizerContext().getMetaData().getSchemas());
        Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(contextManager.getMetaDataContexts().getMetaDataMap());
        metaDataMap.putAll(changedMetaDataContexts.getMetaDataMap());
        contextManager.renewMetaDataContexts(rebuildMetaDataContexts(metaDataMap));
    }
    
    /**
     * Renew data source configuration.
     *
     * @param event data source changed event.
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void renew(final DataSourceChangedEvent event) throws SQLException {
        String schemaName = event.getSchemaName();
        MetaDataContexts changedMetaDataContext = buildChangedMetaDataContext(contextManager.getMetaDataContexts().getMetaDataMap().get(schemaName), event.getDataSourceConfigurations());
        contextManager.getMetaDataContexts().getOptimizerContext().getMetaData().getSchemas().putAll(changedMetaDataContext.getOptimizerContext().getMetaData().getSchemas());
        Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(contextManager.getMetaDataContexts().getMetaDataMap());
        metaDataMap.putAll(changedMetaDataContext.getMetaDataMap());
        Collection<DataSource> pendingClosedDataSources = getPendingClosedDataSources(schemaName, event.getDataSourceConfigurations());
        contextManager.renewMetaDataContexts(rebuildMetaDataContexts(metaDataMap));
        renewTransactionContext(schemaName, contextManager.getMetaDataContexts().getMetaData(schemaName).getResource());
        closeDataSources(schemaName, pendingClosedDataSources);
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param event disabled state changed event
     */
    @Subscribe
    public synchronized void renew(final DisabledStateChangedEvent event) {
        QualifiedSchema qualifiedSchema = event.getQualifiedSchema();
        contextManager.getMetaDataContexts().getMetaDataMap().get(qualifiedSchema.getSchemaName()).getRuleMetaData().getRules()
                .stream()
                .filter(each -> each instanceof StatusContainedRule)
                .forEach(each -> ((StatusContainedRule) each)
                        .updateStatus(new DataSourceNameDisabledEvent(qualifiedSchema.getDataSourceName(), event.isDisabled())));
    }
    
    /**
     * Renew primary data source names.
     *
     * @param event primary state changed event
     */
    @Subscribe
    public synchronized void renew(final PrimaryStateChangedEvent event) {
        QualifiedSchema qualifiedSchema = event.getQualifiedSchema();
        contextManager.getMetaDataContexts().getMetaDataMap().get(qualifiedSchema.getSchemaName()).getRuleMetaData().getRules()
                .stream()
                .filter(each -> each instanceof StatusContainedRule)
                .forEach(each -> ((StatusContainedRule) each)
                        .updateStatus(new PrimaryDataSourceChangedEvent(qualifiedSchema.getSchemaName(), qualifiedSchema.getDataSourceName(), event.getPrimaryDataSourceName())));
    }
    
    /**
     * Renew global rule configurations.
     *
     * @param event global rule configurations changed event
     */
    @Subscribe
    public synchronized void renew(final GlobalRuleConfigurationsChangedEvent event) {
        Collection<RuleConfiguration> newGlobalConfigs = event.getRuleConfigurations();
        if (!newGlobalConfigs.isEmpty()) {
            ShardingSphereRuleMetaData newGlobalRuleMetaData = new ShardingSphereRuleMetaData(newGlobalConfigs,
                    GlobalRulesBuilder.buildRules(newGlobalConfigs, contextManager.getMetaDataContexts().getMetaDataMap()));
            contextManager.renewMetaDataContexts(rebuildMetaDataContexts(newGlobalRuleMetaData));
        }
    }
    
    private MetaDataContexts rebuildMetaDataContexts(final Map<String, ShardingSphereMetaData> schemaMetaData) {
        Preconditions.checkState(contextManager.getMetaDataContexts().getMetaDataPersistService().isPresent());
        return new MetaDataContexts(contextManager.getMetaDataContexts().getMetaDataPersistService().get(),
                schemaMetaData, contextManager.getMetaDataContexts().getGlobalRuleMetaData(), contextManager.getMetaDataContexts().getExecutorEngine(),
                contextManager.getMetaDataContexts().getProps(), contextManager.getMetaDataContexts().getOptimizerContext());
    }
    
    private MetaDataContexts rebuildMetaDataContexts(final ConfigurationProperties props) {
        Preconditions.checkState(contextManager.getMetaDataContexts().getMetaDataPersistService().isPresent());
        return new MetaDataContexts(contextManager.getMetaDataContexts().getMetaDataPersistService().get(),
                contextManager.getMetaDataContexts().getMetaDataMap(), contextManager.getMetaDataContexts().getGlobalRuleMetaData(), contextManager.getMetaDataContexts().getExecutorEngine(),
                props, contextManager.getMetaDataContexts().getOptimizerContext());
    }
    
    private MetaDataContexts rebuildMetaDataContexts(final ShardingSphereRuleMetaData globalRuleMetaData) {
        Preconditions.checkState(contextManager.getMetaDataContexts().getMetaDataPersistService().isPresent());
        return new MetaDataContexts(contextManager.getMetaDataContexts().getMetaDataPersistService().get(),
                contextManager.getMetaDataContexts().getMetaDataMap(), globalRuleMetaData, contextManager.getMetaDataContexts().getExecutorEngine(),
                contextManager.getMetaDataContexts().getProps(), contextManager.getMetaDataContexts().getOptimizerContext());
    }
    
    private void persistSchema(final String schemaName) {
        if (!metaDataPersistService.getDataSourceService().isExisted(schemaName)) {
            metaDataPersistService.getDataSourceService().persist(schemaName, new LinkedHashMap<>());
        }
        if (!metaDataPersistService.getSchemaRuleService().isExisted(schemaName)) {
            metaDataPersistService.getSchemaRuleService().persist(schemaName, new LinkedList<>());
        }
    }
    
    private MetaDataContexts buildChangedMetaDataContext(final ShardingSphereMetaData originalMetaData, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Map<String, Map<String, DataSource>> dataSourcesMap = Collections.singletonMap(originalMetaData.getName(), originalMetaData.getResource().getDataSources());
        Map<String, Collection<RuleConfiguration>> schemaRuleConfigs = Collections.singletonMap(originalMetaData.getName(), ruleConfigs);
        Properties props = contextManager.getMetaDataContexts().getProps().getProps();
        Map<String, Collection<ShardingSphereRule>> rules = SchemaRulesBuilder.buildRules(dataSourcesMap, schemaRuleConfigs, props);
        Map<String, ShardingSphereSchema> schemas = new SchemaLoader(dataSourcesMap, schemaRuleConfigs, rules, props).load();
        metaDataPersistService.getSchemaMetaDataService().persist(originalMetaData.getName(), schemas.get(originalMetaData.getName()));
        return new MetaDataContextsBuilder(dataSourcesMap, schemaRuleConfigs, metaDataPersistService.getGlobalRuleService().load(), schemas, rules, props).build(metaDataPersistService);
    }
    
    private MetaDataContexts buildChangedMetaDataContext(final ShardingSphereMetaData originalMetaData, final Map<String, DataSourceConfiguration> newDataSourceConfigs) throws SQLException {
        Collection<String> deletedDataSources = getDeletedDataSources(originalMetaData, newDataSourceConfigs).keySet();
        Map<String, DataSource> changedDataSources = buildChangedDataSources(originalMetaData, newDataSourceConfigs);
        Map<String, Map<String, DataSource>> dataSourcesMap = Collections.singletonMap(originalMetaData.getName(),
                getNewDataSources(originalMetaData.getResource().getDataSources(), getAddedDataSources(originalMetaData, newDataSourceConfigs), changedDataSources, deletedDataSources));
        Map<String, Collection<RuleConfiguration>> schemaRuleConfigs = Collections.singletonMap(originalMetaData.getName(), originalMetaData.getRuleMetaData().getConfigurations());
        Properties props = contextManager.getMetaDataContexts().getProps().getProps();
        Map<String, Collection<ShardingSphereRule>> rules = SchemaRulesBuilder.buildRules(dataSourcesMap, schemaRuleConfigs, props);
        Map<String, ShardingSphereSchema> schemas = new SchemaLoader(dataSourcesMap, schemaRuleConfigs, rules, props).load();
        metaDataPersistService.getSchemaMetaDataService().persist(originalMetaData.getName(), schemas.get(originalMetaData.getName()));
        return new MetaDataContextsBuilder(dataSourcesMap, schemaRuleConfigs, metaDataPersistService.getGlobalRuleService().load(), schemas, rules, props).build(metaDataPersistService);
    }
    
    private Map<String, DataSource> getNewDataSources(final Map<String, DataSource> originalDataSources,
                                                      final Map<String, DataSource> addedDataSources, final Map<String, DataSource> changedDataSources, final Collection<String> deletedDataSources) {
        Map<String, DataSource> result = new LinkedHashMap<>(originalDataSources);
        result.keySet().removeAll(deletedDataSources);
        result.putAll(changedDataSources);
        result.putAll(addedDataSources);
        return result;
    }
    
    private Map<String, DataSource> getDeletedDataSources(final ShardingSphereMetaData originalMetaData, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        return originalMetaData.getResource().getDataSources().entrySet().stream().filter(entry -> !newDataSourceConfigs.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private Map<String, DataSource> getChangedDataSources(final ShardingSphereMetaData originalMetaData, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        Collection<String> changedDataSourceNames = getChangedDataSourceConfiguration(originalMetaData, newDataSourceConfigs).keySet();
        return originalMetaData.getResource().getDataSources().entrySet().stream().filter(entry -> changedDataSourceNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private Map<String, DataSource> getAddedDataSources(final ShardingSphereMetaData originalMetaData, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        return DataSourceConverter.getDataSourceMap(Maps.filterKeys(newDataSourceConfigs, each -> !originalMetaData.getResource().getDataSources().containsKey(each)));
    }
    
    private Map<String, DataSourceConfiguration> getChangedDataSourceConfiguration(final ShardingSphereMetaData originalMetaData,
                                                                                   final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        return dataSourceConfigurations.entrySet().stream()
                .filter(entry -> isModifiedDataSource(originalMetaData.getResource().getDataSources(), entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private Map<String, DataSource> buildChangedDataSources(final ShardingSphereMetaData originalMetaData, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        return DataSourceConverter.getDataSourceMap(getChangedDataSourceConfiguration(originalMetaData, newDataSourceConfigs));
    }
    
    private boolean isModifiedDataSource(final Map<String, DataSource> originalDataSources, final String dataSourceName, final DataSourceConfiguration dataSourceConfiguration) {
        DataSourceConfiguration dataSourceConfig = DataSourceConverter.getDataSourceConfigurationMap(originalDataSources).get(dataSourceName);
        return null != dataSourceConfig && !dataSourceConfiguration.equals(dataSourceConfig);
    }
    
    private Collection<DataSource> getPendingClosedDataSources(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        Collection<DataSource> result = new LinkedList<>();
        result.addAll(getDeletedDataSources(contextManager.getMetaDataContexts().getMetaData(schemaName), dataSourceConfigurations).values());
        result.addAll(getChangedDataSources(contextManager.getMetaDataContexts().getMetaData(schemaName), dataSourceConfigurations).values());
        return result;
    }
    
    private void closeDataSources(final String schemaName, final Collection<DataSource> dataSources) {
        ShardingSphereResource resource = contextManager.getMetaDataContexts().getMetaData(schemaName).getResource();
        dataSources.forEach(each -> closeDataSource(resource, each));
    }
    
    private void closeDataSource(final ShardingSphereResource resource, final DataSource dataSource) {
        try {
            resource.close(dataSource);
        } catch (final SQLException ex) {
            log.error("Close data source failed", ex);
        }
    }
    
    private void renewTransactionContext(final String schemaName, final ShardingSphereResource resource) {
        closeStaleEngine(schemaName);
        Map<String, ShardingSphereTransactionManagerEngine> existedEngines = contextManager.getTransactionContexts().getEngines();
        existedEngines.put(schemaName, createNewEngine(resource.getDatabaseType(), resource.getDataSources()));
        renewContexts(existedEngines);
    }
    
    private void closeStaleEngine(final String schemaName) {
        ShardingSphereTransactionManagerEngine staleEngine = contextManager.getTransactionContexts().getEngines().remove(schemaName);
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
    
    private ShardingSphereTransactionManagerEngine createNewEngine(final DatabaseType databaseType, final Map<String, DataSource> dataSources) {
        ShardingSphereTransactionManagerEngine result = new ShardingSphereTransactionManagerEngine();
        result.init(databaseType, dataSources, getTransactionRule());
        return result;
    }
    
    private TransactionRule getTransactionRule() {
        Optional<TransactionRule> transactionRule = contextManager.getMetaDataContexts().getGlobalRuleMetaData().getRules().stream()
                .filter(each -> each instanceof TransactionRule).map(each -> (TransactionRule) each).findFirst();
        return transactionRule.orElseGet(() -> new TransactionRule(new DefaultTransactionRuleConfigurationBuilder().build()));
    }
    
    private void renewContexts(final Map<String, ShardingSphereTransactionManagerEngine> engines) {
        contextManager.renewTransactionContexts(new TransactionContexts(engines));
    }
}
