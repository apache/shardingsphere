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
import org.apache.shardingsphere.infra.datasource.state.DataSourceState;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DynamicDataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOnlineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.StateEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.PrimaryStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.StorageNodeChangedEvent;
import org.apache.shardingsphere.mode.metadata.storage.event.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.mode.metadata.storage.event.StorageNodeDataSourceChangedEvent;

import java.util.Optional;

/**
 * State changed subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class StateChangedSubscriber {
    
    private final RegistryCenter registryCenter;
    
    private final ContextManager contextManager;
    
    public StateChangedSubscriber(final RegistryCenter registryCenter, final ContextManager contextManager) {
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
}
