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
import org.apache.shardingsphere.authority.engine.ShardingSphereAuthority;
import org.apache.shardingsphere.authority.engine.AuthorityContext;
import org.apache.shardingsphere.authority.engine.impl.DefaultAuthority;
import org.apache.shardingsphere.authority.loader.storage.impl.StoragePrivilegeBuilder;
import org.apache.shardingsphere.authority.loader.storage.impl.StoragePrivilegeLoader;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
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
        ShardingSphereAuthority authority = createAuthority(event.getUsers());
        AuthorityContext.getInstance().init(authority);
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
    
    private ShardingSphereAuthority createAuthority(final Collection<ShardingSphereUser> users) {
        ShardingSphereAuthority result = new DefaultAuthority();
        Collection<ShardingSphereUser> newUsers = getNewUsers(users);
        Map<ShardingSphereUser, ShardingSpherePrivileges> modifiedUsers = getModifiedUsers(users);
        for (ShardingSphereUser each : newUsers) {
            modifiedUsers.put(each, new ShardingSpherePrivileges());
        }
        result.init(modifiedUsers);
        return result;
    }
    
    private Collection<ShardingSphereUser> getNewUsers(final Collection<ShardingSphereUser> users) {
        return users.stream().filter(each -> !metaDataContexts.getUsers().findUser(each.getGrantee()).isPresent()).collect(Collectors.toList());
    }
    
    private Map<ShardingSphereUser, ShardingSpherePrivileges> getModifiedUsers(final Collection<ShardingSphereUser> users) {
        Map<ShardingSphereUser, ShardingSpherePrivileges> result = new HashMap<>(users.size(), 1);
        for (ShardingSphereUser each : users) {
            Optional<ShardingSphereUser> user = metaDataContexts.getUsers().findUser(each.getGrantee());
            if (user.isPresent()) {
                Optional<ShardingSpherePrivileges> privileges = AuthorityContext.getInstance().getAuthority().findPrivileges(user.get().getGrantee());
                privileges.ifPresent(optional -> result.put(user.get(), optional));
            }
        }
        return result;
    }
    
    private void reloadPrivilege(final Collection<ShardingSphereUser> users) {
        ShardingSphereAuthority authority = AuthorityContext.getInstance().getAuthority();
        DatabaseType databaseType = metaDataContexts.getMetaDataMap().values().iterator().next().getResource().getDatabaseType();
        Optional<StoragePrivilegeLoader> loader = TypedSPIRegistry.findRegisteredService(StoragePrivilegeLoader.class, databaseType.getName(), new Properties());
        if (loader.isPresent()) {
            Map<ShardingSphereUser, ShardingSpherePrivileges> privileges = StoragePrivilegeBuilder.build(databaseType, metaDataContexts.getMetaDataMap().values(), users);
            authority.getAuthority().putAll(getPrivilegesWithPassword(privileges));
        }
        AuthorityContext.getInstance().init(authority);
    }
    
    private Map<ShardingSphereUser, ShardingSpherePrivileges> getPrivilegesWithPassword(final Map<ShardingSphereUser, ShardingSpherePrivileges> privileges) {
        Map<ShardingSphereUser, ShardingSpherePrivileges> result = new HashMap<>(privileges.size(), 1);
        for (Map.Entry<ShardingSphereUser, ShardingSpherePrivileges> entry : privileges.entrySet()) {
            if (privileges.containsKey(entry.getKey())) {
                Optional<ShardingSphereUser> user = metaDataContexts.getUsers().findUser(entry.getKey().getGrantee());
                Preconditions.checkState(user.isPresent());
                result.put(user.get(), entry.getValue());
            }
        }
        return result;
    }
}
