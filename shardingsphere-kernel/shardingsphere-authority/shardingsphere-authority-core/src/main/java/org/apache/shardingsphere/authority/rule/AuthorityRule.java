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

package org.apache.shardingsphere.authority.rule;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.model.AuthorityRegistry;
import org.apache.shardingsphere.authority.spi.AuthorityProviderAlgorithm;
import org.apache.shardingsphere.authority.factory.AuthorityProviderAlgorithmFactory;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Authority rule.
 */
public final class AuthorityRule implements GlobalRule {
    
    private final Collection<ShardingSphereUser> users;
    
    private final AuthorityProviderAlgorithm provider;
    
    private volatile AuthorityRegistry authorityRegistry;
    
    public AuthorityRule(final AuthorityRuleConfiguration config, final Map<String, ShardingSphereMetaData> metaDataMap) {
        users = config.getUsers();
        provider = AuthorityProviderAlgorithmFactory.newInstance(config.getProvider());
        authorityRegistry = provider.buildAuthorityRegistry(metaDataMap, config.getUsers());
    }
    
    /**
     * Find user.
     * @param grantee grantee user
     * @return user
     */
    public Optional<ShardingSphereUser> findUser(final Grantee grantee) {
        return users.stream().filter(each -> each.getGrantee().equals(grantee)).findFirst();
    }
    
    /**
     * Find Privileges.
     *
     * @param grantee grantee
     * @return found privileges
     */
    public Optional<ShardingSpherePrivileges> findPrivileges(final Grantee grantee) {
        return authorityRegistry.findPrivileges(grantee);
    }
    
    /**
     * Refresh authority.
     *
     * @param metaDataMap meta data map
     * @param users users
     */
    public synchronized void refresh(final Map<String, ShardingSphereMetaData> metaDataMap, final Collection<ShardingSphereUser> users) {
        authorityRegistry = provider.buildAuthorityRegistry(metaDataMap, users);
    }
    
    @Override
    public String getType() {
        return AuthorityRule.class.getSimpleName();
    }
}
