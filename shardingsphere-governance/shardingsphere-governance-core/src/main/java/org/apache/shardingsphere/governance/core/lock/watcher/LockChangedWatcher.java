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

package org.apache.shardingsphere.governance.core.lock.watcher;

import org.apache.shardingsphere.governance.core.lock.service.LockNode;
import org.apache.shardingsphere.governance.core.registry.GovernanceWatcher;
import org.apache.shardingsphere.governance.core.registry.GovernanceEvent;
import org.apache.shardingsphere.governance.core.lock.event.LockNotificationEvent;
import org.apache.shardingsphere.governance.core.lock.event.LockReleasedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Lock changed watcher.
 */
public final class LockChangedWatcher implements GovernanceWatcher<GovernanceEvent> {
    
    @Override
    public Collection<String> getWatchingKeys(final Collection<String> schemaNames) {
        return Collections.singleton(LockNode.getLockRootNodePath());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Arrays.asList(Type.ADDED, Type.DELETED);
    }
    
    @Override
    public Optional<GovernanceEvent> createGovernanceEvent(final DataChangedEvent event) {
        if (!event.getKey().equals(LockNode.getLockRootNodePath()) && LockNode.getLockName(event.getKey()).isPresent()) {
            if (event.getType() == Type.ADDED) {
                return Optional.of(new LockNotificationEvent(LockNode.getLockName(event.getKey()).get()));
            } else if (event.getType() == Type.DELETED) {
                return Optional.of(new LockReleasedEvent(LockNode.getLockName(event.getKey()).get()));
            }
        }
        return Optional.empty();
    }
}
