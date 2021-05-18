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

import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Collection;

/**
 * Governance listener manager.
 */
public final class GovernanceListenerManager {
    
    static {
        ShardingSphereServiceLoader.register(GovernanceListenerFactory.class);
    }
    
    private final RegistryCenterRepository registryCenterRepository;
    
    private final Collection<String> schemaNames;
    
    private final Collection<GovernanceListenerFactory> governanceListenerFactories;
    
    public GovernanceListenerManager(final RegistryCenterRepository registryCenterRepository, final Collection<String> schemaNames) {
        this.registryCenterRepository = registryCenterRepository;
        this.schemaNames = schemaNames;
        governanceListenerFactories = ShardingSphereServiceLoader.getSingletonServiceInstances(GovernanceListenerFactory.class);
    }
    
    /**
     * Initialize all state changed listeners.
     */
    public void initListeners() {
        for (GovernanceListenerFactory each : governanceListenerFactories) {
            each.create(registryCenterRepository, schemaNames).watch(each.getWatchTypes().toArray(new Type[0]));
        }
    }
}
