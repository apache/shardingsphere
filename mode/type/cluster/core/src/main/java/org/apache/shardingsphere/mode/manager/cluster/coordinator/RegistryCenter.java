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
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.lock.LockPersistService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcherFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.subscriber.SchemaMetaDataRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.subscriber.ShardingSphereSchemaDataRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.service.ComputeNodeStatusService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.subscriber.ComputeNodeStatusSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.service.StorageNodeStatusService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.subscriber.StorageNodeStatusSubscriber;
import org.apache.shardingsphere.mode.manager.cluster.process.subscriber.ProcessRegistrySubscriber;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Map;

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
    private final LockPersistService lockPersistService;
    
    @Getter
    private final EventBusContext eventBusContext;
    
    private final InstanceMetaData instanceMetaData;
    
    private final Map<String, DatabaseConfiguration> databaseConfigs;
    
    private final GovernanceWatcherFactory listenerFactory;
    
    public RegistryCenter(final ClusterPersistRepository repository, final EventBusContext eventBusContext,
                          final InstanceMetaData instanceMetaData, final Map<String, DatabaseConfiguration> databaseConfigs) {
        this.repository = repository;
        this.eventBusContext = eventBusContext;
        this.instanceMetaData = instanceMetaData;
        this.databaseConfigs = databaseConfigs;
        storageNodeStatusService = new StorageNodeStatusService(repository);
        computeNodeStatusService = new ComputeNodeStatusService(repository);
        lockPersistService = new ClusterLockPersistService(repository.getDistributedLockHolder());
        listenerFactory = new GovernanceWatcherFactory(repository, eventBusContext, getJDBCDatabaseName());
        createSubscribers(repository);
    }
    
    private String getJDBCDatabaseName() {
        return instanceMetaData instanceof JDBCInstanceMetaData ? databaseConfigs.keySet().stream().findFirst().orElse(null) : null;
    }
    
    private void createSubscribers(final ClusterPersistRepository repository) {
        new SchemaMetaDataRegistrySubscriber(repository, eventBusContext);
        new ComputeNodeStatusSubscriber(this, repository);
        new StorageNodeStatusSubscriber(repository, eventBusContext);
        new ProcessRegistrySubscriber(repository, eventBusContext);
        new ShardingSphereSchemaDataRegistrySubscriber(repository, eventBusContext);
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
