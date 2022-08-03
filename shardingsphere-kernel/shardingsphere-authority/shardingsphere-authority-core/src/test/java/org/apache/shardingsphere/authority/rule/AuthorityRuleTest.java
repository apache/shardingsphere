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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class AuthorityRuleTest {
    
    @Test
    public void assertFindUser() {
        AuthorityRule rule = createAuthorityRule();
        Optional<ShardingSphereUser> actual = rule.findUser(new Grantee("admin", "localhost"));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getGrantee().getUsername(), is("admin"));
        assertThat(actual.get().getGrantee().getHostname(), is("localhost"));
    }
    
    @Test
    public void assertNotFindUser() {
        assertFalse(createAuthorityRule().findUser(new Grantee("admin", "127.0.0.1")).isPresent());
    }
    
    @Test
    public void assertFindPrivileges() {
        assertTrue(createAuthorityRule().findPrivileges(new Grantee("admin", "localhost")).isPresent());
    }
    
    @Test
    public void assertRefresh() {
        Collection<ShardingSphereUser> users = new LinkedList<>();
        users.add(new ShardingSphereUser("root", "root", "localhost"));
        users.add(new ShardingSphereUser("admin", "123456", "localhost"));
        users.add(new ShardingSphereUser("sharding-sphere", "123456", "127.0.0.1"));
        AuthorityRule rule = createAuthorityRule();
        rule.refresh(Collections.emptyMap(), users);
        assertTrue(rule.findPrivileges(new Grantee("sharding-sphere", "localhost")).isPresent());
    }
    
    private AuthorityRule createAuthorityRule() {
        Collection<ShardingSphereUser> users = new LinkedList<>();
        users.add(new ShardingSphereUser("root", "root", "localhost"));
        users.add(new ShardingSphereUser("admin", "123456", "localhost"));
        return new AuthorityRule(new AuthorityRuleConfiguration(users, new AlgorithmConfiguration("ALL_PERMITTED", new Properties())), Collections.emptyMap());
    }
}
