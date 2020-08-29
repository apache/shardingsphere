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

package org.apache.shardingsphere.orchestration.core.schema;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.StatusContainedRule;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.kernel.context.SchemaContextsBuilder;
import org.apache.shardingsphere.kernel.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.kernel.context.runtime.RuntimeContext;
import org.apache.shardingsphere.kernel.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.orchestration.core.common.event.auth.AuthenticationChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.datasource.DataSourceChangeCompletedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.schema.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.orchestration.core.common.eventbus.OrchestrationEventBus;
import org.apache.shardingsphere.orchestration.core.facade.OrchestrationFacade;
import org.apache.shardingsphere.orchestration.core.metadata.event.MetaDataChangedEvent;
import org.apache.shardingsphere.orchestration.core.registry.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registry.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registry.schema.OrchestrationSchema;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Orchestration schema contexts.
 */
public abstract class OrchestrationSchemaContexts implements SchemaContexts {
    
    private final OrchestrationFacade orchestrationFacade;
    
    private volatile SchemaContexts schemaContexts;
    
    protected OrchestrationSchemaContexts(final SchemaContexts schemaContexts, final OrchestrationFacade orchestrationFacade) {
        this.orchestrationFacade = orchestrationFacade;
        this.schemaContexts = schemaContexts;
        OrchestrationEventBus.getInstance().register(this);
        disableDataSources();
        persistMetaData();
    }
    
    private void disableDataSources() {
        Collection<String> disabledDataSources = orchestrationFacade.getRegistryCenter().loadDisabledDataSources();
        if (!disabledDataSources.isEmpty()) {
            schemaContexts.getSchemaContexts().forEach((key, value)
                -> value.getSchema().getRules().stream().filter(each -> each instanceof StatusContainedRule).forEach(each -> disableDataSources(key, disabledDataSources, (StatusContainedRule) each)));
        }
    }
    
    private void disableDataSources(final String schemaName, final Collection<String> disabledDataSources, final StatusContainedRule rule) {
        disabledDataSources.stream().filter(each -> each.startsWith(schemaName)).map(this::getDataSourceName).forEach(each -> rule.updateRuleStatus(new DataSourceNameDisabledEvent(each, true)));
    }
    
    private String getDataSourceName(final String disabledDataSource) {
        return new OrchestrationSchema(disabledDataSource).getDataSourceName();
    }
    
    private void persistMetaData() {
        schemaContexts.getSchemaContexts().forEach((key, value) -> orchestrationFacade.getMetaDataCenter().persistMetaDataCenterNode(key, value.getSchema().getMetaData().getSchema()));
    }
    
    @Override
    public final DatabaseType getDatabaseType() {
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
    public final Map<String, SchemaContext> getSchemaContexts() {
        return schemaContexts.getSchemaContexts();
    }
    
    @Override
    public final SchemaContext getDefaultSchemaContext() {
        return schemaContexts.getDefaultSchemaContext();
    }
    
    @Override
    public final Authentication getAuthentication() {
        return schemaContexts.getAuthentication();
    }
    
    @Override
    public final ConfigurationProperties getProps() {
        return schemaContexts.getProps();
    }
    
    @Override
    public final boolean isCircuitBreak() {
        return schemaContexts.isCircuitBreak();
    }
    
    @Override
    public final void close() throws Exception {
        schemaContexts.close();
        orchestrationFacade.close();
    }
    
    /**
     * Renew to add new schema.
     *
     * @param event schema add event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent event) throws SQLException {
        Map<String, SchemaContext> schemas = new HashMap<>(schemaContexts.getSchemaContexts());
        schemas.put(event.getSchemaName(), createAddedSchemaContext(event));
        schemaContexts = new StandardSchemaContexts(schemas, schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
        orchestrationFacade.getMetaDataCenter().persistMetaDataCenterNode(event.getSchemaName(), schemaContexts.getSchemaContexts().get(event.getSchemaName()).getSchema().getMetaData().getSchema());
        OrchestrationEventBus.getInstance().post(
                new DataSourceChangeCompletedEvent(event.getSchemaName(), schemaContexts.getDatabaseType(), schemas.get(event.getSchemaName()).getSchema().getDataSources()));
    }
    
    /**
     * Renew to delete new schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent event) {
        Map<String, SchemaContext> schemas = new HashMap<>(schemaContexts.getSchemaContexts());
        schemas.remove(event.getSchemaName());
        schemaContexts = new StandardSchemaContexts(schemas, schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
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
        schemaContexts = new StandardSchemaContexts(schemaContexts.getSchemaContexts(), event.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
    }
    
    /**
     * Renew meta data of the schema.
     *
     * @param event meta data changed event
     */
    @Subscribe
    public synchronized void renew(final MetaDataChangedEvent event) {
        Map<String, SchemaContext> newSchemaContexts = new HashMap<>(schemaContexts.getSchemaContexts().size(), 1);
        for (Entry<String, SchemaContext> entry : schemaContexts.getSchemaContexts().entrySet()) {
            String schemaName = entry.getKey();
            SchemaContext oldSchemaContext = entry.getValue();
            SchemaContext newSchemaContext = event.getSchemaNames().contains(schemaName) 
                    ? new SchemaContext(oldSchemaContext.getName(), getChangedShardingSphereSchema(oldSchemaContext.getSchema(), event.getRuleSchemaMetaData()), oldSchemaContext.getRuntimeContext())
                    : oldSchemaContext;
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
        Map<String, SchemaContext> newSchemaContexts = new HashMap<>(schemaContexts.getSchemaContexts());
        String schemaName = event.getSchemaName();
        newSchemaContexts.remove(schemaName);
        newSchemaContexts.put(schemaName, getChangedSchemaContext(schemaContexts.getSchemaContexts().get(schemaName), event.getRuleConfigurations()));
        schemaContexts = new StandardSchemaContexts(newSchemaContexts, schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
        orchestrationFacade.getMetaDataCenter().persistMetaDataCenterNode(schemaName, newSchemaContexts.get(schemaName).getSchema().getMetaData().getSchema());
    }
    
    /**
     * Renew data source configuration.
     *
     * @param event data source changed event.
     * @throws Exception exception
     */
    @Subscribe
    public synchronized void renew(final DataSourceChangedEvent event) throws Exception {
        String schemaName = event.getSchemaName();
        Map<String, SchemaContext> newSchemaContexts = new HashMap<>(schemaContexts.getSchemaContexts());
        newSchemaContexts.remove(schemaName);
        newSchemaContexts.put(schemaName, getChangedSchemaContext(schemaContexts.getSchemaContexts().get(schemaName), event.getDataSourceConfigurations()));
        schemaContexts = new StandardSchemaContexts(newSchemaContexts, schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType());
        OrchestrationEventBus.getInstance().post(
                new DataSourceChangeCompletedEvent(event.getSchemaName(), schemaContexts.getDatabaseType(), newSchemaContexts.get(event.getSchemaName()).getSchema().getDataSources()));
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param event disabled state changed event
     */
    @Subscribe
    public synchronized void renew(final DisabledStateChangedEvent event) {
        OrchestrationSchema orchestrationSchema = event.getOrchestrationSchema();
        Collection<ShardingSphereRule> rules = schemaContexts.getSchemaContexts().get(orchestrationSchema.getSchemaName()).getSchema().getRules();
        for (ShardingSphereRule each : rules) {
            if (each instanceof StatusContainedRule) {
                ((StatusContainedRule) each).updateRuleStatus(new DataSourceNameDisabledEvent(orchestrationSchema.getDataSourceName(), event.isDisabled()));
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
                schemaContexts.getSchemaContexts(), schemaContexts.getAuthentication(), schemaContexts.getProps(), schemaContexts.getDatabaseType(), event.isCircuitBreak());
    }
    
    private SchemaContext createAddedSchemaContext(final SchemaAddedEvent schemaAddedEvent) throws SQLException {
        String schemaName = schemaAddedEvent.getSchemaName();
        Map<String, Map<String, DataSource>> dataSourcesMap = createDataSourcesMap(Collections.singletonMap(schemaName, schemaAddedEvent.getDataSourceConfigurations()));
        DatabaseType databaseType = getDatabaseType(dataSourcesMap);
        SchemaContextsBuilder schemaContextsBuilder = new SchemaContextsBuilder(databaseType, dataSourcesMap, 
                Collections.singletonMap(schemaName, schemaAddedEvent.getRuleConfigurations()), schemaContexts.getAuthentication(), schemaContexts.getProps().getProps());
        return schemaContextsBuilder.build().getSchemaContexts().get(schemaName);
    }
    
    private Map<String, SchemaContext> getChangedSchemaContexts(final ConfigurationProperties props) {
        Map<String, SchemaContext> result = new HashMap<>(schemaContexts.getSchemaContexts().size());
        for (Entry<String, SchemaContext> entry : schemaContexts.getSchemaContexts().entrySet()) {
            RuntimeContext runtimeContext = entry.getValue().getRuntimeContext();
            result.put(entry.getKey(), new SchemaContext(entry.getValue().getName(), entry.getValue().getSchema(), new RuntimeContext(runtimeContext.getCachedDatabaseMetaData(),
                    new ExecutorKernel(props.<Integer>getValue(ConfigurationPropertyKey.EXECUTOR_SIZE)), runtimeContext.getSqlParserEngine(), runtimeContext.getTransactionManagerEngine())));
        }
        return result;
    }
    
    private ShardingSphereSchema getChangedShardingSphereSchema(final ShardingSphereSchema oldShardingSphereSchema, final RuleSchemaMetaData newRuleSchemaMetaData) {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(oldShardingSphereSchema.getMetaData().getDataSources(), newRuleSchemaMetaData);
        return new ShardingSphereSchema(oldShardingSphereSchema.getConfigurations(), oldShardingSphereSchema.getRules(), oldShardingSphereSchema.getDataSources(), metaData);
    }
    
    private SchemaContext getChangedSchemaContext(final SchemaContext oldSchemaContext, final Collection<RuleConfiguration> configurations) throws SQLException {
        ShardingSphereSchema oldSchema = oldSchemaContext.getSchema();
        SchemaContextsBuilder builder = new SchemaContextsBuilder(schemaContexts.getDatabaseType(), Collections.singletonMap(oldSchemaContext.getName(), oldSchema.getDataSources()),
                Collections.singletonMap(oldSchemaContext.getName(), configurations), schemaContexts.getAuthentication(), schemaContexts.getProps().getProps());
        return builder.build().getSchemaContexts().values().iterator().next();
    }
    
    private SchemaContext getChangedSchemaContext(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSources) throws Exception {
        Collection<String> deletedDataSources = getDeletedDataSources(oldSchemaContext, newDataSources);
        Map<String, DataSource> modifiedDataSources = getModifiedDataSources(oldSchemaContext, newDataSources);
        oldSchemaContext.getSchema().closeDataSources(deletedDataSources);
        oldSchemaContext.getSchema().closeDataSources(modifiedDataSources.keySet());
        oldSchemaContext.getRuntimeContext().getTransactionManagerEngine().close();
        Map<String, Map<String, DataSource>> dataSourcesMap = Collections.singletonMap(oldSchemaContext.getName(), 
                getNewDataSources(oldSchemaContext.getSchema().getDataSources(), getAddedDataSources(oldSchemaContext, newDataSources), modifiedDataSources, deletedDataSources));
        return new SchemaContextsBuilder(schemaContexts.getDatabaseType(), dataSourcesMap,
                Collections.singletonMap(oldSchemaContext.getName(), oldSchemaContext.getSchema().getConfigurations()), schemaContexts.getAuthentication(), 
                schemaContexts.getProps().getProps()).build().getSchemaContexts().get(oldSchemaContext.getName());
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
    
    private Collection<String> getDeletedDataSources(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSources) {
        Collection<String> result = new LinkedList<>(oldSchemaContext.getSchema().getDataSources().keySet());
        result.removeAll(newDataSources.keySet());
        return result;
    }
    
    protected abstract Map<String, DataSource> getAddedDataSources(SchemaContext oldSchemaContext, Map<String, DataSourceConfiguration> newDataSources);
    
    protected abstract Map<String, DataSource> getModifiedDataSources(SchemaContext oldSchemaContext, Map<String, DataSourceConfiguration> newDataSources);
    
    protected abstract Map<String, Map<String, DataSource>> createDataSourcesMap(Map<String, Map<String, DataSourceConfiguration>> dataSourcesMap);
}
