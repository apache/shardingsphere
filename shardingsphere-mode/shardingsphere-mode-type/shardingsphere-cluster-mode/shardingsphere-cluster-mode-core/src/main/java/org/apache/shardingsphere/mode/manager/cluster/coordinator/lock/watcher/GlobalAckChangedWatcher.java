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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.watcher;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.AckLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.AckLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Global ack changed watcher.
 */
public final class GlobalAckChangedWatcher implements GovernanceWatcher<GovernanceEvent> {
    
    @Override
    public Collection<String> getWatchingKeys() {
        return Collections.singleton(LockNode.getGlobalSchemaLockedAckNodePath());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.DELETED);
    }
    
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        Optional<String> ackLockedName = LockNode.parseGlobalSchemaLockedAckNodePath(event.getKey());
        if (ackLockedName.isPresent()) {
            return handleGlobalAckEvent(event.getType(), ackLockedName.get());
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> handleGlobalAckEvent(final Type eventType, final String lockedName) {
        if (Type.ADDED == eventType) {
            return Optional.of(new AckLockedEvent(lockedName));
        } else if (Type.DELETED == eventType) {
            return Optional.of(new AckLockReleasedEvent(lockedName));
        }
        return Optional.empty();
    }
}
