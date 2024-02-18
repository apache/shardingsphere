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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.infra.state.datasource.DataSourceStateManager;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.NewRegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.cluster.event.ClusterLockDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.cluster.event.ClusterStateEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.cluster.event.ClusterStatusChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOnlineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.StateEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.WorkerIdEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.StorageNodeChangedEvent;

import java.util.Optional;

/**
 * TODO replace the old StateChangedSubscriber after meta data refactor completed
 * New state changed subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class NewStateChangedSubscriber {
    
    private final NewRegistryCenter registryCenter;
    
    private final ContextManager contextManager;
    
    public NewStateChangedSubscriber(final NewRegistryCenter registryCenter, final ContextManager contextManager) {
        this.registryCenter = registryCenter;
        this.contextManager = contextManager;
        contextManager.getInstanceContext().getEventBusContext().register(this);
    }
    
    /**
     * Renew disabled data source names.
     * 
     * @param event Storage node changed event
     */
    @Subscribe
    public synchronized void renew(final StorageNodeChangedEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getQualifiedDatabase().getDatabaseName())) {
            return;
        }
        QualifiedDatabase qualifiedDatabase = event.getQualifiedDatabase();
        Optional<StaticDataSourceContainedRule> staticDataSourceRule = contextManager.getMetaDataContexts()
                .getMetaData().getDatabase(qualifiedDatabase.getDatabaseName()).getRuleMetaData().findSingleRule(StaticDataSourceContainedRule.class);
        staticDataSourceRule.ifPresent(optional -> optional.updateStatus(new StorageNodeDataSourceChangedEvent(qualifiedDatabase, event.getDataSource())));
        DataSourceStateManager.getInstance().updateState(
                qualifiedDatabase.getDatabaseName(), qualifiedDatabase.getDataSourceName(), DataSourceState.valueOf(event.getDataSource().getStatus().name()));
    }
    
    /**
     * Reset cluster state.
     * 
     * @param event cluster lock deleted event
     */
    @Subscribe
    public synchronized void renew(final ClusterLockDeletedEvent event) {
        contextManager.getInstanceContext().getEventBusContext().post(new ClusterStatusChangedEvent(event.getState()));
    }
    
    /**
     * Renew cluster state.
     * 
     * @param event cluster state event
     */
    @Subscribe
    public synchronized void renew(final ClusterStateEvent event) {
        contextManager.updateClusterState(event.getStatus());
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
        contextManager.getInstanceContext().updateWorkerId(event.getInstanceId(), event.getWorkerId());
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
}
