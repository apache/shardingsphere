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
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.governance.core.event.model.auth.AuthenticationChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceChangeCompletedEvent;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataAddedEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataDeletedEvent;
import org.apache.shardingsphere.governance.core.event.model.props.PropertiesChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaChangedEvent;
import org.apache.shardingsphere.governance.core.facade.GovernanceFacade;
import org.apache.shardingsphere.governance.core.registry.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.schema.GovernanceSchema;
import org.apache.shardingsphere.infra.auth.MemoryAuthentication;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.rule.type.StatusContainedRule;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Governance meta data contexts.
 */
public final class GovernanceMetaDataContexts implements MetaDataContexts {
    
    private final GovernanceFacade governanceFacade;
    
    private volatile MetaDataContexts metaDataContexts;
    
    public GovernanceMetaDataContexts(final MetaDataContexts metaDataContexts, final GovernanceFacade governanceFacade) {
        this.governanceFacade = governanceFacade;
        this.metaDataContexts = metaDataContexts;
        ShardingSphereEventBus.getInstance().register(this);
        disableDataSources();
        persistMetaData();
    }
    
    private void disableDataSources() {
        metaDataContexts.getMetaDataMap().forEach((key, value)
            -> value.getRuleMetaData().getRules().stream().filter(each -> each instanceof StatusContainedRule).forEach(each -> disableDataSources(key, (StatusContainedRule) each)));
    }
    
    private void disableDataSources(final String schemaName, final StatusContainedRule rule) {
        Collection<String> disabledDataSources = governanceFacade.getRegistryCenter().loadDisabledDataSources(schemaName);
        disabledDataSources.stream().map(this::getDataSourceName).forEach(each -> rule.updateRuleStatus(new DataSourceNameDisabledEvent(each, true)));
    }
    
    private String getDataSourceName(final String disabledDataSource) {
        return new GovernanceSchema(disabledDataSource).getDataSourceName();
    }
    
    private void persistMetaData() {
        metaDataContexts.getMetaDataMap().forEach((key, value) -> governanceFacade.getConfigCenter().persistSchema(key, value.getSchema()));
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return metaDataContexts.getDatabaseType();
    }
    
    private DatabaseType getDatabaseType(final Map<String, Map<String, DataSource>> dataSourcesMap) throws SQLException {
        if (dataSourcesMap.isEmpty() || dataSourcesMap.values().iterator().next().isEmpty()) {
            return metaDataContexts.getDatabaseType();
        }
        DataSource dataSource = dataSourcesMap.values().iterator().next().values().iterator().next();
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseTypeRegistry.getDatabaseTypeByURL(connection.getMetaData().getURL());
        }
    }
    
    @Override
    public Map<String, ShardingSphereMetaData> getMetaDataMap() {
        return metaDataContexts.getMetaDataMap();
    }
    
    @Override
    public ShardingSphereMetaData getDefaultMetaData() {
        return metaDataContexts.getDefaultMetaData();
    }
    
    @Override
    public ExecutorEngine getExecutorEngine() {
        return metaDataContexts.getExecutorEngine();
    }
    
    @Override
    public MemoryAuthentication getAuthentication() {
        return metaDataContexts.getAuthentication();
    }
    
    @Override
    public ConfigurationProperties getProps() {
        return metaDataContexts.getProps();
    }
    
    @Override
    public void close() throws IOException {
        metaDataContexts.close();
        governanceFacade.close();
    }
    
    /**
     * Renew to add new schema.
     *
     * @param event schema add event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void renew(final MetaDataAddedEvent event) throws SQLException {
        Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(metaDataContexts.getMetaDataMap());
        metaDataMap.put(event.getSchemaName(), createAddedMetaData(event));
        metaDataContexts = new StandardMetaDataContexts(
                metaDataMap, metaDataContexts.getExecutorEngine(), metaDataContexts.getAuthentication(), metaDataContexts.getProps(), metaDataContexts.getDatabaseType());
        governanceFacade.getConfigCenter().persistSchema(event.getSchemaName(), metaDataContexts.getMetaDataMap().get(event.getSchemaName()).getSchema());
        ShardingSphereEventBus.getInstance().post(
                new DataSourceChangeCompletedEvent(event.getSchemaName(), metaDataContexts.getDatabaseType(), metaDataMap.get(event.getSchemaName()).getResource().getDataSources()));
    }
    
    /**
     * Renew to delete new schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final MetaDataDeletedEvent event) {
        Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(metaDataContexts.getMetaDataMap());
        metaDataMap.remove(event.getSchemaName());
        metaDataContexts = new StandardMetaDataContexts(
                metaDataMap, metaDataContexts.getExecutorEngine(), metaDataContexts.getAuthentication(), metaDataContexts.getProps(), metaDataContexts.getDatabaseType());
        governanceFacade.getConfigCenter().deleteSchema(event.getSchemaName());
    }
    
    /**
     * Renew properties.
     *
     * @param event properties changed event
     */
    @Subscribe
    public synchronized void renew(final PropertiesChangedEvent event) {
        ConfigurationProperties props = new ConfigurationProperties(event.getProps());
        metaDataContexts = new StandardMetaDataContexts(getChangedMataDataMap(), metaDataContexts.getExecutorEngine(), metaDataContexts.getAuthentication(), props, metaDataContexts.getDatabaseType());
    }
    
    /**
     * Renew authentication.
     *
     * @param event authentication changed event
     */
    @Subscribe
    public synchronized void renew(final AuthenticationChangedEvent event) {
        metaDataContexts = new StandardMetaDataContexts(
                metaDataContexts.getMetaDataMap(), metaDataContexts.getExecutorEngine(), event.getAuthentication(), metaDataContexts.getProps(), metaDataContexts.getDatabaseType());
    }
    
    /**
     * Renew meta data of the schema.
     *
     * @param event meta data changed event
     */
    @Subscribe
    public synchronized void renew(final SchemaChangedEvent event) {
        try {
            Map<String, ShardingSphereMetaData> newMetaDataMap = new HashMap<>(metaDataContexts.getMetaDataMap().size(), 1);
            for (Entry<String, ShardingSphereMetaData> entry : metaDataContexts.getMetaDataMap().entrySet()) {
                String schemaName = entry.getKey();
                ShardingSphereMetaData oldMetaData = entry.getValue();
                ShardingSphereMetaData newMetaData = event.getSchemaName().equals(schemaName) ? getChangedMetaData(oldMetaData, event.getSchema(), schemaName) : oldMetaData;
                newMetaDataMap.put(schemaName, newMetaData);
            }
            metaDataContexts = new StandardMetaDataContexts(
                    newMetaDataMap, metaDataContexts.getExecutorEngine(), metaDataContexts.getAuthentication(), metaDataContexts.getProps(), metaDataContexts.getDatabaseType());
        } finally {
            governanceFacade.getLockCenter().unlock();
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
        Map<String, ShardingSphereMetaData> newMetaDataMap = new HashMap<>(metaDataContexts.getMetaDataMap());
        String schemaName = event.getSchemaName();
        newMetaDataMap.remove(schemaName);
        newMetaDataMap.put(schemaName, getChangedMetaData(metaDataContexts.getMetaDataMap().get(schemaName), event.getRuleConfigurations()));
        metaDataContexts = new StandardMetaDataContexts(
                newMetaDataMap, metaDataContexts.getExecutorEngine(), metaDataContexts.getAuthentication(), metaDataContexts.getProps(), metaDataContexts.getDatabaseType());
        governanceFacade.getConfigCenter().persistSchema(schemaName, newMetaDataMap.get(schemaName).getSchema());
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
        Map<String, ShardingSphereMetaData> newMetaDataMap = new HashMap<>(metaDataContexts.getMetaDataMap());
        newMetaDataMap.remove(schemaName);
        newMetaDataMap.put(schemaName, getChangedMetaData(metaDataContexts.getMetaDataMap().get(schemaName), event.getDataSourceConfigurations()));
        metaDataContexts = new StandardMetaDataContexts(
                newMetaDataMap, metaDataContexts.getExecutorEngine(), metaDataContexts.getAuthentication(), metaDataContexts.getProps(), metaDataContexts.getDatabaseType());
        ShardingSphereEventBus.getInstance().post(
                new DataSourceChangeCompletedEvent(event.getSchemaName(), metaDataContexts.getDatabaseType(), newMetaDataMap.get(event.getSchemaName()).getResource().getDataSources()));
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
    
    private ShardingSphereMetaData createAddedMetaData(final MetaDataAddedEvent event) throws SQLException {
        String schemaName = event.getSchemaName();
        Map<String, Map<String, DataSource>> dataSourcesMap = createDataSourcesMap(Collections.singletonMap(schemaName, 
                governanceFacade.getConfigCenter().loadDataSourceConfigurations(schemaName)));
        DatabaseType databaseType = getDatabaseType(dataSourcesMap);
        MetaDataContextsBuilder metaDataContextsBuilder = new MetaDataContextsBuilder(databaseType, dataSourcesMap, 
                Collections.singletonMap(schemaName, governanceFacade.getConfigCenter().loadRuleConfigurations(schemaName)), 
                metaDataContexts.getAuthentication(), metaDataContexts.getProps().getProps());
        return metaDataContextsBuilder.build().getMetaDataMap().get(schemaName);
    }
    
    private Map<String, ShardingSphereMetaData> getChangedMataDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(metaDataContexts.getMetaDataMap().size());
        for (Entry<String, ShardingSphereMetaData> entry : metaDataContexts.getMetaDataMap().entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private ShardingSphereMetaData getChangedMetaData(final ShardingSphereMetaData oldMetaData, final ShardingSphereSchema schema, final String schemaName) {
        // TODO refresh table addressing mapper
        return new ShardingSphereMetaData(schemaName, oldMetaData.getResource(), oldMetaData.getRuleMetaData(), schema);
    }
    
    private ShardingSphereMetaData getChangedMetaData(final ShardingSphereMetaData oldMetaData, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        MetaDataContextsBuilder builder = new MetaDataContextsBuilder(metaDataContexts.getDatabaseType(), Collections.singletonMap(oldMetaData.getName(), oldMetaData.getResource().getDataSources()),
                Collections.singletonMap(oldMetaData.getName(), ruleConfigs), metaDataContexts.getAuthentication(), metaDataContexts.getProps().getProps());
        return builder.build().getMetaDataMap().values().iterator().next();
    }
    
    private ShardingSphereMetaData getChangedMetaData(final ShardingSphereMetaData oldMetaData, final Map<String, DataSourceConfiguration> newDataSourceConfigs) throws SQLException {
        Collection<String> deletedDataSources = getDeletedDataSources(oldMetaData, newDataSourceConfigs);
        Map<String, DataSource> modifiedDataSources = getModifiedDataSources(oldMetaData, newDataSourceConfigs);
        oldMetaData.getResource().close(deletedDataSources);
        oldMetaData.getResource().close(modifiedDataSources.keySet());
        Map<String, Map<String, DataSource>> dataSourcesMap = Collections.singletonMap(oldMetaData.getName(), 
                getNewDataSources(oldMetaData.getResource().getDataSources(), getAddedDataSources(oldMetaData, newDataSourceConfigs), modifiedDataSources, deletedDataSources));
        return new MetaDataContextsBuilder(metaDataContexts.getDatabaseType(), dataSourcesMap,
                Collections.singletonMap(oldMetaData.getName(), oldMetaData.getRuleMetaData().getConfigurations()), metaDataContexts.getAuthentication(), 
                metaDataContexts.getProps().getProps()).build().getMetaDataMap().get(oldMetaData.getName());
    }
    
    private Map<String, DataSource> getNewDataSources(final Map<String, DataSource> oldDataSources, 
                                                      final Map<String, DataSource> addedDataSources, final Map<String, DataSource> modifiedDataSources, final Collection<String> deletedDataSources) {
        Map<String, DataSource> result = new LinkedHashMap<>(oldDataSources);
        result.keySet().removeAll(deletedDataSources);
        result.keySet().removeAll(modifiedDataSources.keySet());
        result.putAll(modifiedDataSources);
        result.putAll(addedDataSources);
        return result;
    }
    
    private Collection<String> getDeletedDataSources(final ShardingSphereMetaData oldMetaData, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        Collection<String> result = new LinkedList<>(oldMetaData.getResource().getDataSources().keySet());
        result.removeAll(newDataSourceConfigs.keySet());
        return result;
    }
    
    private Map<String, DataSource> getAddedDataSources(final ShardingSphereMetaData oldMetaData, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        return DataSourceConverter.getDataSourceMap(Maps.filterKeys(newDataSourceConfigs, each -> !oldMetaData.getResource().getDataSources().containsKey(each)));
    }
    
    private Map<String, DataSource> getModifiedDataSources(final ShardingSphereMetaData oldMetaData, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        Map<String, DataSourceConfiguration> modifiedDataSourceConfigs = newDataSourceConfigs.entrySet().stream()
                .filter(entry -> isModifiedDataSource(oldMetaData.getResource().getDataSources(), entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        return DataSourceConverter.getDataSourceMap(modifiedDataSourceConfigs);
    }
    
    private boolean isModifiedDataSource(final Map<String, DataSource> oldDataSources, final String newDataSourceName, final DataSourceConfiguration newDataSourceConfig) {
        DataSourceConfiguration dataSourceConfig = DataSourceConverter.getDataSourceConfigurationMap(oldDataSources).get(newDataSourceName);
        return null != dataSourceConfig && !newDataSourceConfig.equals(dataSourceConfig);
    }
    
    private Map<String, Map<String, DataSource>> createDataSourcesMap(final Map<String, Map<String, DataSourceConfiguration>> dataSourcesConfigs) {
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(dataSourcesConfigs.size(), 1);
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : dataSourcesConfigs.entrySet()) {
            result.put(entry.getKey(), DataSourceConverter.getDataSourceMap(entry.getValue()));
        }
        return result;
    }
}
