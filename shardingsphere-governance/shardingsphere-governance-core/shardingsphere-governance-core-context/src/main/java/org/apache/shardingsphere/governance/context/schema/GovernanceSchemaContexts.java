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

package org.apache.shardingsphere.governance.context.schema;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.event.GovernanceEventBus;
import org.apache.shardingsphere.governance.core.event.model.auth.AuthenticationChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceChangeCompletedEvent;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.props.PropertiesChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaAddedEvent;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.governance.core.facade.GovernanceFacade;
import org.apache.shardingsphere.governance.core.registry.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.schema.GovernanceSchema;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.schema.SchemaContexts;
import org.apache.shardingsphere.infra.context.schema.SchemaContextsBuilder;
import org.apache.shardingsphere.infra.context.schema.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.StatusContainedRule;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

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
 * Governance schema contexts.
 */
public final class GovernanceSchemaContexts implements SchemaContexts {
    
    private final GovernanceFacade governanceFacade;
    
    private volatile SchemaContexts schemaContexts;
    
    public GovernanceSchemaContexts(final SchemaContexts schemaContexts, final GovernanceFacade governanceFacade) {
        this.governanceFacade = governanceFacade;
        this.schemaContexts = schemaContexts;
        GovernanceEventBus.getInstance().register(this);
        disableDataSources();
        persistMetaData();
    }
    
    private void disableDataSources() {
        schemaContexts.getMetaDataMap().forEach((key, value)
            -> value.getRules().stream().filter(each -> each instanceof StatusContainedRule).forEach(each -> disableDataSources(key, (StatusContainedRule) each)));
    }
    
    private void disableDataSources(final String schemaName, final StatusContainedRule rule) {
        Collection<String> disabledDataSources = governanceFacade.getRegistryCenter().loadDisabledDataSources(schemaName);
        disabledDataSources.stream().map(this::getDataSourceName).forEach(each -> rule.updateRuleStatus(new DataSourceNameDisabledEvent(each, true)));
    }
    
    private String getDataSourceName(final String disabledDataSource) {
        return new GovernanceSchema(disabledDataSource).getDataSourceName();
    }
    
    private void persistMetaData() {
        schemaContexts.getMetaDataMap().forEach((key, value) -> governanceFacade.getConfigCenter().persistMetaData(key, value.getSchema().getSchemaMetaData()));
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return schemaContexts.getDatabaseType();
    }
    
    private DatabaseType getDatabaseType(final Map<String, Map<String, DataSource>> dataSourcesMap) throws SQLException {
        if (dataSourcesMap.isEmpty() || dataSourcesMap.values().iterator().next().isEmpty()) {
            return schemaContexts.getDatabaseType();
        }
        DataSource dataSource = dataSourcesMap.values().iterator().next().values().iterator().next();
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseTypeRegistry.getDatabaseTypeByURL(connection.getMetaData().getURL());
        }
    }
    
    @Override
    public Map<String, ShardingSphereMetaData> getMetaDataMap() {
        return schemaContexts.getMetaDataMap();
    }
    
    @Override
    public ShardingSphereMetaData getDefaultMetaData() {
        return schemaContexts.getDefaultMetaData();
    }
    
    @Override
    public ExecutorKernel getExecutorKernel() {
        return schemaContexts.getExecutorKernel();
    }
    
    @Override
    public Authentication getAuthentication() {
        return schemaContexts.getAuthentication();
    }
    
    @Override
    public ConfigurationProperties getProps() {
        return schemaContexts.getProps();
    }
    
    @Override
    public boolean isCircuitBreak() {
        return schemaContexts.isCircuitBreak();
    }
    
    @Override
    public void close() throws IOException {
        schemaContexts.close();
        governanceFacade.close();
    }
    
    /**
     * Renew to add new schema.
     *
     * @param event schema add event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent event) throws SQLException {
        Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(schemaContexts.getMetaDataMap());
        metaDataMap.put(event.getSchemaName(), createAddedMetaData(event));
        schemaContexts = new StandardSchemaContexts(metaDataMap, schemaContexts.getExecutorKernel(), schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
        governanceFacade.getConfigCenter().persistMetaData(event.getSchemaName(), schemaContexts.getMetaDataMap().get(event.getSchemaName()).getSchema().getSchemaMetaData());
        GovernanceEventBus.getInstance().post(
                new DataSourceChangeCompletedEvent(event.getSchemaName(), schemaContexts.getDatabaseType(), metaDataMap.get(event.getSchemaName()).getDataSources()));
    }
    
    /**
     * Renew to delete new schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent event) {
        Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(schemaContexts.getMetaDataMap());
        metaDataMap.remove(event.getSchemaName());
        schemaContexts = new StandardSchemaContexts(metaDataMap, schemaContexts.getExecutorKernel(), schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
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
        schemaContexts = new StandardSchemaContexts(getChangedMataDataMap(), schemaContexts.getExecutorKernel(), schemaContexts.getAuthentication(), props, schemaContexts.getDatabaseType());
    }
    
    /**
     * Renew authentication.
     *
     * @param event authentication changed event
     */
    @Subscribe
    public synchronized void renew(final AuthenticationChangedEvent event) {
        schemaContexts = new StandardSchemaContexts(
                schemaContexts.getMetaDataMap(), schemaContexts.getExecutorKernel(), event.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
    }
    
    /**
     * Renew meta data of the schema.
     *
     * @param event meta data changed event
     */
    @Subscribe
    public synchronized void renew(final MetaDataChangedEvent event) {
        Map<String, ShardingSphereMetaData> newMetaDataMap = new HashMap<>(schemaContexts.getMetaDataMap().size(), 1);
        for (Entry<String, ShardingSphereMetaData> entry : schemaContexts.getMetaDataMap().entrySet()) {
            String schemaName = entry.getKey();
            ShardingSphereMetaData oldMetaData = entry.getValue();
            ShardingSphereMetaData newMetaData = event.getSchemaName().equals(schemaName) ? getChangedMetaData(oldMetaData, event.getMetaData(), schemaName) : oldMetaData;
            newMetaDataMap.put(schemaName, newMetaData);
        }
        schemaContexts = new StandardSchemaContexts(
                newMetaDataMap, schemaContexts.getExecutorKernel(), schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
    }
    
    /**
     * Renew rule configurations.
     *
     * @param event rule configurations changed event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsChangedEvent event) throws SQLException {
        Map<String, ShardingSphereMetaData> newMetaDataMap = new HashMap<>(schemaContexts.getMetaDataMap());
        String schemaName = event.getSchemaName();
        newMetaDataMap.remove(schemaName);
        newMetaDataMap.put(schemaName, getChangedMetaData(schemaContexts.getMetaDataMap().get(schemaName), event.getRuleConfigurations()));
        schemaContexts = new StandardSchemaContexts(
                newMetaDataMap, schemaContexts.getExecutorKernel(), schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
        governanceFacade.getConfigCenter().persistMetaData(schemaName, newMetaDataMap.get(schemaName).getSchema().getSchemaMetaData());
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
        Map<String, ShardingSphereMetaData> newMetaDataMap = new HashMap<>(schemaContexts.getMetaDataMap());
        newMetaDataMap.remove(schemaName);
        newMetaDataMap.put(schemaName, getChangedMetaData(schemaContexts.getMetaDataMap().get(schemaName), event.getDataSourceConfigurations()));
        schemaContexts = new StandardSchemaContexts(
                newMetaDataMap, schemaContexts.getExecutorKernel(), schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
        GovernanceEventBus.getInstance().post(
                new DataSourceChangeCompletedEvent(event.getSchemaName(), schemaContexts.getDatabaseType(), newMetaDataMap.get(event.getSchemaName()).getDataSources()));
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param event disabled state changed event
     */
    @Subscribe
    public synchronized void renew(final DisabledStateChangedEvent event) {
        GovernanceSchema governanceSchema = event.getGovernanceSchema();
        Collection<ShardingSphereRule> rules = schemaContexts.getMetaDataMap().get(governanceSchema.getSchemaName()).getRules();
        for (ShardingSphereRule each : rules) {
            if (each instanceof StatusContainedRule) {
                ((StatusContainedRule) each).updateRuleStatus(new DataSourceNameDisabledEvent(governanceSchema.getDataSourceName(), event.isDisabled()));
            }
        }
    }
    
    /**
     * Renew circuit breaker state.
     *
     * @param event circuit state changed event
     */
    @Subscribe
    public synchronized void renew(final CircuitStateChangedEvent event) {
        schemaContexts = new StandardSchemaContexts(schemaContexts.getMetaDataMap(), 
                schemaContexts.getExecutorKernel(), schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType(), event.isCircuitBreak());
    }
    
    private ShardingSphereMetaData createAddedMetaData(final SchemaAddedEvent event) throws SQLException {
        String schemaName = event.getSchemaName();
        Map<String, Map<String, DataSource>> dataSourcesMap = createDataSourcesMap(Collections.singletonMap(schemaName, 
                governanceFacade.getConfigCenter().loadDataSourceConfigurations(schemaName)));
        DatabaseType databaseType = getDatabaseType(dataSourcesMap);
        SchemaContextsBuilder schemaContextsBuilder = new SchemaContextsBuilder(databaseType, dataSourcesMap, 
                Collections.singletonMap(schemaName, governanceFacade.getConfigCenter().loadRuleConfigurations(schemaName)), schemaContexts.getAuthentication(), schemaContexts.getProps().getProps());
        return schemaContextsBuilder.build().getMetaDataMap().get(schemaName);
    }
    
    private Map<String, ShardingSphereMetaData> getChangedMataDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(schemaContexts.getMetaDataMap().size());
        for (Entry<String, ShardingSphereMetaData> entry : schemaContexts.getMetaDataMap().entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private ShardingSphereMetaData getChangedMetaData(final ShardingSphereMetaData oldMetaData, final PhysicalSchemaMetaData newSchemaMetaData, final String schemaName) {
        // TODO refresh tableAddressingMetaData
        ShardingSphereSchema schema = new ShardingSphereSchema(
                oldMetaData.getSchema().getDataSourcesMetaData(), oldMetaData.getSchema().getTableAddressingMetaData(), newSchemaMetaData, oldMetaData.getSchema().getCachedDatabaseMetaData());
        return new ShardingSphereMetaData(schemaName, oldMetaData.getConfigurations(), oldMetaData.getRules(), oldMetaData.getDataSources(), schema);
    }
    
    private ShardingSphereMetaData getChangedMetaData(final ShardingSphereMetaData oldMetaData, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        SchemaContextsBuilder builder = new SchemaContextsBuilder(schemaContexts.getDatabaseType(), Collections.singletonMap(oldMetaData.getName(), oldMetaData.getDataSources()),
                Collections.singletonMap(oldMetaData.getName(), ruleConfigs), schemaContexts.getAuthentication(), schemaContexts.getProps().getProps());
        return builder.build().getMetaDataMap().values().iterator().next();
    }
    
    private ShardingSphereMetaData getChangedMetaData(final ShardingSphereMetaData oldMetaData, final Map<String, DataSourceConfiguration> newDataSourceConfigs) throws SQLException {
        Collection<String> deletedDataSources = getDeletedDataSources(oldMetaData, newDataSourceConfigs);
        Map<String, DataSource> modifiedDataSources = getModifiedDataSources(oldMetaData, newDataSourceConfigs);
        oldMetaData.closeDataSources(deletedDataSources);
        oldMetaData.closeDataSources(modifiedDataSources.keySet());
        Map<String, Map<String, DataSource>> dataSourcesMap = Collections.singletonMap(oldMetaData.getName(), 
                getNewDataSources(oldMetaData.getDataSources(), getAddedDataSources(oldMetaData, newDataSourceConfigs), modifiedDataSources, deletedDataSources));
        return new SchemaContextsBuilder(schemaContexts.getDatabaseType(), dataSourcesMap,
                Collections.singletonMap(oldMetaData.getName(), oldMetaData.getConfigurations()), schemaContexts.getAuthentication(), 
                schemaContexts.getProps().getProps()).build().getMetaDataMap().get(oldMetaData.getName());
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
        Collection<String> result = new LinkedList<>(oldMetaData.getDataSources().keySet());
        result.removeAll(newDataSourceConfigs.keySet());
        return result;
    }
    
    private Map<String, DataSource> getAddedDataSources(final ShardingSphereMetaData oldMetaData, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        return DataSourceConverter.getDataSourceMap(Maps.filterKeys(newDataSourceConfigs, each -> !oldMetaData.getDataSources().containsKey(each)));
    }
    
    private Map<String, DataSource> getModifiedDataSources(final ShardingSphereMetaData oldMetaData, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        Map<String, DataSourceConfiguration> modifiedDataSourceConfigs = newDataSourceConfigs.entrySet().stream()
                .filter(entry -> isModifiedDataSource(oldMetaData.getDataSources(), entry.getKey(), entry.getValue()))
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
