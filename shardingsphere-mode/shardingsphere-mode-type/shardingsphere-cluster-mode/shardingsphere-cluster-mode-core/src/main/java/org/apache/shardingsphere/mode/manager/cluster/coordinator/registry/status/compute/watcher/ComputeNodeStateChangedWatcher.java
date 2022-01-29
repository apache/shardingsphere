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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.watcher;

import com.google.common.base.Strings;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.StateEvent;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.WorkerIdEvent;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Compute node state changed watcher.
 */
public final class ComputeNodeStateChangedWatcher implements GovernanceWatcher<GovernanceEvent> {
    
    @Override
    public Collection<String> getWatchingKeys() {
        return Collections.singleton(ComputeNode.getAttributesNodePath());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED);
    }
    
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        String instanceId = ComputeNode.getInstanceIdByAttributes(event.getKey());
        if (!Strings.isNullOrEmpty(instanceId)) {
            if (event.getKey().equals(ComputeNode.getInstanceStatusNodePath(instanceId))) {
                Collection<String> status = Strings.isNullOrEmpty(event.getValue()) ? new ArrayList<>() : YamlEngine.unmarshal(event.getValue(), Collection.class);
                return Optional.of(new StateEvent(instanceId, status));
            } else if (event.getKey().equals(ComputeNode.getInstanceWorkerIdNodePath(instanceId))) {
                return Optional.of(new WorkerIdEvent(instanceId, Strings.isNullOrEmpty(event.getValue()) ? null : Long.valueOf(event.getValue())));
            } else if (event.getKey().equals(ComputeNode.getInstanceLabelsNodePath(instanceId))) {
                return Optional.of(new LabelsEvent(instanceId, Strings.isNullOrEmpty(event.getValue()) ? new ArrayList<>() : YamlEngine.unmarshal(event.getValue(), Collection.class)));
            }
        }
        return Optional.empty();
    }
}
