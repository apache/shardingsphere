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

package org.apache.shardingsphere.governance.context.authority;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import lombok.Setter;
import org.apache.shardingsphere.authority.engine.Authentication;
import org.apache.shardingsphere.authority.engine.AuthenticationContext;
import org.apache.shardingsphere.authority.engine.impl.DefaultAuthentication;
import org.apache.shardingsphere.authority.loader.storage.impl.StoragePrivilegeBuilder;
import org.apache.shardingsphere.authority.loader.storage.impl.StoragePrivilegeLoader;
import org.apache.shardingsphere.authority.model.Privileges;
import org.apache.shardingsphere.governance.core.event.model.auth.PrivilegeChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.auth.UserRuleChangedEvent;
import org.apache.shardingsphere.infra.context.metadata.MetaDataAwareEventSubscriber;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Governance authority context.
 */
@Setter
public final class GovernanceAuthorityContext implements MetaDataAwareEventSubscriber {
    
    private volatile MetaDataContexts metaDataContexts;
    
    /**
     * Renew user.
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
        Map<ShardingSphereUser, Privileges> modifiedUsers = getModifiedUsers(users);
        for (ShardingSphereUser each : newUsers) {
            modifiedUsers.put(each, new Privileges());
        }
        result.init(modifiedUsers);
        return result;
    }
    
    private Collection<ShardingSphereUser> getNewUsers(final Collection<ShardingSphereUser> users) {
        return users.stream().filter(each -> !AuthenticationContext.getInstance().getAuthentication().findUser(each.getGrantee()).isPresent()).collect(Collectors.toList());
    }
    
    private Map<ShardingSphereUser, Privileges> getModifiedUsers(final Collection<ShardingSphereUser> users) {
        Map<ShardingSphereUser, Privileges> result = new HashMap<>(users.size(), 1);
        for (ShardingSphereUser each : users) {
            Optional<ShardingSphereUser> user = AuthenticationContext.getInstance().getAuthentication().findUser(each.getGrantee());
            if (user.isPresent()) {
                Optional<Privileges> privilege = AuthenticationContext.getInstance().getAuthentication().findPrivileges(user.get().getGrantee());
                privilege.ifPresent(optional -> result.put(user.get(), optional));
            }
        }
        return result;
    }
    
    private void reloadPrivilege(final Collection<ShardingSphereUser> users) {
        Authentication authentication = AuthenticationContext.getInstance().getAuthentication();
        DatabaseType databaseType = metaDataContexts.getMetaDataMap().values().iterator().next().getResource().getDatabaseType();
        Optional<StoragePrivilegeLoader> loader = TypedSPIRegistry.findRegisteredService(StoragePrivilegeLoader.class, databaseType.getName(), new Properties());
        if (loader.isPresent()) {
            Map<ShardingSphereUser, Privileges> privileges = StoragePrivilegeBuilder.build(databaseType, metaDataContexts.getMetaDataMap().values(), users);
            authentication.getAuthentication().putAll(getPrivilegesWithPassword(authentication, privileges));
        }
        AuthenticationContext.getInstance().init(authentication);
    }
    
    private Map<ShardingSphereUser, Privileges> getPrivilegesWithPassword(final Authentication authentication, final Map<ShardingSphereUser, Privileges> privileges) {
        Map<ShardingSphereUser, Privileges> result = new HashMap<>(privileges.size(), 1);
        for (Map.Entry<ShardingSphereUser, Privileges> entry : privileges.entrySet()) {
            if (privileges.containsKey(entry.getKey())) {
                Optional<ShardingSphereUser> user = authentication.findUser(entry.getKey().getGrantee());
                Preconditions.checkState(user.isPresent());
                result.put(user.get(), entry.getValue());
            }
        }
        return result;
    }
}
