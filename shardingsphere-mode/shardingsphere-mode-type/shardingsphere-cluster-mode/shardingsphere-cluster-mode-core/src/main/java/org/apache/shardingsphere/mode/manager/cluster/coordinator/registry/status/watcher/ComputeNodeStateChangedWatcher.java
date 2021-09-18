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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.watcher;

import org.apache.shardingsphere.infra.state.StateEvent;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.ClusterInstance;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.ComputeNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.node.StatusNode;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Compute node state changed watcher.
 */
public final class ComputeNodeStateChangedWatcher implements GovernanceWatcher<StateEvent> {
    
    @Override
    public Collection<String> getWatchingKeys() {
        return Collections.singleton(StatusNode.getComputeNodeRootPath());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.DELETED);
    }
    
    @Override
    public Optional<StateEvent> createGovernanceEvent(final DataChangedEvent event) {
        return isCircuitBreaker(event.getKey()) ? Optional.of(new StateEvent(StateType.CIRCUIT_BREAK, Type.ADDED == event.getType())) : Optional.empty();
    }
    
    private boolean isCircuitBreaker(final String dataChangedPath) {
        return dataChangedPath.startsWith(StatusNode.getComputeNodeStatusPath(ComputeNodeStatus.CIRCUIT_BREAKER))
                && dataChangedPath.substring(dataChangedPath.lastIndexOf("/") + 1).equals(ClusterInstance.getInstance().getId());
    }
}
