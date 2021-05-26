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

package org.apache.shardingsphere.governance.core.registry.service.config.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNode;
import org.apache.shardingsphere.governance.core.registry.service.config.GlobalRegistryService;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.mapper.event.dcl.impl.CreateUserStatementEvent;
import org.apache.shardingsphere.infra.metadata.mapper.event.dcl.impl.GrantStatementEvent;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUsers;
import org.apache.shardingsphere.infra.metadata.user.yaml.config.YamlUsersConfigurationConverter;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Global rule registry service.
 */
public final class GlobalRuleRegistryService implements GlobalRegistryService<Collection<RuleConfiguration>> {
    
    private final RegistryCenterRepository repository;
    
    private final RegistryCenterNode node;
    
    public GlobalRuleRegistryService(final RegistryCenterRepository repository) {
        this.repository = repository;
        node = new RegistryCenterNode();
    }
    
    @Override
    public void persist(final Collection<RuleConfiguration> globalRuleConfigs, final boolean isOverwrite) {
        if (!globalRuleConfigs.isEmpty() && (isOverwrite || !isExisted())) {
            repository.persist(node.getGlobalRuleNode(), YamlEngine.marshal(new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(globalRuleConfigs)));
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Collection<RuleConfiguration> load() {
        return isExisted()
                ? new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(repository.get(node.getGlobalRuleNode()), Collection.class)) : Collections.emptyList();
    }
    
    private boolean isExisted() {
        return !Strings.isNullOrEmpty(repository.get(node.getGlobalRuleNode()));
    }
    
    
    /**
     * Update when user created.
     *
     * @param event create user statement event
     */
    @Subscribe
    public void update(final CreateUserStatementEvent event) {
        Collection<RuleConfiguration> globalRuleConfigs = load();
        Optional<AuthorityRuleConfiguration> authorityRuleConfig = globalRuleConfigs.stream().filter(each -> each instanceof AuthorityRuleConfiguration)
                .findAny().map(each -> (AuthorityRuleConfiguration) each);
        Preconditions.checkState(authorityRuleConfig.isPresent());
        refreshAuthorityRuleConfiguration(authorityRuleConfig.get(), event.getUsers());
        persist(globalRuleConfigs, true);
    }
    
    /**
     * Update when granted.
     *
     * @param event grant statement event
     */
    @Subscribe
    public void update(final GrantStatementEvent event) {
        if (!event.getUsers().isEmpty()) {
            repository.persist(node.getPrivilegeNodePath(), YamlEngine.marshal(YamlUsersConfigurationConverter.convertYamlUserConfigurations(event.getUsers())));
        }
    }
    
    private void refreshAuthorityRuleConfiguration(final AuthorityRuleConfiguration authRuleConfig, final Collection<ShardingSphereUser> createUsers) {
        Collection<ShardingSphereUser> oldUsers = authRuleConfig.getUsers();
        Collection<ShardingSphereUser> newUsers = oldUsers.isEmpty() ? createUsers : getChangedShardingSphereUsers(oldUsers, createUsers);
        authRuleConfig.getUsers().removeAll(oldUsers);
        authRuleConfig.getUsers().addAll(newUsers);
    }
    
    private Collection<ShardingSphereUser> getChangedShardingSphereUsers(final Collection<ShardingSphereUser> oldUsers, final Collection<ShardingSphereUser> newUsers) {
        Collection<ShardingSphereUser> result = new LinkedList<>(oldUsers);
        ShardingSphereUsers shardingSphereUsers = new ShardingSphereUsers(oldUsers);
        for (ShardingSphereUser each : newUsers) {
            shardingSphereUsers.findUser(each.getGrantee()).ifPresent(result::remove);
            result.add(each);
        }
        return result;
    }
}
