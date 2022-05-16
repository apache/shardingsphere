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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.watcher;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeServiceFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.event.MutexAckLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.event.MutexAckLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.event.MutexLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.event.MutexLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeType;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public final class MutexLockChangedWatcher implements GovernanceWatcher<GovernanceEvent> {
    
    private final LockNodeService lockNode = LockNodeServiceFactory.getInstance().getLockNodeService(LockNodeType.MUTEX);
    
    @Override
    public Collection<String> getWatchingKeys() {
        return Collections.singleton(lockNode.getLocksNodePath());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.DELETED);
    }
    
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        Optional<String> lockedName = lockNode.parseLocksNodePath(event.getKey());
        if (lockedName.isPresent()) {
            return handleMutexLocksEvent(event.getType(), lockedName.get());
        }
        lockedName = lockNode.parseLocksAckNodePath(event.getKey());
        if (lockedName.isPresent()) {
            return handleMutexLocksAckEvent(event.getType(), lockedName.get());
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> handleMutexLocksEvent(final Type eventType, final String lockedName) {
        if (Type.ADDED == eventType) {
            return Optional.of(new MutexLockedEvent(lockedName));
        } else if (Type.DELETED == eventType) {
            return Optional.of(new MutexLockReleasedEvent(lockedName));
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> handleMutexLocksAckEvent(final Type eventType, final String ackLockedName) {
        if (Type.ADDED == eventType) {
            return Optional.of(new MutexAckLockedEvent(ackLockedName));
        } else if (Type.DELETED == eventType) {
            return Optional.of(new MutexAckLockReleasedEvent(ackLockedName));
        }
        return Optional.empty();
    }
}
