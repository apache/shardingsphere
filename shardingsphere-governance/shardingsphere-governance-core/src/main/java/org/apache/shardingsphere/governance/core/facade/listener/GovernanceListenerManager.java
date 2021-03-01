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

package org.apache.shardingsphere.governance.core.facade.listener;

import org.apache.shardingsphere.governance.core.config.listener.ConfigurationListenerManager;
import org.apache.shardingsphere.governance.core.registry.listener.RegistryListenerManager;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;

import java.util.Collection;

/**
 * Governance listener manager.
 */
public final class GovernanceListenerManager {
    
    private final ConfigurationListenerManager configurationListenerManager;
    
    private final RegistryListenerManager registryListenerManager;
    
    public GovernanceListenerManager(final RegistryRepository registryRepository, final ConfigurationRepository configurationRepository, final Collection<String> schemaNames) {
        configurationListenerManager = new ConfigurationListenerManager(configurationRepository, schemaNames);
        registryListenerManager = new RegistryListenerManager(registryRepository, schemaNames);
    }
    
    /**
     * Initialize all governance listeners.
     */
    public void init() {
        configurationListenerManager.initListeners();
        registryListenerManager.initListeners();
    }
}
