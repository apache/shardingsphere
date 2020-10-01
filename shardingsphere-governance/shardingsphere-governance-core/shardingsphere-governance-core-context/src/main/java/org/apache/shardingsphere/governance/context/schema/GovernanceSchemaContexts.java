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
import org.apache.shardingsphere.governance.core.event.model.auth.AuthenticationChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceChangeCompletedEvent;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.props.PropertiesChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaAddedEvent;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.governance.core.event.GovernanceEventBus;
import org.apache.shardingsphere.governance.core.facade.GovernanceFacade;
import org.apache.shardingsphere.governance.core.registry.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.governance.core.registry.schema.GovernanceSchema;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.schema.SchemaContext;
import org.apache.shardingsphere.infra.context.schema.SchemaContexts;
import org.apache.shardingsphere.infra.context.schema.SchemaContextsBuilder;
import org.apache.shardingsphere.infra.context.schema.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.context.schema.runtime.RuntimeContext;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.StatusContainedRule;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;

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
        schemaContexts.getSchemaContextMap().forEach((key, value)
            -> value.getSchema().getRules().stream().filter(each -> each instanceof StatusContainedRule).forEach(each -> disableDataSources(key, (StatusContainedRule) each)));
    }
    
    private void disableDataSources(final String schemaName, final StatusContainedRule rule) {
        Collection<String> disabledDataSources = governanceFacade.getRegistryCenter().loadDisabledDataSources(schemaName);
        disabledDataSources.stream().map(this::getDataSourceName).forEach(each -> rule.updateRuleStatus(new DataSourceNameDisabledEvent(each, true)));
    }
    
    private String getDataSourceName(final String disabledDataSource) {
        return new GovernanceSchema(disabledDataSource).getDataSourceName();
    }
    
    private void persistMetaData() {
        schemaContexts.getSchemaContextMap().forEach((key, value) -> governanceFacade.getConfigCenter()
            .persistMetaData(key, value.getSchema().getMetaData().getRuleSchemaMetaData()));
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
            return DatabaseTypes.getDatabaseTypeByURL(connection.getMetaData().getURL());
        }
    }
    
    @Override
    public Map<String, SchemaContext> getSchemaContextMap() {
        return schemaContexts.getSchemaContextMap();
    }
    
    @Override
    public SchemaContext getDefaultSchemaContext() {
        return schemaContexts.getDefaultSchemaContext();
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
        Map<String, SchemaContext> schemas = new HashMap<>(schemaContexts.getSchemaContextMap());
        schemas.put(event.getSchemaName(), createAddedSchemaContext(event));
        schemaContexts = new StandardSchemaContexts(schemas, schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
        governanceFacade.getConfigCenter().persistMetaData(event.getSchemaName(), 
                schemaContexts.getSchemaContextMap().get(event.getSchemaName()).getSchema().getMetaData().getRuleSchemaMetaData());
        GovernanceEventBus.getInstance().post(
                new DataSourceChangeCompletedEvent(event.getSchemaName(), schemaContexts.getDatabaseType(), schemas.get(event.getSchemaName()).getSchema().getDataSources()));
    }
    
    /**
     * Renew to delete new schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent event) {
        Map<String, SchemaContext> schemas = new HashMap<>(schemaContexts.getSchemaContextMap());
        schemas.remove(event.getSchemaName());
        schemaContexts = new StandardSchemaContexts(schemas, schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
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
        schemaContexts = new StandardSchemaContexts(getChangedSchemaContexts(props), schemaContexts.getAuthentication(), props, schemaContexts.getDatabaseType());
    }
    
    /**
     * Renew authentication.
     *
     * @param event authentication changed event
     */
    @Subscribe
    public synchronized void renew(final AuthenticationChangedEvent event) {
        schemaContexts = new StandardSchemaContexts(schemaContexts.getSchemaContextMap(), event.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
    }
    
    /**
     * Renew meta data of the schema.
     *
     * @param event meta data changed event
     */
    @Subscribe
    public synchronized void renew(final MetaDataChangedEvent event) {
        Map<String, SchemaContext> newSchemaContexts = new HashMap<>(schemaContexts.getSchemaContextMap().size(), 1);
        for (Entry<String, SchemaContext> entry : schemaContexts.getSchemaContextMap().entrySet()) {
            String schemaName = entry.getKey();
            SchemaContext oldSchemaContext = entry.getValue();
            SchemaContext newSchemaContext = event.getSchemaNames().contains(schemaName) 
                    ? new SchemaContext(getChangedShardingSphereSchema(oldSchemaContext.getSchema(), event.getRuleSchemaMetaData(), schemaName),
                    oldSchemaContext.getRuntimeContext()) : oldSchemaContext;
            newSchemaContexts.put(schemaName, newSchemaContext);
        }
        schemaContexts = new StandardSchemaContexts(newSchemaContexts, schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
    }
    
    /**
     * Renew rule configurations.
     *
     * @param event rule configurations changed event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsChangedEvent event) throws SQLException {
        Map<String, SchemaContext> newSchemaContexts = new HashMap<>(schemaContexts.getSchemaContextMap());
        String schemaName = event.getSchemaName();
        newSchemaContexts.remove(schemaName);
        newSchemaContexts.put(schemaName, getChangedSchemaContext(schemaContexts.getSchemaContextMap().get(schemaName), event.getRuleConfigurations()));
        schemaContexts = new StandardSchemaContexts(newSchemaContexts, schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
        governanceFacade.getConfigCenter().persistMetaData(schemaName, newSchemaContexts.get(schemaName).getSchema().getMetaData().getRuleSchemaMetaData());
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
        Map<String, SchemaContext> newSchemaContexts = new HashMap<>(schemaContexts.getSchemaContextMap());
        newSchemaContexts.remove(schemaName);
        newSchemaContexts.put(schemaName, getChangedSchemaContext(schemaContexts.getSchemaContextMap().get(schemaName), event.getDataSourceConfigurations()));
        schemaContexts = new StandardSchemaContexts(newSchemaContexts, schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
        GovernanceEventBus.getInstance().post(
                new DataSourceChangeCompletedEvent(event.getSchemaName(), schemaContexts.getDatabaseType(), newSchemaContexts.get(event.getSchemaName()).getSchema().getDataSources()));
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param event disabled state changed event
     */
    @Subscribe
    public synchronized void renew(final DisabledStateChangedEvent event) {
        GovernanceSchema governanceSchema = event.getGovernanceSchema();
        Collection<ShardingSphereRule> rules = schemaContexts.getSchemaContextMap().get(governanceSchema.getSchemaName()).getSchema().getRules();
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
        schemaContexts = new StandardSchemaContexts(
                schemaContexts.getSchemaContextMap(), schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType(), event.isCircuitBreak());
    }
    
    private SchemaContext createAddedSchemaContext(final SchemaAddedEvent event) throws SQLException {
        String schemaName = event.getSchemaName();
        Map<String, Map<String, DataSource>> dataSourcesMap = createDataSourcesMap(Collections.singletonMap(schemaName, 
                governanceFacade.getConfigCenter().loadDataSourceConfigurations(schemaName)));
        DatabaseType databaseType = getDatabaseType(dataSourcesMap);
        SchemaContextsBuilder schemaContextsBuilder = new SchemaContextsBuilder(databaseType, dataSourcesMap, 
                Collections.singletonMap(schemaName, governanceFacade.getConfigCenter().loadRuleConfigurations(schemaName)), schemaContexts.getAuthentication(), schemaContexts.getProps().getProps());
        return schemaContextsBuilder.build().getSchemaContextMap().get(schemaName);
    }
    
    private Map<String, SchemaContext> getChangedSchemaContexts(final ConfigurationProperties props) {
        Map<String, SchemaContext> result = new HashMap<>(schemaContexts.getSchemaContextMap().size());
        for (Entry<String, SchemaContext> entry : schemaContexts.getSchemaContextMap().entrySet()) {
            RuntimeContext runtimeContext = entry.getValue().getRuntimeContext();
            result.put(entry.getKey(), new SchemaContext(entry.getValue().getSchema(), new RuntimeContext(runtimeContext.getCachedDatabaseMetaData(),
                    new ExecutorKernel(props.<Integer>getValue(ConfigurationPropertyKey.EXECUTOR_SIZE)), runtimeContext.getSqlParserEngine())));
        }
        return result;
    }
    
    private ShardingSphereSchema getChangedShardingSphereSchema(final ShardingSphereSchema oldShardingSphereSchema, final RuleSchemaMetaData newRuleSchemaMetaData, final String schemaName) {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(oldShardingSphereSchema.getMetaData().getDataSourceMetaDatas(), newRuleSchemaMetaData, schemaName);
        return new ShardingSphereSchema(schemaName, oldShardingSphereSchema.getConfigurations(), oldShardingSphereSchema.getRules(), oldShardingSphereSchema.getDataSources(), metaData);
    }
    
    private SchemaContext getChangedSchemaContext(final SchemaContext oldSchemaContext, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        ShardingSphereSchema oldSchema = oldSchemaContext.getSchema();
        SchemaContextsBuilder builder = new SchemaContextsBuilder(schemaContexts.getDatabaseType(), Collections.singletonMap(oldSchemaContext.getSchema().getName(), oldSchema.getDataSources()),
                Collections.singletonMap(oldSchemaContext.getSchema().getName(), ruleConfigs), schemaContexts.getAuthentication(), schemaContexts.getProps().getProps());
        return builder.build().getSchemaContextMap().values().iterator().next();
    }
    
    private SchemaContext getChangedSchemaContext(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSourceConfigs) throws SQLException {
        Collection<String> deletedDataSources = getDeletedDataSources(oldSchemaContext, newDataSourceConfigs);
        Map<String, DataSource> modifiedDataSources = getModifiedDataSources(oldSchemaContext, newDataSourceConfigs);
        oldSchemaContext.getSchema().closeDataSources(deletedDataSources);
        oldSchemaContext.getSchema().closeDataSources(modifiedDataSources.keySet());
        Map<String, Map<String, DataSource>> dataSourcesMap = Collections.singletonMap(oldSchemaContext.getSchema().getName(), 
                getNewDataSources(oldSchemaContext.getSchema().getDataSources(), getAddedDataSources(oldSchemaContext, newDataSourceConfigs), modifiedDataSources, deletedDataSources));
        return new SchemaContextsBuilder(schemaContexts.getDatabaseType(), dataSourcesMap,
                Collections.singletonMap(oldSchemaContext.getSchema().getName(), oldSchemaContext.getSchema().getConfigurations()), schemaContexts.getAuthentication(), 
                schemaContexts.getProps().getProps()).build().getSchemaContextMap().get(oldSchemaContext.getSchema().getName());
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
    
    private Collection<String> getDeletedDataSources(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        Collection<String> result = new LinkedList<>(oldSchemaContext.getSchema().getDataSources().keySet());
        result.removeAll(newDataSourceConfigs.keySet());
        return result;
    }
    
    private Map<String, DataSource> getAddedDataSources(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        return DataSourceConverter.getDataSourceMap(Maps.filterKeys(newDataSourceConfigs, each -> !oldSchemaContext.getSchema().getDataSources().containsKey(each)));
    }
    
    private Map<String, DataSource> getModifiedDataSources(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSourceConfigs) {
        Map<String, DataSourceConfiguration> modifiedDataSourceConfigs = newDataSourceConfigs.entrySet().stream()
                .filter(entry -> isModifiedDataSource(oldSchemaContext.getSchema().getDataSources(), entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (key, repeatKey) -> key, LinkedHashMap::new));
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
