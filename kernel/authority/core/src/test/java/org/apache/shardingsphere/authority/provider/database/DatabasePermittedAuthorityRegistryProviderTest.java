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

import org.apache.shardingsphere.authority.model.AuthorityRegistry;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.spi.AuthorityRegistryProvider;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabasePermittedAuthorityRegistryProviderTest {
    
    @Test
    void assertBuild() {
        Properties props = PropertiesBuilder.build(
                new Property(DatabasePermittedAuthorityRegistryProvider.PROP_USER_DATABASE_MAPPINGS, "root@localhost=test, user1@127.0.0.1=db_dal_admin, user1@=test, user1@=test1, user1@=*"));
        AuthorityRegistryProvider provider = TypedSPILoader.getService(AuthorityRegistryProvider.class, "DATABASE_PERMITTED", props);
        AuthorityRegistry actual = provider.build(Collections.emptyMap(), Collections.singletonList(new ShardingSphereUser("user1", "", "127.0.0.2")));
        Optional<ShardingSpherePrivileges> privileges = actual.findPrivileges(new Grantee("user1", "127.0.0.2"));
        assertTrue(privileges.isPresent());
        assertTrue(privileges.get().hasPrivileges("test"));
        assertTrue(privileges.get().hasPrivileges("db_dal_admin"));
    }
}
