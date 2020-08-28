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

package org.apache.shardingsphere.orchestration.core.facade.listener;

import org.apache.shardingsphere.orchestration.core.config.listener.ConfigurationListenerManager;
import org.apache.shardingsphere.orchestration.core.metadata.listener.MetaDataListenerManager;
import org.apache.shardingsphere.orchestration.core.registry.listener.RegistryListenerManager;
import org.apache.shardingsphere.orchestration.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.orchestration.repository.api.RegistryRepository;

import java.util.Collection;

/**
 * Orchestration listener manager.
 */
public final class OrchestrationListenerManager {
    
    private final ConfigurationListenerManager configurationListenerManager;
    
    private final RegistryListenerManager registryListenerManager;
    
    private final MetaDataListenerManager metaDataListenerManager;
    
    public OrchestrationListenerManager(final RegistryRepository registryRepository, final ConfigurationRepository configurationRepository, final Collection<String> schemaNames) {
        configurationListenerManager = new ConfigurationListenerManager(configurationRepository, schemaNames);
        registryListenerManager = new RegistryListenerManager(registryRepository);
        metaDataListenerManager = new MetaDataListenerManager(configurationRepository, schemaNames);
    }
    
    /**
     * Initialize all orchestration listeners.
     */
    public void init() {
        configurationListenerManager.initListeners();
        registryListenerManager.initListeners();
        metaDataListenerManager.initListeners();
    }
}
