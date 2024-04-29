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
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.spi.PrivilegeProvider;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Map;
import java.util.Optional;

/**
 * Authority rule.
 */
public final class AuthorityRule implements GlobalRule {
    
    @Getter
    private final AuthorityRuleConfiguration configuration;
    
    private final Map<Grantee, ShardingSpherePrivileges> privileges;
    
    public AuthorityRule(final AuthorityRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        privileges = TypedSPILoader.getService(PrivilegeProvider.class, ruleConfig.getPrivilegeProvider().getType(), ruleConfig.getPrivilegeProvider().getProps()).build(ruleConfig);
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
     * Find user.
     *
     * @param grantee grantee user
     * @return found user
     */
    @HighFrequencyInvocation
    public Optional<ShardingSphereUser> findUser(final Grantee grantee) {
        return configuration.getUsers().stream().filter(each -> each.getGrantee().accept(grantee)).findFirst();
    }
    
    /**
     * Find privileges.
     *
     * @param grantee grantee
     * @return found privileges
     */
    @HighFrequencyInvocation
    public Optional<ShardingSpherePrivileges> findPrivileges(final Grantee grantee) {
        return privileges.keySet().stream().filter(each -> each.accept(grantee)).findFirst().map(privileges::get);
    }
    
    @Override
    public RuleAttributes getAttributes() {
        return new RuleAttributes();
    }
}
