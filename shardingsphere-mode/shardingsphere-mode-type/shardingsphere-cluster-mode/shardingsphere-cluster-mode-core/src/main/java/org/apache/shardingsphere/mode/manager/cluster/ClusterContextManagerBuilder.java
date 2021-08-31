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

package org.apache.shardingsphere.mode.manager.cluster;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.lock.InnerLockReleasedEvent;
import org.apache.shardingsphere.infra.lock.LockNameUtil;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.optimize.core.metadata.FederateSchemaMetadata;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceEvent;
import org.apache.shardingsphere.infra.rule.identifier.type.StatusContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.authority.event.AuthorityChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.config.event.datasource.DataSourceChangeCompletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.config.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.config.event.datasource.DataSourceDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.config.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.config.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.config.event.schema.SchemaChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.state.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.state.event.PrimaryStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.governance.schema.GovernanceSchema;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.mode.persist.PersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

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
 * Cluster context manager builder.
 */
public final class ClusterContextManagerBuilder implements ContextManagerBuilder {
    
    static {
        ShardingSphereServiceLoader.register(ClusterPersistRepository.class);
    }
    
    private PersistService persistService;
    
    private ContextManager contextManager;
    
    @Override
    public ContextManager build(final ModeConfiguration modeConfig, final Map<String, Map<String, DataSource>> dataSourcesMap,
                                final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, final Collection<RuleConfiguration> globalRuleConfigs,
                                final Properties props, final boolean isOverwrite) throws SQLException {
        ShardingSphereEventBus.getInstance().register(this);
        ClusterPersistRepository repository = createClusterPersistRepository((ClusterPersistRepositoryConfiguration) modeConfig.getRepository());
        persistService = new PersistService(repository);
        RegistryCenter registryCenter = new RegistryCenter(repository);
        persistConfigurations(persistService, dataSourcesMap, schemaRuleConfigs, globalRuleConfigs, props, isOverwrite);
        Collection<String> schemaNames = persistService.getSchemaMetaDataService().loadAllNames();
        MetaDataContexts metaDataContexts = new MetaDataContextsBuilder(loadDataSourcesMap(persistService, dataSourcesMap, schemaNames),
                loadSchemaRules(persistService, schemaNames), persistService.getGlobalRuleService().load(), persistService.getPropsService().load()).build(persistService);
        TransactionContexts transactionContexts = createTransactionContexts(metaDataContexts);
        contextManager = new ClusterContextManager(persistService, registryCenter);
        contextManager.init(metaDataContexts, transactionContexts);
        return contextManager;
    }
    
    private ClusterPersistRepository createClusterPersistRepository(final ClusterPersistRepositoryConfiguration config) {
        Preconditions.checkNotNull(config, "Cluster persist repository configuration cannot be null.");
        ClusterPersistRepository result = TypedSPIRegistry.getRegisteredService(ClusterPersistRepository.class, config.getType(), config.getProps());
        result.init(config);
        return result;
    }
    
    private void persistConfigurations(final PersistService persistService, final Map<String, Map<String, DataSource>> dataSourcesMap,
                                       final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, final Collection<RuleConfiguration> globalRuleConfigs,
                                       final Properties props, final boolean overwrite) {
        if (!isEmptyLocalConfiguration(dataSourcesMap, schemaRuleConfigs, globalRuleConfigs, props)) {
            persistService.persistConfigurations(getDataSourceConfigurations(dataSourcesMap), schemaRuleConfigs, globalRuleConfigs, props, overwrite);
        }
    }
    
    private boolean isEmptyLocalConfiguration(final Map<String, Map<String, DataSource>> dataSourcesMap,
                                              final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, final Collection<RuleConfiguration> globalRuleConfigs, final Properties props) {
        return isEmptyLocalDataSourcesMap(dataSourcesMap) && isEmptyLocalSchemaRuleConfigurations(schemaRuleConfigs) && globalRuleConfigs.isEmpty() && props.isEmpty();
    }
    
    private boolean isEmptyLocalDataSourcesMap(final Map<String, Map<String, DataSource>> dataSourcesMap) {
        return dataSourcesMap.entrySet().stream().allMatch(entry -> entry.getValue().isEmpty());
    }
    
    private boolean isEmptyLocalSchemaRuleConfigurations(final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs) {
        return schemaRuleConfigs.entrySet().stream().allMatch(entry -> entry.getValue().isEmpty());
    }
    
    private Map<String, Map<String, DataSourceConfiguration>> getDataSourceConfigurations(final Map<String, Map<String, DataSource>> dataSourcesMap) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>(dataSourcesMap.size(), 1);
        for (Entry<String, Map<String, DataSource>> entry : dataSourcesMap.entrySet()) {
            result.put(entry.getKey(), DataSourceConverter.getDataSourceConfigurationMap(entry.getValue()));
        }
        return result;
    }
    
    private Map<String, Map<String, DataSource>> loadDataSourcesMap(final PersistService persistService, final Map<String, Map<String, DataSource>> dataSourcesMap,
                                                                    final Collection<String> schemaNames) {
        Map<String, Map<String, DataSourceConfiguration>> loadedDataSourceConfigs = loadDataSourceConfigurations(persistService, schemaNames);
        Map<String, Map<String, DataSourceConfiguration>> changedDataSourceConfigs = getChangedDataSourceConfigurations(dataSourcesMap, loadedDataSourceConfigs);
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(dataSourcesMap);
        getChangedDataSources(changedDataSourceConfigs).forEach((key, value) -> {
            if (result.containsKey(key)) {
                result.get(key).putAll(value);
            } else {
                result.put(key, value);
            }
        });
        return result;
    }
    
    private Map<String, Map<String, DataSourceConfiguration>> loadDataSourceConfigurations(final PersistService persistService, final Collection<String> schemaNames) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>();
        for (String each : schemaNames) {
            result.put(each, persistService.getDataSourceService().load(each));
        }
        return result;
    }
    
    private Map<String, Map<String, DataSourceConfiguration>> getChangedDataSourceConfigurations(final Map<String, Map<String, DataSource>> configuredDataSourcesMap, 
                                                                                                 final Map<String, Map<String, DataSourceConfiguration>> loadedDataSourceConfigs) {
        if (isEmptyLocalDataSourcesMap(configuredDataSourcesMap)) {
            return loadedDataSourceConfigs;
        }
        Map<String, Map<String, DataSourceConfiguration>> result = new HashMap<>(loadedDataSourceConfigs.size(), 1);
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : loadedDataSourceConfigs.entrySet()) {
            if (configuredDataSourcesMap.containsKey(entry.getKey())) {
                Map<String, DataSourceConfiguration> changedDataSources = getChangedDataSourcesConfigurations(configuredDataSourcesMap.get(entry.getKey()), entry.getValue());
                if (!changedDataSources.isEmpty()) {
                    result.put(entry.getKey(), changedDataSources);
                }
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private Map<String, DataSourceConfiguration> getChangedDataSourcesConfigurations(final Map<String, DataSource> dataSourceMap, 
                                                                                     final Map<String, DataSourceConfiguration> loadedDataSourceConfigurationMap) {
        Map<String, DataSourceConfiguration> dataSourceConfigurationMap = DataSourceConverter.getDataSourceConfigurationMap(dataSourceMap);
        return loadedDataSourceConfigurationMap.entrySet().stream().filter(entry -> !dataSourceConfigurationMap.containsKey(entry.getKey()) 
                || !dataSourceConfigurationMap.get(entry.getKey()).equals(entry.getValue())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private Map<String, DataSource> createDataSources(final Map<String, DataSourceConfiguration> dataSourceConfigs) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceConfigs.size(), 1);
        for (Entry<String, DataSourceConfiguration> each : dataSourceConfigs.entrySet()) {
            result.put(each.getKey(), each.getValue().createDataSource());
        }
        return result;
    }
    
    private Map<String, Collection<RuleConfiguration>> loadSchemaRules(final PersistService persistService, final Collection<String> schemaNames) {
        return schemaNames.stream().collect(Collectors.toMap(
            each -> each, each -> persistService.getSchemaRuleService().load(each), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private TransactionContexts createTransactionContexts(final MetaDataContexts metaDataContexts) {
        Map<String, ShardingSphereTransactionManagerEngine> engines = new HashMap<>(metaDataContexts.getAllSchemaNames().size(), 1);
        TransactionRule transactionRule = getTransactionRule(metaDataContexts);
        for (String each : metaDataContexts.getAllSchemaNames()) {
            ShardingSphereTransactionManagerEngine engine = new ShardingSphereTransactionManagerEngine();
            ShardingSphereResource resource = metaDataContexts.getMetaData(each).getResource();
            engine.init(resource.getDatabaseType(), resource.getDataSources(), transactionRule);
            engines.put(each, engine);
        }
        return new TransactionContexts(engines);
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
        ShardingSphereMetaData metaData = buildMetaData(event.getSchemaName());
        contextManager.getMetaDataContexts().getOptimizeContextFactory().getSchemaMetadatas().getSchemas().put(event.getSchemaName(), new FederateSchemaMetadata(event.getSchemaName(), 
                metaData.getSchema().getTables()));
        contextManager.getMetaDataContexts().getMetaDataMap().put(event.getSchemaName(), metaData);
        contextManager.renewMetaDataContexts(new MetaDataContexts(persistService,
                contextManager.getMetaDataContexts().getMetaDataMap(), contextManager.getMetaDataContexts().getGlobalRuleMetaData(), contextManager.getMetaDataContexts().getExecutorEngine(),
                contextManager.getMetaDataContexts().getProps(), contextManager.getMetaDataContexts().getOptimizeContextFactory()));
        ShardingSphereEventBus.getInstance().post(new DataSourceChangeCompletedEvent(event.getSchemaName(),
                contextManager.getMetaDataContexts().getMetaDataMap().get(event.getSchemaName()).getResource().getDatabaseType(), metaData.getResource().getDataSources()));
    }
    
    /**
     * Renew to delete schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent event) {
        String schemaName = event.getSchemaName();
        closeDataSources(schemaName);
        Map<String, ShardingSphereMetaData> schemaMetaData = new HashMap<>(contextManager.getMetaDataContexts().getMetaDataMap());
        schemaMetaData.remove(schemaName);
        contextManager.getMetaDataContexts().getOptimizeContextFactory().getSchemaMetadatas().getSchemas().remove(schemaName);
        contextManager.renewMetaDataContexts(new MetaDataContexts(persistService,
                schemaMetaData, contextManager.getMetaDataContexts().getGlobalRuleMetaData(), contextManager.getMetaDataContexts().getExecutorEngine(),
                contextManager.getMetaDataContexts().getProps(), contextManager.getMetaDataContexts().getOptimizeContextFactory()));
        ShardingSphereEventBus.getInstance().post(new DataSourceDeletedEvent(schemaName));
    }
    
    /**
     * Renew properties.
     *
     * @param event properties changed event
     */
    @Subscribe
    public synchronized void renew(final PropertiesChangedEvent event) {
        ConfigurationProperties props = new ConfigurationProperties(event.getProps());
        contextManager.renewMetaDataContexts(new MetaDataContexts(persistService,
                contextManager.getMetaDataContexts().getMetaDataMap(), contextManager.getMetaDataContexts().getGlobalRuleMetaData(), contextManager.getMetaDataContexts().getExecutorEngine(), 
                props, contextManager.getMetaDataContexts().getOptimizeContextFactory()));
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
        try {
            Map<String, ShardingSphereMetaData> schemaMetaData = new HashMap<>(contextManager.getMetaDataContexts().getMetaDataMap().size(), 1);
            for (Entry<String, ShardingSphereMetaData> entry : contextManager.getMetaDataContexts().getMetaDataMap().entrySet()) {
                String schemaName = entry.getKey();
                ShardingSphereMetaData originalMetaData = entry.getValue();
                ShardingSphereMetaData metaData = event.getSchemaName().equals(schemaName) ? getChangedMetaData(originalMetaData, event.getSchema(), schemaName) : originalMetaData;
                schemaMetaData.put(schemaName, metaData);
                contextManager.getMetaDataContexts().getOptimizeContextFactory().getSchemaMetadatas().getSchemas().put(event.getSchemaName(),
                        new FederateSchemaMetadata(event.getSchemaName(), metaData.getSchema().getTables()));
            }
            contextManager.renewMetaDataContexts(new MetaDataContexts(persistService,
                    schemaMetaData, contextManager.getMetaDataContexts().getGlobalRuleMetaData(), contextManager.getMetaDataContexts().getExecutorEngine(),
                    contextManager.getMetaDataContexts().getProps(), contextManager.getMetaDataContexts().getOptimizeContextFactory()));
        } finally {
            ShardingSphereEventBus.getInstance().post(new InnerLockReleasedEvent(LockNameUtil.getMetadataRefreshLockName()));
        }
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
        ShardingSphereMetaData metaData = getChangedMetaData(contextManager.getMetaDataContexts().getMetaDataMap().get(schemaName), event.getRuleConfigurations());
        Map<String, ShardingSphereMetaData> schemaMetaData = rebuildSchemaMetaData(schemaName, metaData);
        contextManager.renewMetaDataContexts(new MetaDataContexts(persistService, schemaMetaData, contextManager.getMetaDataContexts().getGlobalRuleMetaData(), 
                contextManager.getMetaDataContexts().getExecutorEngine(),
                contextManager.getMetaDataContexts().getProps(), contextManager.getMetaDataContexts().getOptimizeContextFactory()));
        persistService.getSchemaMetaDataService().persist(schemaName, schemaMetaData.get(schemaName).getSchema());
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
        Collection<DataSource> pendingClosedDataSources = getPendingClosedDataSources(schemaName, event.getDataSourceConfigurations());
        ShardingSphereMetaData metaData = rebuildMetaData(contextManager.getMetaDataContexts().getMetaDataMap().get(schemaName), event.getDataSourceConfigurations());
        Map<String, ShardingSphereMetaData> schemaMetaData = rebuildSchemaMetaData(schemaName, metaData);
        contextManager.renewMetaDataContexts(new MetaDataContexts(persistService, schemaMetaData,
                contextManager.getMetaDataContexts().getGlobalRuleMetaData(), contextManager.getMetaDataContexts().getExecutorEngine(), contextManager.getMetaDataContexts().getProps(), 
                contextManager.getMetaDataContexts().getOptimizeContextFactory()));
        ShardingSphereEventBus.getInstance().post(new DataSourceChangeCompletedEvent(event.getSchemaName(),
                contextManager.getMetaDataContexts().getMetaDataMap().get(event.getSchemaName()).getResource().getDatabaseType(), 
                schemaMetaData.get(event.getSchemaName()).getResource().getDataSources()));
        closeDataSources(schemaName, pendingClosedDataSources);
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param event disabled state changed event
     */
    @Subscribe
    public synchronized void renew(final DisabledStateChangedEvent event) {
        GovernanceSchema governanceSchema = event.getGovernanceSchema();
        Collection<ShardingSphereRule> rules = contextManager.getMetaDataContexts().getMetaDataMap().get(governanceSchema.getSchemaName()).getRuleMetaData().getRules();
        for (ShardingSphereRule each : rules) {
            if (each instanceof StatusContainedRule) {
                ((StatusContainedRule) each).updateRuleStatus(new DataSourceNameDisabledEvent(governanceSchema.getDataSourceName(), event.isDisabled()));
            }
        }
    }
    
    /**
     * Renew primary data source names.
     *
     * @param event primary state changed event
     */
    @Subscribe
    public synchronized void renew(final PrimaryStateChangedEvent event) {
        GovernanceSchema governanceSchema = event.getGovernanceSchema();
        Collection<ShardingSphereRule> rules = contextManager.getMetaDataContexts().getMetaDataMap().get(governanceSchema.getSchemaName()).getRuleMetaData().getRules();
        for (ShardingSphereRule each : rules) {
            if (each instanceof StatusContainedRule) {
                ((StatusContainedRule) each).updateRuleStatus(new PrimaryDataSourceEvent(governanceSchema.getSchemaName(), governanceSchema.getDataSourceName(), event.getPrimaryDataSourceName()));
            }
        }
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
                    ShardingSphereRulesBuilder.buildGlobalRules(newGlobalConfigs, contextManager.getMetaDataContexts().getMetaDataMap()));
            contextManager.renewMetaDataContexts(new MetaDataContexts(persistService, contextManager.getMetaDataContexts().getMetaDataMap(), newGlobalRuleMetaData, 
                    contextManager.getMetaDataContexts().getExecutorEngine(),
                    contextManager.getMetaDataContexts().getProps(), contextManager.getMetaDataContexts().getOptimizeContextFactory()));
        }
    }
    
    private Map<String, ShardingSphereMetaData> rebuildSchemaMetaData(final String schemaName, final ShardingSphereMetaData metaData) {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(contextManager.getMetaDataContexts().getMetaDataMap());
        result.put(schemaName, metaData);
        contextManager.getMetaDataContexts().getOptimizeContextFactory().getSchemaMetadatas().getSchemas().put(schemaName, new FederateSchemaMetadata(schemaName, metaData.getSchema().getTables()));
        return result;
    }
    
    private void persistSchema(final String schemaName) {
        if (!persistService.getDataSourceService().isExisted(schemaName)) {
            persistService.getDataSourceService().persist(schemaName, new LinkedHashMap<>());
        }
        if (!persistService.getSchemaRuleService().isExisted(schemaName)) {
            persistService.getSchemaRuleService().persist(schemaName, new LinkedList<>());
        }
    }
    
    private ShardingSphereMetaData buildMetaData(final String schemaName) throws SQLException {
        Map<String, Map<String, DataSource>> dataSourcesMap = createDataSourcesMap(Collections.singletonMap(schemaName, persistService.getDataSourceService().load(schemaName)));
        return new MetaDataContextsBuilder(dataSourcesMap,
                Collections.singletonMap(schemaName, persistService.getSchemaRuleService().load(schemaName)),
                persistService.getGlobalRuleService().load(),
                contextManager.getMetaDataContexts().getProps().getProps()).build(persistService).getMetaData(schemaName);
    }
    
    private ShardingSphereMetaData getChangedMetaData(final ShardingSphereMetaData originalMetaData, final ShardingSphereSchema schema, final String schemaName) {
        // TODO refresh table addressing mapper
        return new ShardingSphereMetaData(schemaName, originalMetaData.getResource(), originalMetaData.getRuleMetaData(), schema);
    }
    
    private ShardingSphereMetaData getChangedMetaData(final ShardingSphereMetaData originalMetaData, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        MetaDataContextsBuilder builder = new MetaDataContextsBuilder(Collections.singletonMap(originalMetaData.getName(), originalMetaData.getResource().getDataSources()),
                Collections.singletonMap(originalMetaData.getName(), ruleConfigs), persistService.getGlobalRuleService().load(), contextManager.getMetaDataContexts().getProps().getProps());
        return builder.build(persistService).getMetaDataMap().values().iterator().next();
    }
    
    private ShardingSphereMetaData rebuildMetaData(final ShardingSphereMetaData originalMetaData, final Map<String, DataSourceConfiguration> newDataSourceConfigs) throws SQLException {
        Collection<String> deletedDataSources = getDeletedDataSources(originalMetaData, newDataSourceConfigs).keySet();
        Map<String, DataSource> changedDataSources = buildChangedDataSources(originalMetaData, newDataSourceConfigs);
        Map<String, Map<String, DataSource>> dataSourcesMap = Collections.singletonMap(originalMetaData.getName(),
                getNewDataSources(originalMetaData.getResource().getDataSources(), getAddedDataSources(originalMetaData, newDataSourceConfigs), changedDataSources, deletedDataSources));
        return new MetaDataContextsBuilder(dataSourcesMap, Collections.singletonMap(originalMetaData.getName(),
                originalMetaData.getRuleMetaData().getConfigurations()), persistService.getGlobalRuleService().load(),
                contextManager.getMetaDataContexts().getProps().getProps()).build(persistService).getMetaData(originalMetaData.getName());
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
    
    private Map<String, Map<String, DataSource>> getChangedDataSources(final Map<String, Map<String, DataSourceConfiguration>> changedDataSourceConfigurations) {
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(changedDataSourceConfigurations.size(), 1);
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : changedDataSourceConfigurations.entrySet()) {
            result.put(entry.getKey(), createDataSources(entry.getValue()));
        }
        return result;
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
    
    private Map<String, Map<String, DataSource>> createDataSourcesMap(final Map<String, Map<String, DataSourceConfiguration>> dataSourcesConfigs) {
        return dataSourcesConfigs.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> DataSourceConverter.getDataSourceMap(entry.getValue())));
    }
    
    private Collection<DataSource> getPendingClosedDataSources(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        Collection<DataSource> result = new LinkedList<>();
        result.addAll(getDeletedDataSources(contextManager.getMetaDataContexts().getMetaData(schemaName), dataSourceConfigurations).values());
        result.addAll(getChangedDataSources(contextManager.getMetaDataContexts().getMetaData(schemaName), dataSourceConfigurations).values());
        return result;
    }
    
    private void closeDataSources(final String schemaName) {
        if (null != contextManager.getMetaDataContexts().getMetaData(schemaName) 
                && null != contextManager.getMetaDataContexts().getMetaData(schemaName).getResource()) {
            closeDataSources(schemaName, contextManager.getMetaDataContexts().getMetaData(schemaName).getResource().getDataSources().values());
        }
    }
    
    private void closeDataSources(final String schemaName, final Collection<DataSource> dataSources) {
        ShardingSphereResource resource = contextManager.getMetaDataContexts().getMetaData(schemaName).getResource();
        dataSources.forEach(each -> closeDataSource(resource, each));
    }
    
    private void closeDataSource(final ShardingSphereResource resource, final DataSource dataSource) {
        try {
            resource.close(dataSource);
            // CHECKSTYLE:OFF
        } catch (final Exception ignore) {
            // CHECKSTYLE:ON
        }
    }
    
    /**
     * Renew transaction manager engine contexts.
     *
     * @param event data source change completed event
     * @throws Exception exception
     */
    @Subscribe
    public synchronized void renewTransactionContext(final DataSourceChangeCompletedEvent event) throws Exception {
        closeStaleEngine(event.getSchemaName());
        Map<String, ShardingSphereTransactionManagerEngine> existedEngines = contextManager.getTransactionContexts().getEngines();
        existedEngines.put(event.getSchemaName(), createNewEngine(event.getDatabaseType(), event.getDataSources()));
        renewContexts(existedEngines);
    }
    
    /**
     * Renew transaction manager engine context.
     *
     * @param event data source deleted event.
     * @throws Exception exception
     */
    @Subscribe
    public synchronized void renewTransactionContext(final DataSourceDeletedEvent event) throws Exception {
        closeStaleEngine(event.getSchemaName());
        renewContexts(contextManager.getTransactionContexts().getEngines());
    }
    
    private void closeStaleEngine(final String schemaName) throws Exception {
        ShardingSphereTransactionManagerEngine staleEngine = contextManager.getTransactionContexts().getEngines().remove(schemaName);
        if (null != staleEngine) {
            staleEngine.close();
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
        return transactionRule.orElseGet(() -> new TransactionRule(new TransactionRuleConfiguration(TransactionType.LOCAL.name(), null)));
    }
    
    private TransactionRule getTransactionRule(final MetaDataContexts metaDataContexts) {
        Optional<TransactionRule> transactionRule = metaDataContexts.getGlobalRuleMetaData().getRules().stream().filter(
            each -> each instanceof TransactionRule).map(each -> (TransactionRule) each).findFirst();
        return transactionRule.orElseGet(() -> new TransactionRule(new TransactionRuleConfiguration(TransactionType.LOCAL.name(), null)));
    }
    
    private void renewContexts(final Map<String, ShardingSphereTransactionManagerEngine> engines) {
        contextManager.renewTransactionContexts(new TransactionContexts(engines));
    }
    
    @Override
    public String getType() {
        return "Cluster";
    }
}
