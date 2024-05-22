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
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.rule.attribute.datasource.StaticDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.infra.state.datasource.DataSourceStateManager;
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.cluster.event.ClusterStateEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOnlineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ComputeNodeInstanceStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.WorkerIdEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.StorageNodeChangedEvent;

/**
 * State changed subscriber.
 */
@SuppressWarnings("unused")
public final class StateChangedSubscriber implements EventSubscriber {
    
    private final ContextManager contextManager;
    
    public StateChangedSubscriber(final ContextManager contextManager) {
        this.contextManager = contextManager;
    }
    
    /**
     * Renew disabled data source names.
     * 
     * @param event Storage node changed event
     */
    @Subscribe
    public synchronized void renew(final StorageNodeChangedEvent event) {
        QualifiedDataSource qualifiedDataSource = event.getQualifiedDataSource();
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(qualifiedDataSource.getDatabaseName())) {
            return;
        }
        DataSourceStateManager.getInstance().updateState(qualifiedDataSource, DataSourceState.valueOf(event.getStatus().getStatus().name()));
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabase(qualifiedDataSource.getDatabaseName());
        for (StaticDataSourceRuleAttribute each : database.getRuleMetaData().getAttributes(StaticDataSourceRuleAttribute.class)) {
            each.updateStatus(qualifiedDataSource, event.getStatus().getStatus());
        }
    }
    
    /**
     * Renew cluster state.
     * 
     * @param event cluster state event
     */
    @Subscribe
    public synchronized void renew(final ClusterStateEvent event) {
        contextManager.getStateContext().switchCurrentClusterState(event.getClusterState());
    }
    
    /**
     * Renew compute node instance state.
     * 
     * @param event compute node instance state changed event
     */
    @Subscribe
    public synchronized void renew(final ComputeNodeInstanceStateChangedEvent event) {
        contextManager.getComputeNodeInstanceContext().updateStatus(event.getInstanceId(), event.getStatus());
    }
    
    /**
     * Renew instance worker id.
     * 
     * @param event worker id event
     */
    @Subscribe
    public synchronized void renew(final WorkerIdEvent event) {
        contextManager.getComputeNodeInstanceContext().updateWorkerId(event.getInstanceId(), event.getWorkerId());
    }
    
    /**
     * Renew instance labels.
     * 
     * @param event label event
     */
    @Subscribe
    public synchronized void renew(final LabelsEvent event) {
        // TODO labels may be empty
        contextManager.getComputeNodeInstanceContext().updateLabel(event.getInstanceId(), event.getLabels());
    }
    
    /**
     * Renew instance list.
     *
     * @param event compute node online event
     */
    @Subscribe
    public synchronized void renew(final InstanceOnlineEvent event) {
        contextManager.getComputeNodeInstanceContext().addComputeNodeInstance(contextManager.getPersistServiceFacade()
                .getComputeNodePersistService().loadComputeNodeInstance(event.getInstanceMetaData()));
    }
    
    /**
     * Renew instance list.
     *
     * @param event compute node offline event
     */
    @Subscribe
    public synchronized void renew(final InstanceOfflineEvent event) {
        contextManager.getComputeNodeInstanceContext().deleteComputeNodeInstance(new ComputeNodeInstance(event.getInstanceMetaData()));
    }
}
