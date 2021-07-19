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

package org.apache.shardingsphere.governance.core;

import lombok.Getter;
import org.apache.shardingsphere.governance.core.registry.GovernanceWatcherFactory;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;

import java.util.Collection;

/**
 * Governance facade.
 */
public final class GovernanceFacade {
    
    @Getter
    private RegistryCenter registryCenter;
    
    private GovernanceWatcherFactory listenerFactory;
    
    /**
     * Initialize governance facade.
     *
     * @param repository registry center repository
     * @param schemaNames schema names
     */
    public void init(final RegistryCenterRepository repository, final Collection<String> schemaNames) {
        registryCenter = new RegistryCenter(repository);
        listenerFactory = new GovernanceWatcherFactory(repository, schemaNames);
    }
    
    /**
     * Online instance.
     */
    public void onlineInstance() {
        registryCenter.registerInstanceOnline();
        listenerFactory.watchListeners();
    }
}
