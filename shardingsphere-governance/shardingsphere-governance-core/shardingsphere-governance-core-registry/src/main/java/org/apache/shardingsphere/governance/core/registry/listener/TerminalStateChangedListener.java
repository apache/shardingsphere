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

package org.apache.shardingsphere.governance.core.registry.listener;

import org.apache.shardingsphere.governance.core.event.listener.PostGovernanceRepositoryEventListener;
import org.apache.shardingsphere.governance.core.event.model.GovernanceEvent;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNode;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNodeStatus;
import org.apache.shardingsphere.governance.core.registry.instance.GovernanceInstance;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.infra.state.StateEvent;
import org.apache.shardingsphere.infra.state.StateType;

import java.util.Collections;
import java.util.Optional;

/**
 * Terminal state changed listener.
 */
public final class TerminalStateChangedListener extends PostGovernanceRepositoryEventListener<StateEvent> {
    
    public TerminalStateChangedListener(final RegistryRepository registryRepository) {
        super(registryRepository, Collections.singleton(new RegistryCenterNode().getProxyNodePath(GovernanceInstance.getInstance().getInstanceId())));
    }
    
    @Override
    protected Optional<StateEvent> createEvent(final DataChangedEvent event) {
        return Optional.of(new StateEvent(StateType.CIRCUIT_BREAK, RegistryCenterNodeStatus.DISABLED.toString().equalsIgnoreCase(event.getValue())));
    }
}
