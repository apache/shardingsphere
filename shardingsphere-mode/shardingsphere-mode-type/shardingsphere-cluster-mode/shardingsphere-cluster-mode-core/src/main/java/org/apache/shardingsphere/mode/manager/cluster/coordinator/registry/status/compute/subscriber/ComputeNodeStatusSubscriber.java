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

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.ComputeNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ComputeNodeStatusChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsChangedEvent;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Compute node status subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ComputeNodeStatusSubscriber {
    
    private final RegistryCenter registryCenter;
    
    private final ClusterPersistRepository repository;
    
    public ComputeNodeStatusSubscriber(final RegistryCenter registryCenter, final ClusterPersistRepository repository) {
        this.registryCenter = registryCenter;
        this.repository = repository;
        registryCenter.getEventBusContext().register(this);
    }
    
    /**
     * Update compute node status.
     *
     * @param event compute node status changed event
     */
    @Subscribe
    public void update(final ComputeNodeStatusChangedEvent event) {
        String computeStatusNodePath = ComputeNode.getInstanceStatusNodePath(event.getInstanceId());
        String yamlContext = repository.get(computeStatusNodePath);
        Collection<String> status = Strings.isNullOrEmpty(yamlContext) ? new ArrayList<>() : YamlEngine.unmarshal(yamlContext, Collection.class);
        if (event.getStatus() == ComputeNodeStatus.CIRCUIT_BREAK) {
            status.add(ComputeNodeStatus.CIRCUIT_BREAK.name());
        } else {
            status.remove(ComputeNodeStatus.CIRCUIT_BREAK.name());
        }
        repository.persistEphemeral(computeStatusNodePath, YamlEngine.marshal(status));
    }
    
    /**
     * Update compute node labels.
     * 
     * @param event labels changed event
     */
    @Subscribe
    public void update(final LabelsChangedEvent event) {
        if (event.getLabels().isEmpty()) {
            registryCenter.getComputeNodeStatusService().persistInstanceLabels(event.getInstanceId(), Collections.EMPTY_LIST);
        } else {
            registryCenter.getComputeNodeStatusService().persistInstanceLabels(event.getInstanceId(), event.getLabels());
        }
    }
}
