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

import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNode;
import org.apache.shardingsphere.governance.core.registry.listener.PostGovernanceRepositoryEventListener;
import org.apache.shardingsphere.governance.core.registry.listener.event.GovernanceEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.Collections;
import java.util.Optional;

/**
 * Global rule changed listener.
 */
public final class GlobalRuleChangedListener extends PostGovernanceRepositoryEventListener<GovernanceEvent> {

    public GlobalRuleChangedListener(final RegistryRepository registryRepository) {
        super(registryRepository, Collections.singleton(new RegistryCenterNode().getGlobalRuleNode()));
    }

    @Override
    protected Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
        return Optional.of(new GlobalRuleConfigurationsChangedEvent("", Collections.singleton(YamlEngine.unmarshal(event.getValue(), AuthorityRuleConfiguration.class))));
    }
}
