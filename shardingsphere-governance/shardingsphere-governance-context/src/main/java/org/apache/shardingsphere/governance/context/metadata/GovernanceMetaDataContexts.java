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

package org.apache.shardingsphere.governance.context.metadata;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.governance.context.authority.listener.event.AuthorityChangedEvent;
import org.apache.shardingsphere.governance.core.lock.ShardingSphereDistributeLock;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.config.event.datasource.DataSourceChangeCompletedEvent;
import org.apache.shardingsphere.governance.core.registry.config.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.registry.config.event.datasource.DataSourceDeletedEvent;
import org.apache.shardingsphere.governance.core.registry.config.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.governance.core.registry.config.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.registry.config.event.schema.SchemaChangedEvent;
import org.apache.shardingsphere.governance.core.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.governance.core.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.governance.core.registry.state.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.state.event.PrimaryStateChangedEvent;
import org.apache.shardingsphere.governance.core.schema.GovernanceSchema;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.lock.InnerLockReleasedEvent;
import org.apache.shardingsphere.infra.lock.LockNameUtil;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.apache.shardingsphere.infra.optimize.core.metadata.FederateSchemaMetadata;
import org.apache.shardingsphere.infra.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceEvent;
import org.apache.shardingsphere.infra.rule.identifier.type.StatusContainedRule;
import org.apache.shardingsphere.infra.state.StateContext;

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
import java.util.stream.Collectors;

/**
 * Governance meta data contexts.
 */
public final class GovernanceMetaDataContexts implements MetaDataContexts {
    
    private volatile StandardMetaDataContexts metaDataContexts;
    
    private final DistMetaDataPersistService distMetaDataPersistService;
    
    private final RegistryCenter registryCenter;
    
    private final ShardingSphereLock lock;
    
    public GovernanceMetaDataContexts(final StandardMetaDataContexts metaDataContexts,
                                      final DistMetaDataPersistService distMetaDataPersistService, final RegistryCenter registryCenter) {
        this.metaDataContexts = metaDataContexts;
        this.distMetaDataPersistService = distMetaDataPersistService;
        this.registryCenter = registryCenter;
        ShardingSphereEventBus.getInstance().register(this);
        disableDataSources();
        persistMetaData();
        lock = createShardingSphereLock(registryCenter.getRepository());
    }
    
    private void disableDataSources() {
        metaDataContexts.getMetaDataMap().forEach((key, value)
            -> value.getRuleMetaData().getRules().stream().filter(each -> each instanceof StatusContainedRule).forEach(each -> disableDataSources(key, (StatusContainedRule) each)));
    }
    
    private void disableDataSources(final String schemaName, final StatusContainedRule rule) {
        Collection<String> disabledDataSources = registryCenter.getDataSourceStatusService().loadDisabledDataSources(schemaName);
        disabledDataSources.stream().map(this::getDataSourceName).forEach(each -> rule.updateRuleStatus(new DataSourceNameDisabledEvent(each, true)));
    }
    
    private String getDataSourceName(final String disabledDataSource) {
        return new GovernanceSchema(disabledDataSource).getDataSourceName();
    }
    
    private void persistMetaData() {
        metaDataContexts.getMetaDataMap().forEach((key, value) -> distMetaDataPersistService.getSchemaMetaDataService().persist(key, value.getSchema()));
    }
    
    private ShardingSphereLock createShardingSphereLock(final RegistryCenterRepository repository) {
        return metaDataContexts.getProps().<Boolean>getValue(ConfigurationPropertyKey.LOCK_ENABLED)
                ? new ShardingSphereDistributeLock(repository, metaDataContexts.getProps().<Long>getValue(ConfigurationPropertyKey.LOCK_WAIT_TIMEOUT_MILLISECONDS)) : null;
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
        metaDataContexts.getOptimizeContextFactory().getSchemaMetadatas().getSchemas().put(event.getSchemaName(), new FederateSchemaMetadata(event.getSchemaName(), metaData.getSchema().getTables()));
        metaDataContexts.getMetaDataMap().put(event.getSchemaName(), metaData);
        metaDataContexts = new StandardMetaDataContexts(distMetaDataPersistService,
                metaDataContexts.getMetaDataMap(), metaDataContexts.getGlobalRuleMetaData(), metaDataContexts.getExecutorEngine(), 
                metaDataContexts.getProps(), metaDataContexts.getOptimizeContextFactory());
        ShardingSphereEventBus.getInstance().post(new DataSourceChangeCompletedEvent(event.getSchemaName(), 
                metaDataContexts.getMetaDataMap().get(event.getSchemaName()).getResource().getDatabaseType(), metaData.getResource().getDataSources()));
    }
    
    /**
     * Renew to delete schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent event) {
        String schemaName = event.getSchemaName();
        closeDataSources(schemaName, metaDataContexts.getMetaData(schemaName).getResource().getDataSources().values());
        Map<String, ShardingSphereMetaData> schemaMetaData = new HashMap<>(metaDataContexts.getMetaDataMap());
        schemaMetaData.remove(schemaName);
        metaDataContexts.getOptimizeContextFactory().getSchemaMetadatas().getSchemas().remove(schemaName);
        metaDataContexts = new StandardMetaDataContexts(distMetaDataPersistService,
                schemaMetaData, metaDataContexts.getGlobalRuleMetaData(), metaDataContexts.getExecutorEngine(), metaDataContexts.getProps(), metaDataContexts.getOptimizeContextFactory());
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
        metaDataContexts = new StandardMetaDataContexts(distMetaDataPersistService, 
                metaDataContexts.getMetaDataMap(), metaDataContexts.getGlobalRuleMetaData(), metaDataContexts.getExecutorEngine(), props, metaDataContexts.getOptimizeContextFactory());
    }
    
    /**
     * Renew authority.
     *
     * @param event authority changed event
     */
    @Subscribe
    public synchronized void renew(final AuthorityChangedEvent event) {
        Optional<AuthorityRule> rule = metaDataContexts.getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof AuthorityRule).findAny().map(each -> (AuthorityRule) each);
        rule.ifPresent(optional -> optional.refresh(metaDataContexts.getMetaDataMap(), event.getUsers()));
    }
    
    /**
     * Renew meta data of the schema.
     *
     * @param event meta data changed event
     */
    @Subscribe
    public synchronized void renew(final SchemaChangedEvent event) {
        try {
            Map<String, ShardingSphereMetaData> schemaMetaData = new HashMap<>(metaDataContexts.getMetaDataMap().size(), 1);
            for (Entry<String, ShardingSphereMetaData> entry : metaDataContexts.getMetaDataMap().entrySet()) {
                String schemaName = entry.getKey();
                ShardingSphereMetaData originalMetaData = entry.getValue();
                ShardingSphereMetaData metaData = event.getSchemaName().equals(schemaName) ? getChangedMetaData(originalMetaData, event.getSchema(), schemaName) : originalMetaData;
                schemaMetaData.put(schemaName, metaData);
                metaDataContexts.getOptimizeContextFactory().getSchemaMetadatas().getSchemas().put(event.getSchemaName(),
                        new FederateSchemaMetadata(event.getSchemaName(), metaData.getSchema().getTables()));
            }
            metaDataContexts = new StandardMetaDataContexts(distMetaDataPersistService,
                    schemaMetaData, metaDataContexts.getGlobalRuleMetaData(), metaDataContexts.getExecutorEngine(), metaDataContexts.getProps(), metaDataContexts.getOptimizeContextFactory());
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
        ShardingSphereMetaData metaData = getChangedMetaData(metaDataContexts.getMetaDataMap().get(schemaName), event.getRuleConfigurations());
        Map<String, ShardingSphereMetaData> schemaMetaData = rebuildSchemaMetaData(schemaName, metaData);
        metaDataContexts = new StandardMetaDataContexts(distMetaDataPersistService, schemaMetaData, metaDataContexts.getGlobalRuleMetaData(), metaDataContexts.getExecutorEngine(),
                metaDataContexts.getProps(), metaDataContexts.getOptimizeContextFactory());
        distMetaDataPersistService.getSchemaMetaDataService().persist(schemaName, schemaMetaData.get(schemaName).getSchema());
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
        ShardingSphereMetaData metaData = rebuildMetaData(metaDataContexts.getMetaDataMap().get(schemaName), event.getDataSourceConfigurations());
        Map<String, ShardingSphereMetaData> schemaMetaData = rebuildSchemaMetaData(schemaName, metaData);
        metaDataContexts = new StandardMetaDataContexts(distMetaDataPersistService, schemaMetaData, 
                metaDataContexts.getGlobalRuleMetaData(), metaDataContexts.getExecutorEngine(), metaDataContexts.getProps(), metaDataContexts.getOptimizeContextFactory());
        ShardingSphereEventBus.getInstance().post(new DataSourceChangeCompletedEvent(event.getSchemaName(),
                metaDataContexts.getMetaDataMap().get(event.getSchemaName()).getResource().getDatabaseType(), schemaMetaData.get(event.getSchemaName()).getResource().getDataSources()));
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
        Collection<ShardingSphereRule> rules = metaDataContexts.getMetaDataMap().get(governanceSchema.getSchemaName()).getRuleMetaData().getRules();
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
        Collection<ShardingSphereRule> rules = metaDataContexts.getMetaDataMap().get(governanceSchema.getSchemaName()).getRuleMetaData().getRules();
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
                    ShardingSphereRulesBuilder.buildGlobalRules(newGlobalConfigs, metaDataContexts.getMetaDataMap()));
            metaDataContexts = new StandardMetaDataContexts(distMetaDataPersistService, metaDataContexts.getMetaDataMap(), newGlobalRuleMetaData, metaDataContexts.getExecutorEngine(),
                    metaDataContexts.getProps(), metaDataContexts.getOptimizeContextFactory());
        }
    }

    private Map<String, ShardingSphereMetaData> rebuildSchemaMetaData(final String schemaName, final ShardingSphereMetaData metaData) {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(metaDataContexts.getMetaDataMap());
        result.put(schemaName, metaData);
        metaDataContexts.getOptimizeContextFactory().getSchemaMetadatas().getSchemas().put(schemaName, new FederateSchemaMetadata(schemaName, metaData.getSchema().getTables()));
        return result;
    }
    
    private void persistSchema(final String schemaName) {
        if (!distMetaDataPersistService.getDataSourceService().isExisted(schemaName)) {
            distMetaDataPersistService.getDataSourceService().persist(schemaName, new LinkedHashMap<>());
        }
        if (!distMetaDataPersistService.getSchemaRuleService().isExisted(schemaName)) {
            distMetaDataPersistService.getSchemaRuleService().persist(schemaName, new LinkedList<>());
        }
    }
    
    private ShardingSphereMetaData buildMetaData(final String schemaName) throws SQLException {
        Map<String, Map<String, DataSource>> dataSourcesMap = createDataSourcesMap(Collections.singletonMap(schemaName, distMetaDataPersistService.getDataSourceService().load(schemaName)));
        return new MetaDataContextsBuilder(dataSourcesMap,
                Collections.singletonMap(schemaName, distMetaDataPersistService.getSchemaRuleService().load(schemaName)),
                distMetaDataPersistService.getGlobalRuleService().load(), 
                metaDataContexts.getProps().getProps()).build(distMetaDataPersistService).getMetaData(schemaName);
    }
    
    private ShardingSphereMetaData getChangedMetaData(final ShardingSphereMetaData originalMetaData, final ShardingSphereSchema schema, final String schemaName) {
        // TODO refresh table addressing mapper
        return new ShardingSphereMetaData(schemaName, originalMetaData.getResource(), originalMetaData.getRuleMetaData(), schema);
    }
    
    private ShardingSphereMetaData getChangedMetaData(final ShardingSphereMetaData originalMetaData, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        MetaDataContextsBuilder builder = new MetaDataContextsBuilder(Collections.singletonMap(originalMetaData.getName(), originalMetaData.getResource().getDataSources()),
                Collections.singletonMap(originalMetaData.getName(), ruleConfigs), distMetaDataPersistService.getGlobalRuleService().load(), metaDataContexts.getProps().getProps());
        return builder.build(distMetaDataPersistService).getMetaDataMap().values().iterator().next();
    }
    
    private ShardingSphereMetaData rebuildMetaData(final ShardingSphereMetaData originalMetaData, final Map<String, DataSourceConfiguration> newDataSourceConfigs) throws SQLException {
        Collection<String> deletedDataSources = getDeletedDataSources(originalMetaData, newDataSourceConfigs).keySet();
        Map<String, DataSource> changedDataSources = buildChangedDataSources(originalMetaData, newDataSourceConfigs);
        Map<String, Map<String, DataSource>> dataSourcesMap = Collections.singletonMap(originalMetaData.getName(), 
                getNewDataSources(originalMetaData.getResource().getDataSources(), getAddedDataSources(originalMetaData, newDataSourceConfigs), changedDataSources, deletedDataSources));
        return new MetaDataContextsBuilder(dataSourcesMap, Collections.singletonMap(originalMetaData.getName(),
                originalMetaData.getRuleMetaData().getConfigurations()), distMetaDataPersistService.getGlobalRuleService().load(),
                metaDataContexts.getProps().getProps()).build(distMetaDataPersistService).getMetaData(originalMetaData.getName());
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
    
    private Map<String, Map<String, DataSource>> createDataSourcesMap(final Map<String, Map<String, DataSourceConfiguration>> dataSourcesConfigs) {
        return dataSourcesConfigs.entrySet().stream().collect(Collectors.toMap(Entry::getKey, 
            entry -> DataSourceConverter.getDataSourceMap(entry.getValue())));
    }
    
    private Collection<DataSource> getPendingClosedDataSources(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        Collection<DataSource> result = new LinkedList<>();
        result.addAll(getDeletedDataSources(metaDataContexts.getMetaData(schemaName), dataSourceConfigurations).values());
        result.addAll(getChangedDataSources(metaDataContexts.getMetaData(schemaName), dataSourceConfigurations).values());
        return result;
    }
    
    private void closeDataSources(final String schemaName, final Collection<DataSource> dataSources) {
        ShardingSphereResource resource = metaDataContexts.getMetaData(schemaName).getResource();
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
    
    @Override
    public Optional<DistMetaDataPersistService> getDistMetaDataPersistService() {
        return Optional.of(distMetaDataPersistService);
    }
    
    @Override
    public Collection<String> getAllSchemaNames() {
        return metaDataContexts.getAllSchemaNames();
    }
    
    @Override
    public Map<String, ShardingSphereMetaData> getMetaDataMap() {
        return metaDataContexts.getMetaDataMap();
    }
    
    @Override
    public ShardingSphereMetaData getMetaData(final String schemaName) {
        return metaDataContexts.getMetaData(schemaName);
    }
    
    @Override
    public ShardingSphereRuleMetaData getGlobalRuleMetaData() {
        return metaDataContexts.getGlobalRuleMetaData();
    }
    
    @Override
    public ExecutorEngine getExecutorEngine() {
        return metaDataContexts.getExecutorEngine();
    }
    
    @Override
    public OptimizeContextFactory getOptimizeContextFactory() {
        return metaDataContexts.getOptimizeContextFactory();
    }
    
    @Override
    public ConfigurationProperties getProps() {
        return metaDataContexts.getProps();
    }
    
    @Override
    public Optional<ShardingSphereLock> getLock() {
        return Optional.ofNullable(lock);
    }
    
    @Override
    public StateContext getStateContext() {
        return metaDataContexts.getStateContext();
    }
    
    @Override
    public void close() {
        metaDataContexts.close();
    }
}
