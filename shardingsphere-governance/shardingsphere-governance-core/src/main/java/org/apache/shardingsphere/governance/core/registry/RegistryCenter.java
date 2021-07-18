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
import org.apache.shardingsphere.infra.config.persist.service.SchemaMetaDataPersistService;
import org.apache.shardingsphere.governance.core.registry.process.subscriber.ProcessRegistrySubscriber;
import org.apache.shardingsphere.governance.core.registry.state.service.DataSourceStatusRegistryService;
import org.apache.shardingsphere.governance.core.registry.state.service.InstanceStatusRegistryService;
import org.apache.shardingsphere.governance.core.registry.state.subscriber.DataSourceStatusRegistrySubscriber;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;

/**
 * Registry center.
 */
@Getter
public final class RegistryCenter {
    
    private final String instanceId;
    
    private final SchemaMetaDataPersistService schemaService;
    
    private final DataSourceStatusRegistryService dataSourceStatusService;
    
    private final InstanceStatusRegistryService instanceStatusService;
    
    private final LockRegistryService lockService;
    
    public RegistryCenter(final RegistryCenterRepository repository) {
        instanceId = GovernanceInstance.getInstance().getId();
        schemaService = new SchemaMetaDataPersistService(repository);
        dataSourceStatusService = new DataSourceStatusRegistryService(repository);
        instanceStatusService = new InstanceStatusRegistryService(repository);
        lockService = new LockRegistryService(repository);
        createSubscribers(repository);
    }
    
    private void createSubscribers(final RegistryCenterRepository repository) {
        new DataSourceRegistrySubscriber(repository);
        new GlobalRuleRegistrySubscriber(repository);
        new SchemaRuleRegistrySubscriber(repository);
        new DataSourceStatusRegistrySubscriber(repository);
        new ScalingRegistrySubscriber(repository);
        new ProcessRegistrySubscriber(repository);
    }
    
    /**
     * Register instance online.
     */
    public void registerInstanceOnline() {
        instanceStatusService.registerInstanceOnline(instanceId);
    }
}
