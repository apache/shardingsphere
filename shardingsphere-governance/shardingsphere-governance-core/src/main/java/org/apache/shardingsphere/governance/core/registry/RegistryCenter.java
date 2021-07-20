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

package org.apache.shardingsphere.governance.core.registry;

import lombok.Getter;
import org.apache.shardingsphere.governance.core.GovernanceInstance;
import org.apache.shardingsphere.governance.core.lock.service.LockRegistryService;
import org.apache.shardingsphere.governance.core.registry.cache.subscriber.ScalingRegistrySubscriber;
import org.apache.shardingsphere.governance.core.registry.config.subscriber.DataSourceRegistrySubscriber;
import org.apache.shardingsphere.governance.core.registry.config.subscriber.GlobalRuleRegistrySubscriber;
import org.apache.shardingsphere.governance.core.registry.config.subscriber.SchemaRuleRegistrySubscriber;
import org.apache.shardingsphere.governance.core.registry.metadata.subscriber.SchemaMetaDataRegistrySubscriber;
import org.apache.shardingsphere.governance.core.registry.process.subscriber.ProcessRegistrySubscriber;
import org.apache.shardingsphere.governance.core.registry.state.service.DataSourceStatusRegistryService;
import org.apache.shardingsphere.governance.core.registry.state.service.InstanceStatusRegistryService;
import org.apache.shardingsphere.governance.core.registry.state.subscriber.DataSourceStatusRegistrySubscriber;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;

import java.util.Collection;

/**
 * Registry center.
 */
public final class RegistryCenter {
    
    private final String instanceId;
    
    @Getter
    private final DataSourceStatusRegistryService dataSourceStatusService;
    
    @Getter
    private final InstanceStatusRegistryService instanceStatusService;
    
    @Getter
    private final LockRegistryService lockService;
    
    private final GovernanceWatcherFactory listenerFactory;
    
    public RegistryCenter(final RegistryCenterRepository repository) {
        instanceId = GovernanceInstance.getInstance().getId();
        dataSourceStatusService = new DataSourceStatusRegistryService(repository);
        instanceStatusService = new InstanceStatusRegistryService(repository);
        lockService = new LockRegistryService(repository);
        listenerFactory = new GovernanceWatcherFactory(repository);
        createSubscribers(repository);
    }
    
    private void createSubscribers(final RegistryCenterRepository repository) {
        new DataSourceRegistrySubscriber(repository);
        new SchemaMetaDataRegistrySubscriber(repository);
        new GlobalRuleRegistrySubscriber(repository);
        new SchemaRuleRegistrySubscriber(repository);
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
