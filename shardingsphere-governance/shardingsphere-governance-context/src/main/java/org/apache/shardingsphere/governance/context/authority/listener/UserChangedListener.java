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

package org.apache.shardingsphere.governance.context.authority.listener;

import org.apache.shardingsphere.governance.context.authority.listener.event.AuthorityChangedEvent;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNode;
import org.apache.shardingsphere.governance.core.registry.listener.PostGovernanceRepositoryEventListener;
import org.apache.shardingsphere.governance.core.registry.listener.event.GovernanceEvent;
import org.apache.shardingsphere.governance.core.yaml.config.YamlConfigurationConverter;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;

import java.util.Collections;
import java.util.Optional;

/**
 * User changed listener.
 */
public final class UserChangedListener extends PostGovernanceRepositoryEventListener<GovernanceEvent> {
    
    public UserChangedListener(final RegistryCenterRepository registryCenterRepository) {
        super(registryCenterRepository, Collections.singletonList(new RegistryCenterNode().getUsersNode()));
    }
    
    @Override
    protected Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
        return Optional.of(new AuthorityChangedEvent(YamlConfigurationConverter.convertUsers(event.getValue())));
    }
}
