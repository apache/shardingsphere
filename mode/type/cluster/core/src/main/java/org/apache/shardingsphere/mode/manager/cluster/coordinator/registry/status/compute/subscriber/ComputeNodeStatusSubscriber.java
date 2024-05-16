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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.event.node.ComputeNodeStatusChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.service.ComputeNodeStatusService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Compute node status subscriber.
 */
public final class ComputeNodeStatusSubscriber implements EventSubscriber {
    
    private final ClusterPersistRepository repository;
    
    private final ComputeNodeStatusService computeNodeStatusService;
    
    public ComputeNodeStatusSubscriber(final ClusterPersistRepository repository) {
        this.repository = repository;
        computeNodeStatusService = new ComputeNodeStatusService(repository);
    }
    
    /**
     * Update compute node status.
     *
     * @param event compute node status changed event
     */
    @Subscribe
    public void update(final ComputeNodeStatusChangedEvent event) {
        repository.persistEphemeral(ComputeNode.getInstanceStatusNodePath(event.getInstanceId()), event.getState().name());
    }
    
    /**
     * Update compute node labels.
     * 
     * @param event labels changed event
     */
    @Subscribe
    public void update(final LabelsChangedEvent event) {
        computeNodeStatusService.persistInstanceLabels(event.getInstanceId(), event.getLabels());
    }
}
