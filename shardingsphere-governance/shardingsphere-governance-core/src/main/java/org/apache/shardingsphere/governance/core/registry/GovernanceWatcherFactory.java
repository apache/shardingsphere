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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Collection;

/**
 * Governance watcher factory.
 */
@RequiredArgsConstructor
public final class GovernanceWatcherFactory {
    
    static {
        ShardingSphereServiceLoader.register(GovernanceWatcher.class);
    }
    
    private final RegistryCenterRepository repository;
    
    private final Collection<String> schemaNames;
    
    /**
     * Watch listeners.
     */
    public void watchListeners() {
        for (GovernanceWatcher<?> each : ShardingSphereServiceLoader.getSingletonServiceInstances(GovernanceWatcher.class)) {
            watch(each);
        }
    }
    
    private void watch(final GovernanceWatcher<?> listener) {
        for (String each : listener.getWatchingKeys(schemaNames)) {
            watch(each, listener);
        }
    }
    
    private void watch(final String watchingKey, final GovernanceWatcher<?> listener) {
        repository.watch(watchingKey, dataChangedEventListener -> {
            if (listener.getWatchingTypes().contains(dataChangedEventListener.getType())) {
                listener.createGovernanceEvent(dataChangedEventListener).ifPresent(ShardingSphereEventBus.getInstance()::post);
            }
        });
    }
}
