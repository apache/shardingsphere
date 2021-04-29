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

package org.apache.shardingsphere.governance.core.registry.listener;

import org.apache.shardingsphere.governance.core.registry.listener.metadata.MetaDataListener;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Collection;

/**
 * Registry listener manager.
 */
public final class RegistryListenerManager {
    
    static {
        ShardingSphereServiceLoader.register(GovernanceListenerFactory.class);
    }
    
    private final RegistryRepository registryRepository;
    
    private final Collection<String> schemaNames;
    
    private final MetaDataListener metaDataListener;
    
    private final Collection<GovernanceListenerFactory> governanceListenerFactories;
    
    public RegistryListenerManager(final RegistryRepository registryRepository, final Collection<String> schemaNames) {
        this.registryRepository = registryRepository;
        this.schemaNames = schemaNames;
        metaDataListener = new MetaDataListener(registryRepository, schemaNames);
        governanceListenerFactories = ShardingSphereServiceLoader.getSingletonServiceInstances(GovernanceListenerFactory.class);
    }
    
    /**
     * Initialize all state changed listeners.
     */
    public void initListeners() {
        metaDataListener.watch();
        for (GovernanceListenerFactory each : governanceListenerFactories) {
            each.create(registryRepository, schemaNames).watch(each.getWatchTypes().toArray(new Type[0]));
        }
    }
}
