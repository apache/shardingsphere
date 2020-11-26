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

package org.apache.shardingsphere.governance.core.lock.listener;

import org.apache.shardingsphere.governance.core.event.listener.PostGovernanceRepositoryEventListener;
import org.apache.shardingsphere.governance.core.event.model.GovernanceEvent;
import org.apache.shardingsphere.governance.core.event.model.lock.GlobalLockAddedEvent;
import org.apache.shardingsphere.governance.core.lock.node.GlobalLockNode;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;

import java.util.Collections;
import java.util.Optional;

/**
 * Global lock changed listener.
 */
public final class GlobalLockChangedListener extends PostGovernanceRepositoryEventListener<GovernanceEvent> {
    
    private final GlobalLockNode globalLockNode;
    
    public GlobalLockChangedListener(final RegistryRepository registryRepository) {
        super(registryRepository, Collections.singleton(new GlobalLockNode().getGlobalLockNodePath()));
        globalLockNode = new GlobalLockNode();
    }
    
    @Override
    protected Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
        return event.getKey().equals(globalLockNode.getGlobalLockNodePath()) ? Optional.of(new GlobalLockAddedEvent()) : Optional.empty();
    }
}
