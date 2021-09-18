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
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcherFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.subscriber.ScalingRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.subscriber.GlobalRuleRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.subscriber.SchemaMetaDataRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.subscriber.ProcessRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.service.StorageNodeStatusService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.service.ComputeNodeStatusService;
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
    
    private final GovernanceWatcherFactory listenerFactory;
    
    public RegistryCenter(final ClusterPersistRepository repository, final Integer port) {
        this.repository = repository;
        ClusterInstance.getInstance().init(port);
        storageNodeStatusService = new StorageNodeStatusService(repository);
        computeNodeStatusService = new ComputeNodeStatusService(repository);
        lockService = new LockRegistryService(repository);
        listenerFactory = new GovernanceWatcherFactory(repository);
        createSubscribers(repository);
    }
    
    private void createSubscribers(final ClusterPersistRepository repository) {
        new SchemaMetaDataRegistrySubscriber(repository);
        new GlobalRuleRegistrySubscriber(repository);
        new StorageNodeStatusSubscriber(repository);
        new ScalingRegistrySubscriber(repository);
        new ProcessRegistrySubscriber(repository);
    }
    
    /**
     * Online instance.
     */
    public void onlineInstance() {
        computeNodeStatusService.registerOnline(ClusterInstance.getInstance().getId());
        listenerFactory.watchListeners();
    }
}
