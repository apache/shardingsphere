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

import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.spi.PrivilegeProvider;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabasePermittedPrivilegeProviderTest {
    
    @Test
    void assertBuild() {
        Properties props = PropertiesBuilder.build(new Property("user-database-mappings", "root@localhost=*, user1@127.0.0.1=sys_db, user1@=foo_db, user1@=bar_db, user2@=*"));
        PrivilegeProvider provider = TypedSPILoader.getService(PrivilegeProvider.class, "DATABASE_PERMITTED", props);
        Map<Grantee, ShardingSpherePrivileges> actual = provider.build(Arrays.asList(
                new ShardingSphereUser("root", "", "localhost"),
                new ShardingSphereUser("user1", "", "127.0.0.1"),
                new ShardingSphereUser("user1", "", "%"),
                new ShardingSphereUser("user3", "", "%")));
        assertThat(actual.size(), is(4));
        assertTrue(actual.get(new Grantee("root", "localhost")).hasPrivileges("sys_db"));
        assertTrue(actual.get(new Grantee("user1", "127.0.0.1")).hasPrivileges("sys_db"));
        assertTrue(actual.get(new Grantee("user1", "127.0.0.1")).hasPrivileges("foo_db"));
        assertTrue(actual.get(new Grantee("user1", "%")).hasPrivileges("bar_db"));
        assertFalse(actual.get(new Grantee("user1", "%")).hasPrivileges("sys_db"));
        assertFalse(actual.get(new Grantee("user3", "%")).hasPrivileges("sys_db"));
    }
}
