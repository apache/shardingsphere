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

package org.apache.shardingsphere.authority.provider.database;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.config.UserConfiguration;
import org.apache.shardingsphere.authority.spi.PrivilegeProvider;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DatabasePermittedPrivilegeProviderTest {
    
    @Test
    void assertBuild() {
        Properties props = PropertiesBuilder.build(new Property("user-database-mappings", "root@localhost=*, user1@127.0.0.1=sys_db, user1@=foo_db, user1@=bar_db, user2@=*"));
        PrivilegeProvider provider = TypedSPILoader.getService(PrivilegeProvider.class, "DATABASE_PERMITTED", props);
        Collection<UserConfiguration> userConfigs = Arrays.asList(
                new UserConfiguration("root", "", "localhost", null, false),
                new UserConfiguration("user1", "", "127.0.0.1", null, false),
                new UserConfiguration("user1", "", "%", null, false),
                new UserConfiguration("user3", "", "%", null, false));
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(userConfigs, mock(AlgorithmConfiguration.class), Collections.emptyMap(), null);
        assertTrue(provider.build(ruleConfig, new Grantee("root", "localhost")).hasPrivileges("sys_db"));
        assertTrue(provider.build(ruleConfig, new Grantee("user1", "127.0.0.1")).hasPrivileges("sys_db"));
        assertTrue(provider.build(ruleConfig, new Grantee("user1", "127.0.0.1")).hasPrivileges("foo_db"));
        assertTrue(provider.build(ruleConfig, new Grantee("user1", "%")).hasPrivileges("bar_db"));
        assertFalse(provider.build(ruleConfig, new Grantee("user1", "%")).hasPrivileges("sys_db"));
        assertFalse(provider.build(ruleConfig, new Grantee("user3", "%")).hasPrivileges("sys_db"));
    }
    
    @Test
    void assertBuildFailed() {
        Properties props = PropertiesBuilder.build(new Property("user-database-mappings", "invalid"));
        assertThrows(IllegalArgumentException.class, () -> TypedSPILoader.getService(PrivilegeProvider.class, "SCHEMA_PRIVILEGES_PERMITTED", props));
    }
}
