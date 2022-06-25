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
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.BatchYamlExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.StatusContainedRule;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.schema.SchemaChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.version.DatabaseVersionChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.ShowProcessListManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.lock.ShowProcessListSimpleLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.node.ProcessNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOnlineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListTriggerEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListUnitCompleteEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.StateEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.WorkerIdEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.PrimaryStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.StorageNodeChangedEvent;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.metadata.storage.event.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.mode.metadata.storage.event.StorageNodeDataSourceChangedEvent;

import java.sql.SQLException;
import java.util.Collection;
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
        disableDataSources();
    }
    
    /**
     * Renew to persist meta data.
     *
     * @param event database added event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void renew(final DatabaseAddedEvent event) throws SQLException {
        contextManager.addDatabase(event.getDatabaseName());
    }
    
    /**
     * Renew to delete database.
     *
     * @param event database delete event
     */
    @Subscribe
    public synchronized void renew(final DatabaseDeletedEvent event) {
        contextManager.deleteDatabase(event.getDatabaseName());
    }
    
    /**
     * Renew to added schema.
     *
     * @param event schema added event
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent event) {
        contextManager.addSchema(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Renew to delete schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent event) {
        contextManager.dropSchema(event.getDatabaseName(), event.getSchemaName());
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
        contextManager.alterSchema(event.getDatabaseName(), event.getSchemaName(), event.getChangedTableMetaData(), event.getDeletedTable());
    }
    
    /**
     * Renew rule configurations.
     *
     * @param event rule configurations changed event
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsChangedEvent event) {
        if (metaDataPersistService.getDatabaseVersionPersistService().isActiveVersion(event.getDatabaseName(), event.getDatabaseVersion())) {
            contextManager.alterRuleConfiguration(event.getDatabaseName(), event.getRuleConfigurations());
            disableDataSources();
        }
    }
    
    /**
     * Renew data source configuration.
     *
     * @param event data source changed event.
     */
    @Subscribe
    public synchronized void renew(final DataSourceChangedEvent event) {
        if (metaDataPersistService.getDatabaseVersionPersistService().isActiveVersion(event.getDatabaseName(), event.getDatabaseVersion())) {
            contextManager.alterDataSourceConfiguration(event.getDatabaseName(), event.getDataSourcePropertiesMap());
            disableDataSources();
        }
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param event Storage node changed event
     */
    @Subscribe
    public synchronized void renew(final StorageNodeChangedEvent event) {
        QualifiedDatabase qualifiedDatabase = event.getQualifiedDatabase();
        contextManager.getMetaDataContexts().getMetaData().getDatabases().get(qualifiedDatabase.getDatabaseName()).getRuleMetaData().getRules()
                .stream().filter(each -> each instanceof StatusContainedRule)
                .forEach(each -> ((StatusContainedRule) each).updateStatus(new StorageNodeDataSourceChangedEvent(qualifiedDatabase, event.getDataSource())));
    }
    
    /**
     * Renew primary data source names.
     *
     * @param event primary state changed event
     */
    @Subscribe
    public synchronized void renew(final PrimaryStateChangedEvent event) {
        QualifiedDatabase qualifiedDatabase = event.getQualifiedDatabase();
        contextManager.getMetaDataContexts().getMetaData().getDatabases().get(qualifiedDatabase.getDatabaseName()).getRuleMetaData().getRules()
                .stream()
                .filter(each -> each instanceof StatusContainedRule)
                .forEach(each -> ((StatusContainedRule) each)
                        .updateStatus(new PrimaryDataSourceChangedEvent(qualifiedDatabase)));
    }
    
    /**
     * Renew global rule configurations.
     *
     * @param event global rule configurations changed event
     */
    @Subscribe
    public synchronized void renew(final GlobalRuleConfigurationsChangedEvent event) {
        contextManager.alterGlobalRuleConfiguration(event.getRuleConfigurations());
        disableDataSources();
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
        if (contextManager.getInstanceContext().getInstance().getInstanceDefinition().getInstanceId().equals(event.getInstanceId())) {
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
        // TODO labels may be empty
        contextManager.getInstanceContext().updateLabel(event.getInstanceId(), event.getLabels());
    }
    
    /**
     * Renew instance list.
     *
     * @param event compute node online event
     */
    @Subscribe
    public synchronized void renew(final InstanceOnlineEvent event) {
        contextManager.getInstanceContext().addComputeNodeInstance(registryCenter.getComputeNodeStatusService().loadComputeNodeInstance(event.getInstanceDefinition()));
    }
    
    /**
     * Renew instance list.
     *
     * @param event compute node offline event
     */
    @Subscribe
    public synchronized void renew(final InstanceOfflineEvent event) {
        contextManager.getInstanceContext().deleteComputeNodeInstance(new ComputeNodeInstance(event.getInstanceDefinition()));
    }
    
    /**
     * Renew with new database version.
     *
     * @param event database version changed event
     */
    @Subscribe
    public synchronized void renew(final DatabaseVersionChangedEvent event) {
        Map<String, DataSourceProperties> dataSourcePropertiesMap = metaDataPersistService.getDataSourceService().load(event.getDatabaseName(), event.getActiveVersion());
        Collection<RuleConfiguration> ruleConfigs = metaDataPersistService.getDatabaseRulePersistService().load(event.getDatabaseName(), event.getActiveVersion());
        contextManager.alterDataSourceAndRuleConfiguration(event.getDatabaseName(), dataSourcePropertiesMap, ruleConfigs);
        disableDataSources();
    }
    
    /**
     * Trigger show process list.
     *
     * @param event show process list trigger event
     */
    @Subscribe
    public synchronized void triggerShowProcessList(final ShowProcessListTriggerEvent event) {
        if (!event.getInstanceId().equals(contextManager.getInstanceContext().getInstance().getInstanceDefinition().getInstanceId())) {
            return;
        }
        Collection<YamlExecuteProcessContext> processContexts = ShowProcessListManager.getInstance().getAllProcessContext();
        if (!processContexts.isEmpty()) {
            registryCenter.getRepository().persist(ProcessNode.getShowProcessListInstancePath(event.getShowProcessListId(), event.getInstanceId()),
                    YamlEngine.marshal(new BatchYamlExecuteProcessContext(new LinkedList<>(processContexts))));
        }
        registryCenter.getRepository().delete(ComputeNode.getProcessTriggerInstanceIdNodePath(event.getInstanceId(), event.getShowProcessListId()));
    }
    
    /**
     * Complete unit show process list.
     *
     * @param event show process list unit complete event
     */
    @Subscribe
    public synchronized void completeUnitShowProcessList(final ShowProcessListUnitCompleteEvent event) {
        ShowProcessListSimpleLock simpleLock = ShowProcessListManager.getInstance().getLocks().get(event.getShowProcessListId());
        if (null != simpleLock) {
            simpleLock.doNotify();
        }
    }
    
    private void disableDataSources() {
        contextManager.getMetaDataContexts().getMetaData().getDatabases().forEach((key, value) -> value.getRuleMetaData().getRules().forEach(each -> {
            if (each instanceof StatusContainedRule) {
                disableDataSources((StatusContainedRule) each);
            }
        }));
    }
    
    private void disableDataSources(final StatusContainedRule rule) {
        Map<String, StorageNodeDataSource> storageNodes = registryCenter.getStorageNodeStatusService().loadStorageNodes();
        Map<String, StorageNodeDataSource> disableDataSources = storageNodes.entrySet().stream().filter(entry -> StorageNodeStatus.isDisable(entry.getValue().getStatus()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        disableDataSources.forEach((key, value) -> rule.updateStatus(new StorageNodeDataSourceChangedEvent(new QualifiedDatabase(key), value)));
    }
}
