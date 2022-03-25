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

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.metadata.schema.QualifiedSchema;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.infra.rule.identifier.type.InstanceAwareRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StatusContainedRule;
import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;
import org.apache.shardingsphere.infra.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.schema.SchemaChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.version.SchemaVersionChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOnlineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.StateEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.WorkerIdEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.XaRecoveryIdEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.PrimaryStateChangedEvent;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cluster context manager coordinator.
 */
public final class ClusterContextManagerCoordinator {
    
    private final MetaDataPersistService metaDataPersistService;
    
    private final ContextManager contextManager;
    
    private final RegistryCenter registryCenter;
    
    public ClusterContextManagerCoordinator(final MetaDataPersistService metaDataPersistService, final ContextManager contextManager, final RegistryCenter registryCenter) {
        this.metaDataPersistService = metaDataPersistService;
        this.contextManager = contextManager;
        this.registryCenter = registryCenter;
        ShardingSphereEventBus.getInstance().register(this);
        buildSpecialRules();
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
        contextManager.alterProperties(event.getProps());
    }
    
    /**
     * Renew meta data of the schema.
     *
     * @param event meta data changed event
     */
    @Subscribe
    public synchronized void renew(final SchemaChangedEvent event) {
        contextManager.alterSchema(event.getSchemaName(), event.getChangedTableMetaData(), event.getDeletedTable());
    }
    
    /**
     * Renew rule configurations.
     *
     * @param event rule configurations changed event
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsChangedEvent event) {
        if (metaDataPersistService.getSchemaVersionPersistService().isActiveVersion(event.getSchemaName(), event.getSchemaVersion())) {
            contextManager.alterRuleConfiguration(event.getSchemaName(), event.getRuleConfigurations());
            buildSpecialRules();
        }
    }
    
    /**
     * Renew data source configuration.
     *
     * @param event data source changed event.
     */
    @Subscribe
    public synchronized void renew(final DataSourceChangedEvent event) {
        if (metaDataPersistService.getSchemaVersionPersistService().isActiveVersion(event.getSchemaName(), event.getSchemaVersion())) {
            contextManager.alterDataSourceConfiguration(event.getSchemaName(), event.getDataSourcePropertiesMap());
            buildSpecialRules();
        }
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
                        .updateStatus(new DataSourceNameDisabledEvent(qualifiedSchema, event.isDisabled())));
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
                        .updateStatus(new PrimaryDataSourceChangedEvent(qualifiedSchema)));
    }
    
    /**
     * Renew global rule configurations.
     *
     * @param event global rule configurations changed event
     */
    @Subscribe
    public synchronized void renew(final GlobalRuleConfigurationsChangedEvent event) {
        contextManager.alterGlobalRuleConfiguration(event.getRuleConfigurations());
    }
    
    /**
     * Renew instance status.
     *
     * @param event state event
     */
    @Subscribe
    public synchronized void renew(final StateEvent event) {
        contextManager.getInstanceContext().updateInstanceStatus(event.getInstanceId(), event.getStatus());
    }
    
    /**
     * Renew instance worker id.
     *
     * @param event worker id event
     */
    @Subscribe
    public synchronized void renew(final WorkerIdEvent event) {
        if (contextManager.getInstanceContext().getInstance().getInstanceDefinition().getInstanceId().getId().equals(event.getInstanceId())) {
            contextManager.getInstanceContext().updateWorkerId(event.getWorkerId());
        }
    }
    
    /**
     * Renew instance labels.
     * 
     * @param event label event
     */
    @Subscribe
    public synchronized void renew(final LabelsEvent event) {
        contextManager.getInstanceContext().updateLabel(event.getInstanceId(), event.getLabels());
    }
    
    /**
     * Renew instance xa recovery id event.
     *
     * @param event xa recovery id event
     */
    @Subscribe
    public synchronized void renew(final XaRecoveryIdEvent event) {
        if (contextManager.getInstanceContext().getInstance().getInstanceDefinition().getInstanceId().getId().equals(event.getInstanceId())) {
            contextManager.getInstanceContext().updateXaRecoveryId(event.getXaRecoveryId());
            contextManager.renewAllTransactionContext();
        }
    }
    
    /**
     * Renew instance list.
     *
     * @param event compute node online event
     */
    @Subscribe
    public synchronized void renew(final InstanceOnlineEvent event) {
        ComputeNodeInstance instance = metaDataPersistService.getComputeNodePersistService().loadComputeNodeInstance(event.getInstanceDefinition());
        contextManager.getInstanceContext().addComputeNodeInstance(instance);
    }
    
    /**
     * Renew instance list.
     *
     * @param event compute node offline event
     */
    @Subscribe
    public synchronized void renew(final InstanceOfflineEvent event) {
        contextManager.getInstanceContext().deleteComputeNodeInstance(metaDataPersistService.getComputeNodePersistService().loadComputeNodeInstance(event.getInstanceDefinition()));
    }
    
    /**
     * Renew with new schema version.
     * 
     * @param event schema version changed event
     */
    @Subscribe
    public synchronized void renew(final SchemaVersionChangedEvent event) {
        Map<String, DataSourceProperties> dataSourcePropertiesMap = metaDataPersistService.getDataSourceService().load(event.getSchemaName(), event.getActiveVersion());
        Collection<RuleConfiguration> ruleConfigs = metaDataPersistService.getSchemaRuleService().load(event.getSchemaName(), event.getActiveVersion());
        contextManager.alterDataSourceAndRuleConfiguration(event.getSchemaName(), dataSourcePropertiesMap, ruleConfigs);
    }
    
    private void persistSchema(final String schemaName) {
        if (!metaDataPersistService.getDataSourceService().isExisted(schemaName)) {
            metaDataPersistService.getDataSourceService().persist(schemaName, new LinkedHashMap<>());
        }
        if (!metaDataPersistService.getSchemaRuleService().isExisted(schemaName)) {
            metaDataPersistService.getSchemaRuleService().persist(schemaName, new LinkedList<>());
        }
    }
    
    private void buildSpecialRules() {
        contextManager.getMetaDataContexts().getMetaDataMap().forEach((key, value) -> value.getRuleMetaData().getRules().forEach(each -> {
            if (each instanceof StatusContainedRule) {
                disableDataSources((StatusContainedRule) each);
            } else if (each instanceof InstanceAwareRule) {
                ((InstanceAwareRule) each).setInstanceContext(contextManager.getInstanceContext());
            }
        }));
    }
    
    private void disableDataSources(final StatusContainedRule rule) {
        Map<String, StorageNodeDataSource> storageNodes = registryCenter.getStorageNodeStatusService().loadStorageNodes();
        Map<String, StorageNodeDataSource> disableDataSources = storageNodes.entrySet().stream().filter(entry ->
                StorageNodeStatus.DISABLE.name().toLowerCase().equals(entry.getValue().getStatus())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        disableDataSources.entrySet().stream().forEach(entry -> rule.updateStatus(new DataSourceNameDisabledEvent(new QualifiedSchema(entry.getKey()), true)));
    }
}
