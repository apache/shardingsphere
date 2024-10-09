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
import org.apache.shardingsphere.authority.config.UserConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorityRuleTest {
    
    @Test
    void assertGetAuthenticatorTypeWithAuthenticationMethodName() {
        ShardingSphereUser user = new ShardingSphereUser("admin", "", "127.0.0.1", "foo", false);
        assertThat(createAuthorityRule(null).getAuthenticatorType(user), is("FOO_AUTHENTICATION"));
    }
    
    @Test
    void assertGetAuthenticatorTypeWithDefaultAuthenticator() {
        ShardingSphereUser user = new ShardingSphereUser("admin", "", "127.0.0.1", "bar", false);
        assertThat(createAuthorityRule("foo").getAuthenticatorType(user), is("FOO_AUTHENTICATION"));
    }
    
    @Test
    void assertGetEmptyAuthenticatorType() {
        ShardingSphereUser user = new ShardingSphereUser("admin", "", "127.0.0.1", "none", false);
        assertThat(createAuthorityRule(null).getAuthenticatorType(user), is(""));
    }
    
    @Test
    void assertGetGrantees() {
        List<Grantee> actual = new ArrayList<>(createAuthorityRule(null).getGrantees());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is(new Grantee("root", "localhost")));
        assertThat(actual.get(1), is(new Grantee("admin", "localhost")));
    }
    
    @Test
    void assertFindUser() {
        Grantee toBefoundGrantee = new Grantee("admin", "localhost");
        Optional<ShardingSphereUser> actual = createAuthorityRule(null).findUser(toBefoundGrantee);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getGrantee(), is(toBefoundGrantee));
    }
    
    @Test
    void assertNotFoundUser() {
        assertFalse(createAuthorityRule(null).findUser(new Grantee("admin", "127.0.0.1")).isPresent());
    }
    
    @Test
    void assertFindPrivileges() {
        assertTrue(createAuthorityRule(null).findPrivileges(new Grantee("admin", "localhost")).isPresent());
    }
    
    @Test
    void assertNotFoundPrivileges() {
        assertFalse(createAuthorityRule(null).findPrivileges(new Grantee("not_found", "")).isPresent());
    }
    
    private AuthorityRule createAuthorityRule(final String defaultAuthenticator) {
        Collection<UserConfiguration> userConfigs = Arrays.asList(
                new UserConfiguration("root", "root", "localhost", null, false),
                new UserConfiguration("admin", "123456", "localhost", null, false));
        AlgorithmConfiguration privilegeProvider = new AlgorithmConfiguration("FIXTURE", new Properties());
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(
                userConfigs, privilegeProvider, Collections.singletonMap("foo", new AlgorithmConfiguration("FOO_AUTHENTICATION", new Properties())), defaultAuthenticator);
        return new AuthorityRule(ruleConfig);
    }
}
