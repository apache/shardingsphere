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

import com.google.common.base.Joiner;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.cluster.facade.ClusterFacade;
import org.apache.shardingsphere.cluster.facade.init.ClusterInitFacade;
import org.apache.shardingsphere.cluster.heartbeat.event.HeartbeatDetectNoticeEvent;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.log.ConfigurationLogger;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.callback.orchestration.MetaDataCallback;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.StatusContainedRule;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.kernel.context.SchemaContextsAware;
import org.apache.shardingsphere.kernel.context.SchemaContextsBuilder;
import org.apache.shardingsphere.kernel.context.runtime.RuntimeContext;
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.kernel.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.masterslave.rule.MasterSlaveRule;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.facade.MetricsTrackerManagerFacade;
import org.apache.shardingsphere.orchestration.core.common.event.AuthenticationChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.ClusterConfigurationChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.MetricsConfigurationChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.PropertiesChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaDeletedEvent;
import org.apache.shardingsphere.orchestration.core.common.eventbus.OrchestrationEventBus;
import org.apache.shardingsphere.orchestration.core.facade.OrchestrationFacade;
import org.apache.shardingsphere.orchestration.core.metadata.event.MetaDataChangedEvent;
import org.apache.shardingsphere.orchestration.core.registry.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registry.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registry.schema.OrchestrationSchema;

import javax.sql.DataSource;
import java.sql.SQLException;
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
        OrchestrationEventBus.getInstance().register(this);
        this.schemaContexts = schemaContexts;
        persistMetaData();
        disableMasterSlaveRules();
    }
    
    private void persistMetaData() {
        schemaContexts.getSchemaContexts().forEach((key, value) -> MetaDataCallback.getInstance().run(key, value.getSchema().getMetaData().getSchema()));
    }
    
    private void disableMasterSlaveRules() {
        Map<String, Collection<MasterSlaveRule>> masterSlaveRules = schemaContexts.getRules(MasterSlaveRule.class);
        if (masterSlaveRules.isEmpty()) {
            return;
        }
        Collection<String> disabledDataSources = OrchestrationFacade.getInstance().getRegistryCenter().loadDisabledDataSources();
        if (disabledDataSources.isEmpty()) {
            return;
        }
        for (Entry<String, Collection<MasterSlaveRule>> entry : masterSlaveRules.entrySet()) {
            for (MasterSlaveRule each : entry.getValue()) {
                disableMasterSlaveRules(each, disabledDataSources, entry.getKey());
            }
        }
    }
    
    private static void disableMasterSlaveRules(final MasterSlaveRule masterSlaveRule,
                                           final Collection<String> disabledDataSources, final String schemaName) {
        masterSlaveRule.getSingleDataSourceRule().getSlaveDataSourceNames().forEach(each -> {
            if (disabledDataSources.contains(Joiner.on(".").join(schemaName, each))) {
                masterSlaveRule.updateRuleStatus(new DataSourceNameDisabledEvent(each, true));
            }
        });
    }
    
    @Override
    public final Map<String, SchemaContext> getSchemaContexts() {
        return schemaContexts.getSchemaContexts();
    }
    
    @Override
    public final ConfigurationProperties getProps() {
        return schemaContexts.getProps();
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
        String schemaName = schemaAddedEvent.getShardingSchemaName();
        Map<String, SchemaContext> schemas = new HashMap<>(schemaContexts.getSchemaContexts());
        schemas.put(schemaName, getAddedSchemaContext(schemaAddedEvent));
        schemaContexts = new SchemaContexts(schemas, schemaContexts.getProps(), schemaContexts.getAuthentication());
        OrchestrationFacade.getInstance().getMetaDataCenter().persistMetaDataCenterNode(schemaName, schemaContexts.getSchemaContexts().get(schemaName).getSchema().getMetaData().getSchema());
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
        schemaContexts = new SchemaContexts(schemas, schemaContexts.getProps(), schemaContexts.getAuthentication());
    }
    
    /**
     * Renew properties.
     *
     * @param event properties changed event
     */
    @Subscribe
    public synchronized void renew(final PropertiesChangedEvent event) {
        ConfigurationLogger.log(event.getProps());
        ConfigurationProperties props = new ConfigurationProperties(event.getProps());
        schemaContexts = new SchemaContexts(getChangedSchemaContexts(props), props, schemaContexts.getAuthentication());
    }
    
    /**
     * Renew authentication.
     *
     * @param event authentication changed event
     */
    @Subscribe
    public synchronized void renew(final AuthenticationChangedEvent event) {
        ConfigurationLogger.log(event.getAuthentication());
        schemaContexts = new SchemaContexts(schemaContexts.getSchemaContexts(), schemaContexts.getProps(), event.getAuthentication());
    }
    
    /**
     * Renew metrics configuration.
     *
     * @param event metrics configuration changed event
     */
    @Subscribe
    public synchronized void renew(final MetricsConfigurationChangedEvent event) {
        MetricsConfiguration metricsConfiguration = event.getMetricsConfiguration();
        if (metricsConfiguration.getEnable()) {
            MetricsTrackerManagerFacade.restart(metricsConfiguration);
        } else {
            MetricsTrackerManagerFacade.close();
        }
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
        this.schemaContexts = new SchemaContexts(schemaContexts, this.schemaContexts.getProps(), this.schemaContexts.getAuthentication());
    }
    
    /**
     * Renew rule configurations.
     *
     * @param ruleConfigurationsChangedEvent rule configurations changed event.
     *  @throws Exception exception
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsChangedEvent ruleConfigurationsChangedEvent) throws Exception {
        Map<String, SchemaContext> schemaContexts = new HashMap<>(this.schemaContexts.getSchemaContexts());
        String schemaName = ruleConfigurationsChangedEvent.getShardingSchemaName();
        schemaContexts.remove(schemaName);
        schemaContexts.put(schemaName, getChangedSchemaContext(this.schemaContexts.getSchemaContexts().get(schemaName), ruleConfigurationsChangedEvent.getRuleConfigurations()));
        this.schemaContexts = new SchemaContexts(schemaContexts, this.schemaContexts.getProps(), this.schemaContexts.getAuthentication());
        OrchestrationFacade.getInstance().getMetaDataCenter().persistMetaDataCenterNode(schemaName, schemaContexts.get(schemaName).getSchema().getMetaData().getSchema());
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
        this.schemaContexts = new SchemaContexts(schemaContexts, this.schemaContexts.getProps(), this.schemaContexts.getAuthentication());
    }
    
    /**
     * Renew circuit breaker state.
     *
     * @param event circuit state changed event
     */
    @Subscribe
    public synchronized void renew(final CircuitStateChangedEvent event) {
        this.schemaContexts = new SchemaContexts(schemaContexts.getSchemaContexts(), schemaContexts.getProps(), schemaContexts.getAuthentication(), event.isCircuitBreak());
    }
    
    /**
     * Renew cluster facade.
     *
     * @param event cluster configuration changed event
     */
    @Subscribe
    public void renew(final ClusterConfigurationChangedEvent event) {
        if (ClusterInitFacade.isEnabled()) {
            ClusterInitFacade.restart(event.getClusterConfiguration());
        }
    }
    
    /**
     * Heart beat detect.
     *
     * @param event heart beat detect notice event
     */
    @Subscribe
    public synchronized void heartbeat(final HeartbeatDetectNoticeEvent event) {
        if (ClusterInitFacade.isEnabled()) {
            ClusterFacade.getInstance().detectHeartbeat(schemaContexts.getSchemaContexts());
        }
    }
    
    /**
     *  Enable cluster facade after properties changed.
     *
     * @param event properties changed event
     */
    @Subscribe
    public void enable(final PropertiesChangedEvent event) {
        boolean clusterEnabled = new ConfigurationProperties(event.getProps()).<Boolean>getValue(ConfigurationPropertyKey.PROXY_CLUSTER_ENABLED);
        ClusterInitFacade.enable(clusterEnabled);
    }
    
    private SchemaContext getAddedSchemaContext(final SchemaAddedEvent schemaAddedEvent) throws Exception {
        String schemaName = schemaAddedEvent.getShardingSchemaName();
        Map<String, Map<String, DataSource>> dataSourcesMap = createDataSourcesMap(Collections.singletonMap(schemaName, schemaAddedEvent.getDataSourceConfigurations()));
        Map<String, Map<String, DataSourceParameter>> dataSourceParametersMap = createDataSourceParametersMap(Collections.singletonMap(schemaName, schemaAddedEvent.getDataSourceConfigurations()));
        DatabaseType databaseType = getDatabaseType(dataSourceParametersMap.values().iterator().next().values().iterator().next());
        SchemaContextsBuilder schemaContextsBuilder = new SchemaContextsBuilder(dataSourcesMap, dataSourceParametersMap,
                schemaContexts.getAuthentication(), databaseType, Collections.singletonMap(schemaName, schemaAddedEvent.getRuleConfigurations()),
                schemaContexts.getProps().getProps());
        return schemaContextsBuilder.build().getSchemaContexts().get(schemaName);
    }
    
    private DatabaseType getDatabaseType(final DataSourceParameter parameter) {
        if (!schemaContexts.getSchemaContexts().isEmpty()) {
            schemaContexts.getSchemaContexts().values().iterator().next().getSchema().getDatabaseType();
        }
        return DatabaseTypes.getDatabaseTypeByURL(parameter.getUrl());
    }
    
    private Map<String, SchemaContext> getChangedSchemaContexts(final ConfigurationProperties props) {
        Map<String, SchemaContext> result = new HashMap<>(schemaContexts.getSchemaContexts().size());
        for (Entry<String, SchemaContext> entry : this.schemaContexts.getSchemaContexts().entrySet()) {
            RuntimeContext runtimeContext = entry.getValue().getRuntimeContext();
            result.put(entry.getKey(), new SchemaContext(entry.getValue().getName(), entry.getValue().getSchema(), new RuntimeContext(runtimeContext.getCachedDatabaseMetaData(),
                    new ExecutorKernel(props.<Integer>getValue(ConfigurationPropertyKey.EXECUTOR_SIZE)), runtimeContext.getSqlParserEngine(), runtimeContext.getTransactionManagerEngine())));
        }
        return result;
    }
    
    private ShardingSphereSchema getChangedShardingSphereSchema(final ShardingSphereSchema oldShardingSphereSchema, final RuleSchemaMetaData newRuleSchemaMetaData) {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(oldShardingSphereSchema.getMetaData().getDataSources(), newRuleSchemaMetaData);
        return new ShardingSphereSchema(oldShardingSphereSchema.getDatabaseType(), oldShardingSphereSchema.getConfigurations(),
                oldShardingSphereSchema.getRules(), oldShardingSphereSchema.getDataSources(), metaData);
    }
    
    private SchemaContext getChangedSchemaContext(final SchemaContext oldSchemaContext, final Collection<RuleConfiguration> configurations) throws SQLException {
        ShardingSphereSchema oldSchema = oldSchemaContext.getSchema();
        SchemaContextsBuilder builder = new SchemaContextsBuilder(Collections.singletonMap(oldSchemaContext.getName(), oldSchema.getDataSources()),
                Collections.singletonMap(oldSchemaContext.getName(), oldSchema.getDataSourceParameters()),
                schemaContexts.getAuthentication(), oldSchema.getDatabaseType(), Collections.singletonMap(oldSchemaContext.getName(), configurations), schemaContexts.getProps().getProps());
        return builder.build().getSchemaContexts().values().iterator().next();
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
        return new SchemaContextsBuilder(dataSourcesMap, dataSourceParametersMap, schemaContexts.getAuthentication(), oldSchemaContext.getSchema().getDatabaseType(), 
                Collections.singletonMap(oldSchemaContext.getName(), oldSchemaContext.getSchema().getConfigurations()), 
                schemaContexts.getProps().getProps()).build().getSchemaContexts().get(oldSchemaContext.getName());
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
