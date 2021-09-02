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

package org.apache.shardingsphere.mode.manager.cluster;

import lombok.Getter;
import org.apache.shardingsphere.mode.manager.cluster.governance.GovernanceInstance;
import org.apache.shardingsphere.mode.manager.cluster.governance.lock.service.LockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.GovernanceWatcherFactory;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.cache.subscriber.ScalingRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.config.subscriber.GlobalRuleRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.metadata.subscriber.SchemaMetaDataRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.process.subscriber.ProcessRegistrySubscriber;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.state.service.DataSourceStatusRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.state.service.InstanceStatusRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.governance.registry.state.subscriber.DataSourceStatusRegistrySubscriber;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;

/**
 * Registry center.
 */
public final class RegistryCenter {
    
    private final String instanceId;
    
    @Getter
    private final ClusterPersistRepository repository;
    
    @Getter
    private final DataSourceStatusRegistryService dataSourceStatusService;
    
    @Getter
    private final InstanceStatusRegistryService instanceStatusService;
    
    @Getter
    private final LockRegistryService lockService;
    
    private final GovernanceWatcherFactory listenerFactory;
    
    public RegistryCenter(final ClusterPersistRepository repository) {
        this.repository = repository;
        instanceId = GovernanceInstance.getInstance().getId();
        dataSourceStatusService = new DataSourceStatusRegistryService(repository);
        instanceStatusService = new InstanceStatusRegistryService(repository);
        lockService = new LockRegistryService(repository);
        listenerFactory = new GovernanceWatcherFactory(repository);
        createSubscribers(repository);
    }
    
    private void createSubscribers(final ClusterPersistRepository repository) {
        new SchemaMetaDataRegistrySubscriber(repository);
        new GlobalRuleRegistrySubscriber(repository);
        new DataSourceStatusRegistrySubscriber(repository);
        new ScalingRegistrySubscriber(repository);
        new ProcessRegistrySubscriber(repository);
    }
    
    /**
     * Online instance.
     * 
     * @param schemaNames schema names
     */
    public void onlineInstance(final Collection<String> schemaNames) {
        instanceStatusService.registerInstanceOnline(instanceId);
        listenerFactory.watchListeners(schemaNames);
    }
}
