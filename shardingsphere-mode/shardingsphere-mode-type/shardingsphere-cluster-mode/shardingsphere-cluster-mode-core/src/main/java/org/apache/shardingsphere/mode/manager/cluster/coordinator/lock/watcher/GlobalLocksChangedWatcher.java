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

import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.GlobalLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.GlobalLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.GlobalLockNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeUtil;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Global locks changed watcher.
 */
public final class GlobalLocksChangedWatcher implements GovernanceWatcher<GovernanceEvent> {
    
    @Override
    public Collection<String> getWatchingKeys() {
        return Collections.singleton(GlobalLockNode.getGlobalLocksNodePath());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.DELETED);
    }
    
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        Optional<String> lockedName = GlobalLockNode.getLockedKey(event.getKey());
        if (lockedName.isPresent()) {
            String[] schemaInstance = LockNodeUtil.parseLockName(lockedName.get());
            return handleGlobalLocksEvent(event.getType(), schemaInstance[0], schemaInstance[1]);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> handleGlobalLocksEvent(final Type eventType, final String schema, final String instanceId) {
        if (Type.ADDED == eventType) {
            return Optional.of(new GlobalLockedEvent(schema, instanceId));
        } else if (Type.DELETED == eventType) {
            return Optional.of(new GlobalLockReleasedEvent(schema, instanceId));
        } else {
            return Optional.empty();
        }
    }
}
