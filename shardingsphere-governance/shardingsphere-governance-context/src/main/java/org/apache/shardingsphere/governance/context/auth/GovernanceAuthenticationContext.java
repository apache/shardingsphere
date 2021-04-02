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

package org.apache.shardingsphere.governance.context.auth;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.event.model.auth.PrivilegeChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.auth.UserRuleChangedEvent;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.auth.Authentication;
import org.apache.shardingsphere.infra.metadata.auth.AuthenticationContext;
import org.apache.shardingsphere.infra.metadata.auth.builder.PrivilegeBuilder;
import org.apache.shardingsphere.infra.metadata.auth.builder.loader.PrivilegeLoader;
import org.apache.shardingsphere.infra.metadata.auth.builder.loader.PrivilegeLoaderEngine;
import org.apache.shardingsphere.infra.metadata.auth.builtin.DefaultAuthentication;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.ShardingSpherePrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Governance authentication context.
 */
public final class GovernanceAuthenticationContext {
    
    private final MetaDataContexts metaDataContexts;
    
    public GovernanceAuthenticationContext(final MetaDataContexts metaDataContexts) {
        this.metaDataContexts = metaDataContexts;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Renew authentication.
     *
     * @param event user changed event
     */
    @Subscribe
    public synchronized void renew(final UserRuleChangedEvent event) {
        Authentication authentication = createAuthentication(event.getUsers());
        AuthenticationContext.getInstance().init(authentication);
        reloadPrivilege(event.getUsers());
    }
    
    /**
     * Renew privilege.
     *
     * @param event privilege changed event
     */
    @Subscribe
    public synchronized void renew(final PrivilegeChangedEvent event) {
        reloadPrivilege(event.getUsers());
    }
    
    private Authentication createAuthentication(final Collection<ShardingSphereUser> users) {
        Authentication result = new DefaultAuthentication();
        Collection<ShardingSphereUser> newUsers = getNewUsers(users);
        Map<ShardingSphereUser, ShardingSpherePrivilege> modifiedUsers = getModifiedUsers(users);
        for (ShardingSphereUser each : newUsers) {
            modifiedUsers.put(each, new ShardingSpherePrivilege());
        }
        result.init(modifiedUsers);
        return result;
    }
    
    private Collection<ShardingSphereUser> getNewUsers(final Collection<ShardingSphereUser> users) {
        return users.stream().filter(each -> !AuthenticationContext.getInstance().getAuthentication().findUser(each.getGrantee()).isPresent()).collect(Collectors.toList());
    }
    
    private Map<ShardingSphereUser, ShardingSpherePrivilege> getModifiedUsers(final Collection<ShardingSphereUser> users) {
        Map<ShardingSphereUser, ShardingSpherePrivilege> result = new HashMap<>(users.size(), 1);
        for (ShardingSphereUser each : users) {
            Optional<ShardingSphereUser> user = AuthenticationContext.getInstance().getAuthentication().findUser(each.getGrantee());
            if (user.isPresent()) {
                Optional<ShardingSpherePrivilege> privilege = AuthenticationContext.getInstance().getAuthentication().findPrivilege(user.get().getGrantee());
                privilege.ifPresent(optional -> result.put(user.get(), optional));
            }
        }
        return result;
    }
    
    private void reloadPrivilege(final Collection<ShardingSphereUser> users) {
        Authentication authentication = AuthenticationContext.getInstance().getAuthentication();
        Optional<PrivilegeLoader> loader = PrivilegeLoaderEngine.findPrivilegeLoader(metaDataContexts.getMetaDataMap().values().iterator().next().getResource().getDatabaseType());
        if (loader.isPresent()) {
            Map<ShardingSphereUser, ShardingSpherePrivilege> privileges = PrivilegeBuilder.build(metaDataContexts.getMetaDataMap().values(), users, metaDataContexts.getProps());
            authentication.getAuthentication().putAll(getPrivilegesWithPassword(authentication, privileges));
        }
        AuthenticationContext.getInstance().init(authentication);
    }
    
    private Map<ShardingSphereUser, ShardingSpherePrivilege> getPrivilegesWithPassword(final Authentication authentication, final Map<ShardingSphereUser, ShardingSpherePrivilege> privileges) {
        Map<ShardingSphereUser, ShardingSpherePrivilege> result = new HashMap<>(privileges.size(), 1);
        for (Map.Entry<ShardingSphereUser, ShardingSpherePrivilege> entry : privileges.entrySet()) {
            if (privileges.containsKey(entry.getKey())) {
                Optional<ShardingSphereUser> user = authentication.findUser(entry.getKey().getGrantee());
                Preconditions.checkState(user.isPresent());
                result.put(user.get(), entry.getValue());
            }
        }
        return result;
    }
}
