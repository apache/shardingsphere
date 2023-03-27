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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.cluster.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.cluster.event.ClusterStatusChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Cluster status subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ClusterStatusSubscriber {
    
    private final ClusterPersistRepository repository;
    
    public ClusterStatusSubscriber(final ClusterPersistRepository repository, final EventBusContext eventBusContext) {
        this.repository = repository;
        eventBusContext.register(this);
    }
    
    /**
     * Update cluster status.
     *
     * @param event cluster status changed event
     */
    @Subscribe
    public void update(final ClusterStatusChangedEvent event) {
        repository.persist(ComputeNode.getClusterStatusNodePath(), event.getState().name());
    }
}
