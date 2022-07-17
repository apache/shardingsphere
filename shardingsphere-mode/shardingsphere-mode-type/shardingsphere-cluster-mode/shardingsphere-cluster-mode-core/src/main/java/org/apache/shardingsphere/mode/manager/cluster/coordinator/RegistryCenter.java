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

import lombok.Getter;
import org.apache.shardingsphere.infra.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.MutexLockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcherFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.subscriber.ScalingRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.subscriber.SchemaMetaDataRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.subscriber.ProcessRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.service.ComputeNodeStatusService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.subscriber.ComputeNodeStatusSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.service.StorageNodeStatusService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.subscriber.StorageNodeStatusSubscriber;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Registry center.
 */
public final class RegistryCenter {
    
    @Getter
    private final ClusterPersistRepository repository;
    
    @Getter
    private final StorageNodeStatusService storageNodeStatusService;
    
    @Getter
    private final ComputeNodeStatusService computeNodeStatusService;
    
    @Getter
    private final LockRegistryService lockService;
    
    @Getter
    private final EventBusContext eventBusContext;
    
    private final GovernanceWatcherFactory listenerFactory;
    
    public RegistryCenter(final ClusterPersistRepository repository, final EventBusContext eventBusContext) {
        this.repository = repository;
        this.eventBusContext = eventBusContext;
        storageNodeStatusService = new StorageNodeStatusService(repository);
        computeNodeStatusService = new ComputeNodeStatusService(repository);
        lockService = new MutexLockRegistryService(repository);
        listenerFactory = new GovernanceWatcherFactory(repository, eventBusContext);
        createSubscribers(repository);
    }
    
    private void createSubscribers(final ClusterPersistRepository repository) {
        new SchemaMetaDataRegistrySubscriber(repository, eventBusContext);
        new ComputeNodeStatusSubscriber(this, repository);
        new StorageNodeStatusSubscriber(repository, eventBusContext);
        new ScalingRegistrySubscriber(repository, eventBusContext);
        new ProcessRegistrySubscriber(repository, eventBusContext);
    }
    
    /**
     * Online instance.
     * 
     * @param computeNodeInstance compute node instance
     */
    public void onlineInstance(final ComputeNodeInstance computeNodeInstance) {
        computeNodeStatusService.registerOnline(computeNodeInstance.getMetaData());
        computeNodeStatusService.persistInstanceLabels(computeNodeInstance.getCurrentInstanceId(), computeNodeInstance.getLabels());
        listenerFactory.watchListeners();
    }
}
