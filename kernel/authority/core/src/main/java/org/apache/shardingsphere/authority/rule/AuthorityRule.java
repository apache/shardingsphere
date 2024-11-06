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

import lombok.Getter;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.constant.AuthorityOrder;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.spi.PrivilegeProvider;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Authority rule.
 */
public final class AuthorityRule implements GlobalRule {
    
    @Getter
    private final AuthorityRuleConfiguration configuration;
    
    private final Map<ShardingSphereUser, ShardingSpherePrivileges> privileges;
    
    public AuthorityRule(final AuthorityRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        Collection<ShardingSphereUser> users = ruleConfig.getUsers().stream()
                .map(each -> new ShardingSphereUser(each.getUsername(), each.getPassword(), each.getHostname(), each.getAuthenticationMethodName(), each.isAdmin())).collect(Collectors.toList());
        privileges = users.stream().collect(Collectors.toMap(each -> each,
                each -> TypedSPILoader.getService(PrivilegeProvider.class, ruleConfig.getPrivilegeProvider().getType(), ruleConfig.getPrivilegeProvider().getProps())
                        .build(ruleConfig, each.getGrantee()),
                (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    /**
     * Get authenticator type.
     *
     * @param user user
     * @return authenticator type
     */
    public String getAuthenticatorType(final ShardingSphereUser user) {
        if (configuration.getAuthenticators().containsKey(user.getAuthenticationMethodName())) {
            return configuration.getAuthenticators().get(user.getAuthenticationMethodName()).getType();
        }
        if (configuration.getAuthenticators().containsKey(configuration.getDefaultAuthenticator())) {
            return configuration.getAuthenticators().get(configuration.getDefaultAuthenticator()).getType();
        }
        return "";
    }
    
    /**
     * Get grantees.
     *
     * @return grantees
     */
    public Collection<Grantee> getGrantees() {
        return privileges.keySet().stream().map(ShardingSphereUser::getGrantee).collect(Collectors.toList());
    }
    
    /**
     * Find user.
     *
     * @param grantee grantee user
     * @return found user
     */
    @HighFrequencyInvocation
    public Optional<ShardingSphereUser> findUser(final Grantee grantee) {
        for (ShardingSphereUser each : privileges.keySet()) {
            if (each.getGrantee().accept(grantee)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Find privileges.
     *
     * @param grantee grantee
     * @return found privileges
     */
    @HighFrequencyInvocation
    public Optional<ShardingSpherePrivileges> findPrivileges(final Grantee grantee) {
        for (ShardingSphereUser each : privileges.keySet()) {
            if (each.getGrantee().accept(grantee)) {
                return Optional.of(each).map(privileges::get);
            }
        }
        return Optional.empty();
    }
    
    @Override
    public int getOrder() {
        return AuthorityOrder.ORDER;
    }
}
