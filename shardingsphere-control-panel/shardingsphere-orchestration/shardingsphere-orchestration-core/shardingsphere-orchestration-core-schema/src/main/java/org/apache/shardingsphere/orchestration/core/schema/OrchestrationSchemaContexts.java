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
import org.apache.shardingsphere.cluster.facade.ClusterFacade;
import org.apache.shardingsphere.cluster.heartbeat.event.HeartbeatDetectNoticeEvent;
import org.apache.shardingsphere.cluster.heartbeat.eventbus.HeartbeatEventBus;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.log.ConfigurationLogger;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.infra.rule.StatusContainedRule;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.kernel.context.SchemaContextsBuilder;
import org.apache.shardingsphere.kernel.context.SchemaContextsAware;
import org.apache.shardingsphere.kernel.context.runtime.RuntimeContext;
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.kernel.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.orchestration.core.common.event.AuthenticationChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.PropertiesChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaDeletedEvent;
import org.apache.shardingsphere.orchestration.core.common.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.core.metadatacenter.event.MetaDataChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.schema.OrchestrationSchema;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Control panel subscriber.
 * 
 */
public abstract class OrchestrationSchemaContexts implements SchemaContextsAware {
    
    private volatile SchemaContexts schemaContexts;
    
    public OrchestrationSchemaContexts(final SchemaContexts schemaContexts) {
        ShardingOrchestrationEventBus.getInstance().register(this);
        HeartbeatEventBus.getInstance().register(this);
        this.schemaContexts = schemaContexts;
    }
    
    @Override
    public final Map<String, SchemaContext> getSchemaContexts() {
        return schemaContexts.getSchemaContexts();
    }
    
    @Override
    public final ConfigurationProperties getProperties() {
        return schemaContexts.getProperties();
    }
    
    @Override
    public final Authentication getAuthentication() {
        return schemaContexts.getAuthentication();
    }
    
    @Override
    public final SchemaContext getDefaultSchemaContext() {
        return schemaContexts.getDefaultSchemaContext();
    }
    
    @Override
    public final boolean isCircuitBreak() {
        return schemaContexts.isCircuitBreak();
    }
    
    @Override
    public final void close() {
        schemaContexts.close();
    }
    
    /**
     * Renew to add new schema.
     *
     * @param schemaAddedEvent schema add changed event
     * @throws Exception exception
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent schemaAddedEvent) throws Exception {
        Map<String, SchemaContext> schemas = new HashMap<>(schemaContexts.getSchemaContexts());
        schemas.put(schemaAddedEvent.getShardingSchemaName(), getAddedSchemaContext(schemaAddedEvent));
        schemaContexts = new SchemaContexts(schemas, schemaContexts.getProperties(), schemaContexts.getAuthentication());
    }
    
    /**
     * Renew to delete new schema.
     *
     * @param schemaDeletedEvent schema delete changed event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent schemaDeletedEvent) {
        Map<String, SchemaContext> schemas = new HashMap<>(schemaContexts.getSchemaContexts());
        schemas.remove(schemaDeletedEvent.getShardingSchemaName());
        schemaContexts = new SchemaContexts(schemas, schemaContexts.getProperties(), schemaContexts.getAuthentication());
    }
    
    /**
     * Renew properties.
     *
     * @param event properties changed event
     */
    @Subscribe
    public synchronized void renew(final PropertiesChangedEvent event) {
        ConfigurationLogger.log(event.getProps());
        ConfigurationProperties properties = new ConfigurationProperties(event.getProps());
        schemaContexts = new SchemaContexts(getChangedSchemaContexts(properties), properties, schemaContexts.getAuthentication());
    }
    
    /**
     * Renew authentication.
     *
     * @param event authentication changed event
     */
    @Subscribe
    public synchronized void renew(final AuthenticationChangedEvent event) {
        ConfigurationLogger.log(event.getAuthentication());
        schemaContexts = new SchemaContexts(schemaContexts.getSchemaContexts(), schemaContexts.getProperties(), event.getAuthentication());
    }
    
    /**
     * Renew meta data of the schema.
     *
     * @param event meta data changed event.
     */
    @Subscribe
    public synchronized void renew(final MetaDataChangedEvent event) {
        Map<String, SchemaContext> schemaContexts = new HashMap<>(this.schemaContexts.getSchemaContexts().size());
        for (Entry<String, SchemaContext> entry : this.schemaContexts.getSchemaContexts().entrySet()) {
            if (event.getSchemaNames().contains(entry.getKey())) {
                schemaContexts.put(entry.getKey(), new SchemaContext(entry.getValue().getName(),
                        getChangedShardingSphereSchema(entry.getValue().getSchema(), event.getRuleSchemaMetaData()), entry.getValue().getRuntimeContext()));
            } else {
                schemaContexts.put(entry.getKey(), entry.getValue());
            }
        }
        this.schemaContexts = new SchemaContexts(schemaContexts, this.schemaContexts.getProperties(), this.schemaContexts.getAuthentication());
    }
    
    /**
     * Renew rule configurations.
     *
     * @param ruleConfigurationsChangedEvent rule configurations changed event.
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsChangedEvent ruleConfigurationsChangedEvent) {
        Map<String, SchemaContext> schemaContexts = new HashMap<>(this.schemaContexts.getSchemaContexts());
        String schemaName = ruleConfigurationsChangedEvent.getShardingSchemaName();
        schemaContexts.remove(schemaName);
        schemaContexts.put(schemaName, getChangedSchemaContext(this.schemaContexts.getSchemaContexts().get(schemaName), ruleConfigurationsChangedEvent.getRuleConfigurations()));
        this.schemaContexts = new SchemaContexts(schemaContexts, this.schemaContexts.getProperties(), this.schemaContexts.getAuthentication());
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param disabledStateChangedEvent disabled state changed event
     */
    @Subscribe
    public synchronized void renew(final DisabledStateChangedEvent disabledStateChangedEvent) {
        OrchestrationSchema orchestrationSchema = disabledStateChangedEvent.getOrchestrationSchema();
        Collection<ShardingSphereRule> rules = schemaContexts.getSchemaContexts().get(orchestrationSchema.getSchemaName()).getSchema().getRules();
        for (ShardingSphereRule each : rules) {
            if (each instanceof StatusContainedRule) {
                ((StatusContainedRule) each).updateRuleStatus(new DataSourceNameDisabledEvent(orchestrationSchema.getDataSourceName(), disabledStateChangedEvent.isDisabled()));
            }
        }
    }
    
    /**
     * Renew data source configuration.
     *
     * @param dataSourceChangedEvent data source changed event.
     * @throws Exception exception
     */
    @Subscribe
    public synchronized void renew(final DataSourceChangedEvent dataSourceChangedEvent) throws Exception {
        String schemaName = dataSourceChangedEvent.getShardingSchemaName();
        Map<String, SchemaContext> schemaContexts = new HashMap<>(this.schemaContexts.getSchemaContexts());
        schemaContexts.remove(schemaName);
        schemaContexts.put(schemaName, getChangedSchemaContext(this.schemaContexts.getSchemaContexts().get(schemaName), dataSourceChangedEvent.getDataSourceConfigurations()));
        this.schemaContexts = new SchemaContexts(schemaContexts, this.schemaContexts.getProperties(), this.schemaContexts.getAuthentication());
    }
    
    /**
     * Renew circuit breaker state.
     *
     * @param event circuit state changed event
     */
    @Subscribe
    public synchronized void renew(final CircuitStateChangedEvent event) {
        this.schemaContexts = new SchemaContexts(schemaContexts.getSchemaContexts(), schemaContexts.getProperties(), schemaContexts.getAuthentication(), event.isCircuitBreak());
    }
    
    /**
     * Heart beat detect.
     *
     * @param event heart beat detect notice event
     */
    @Subscribe
    public synchronized void heartbeat(final HeartbeatDetectNoticeEvent event) {
        ClusterFacade.getInstance().detectHeartbeat(schemaContexts.getSchemaContexts());
    }
    
    private SchemaContext getAddedSchemaContext(final SchemaAddedEvent schemaAddedEvent) throws Exception {
        String schemaName = schemaAddedEvent.getShardingSchemaName();
        Map<String, Map<String, DataSource>> dataSourcesMap = createDataSourcesMap(Collections.singletonMap(schemaName, schemaAddedEvent.getDataSourceConfigurations()));
        Map<String, Map<String, DataSourceParameter>> dataSourceParametersMap = createDataSourceParametersMap(Collections.singletonMap(schemaName, schemaAddedEvent.getDataSourceConfigurations()));
        DatabaseType databaseType = schemaContexts.getSchemaContexts().values().iterator().next().getSchema().getDatabaseType();
        SchemaContextsBuilder schemaContextsBuilder = new SchemaContextsBuilder(dataSourcesMap, dataSourceParametersMap,
                schemaContexts.getAuthentication(), databaseType, Collections.singletonMap(schemaName, schemaAddedEvent.getRuleConfigurations()),
                schemaContexts.getProperties().getProps());
        return schemaContextsBuilder.build().getSchemaContexts().get(schemaName);
    }
    
    private Map<String, SchemaContext> getChangedSchemaContexts(final ConfigurationProperties properties) {
        Map<String, SchemaContext> result = new HashMap<>(schemaContexts.getSchemaContexts().size());
        for (Entry<String, SchemaContext> entry : this.schemaContexts.getSchemaContexts().entrySet()) {
            RuntimeContext runtimeContext = entry.getValue().getRuntimeContext();
            result.put(entry.getKey(), new SchemaContext(entry.getValue().getName(), entry.getValue().getSchema(), new RuntimeContext(runtimeContext.getCachedDatabaseMetaData(),
                    new ExecutorKernel(properties.<Integer>getValue(ConfigurationPropertyKey.EXECUTOR_SIZE)), runtimeContext.getSqlParserEngine(), runtimeContext.getTransactionManagerEngine())));
        }
        return result;
    }
    
    private ShardingSphereSchema getChangedShardingSphereSchema(final ShardingSphereSchema oldShardingSphereSchema, final RuleSchemaMetaData newRuleSchemaMetaData) {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(oldShardingSphereSchema.getMetaData().getDataSources(), newRuleSchemaMetaData);
        return new ShardingSphereSchema(oldShardingSphereSchema.getDatabaseType(), oldShardingSphereSchema.getConfigurations(),
                oldShardingSphereSchema.getRules(), oldShardingSphereSchema.getDataSources(), metaData);
    }
    
    private SchemaContext getChangedSchemaContext(final SchemaContext schemaContext, final Collection<RuleConfiguration> configurations) {
        ShardingSphereSchema oldSchema = schemaContext.getSchema();
        ShardingSphereSchema newSchema = new ShardingSphereSchema(oldSchema.getDatabaseType(), configurations,
                ShardingSphereRulesBuilder.build(configurations, oldSchema.getDataSources().keySet()), oldSchema.getDataSources(), oldSchema.getDataSourceParameters(), oldSchema.getMetaData());
        return new SchemaContext(schemaContext.getName(), newSchema, schemaContext.getRuntimeContext());
    }
    
    private SchemaContext getChangedSchemaContext(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSources) throws Exception {
        Collection<String> deletedDataSources = getDeletedDataSources(oldSchemaContext, newDataSources);
        Map<String, DataSource> modifiedDataSources = getModifiedDataSources(oldSchemaContext, newDataSources);
        oldSchemaContext.getSchema().closeDataSources(deletedDataSources);
        oldSchemaContext.getSchema().closeDataSources(modifiedDataSources.keySet());
        oldSchemaContext.getRuntimeContext().getTransactionManagerEngine().close();
        Map<String, Map<String, DataSource>> dataSourcesMap = Collections.singletonMap(oldSchemaContext.getName(), getNewDataSources(oldSchemaContext.getSchema().getDataSources(), 
                deletedDataSources, getAddedDataSources(oldSchemaContext, newDataSources), modifiedDataSources));
        Map<String, Map<String, DataSourceParameter>> dataSourceParametersMap = createDataSourceParametersMap(Collections.singletonMap(oldSchemaContext.getName(), newDataSources));
        return new SchemaContextsBuilder(dataSourcesMap, dataSourceParametersMap, this.schemaContexts.getAuthentication(), oldSchemaContext.getSchema().getDatabaseType(), 
                Collections.singletonMap(oldSchemaContext.getName(), oldSchemaContext.getSchema().getConfigurations()), 
                this.schemaContexts.getProperties().getProps()).build().getSchemaContexts().get(oldSchemaContext.getName());
    }
    
    private Map<String, DataSource> getNewDataSources(final Map<String, DataSource> oldDataSources, final Collection<String> deletedDataSources,
                                                                   final Map<String, DataSource> addedDataSources, final Map<String, DataSource> modifiedDataSources) {
        Map<String, DataSource> result = new LinkedHashMap<>(oldDataSources);
        result.keySet().removeAll(deletedDataSources);
        result.keySet().removeAll(modifiedDataSources.keySet());
        result.putAll(modifiedDataSources);
        result.putAll(addedDataSources);
        return result;
    }
    
    private Collection<String> getDeletedDataSources(final SchemaContext oldSchemaContext, final Map<String, DataSourceConfiguration> newDataSources) {
        Collection<String> result = new LinkedList<>(oldSchemaContext.getSchema().getDataSourceParameters().keySet());
        result.removeAll(newDataSources.keySet());
        return result;
    }
    
    /**
     * Get added dataSources.
     * 
     * @param oldSchemaContext old schema context
     * @param newDataSources new data sources
     * @return added data sources
     * @throws Exception exception
     */
    public abstract Map<String, DataSource> getAddedDataSources(SchemaContext oldSchemaContext, Map<String, DataSourceConfiguration> newDataSources) throws Exception;
    
    /**
     * Get modified dataSources.
     * 
     * @param oldSchemaContext old schema context
     * @param newDataSources new data sources
     * @return modified data sources
     * @throws Exception exception
     */
    public abstract Map<String, DataSource> getModifiedDataSources(SchemaContext oldSchemaContext, Map<String, DataSourceConfiguration> newDataSources) throws Exception;
    
    /**
     * Create data sources map.
     * 
     * @param dataSourcesMap data source map
     * @return data sources map
     * @throws Exception exception
     */
    public abstract Map<String, Map<String, DataSource>> createDataSourcesMap(Map<String, Map<String, DataSourceConfiguration>> dataSourcesMap) throws Exception;
    
    /**
     * Create data source parameters map.
     * 
     * @param dataSourcesMap data source map
     * @return data source parameters map
     */
    public abstract Map<String, Map<String, DataSourceParameter>> createDataSourceParametersMap(Map<String, Map<String, DataSourceConfiguration>> dataSourcesMap);
}
