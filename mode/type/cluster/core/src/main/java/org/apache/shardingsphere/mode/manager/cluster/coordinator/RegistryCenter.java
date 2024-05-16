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
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcherFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.service.ComputeNodeStatusService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Map;

/**
 * Registry center.
 */
public final class RegistryCenter {
    
    @Getter
    private final ClusterPersistRepository repository;
    
    private final InstanceMetaData instanceMetaData;
    
    private final Map<String, DatabaseConfiguration> databaseConfigs;
    
    @Getter
    private final ComputeNodeStatusService computeNodeStatusService;
    
    private final GovernanceWatcherFactory listenerFactory;
    
    public RegistryCenter(final EventBusContext eventBusContext,
                          final ClusterPersistRepository repository, final InstanceMetaData instanceMetaData, final Map<String, DatabaseConfiguration> databaseConfigs) {
        this.repository = repository;
        this.instanceMetaData = instanceMetaData;
        this.databaseConfigs = databaseConfigs;
        computeNodeStatusService = new ComputeNodeStatusService(repository);
        listenerFactory = new GovernanceWatcherFactory(repository, eventBusContext, getJDBCDatabaseName());
    }
    
    private String getJDBCDatabaseName() {
        return instanceMetaData instanceof JDBCInstanceMetaData ? databaseConfigs.keySet().stream().findFirst().orElse(null) : null;
    }
    
    /**
     * Online instance.
     * 
     * @param computeNodeInstance compute node instance
     */
    public void onlineInstance(final ComputeNodeInstance computeNodeInstance) {
        computeNodeStatusService.registerOnline(computeNodeInstance.getMetaData());
        computeNodeStatusService.persistInstanceLabels(computeNodeInstance.getCurrentInstanceId(), computeNodeInstance.getLabels());
        computeNodeStatusService.persistInstanceState(computeNodeInstance.getCurrentInstanceId(), computeNodeInstance.getState());
        listenerFactory.watchListeners();
    }
}
