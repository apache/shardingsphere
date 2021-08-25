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

package org.apache.shardingsphere.governance.core.registry.config.subscriber;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.persist.service.impl.GlobalRulePersistService;
import org.apache.shardingsphere.governance.core.registry.state.service.UserStatusRegistryService;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.mapper.event.dcl.impl.CreateUserStatementEvent;
import org.apache.shardingsphere.infra.metadata.mapper.event.dcl.impl.DropUserStatementEvent;
import org.apache.shardingsphere.infra.metadata.mapper.event.dcl.impl.GrantStatementEvent;

import java.util.Collection;
import java.util.Optional;

/**
 * Global rule registry subscriber.
 */
public final class GlobalRuleRegistrySubscriber {
    
    private final GlobalRulePersistService persistService;
    
    private final UserStatusRegistryService userStatusRegistryService;
    
    public GlobalRuleRegistrySubscriber(final ClusterPersistRepository repository) {
        persistService = new GlobalRulePersistService(repository);
        userStatusRegistryService = new UserStatusRegistryService(repository);
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Update when user created.
     *
     * @param event create user statement event
     */
    @Subscribe
    public void update(final CreateUserStatementEvent event) {
        Collection<RuleConfiguration> globalRuleConfigs = persistService.load();
        Optional<AuthorityRuleConfiguration> authorityRuleConfig = globalRuleConfigs.stream().filter(each -> each instanceof AuthorityRuleConfiguration)
                .findAny().map(each -> (AuthorityRuleConfiguration) each);
        Preconditions.checkState(authorityRuleConfig.isPresent(), "No available authority rules for governance.");
        authorityRuleConfig.get().getUsers().addAll(event.getUsers());
        persistService.persist(globalRuleConfigs, true);
    }
    
    /**
     * Update when user dropped.
     *
     * @param event drop user statement event
     */
    @Subscribe
    public void update(final DropUserStatementEvent event) {
        Collection<RuleConfiguration> globalRuleConfigs = persistService.load();
        Optional<AuthorityRuleConfiguration> authorityRuleConfig = globalRuleConfigs.stream().filter(each -> each instanceof AuthorityRuleConfiguration)
                .findAny().map(each -> (AuthorityRuleConfiguration) each);
        Preconditions.checkState(authorityRuleConfig.isPresent(), "No available authority rules for governance.");
        authorityRuleConfig.get().getUsers().removeIf(each -> event.getUsers().contains(each.getGrantee().getUsername()));
        persistService.persist(globalRuleConfigs, true);
    }
    
    /**
     * Update when granted.
     *
     * @param event grant statement event
     */
    @Subscribe
    public void update(final GrantStatementEvent event) {
        if (!event.getUsers().isEmpty()) {
            userStatusRegistryService.persist(event.getUsers());
        }
    }
}
