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
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.state.DataSourceState;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.BatchYamlExecuteProcessContext;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DynamicDataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.version.DatabaseVersionChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.DatabaseDataAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.DatabaseDataDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.SchemaDataAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.SchemaDataDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.TableDataChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOnlineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.KillProcessListIdEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.KillProcessListIdUnitCompleteEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListTriggerEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListUnitCompleteEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.StateEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.PrimaryStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.StorageNodeChangedEvent;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.metadata.storage.event.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.mode.metadata.storage.event.StorageNodeDataSourceChangedEvent;
import org.apache.shardingsphere.mode.process.ShowProcessListManager;
import org.apache.shardingsphere.mode.process.lock.ShowProcessListSimpleLock;
import org.apache.shardingsphere.mode.process.node.ProcessNode;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Context manager coordinator.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ContextManagerCoordinator {
    
    private final MetaDataPersistService persistService;
    
    private final RegistryCenter registryCenter;
    
    private final ContextManager contextManager;
    
    public ContextManagerCoordinator(final MetaDataPersistService persistService, final RegistryCenter registryCenter, final ContextManager contextManager) {
        this.persistService = persistService;
        this.registryCenter = registryCenter;
        this.contextManager = contextManager;
        contextManager.getInstanceContext().getEventBusContext().register(this);
        new ResourceMetaDataCoordinator(contextManager);
        disableDataSources();
    }
    
    /**
     * Renew rule configurations.
     *
     * @param event rule configurations changed event
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsChangedEvent event) {
        if (persistService.getMetaDataVersionPersistService().isActiveVersion(event.getDatabaseName(), event.getDatabaseVersion())) {
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
        if (persistService.getMetaDataVersionPersistService().isActiveVersion(event.getDatabaseName(), event.getDatabaseVersion())) {
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
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getQualifiedDatabase().getDatabaseName())) {
            return;
        }
        Optional<ShardingSphereRule> dynamicDataSourceRule = contextManager.getMetaDataContexts().getMetaData().getDatabase(qualifiedDatabase.getDatabaseName()).getRuleMetaData()
                .getRules().stream().filter(each -> each instanceof DynamicDataSourceContainedRule).findFirst();
        if (dynamicDataSourceRule.isPresent()) {
            ((DynamicDataSourceContainedRule) dynamicDataSourceRule.get()).updateStatus(new StorageNodeDataSourceChangedEvent(qualifiedDatabase, event.getDataSource()));
            return;
        }
        Optional<ShardingSphereRule> staticDataSourceRule = contextManager.getMetaDataContexts().getMetaData().getDatabase(qualifiedDatabase.getDatabaseName()).getRuleMetaData()
                .getRules().stream().filter(each -> each instanceof StaticDataSourceContainedRule).findFirst();
        staticDataSourceRule.ifPresent(optional -> ((StaticDataSourceContainedRule) optional)
                .updateStatus(new StorageNodeDataSourceChangedEvent(qualifiedDatabase, event.getDataSource())));
        DataSourceStateManager.getInstance().updateState(
                qualifiedDatabase.getDatabaseName(), qualifiedDatabase.getDataSourceName(), DataSourceState.valueOf(event.getDataSource().getStatus().toUpperCase()));
    }
    
    /**
     * Renew primary data source names.
     *
     * @param event primary state changed event
     */
    @Subscribe
    public synchronized void renew(final PrimaryStateChangedEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getQualifiedDatabase().getDatabaseName())) {
            return;
        }
        QualifiedDatabase qualifiedDatabase = event.getQualifiedDatabase();
        contextManager.getMetaDataContexts().getMetaData().getDatabase(qualifiedDatabase.getDatabaseName()).getRuleMetaData().getRules()
                .stream()
                .filter(each -> each instanceof DynamicDataSourceContainedRule)
                .forEach(each -> ((DynamicDataSourceContainedRule) each)
                        .restartHeartBeatJob(new PrimaryDataSourceChangedEvent(qualifiedDatabase)));
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
        contextManager.getInstanceContext().addComputeNodeInstance(registryCenter.getComputeNodeStatusService().loadComputeNodeInstance(event.getInstanceMetaData()));
    }
    
    /**
     * Renew instance list.
     *
     * @param event compute node offline event
     */
    @Subscribe
    public synchronized void renew(final InstanceOfflineEvent event) {
        contextManager.getInstanceContext().deleteComputeNodeInstance(new ComputeNodeInstance(event.getInstanceMetaData()));
    }
    
    /**
     * Renew with new database version.
     *
     * @param event database version changed event
     */
    @Subscribe
    public synchronized void renew(final DatabaseVersionChangedEvent event) {
        Map<String, DataSourceProperties> dataSourcePropertiesMap = persistService.getDataSourceService().load(event.getDatabaseName(), event.getActiveVersion());
        Collection<RuleConfiguration> ruleConfigs = persistService.getDatabaseRulePersistService().load(event.getDatabaseName(), event.getActiveVersion());
        contextManager.alterDataSourceAndRuleConfiguration(event.getDatabaseName(), dataSourcePropertiesMap, ruleConfigs);
        disableDataSources();
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
     * Renew to persist ShardingSphere database data.
     *
     * @param event database data added event
     */
    @Subscribe
    public synchronized void renew(final DatabaseDataAddedEvent event) {
        contextManager.addShardingSphereDatabaseData(event.getDatabaseName());
    }
    
    /**
     * Renew to delete ShardingSphere data database.
     *
     * @param event database delete event
     */
    @Subscribe
    public synchronized void renew(final DatabaseDataDeletedEvent event) {
        contextManager.dropShardingSphereDatabaseData(event.getDatabaseName());
    }
    
    /**
     * Renew to added ShardingSphere data schema.
     *
     * @param event schema added event
     */
    @Subscribe
    public synchronized void renew(final SchemaDataAddedEvent event) {
        contextManager.addShardingSphereSchemaData(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Renew to delete ShardingSphere data schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDataDeletedEvent event) {
        contextManager.dropShardingSphereSchemaData(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Renew ShardingSphere data of the table.
     *
     * @param event table data changed event
     */
    @Subscribe
    public synchronized void renew(final TableDataChangedEvent event) {
        contextManager.alterSchemaData(event.getDatabaseName(), event.getSchemaName(), event.getChangedTableData());
        contextManager.alterSchemaData(event.getDatabaseName(), event.getSchemaName(), event.getDeletedTable());
    }
    
    /**
     * Trigger show process list.
     *
     * @param event show process list trigger event
     */
    @Subscribe
    public synchronized void triggerShowProcessList(final ShowProcessListTriggerEvent event) {
        if (!event.getInstanceId().equals(contextManager.getInstanceContext().getInstance().getMetaData().getId())) {
            return;
        }
        Collection<ExecuteProcessContext> processContexts = ShowProcessListManager.getInstance().getAllProcessContext();
        if (!processContexts.isEmpty()) {
            registryCenter.getRepository().persist(ProcessNode.getProcessListInstancePath(event.getProcessListId(), event.getInstanceId()),
                    YamlEngine.marshal(new BatchYamlExecuteProcessContext(processContexts)));
        }
        registryCenter.getRepository().delete(ComputeNode.getProcessTriggerInstanceIdNodePath(event.getInstanceId(), event.getProcessListId()));
    }
    
    /**
     * Trigger show process list.
     *
     * @param event show process list trigger event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void killProcessListId(final KillProcessListIdEvent event) throws SQLException {
        if (!event.getInstanceId().equals(contextManager.getInstanceContext().getInstance().getMetaData().getId())) {
            return;
        }
        Collection<Statement> statements = ShowProcessListManager.getInstance().getProcessStatement(event.getProcessListId());
        for (Statement statement : statements) {
            statement.cancel();
        }
        registryCenter.getRepository().delete(ComputeNode.getProcessKillInstanceIdNodePath(event.getInstanceId(), event.getProcessListId()));
    }
    
    /**
     * Complete unit show process list.
     *
     * @param event show process list unit complete event
     */
    @Subscribe
    public synchronized void completeUnitShowProcessList(final ShowProcessListUnitCompleteEvent event) {
        ShowProcessListSimpleLock simpleLock = ShowProcessListManager.getInstance().getLocks().get(event.getProcessListId());
        if (null != simpleLock) {
            simpleLock.doNotify();
        }
    }
    
    /**
     * Complete unit kill process list id.
     *
     * @param event kill process list id unit complete event
     */
    @Subscribe
    public synchronized void completeUnitKillProcessListId(final KillProcessListIdUnitCompleteEvent event) {
        ShowProcessListSimpleLock simpleLock = ShowProcessListManager.getInstance().getLocks().get(event.getProcessListId());
        if (null != simpleLock) {
            simpleLock.doNotify();
        }
    }
    
    private void disableDataSources() {
        contextManager.getMetaDataContexts().getMetaData().getDatabases().forEach((key, value) -> value.getRuleMetaData().getRules().forEach(each -> {
            if (each instanceof StaticDataSourceContainedRule) {
                disableDataSources((StaticDataSourceContainedRule) each);
            }
        }));
    }
    
    private void disableDataSources(final StaticDataSourceContainedRule rule) {
        Map<String, StorageNodeDataSource> storageNodes = registryCenter.getStorageNodeStatusService().loadStorageNodes();
        Map<String, StorageNodeDataSource> disableDataSources = storageNodes.entrySet().stream().filter(entry -> StorageNodeStatus.isDisable(entry.getValue().getStatus()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        disableDataSources.forEach((key, value) -> rule.updateStatus(new StorageNodeDataSourceChangedEvent(new QualifiedDatabase(key), value)));
    }
}
