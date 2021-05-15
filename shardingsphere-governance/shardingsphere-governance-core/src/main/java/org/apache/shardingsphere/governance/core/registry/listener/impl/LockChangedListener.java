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

package org.apache.shardingsphere.governance.core.registry.listener.impl;

import org.apache.shardingsphere.governance.core.lock.node.LockNode;
import org.apache.shardingsphere.governance.core.registry.listener.PostGovernanceRepositoryEventListener;
import org.apache.shardingsphere.governance.core.registry.listener.event.GovernanceEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.lock.LockNotificationEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.lock.LockReleasedEvent;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;

import java.util.Collections;
import java.util.Optional;

/**
 * Lock changed listener.
 */
public final class LockChangedListener extends PostGovernanceRepositoryEventListener<GovernanceEvent> {
    
    private final LockNode lockNode;
    
    public LockChangedListener(final RegistryCenterRepository registryCenterRepository) {
        super(registryCenterRepository, Collections.singleton(new LockNode().getLockRootNodePath()));
        lockNode = new LockNode();
    }
    
    @Override
    protected Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
        if (!event.getKey().equals(lockNode.getLockRootNodePath()) && lockNode.getLockName(event.getKey()).isPresent()) {
            if (event.getType() == Type.ADDED) {
                return Optional.of(new LockNotificationEvent(lockNode.getLockName(event.getKey()).get()));
            } else if (event.getType() == Type.DELETED) {
                return Optional.of(new LockReleasedEvent(lockNode.getLockName(event.getKey()).get()));
            }
        }
        return Optional.empty();
    }
}
