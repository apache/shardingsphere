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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.watcher;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.event.AckLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.event.AckLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.service.GlobalLockNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.util.LockNodeUtil;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;

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
        return Collections.singleton(GlobalLockNode.getGlobalAckNodePath());
    }
    
    @Override
    public Collection<DataChangedEvent.Type> getWatchingTypes() {
        return Arrays.asList(DataChangedEvent.Type.ADDED, DataChangedEvent.Type.DELETED);
    }
    
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        String key = event.getKey();
        Optional<String> ackLockedName = GlobalLockNode.getAckLockedKey(key);
        if (ackLockedName.isPresent()) {
            String[] schemaInstance = LockNodeUtil.parseLockName(ackLockedName.get());
            return handleGlobalAckEvent(event.getType(), schemaInstance[0], schemaInstance[1]);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> handleGlobalAckEvent(final DataChangedEvent.Type eventType, final String schema, final String lockedInstanceId) {
        if (DataChangedEvent.Type.ADDED == eventType) {
            return Optional.of(new AckLockedEvent(schema, lockedInstanceId));
        } else if (DataChangedEvent.Type.DELETED == eventType) {
            return Optional.of(new AckLockReleasedEvent(schema, lockedInstanceId));
        } else {
            return Optional.empty();
        }
    }
}
