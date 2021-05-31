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

import org.apache.shardingsphere.governance.core.registry.listener.builder.GovernanceListenerBuilder;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Collection;

/**
 * Governance listener manager.
 */
public final class GovernanceListenerManager {
    
    static {
        ShardingSphereServiceLoader.register(GovernanceListenerBuilder.class);
    }
    
    private final RegistryCenterRepository repository;
    
    private final Collection<String> schemaNames;
    
    private final Collection<GovernanceListenerBuilder> governanceListenerBuilders;
    
    public GovernanceListenerManager(final RegistryCenterRepository repository, final Collection<String> schemaNames) {
        this.repository = repository;
        this.schemaNames = schemaNames;
        governanceListenerBuilders = ShardingSphereServiceLoader.getSingletonServiceInstances(GovernanceListenerBuilder.class);
    }
    
    /**
     * Watch listeners.
     */
    public void watchListeners() {
        for (GovernanceListenerBuilder each : governanceListenerBuilders) {
            watch(each, each.create(schemaNames));
        }
    }
    
    private void watch(final GovernanceListenerBuilder builder, final GovernanceListener<?> listener) {
        for (String each : listener.getWatchKeys()) {
            watch(each, builder.getWatchTypes(), listener);
        }
    }
    
    private void watch(final String watchKey, final Collection<Type> types, final GovernanceListener<?> listener) {
        repository.watch(watchKey, dataChangedEventListener -> {
            if (types.contains(dataChangedEventListener.getType())) {
                listener.createEvent(dataChangedEventListener).ifPresent(ShardingSphereEventBus.getInstance()::post);
            }
        });
    }
}
