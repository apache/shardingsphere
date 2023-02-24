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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;

/**
 * Governance watcher factory.
 */
@RequiredArgsConstructor
public final class GovernanceWatcherFactory {
    
    private final ClusterPersistRepository repository;
    
    private final EventBusContext eventBusContext;
    
    private final String databaseName;
    
    /**
     * Watch listeners.
     */
    public void watchListeners() {
        for (GovernanceWatcher<?> each : ShardingSphereServiceLoader.getServiceInstances(GovernanceWatcher.class)) {
            watch(each);
        }
    }
    
    private void watch(final GovernanceWatcher<?> listener) {
        for (String each : listener.getWatchingKeys(databaseName)) {
            watch(each, listener);
        }
    }
    
    private void watch(final String watchingKey, final GovernanceWatcher<?> listener) {
        repository.watch(watchingKey, dataChangedEventListener -> {
            if (listener.getWatchingTypes().contains(dataChangedEventListener.getType())) {
                listener.createGovernanceEvent(dataChangedEventListener).ifPresent(eventBusContext::post);
            }
        });
    }
}
