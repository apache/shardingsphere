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

package org.apache.shardingsphere.authority.rule.builder;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.config.UserConfiguration;
import org.apache.shardingsphere.infra.rule.builder.global.DefaultGlobalRuleConfigurationBuilder;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRuleBuilder;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultAuthorityRuleConfigurationBuilderTest {
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertBuild() {
        Map<GlobalRuleBuilder, DefaultGlobalRuleConfigurationBuilder> builders = OrderedSPILoader.getServices(
                DefaultGlobalRuleConfigurationBuilder.class, Collections.singleton(new AuthorityRuleBuilder()));
        AuthorityRuleConfiguration actual = (AuthorityRuleConfiguration) builders.values().iterator().next().build();
        Collection<UserConfiguration> users = actual.getUsers();
        assertThat(users.size(), is(1));
        UserConfiguration user = users.iterator().next();
        assertThat(user.getUsername(), is(DefaultUser.USERNAME));
        assertThat(user.getPassword(), is(DefaultUser.PASSWORD));
        assertThat(user.getHostname(), is(DefaultUser.HOSTNAME));
        assertThat(user.getAuthenticationMethodName(), is(""));
        assertTrue(user.isAdmin());
        assertThat(actual.getPrivilegeProvider().getType(), is("ALL_PERMITTED"));
        assertThat(actual.getPrivilegeProvider().getProps(), is(new Properties()));
        assertTrue(actual.getAuthenticators().isEmpty());
        assertNull(actual.getDefaultAuthenticator());
    }
}
