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

package org.apache.shardingsphere.authority.provider.simple;

import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.provider.schema.SchemaPrivilegesPermittedAuthorityProviderAlgorithm;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public final class SchemaPrivilegesPermittedAuthorityProviderAlgorithmTest {
    
    @Test
    public void assertFindPrivileges() {
        SchemaPrivilegesPermittedAuthorityProviderAlgorithm algorithm = new SchemaPrivilegesPermittedAuthorityProviderAlgorithm();
        Properties props = new Properties();
        props.setProperty(SchemaPrivilegesPermittedAuthorityProviderAlgorithm.PROP_USER_SCHEMA_MAPPINGS, "root@localhost=test, user1@127.0.0.1=db_dal_admin, user1@=test, user1@=test1");
        algorithm.setProps(props);
        algorithm.init(Collections.emptyMap(), Collections.singletonList(new ShardingSphereUser("user1", "", "127.0.0.2")));
        Optional<ShardingSpherePrivileges> privileges = algorithm.findPrivileges(new Grantee("user1", "127.0.0.2"));
        assertTrue(privileges.isPresent());
        assertTrue(privileges.get().hasPrivileges("test"));
    }
    
    @Test
    public void assertRefreshPrivileges() {
        SchemaPrivilegesPermittedAuthorityProviderAlgorithm algorithm = new SchemaPrivilegesPermittedAuthorityProviderAlgorithm();
        Properties props = new Properties();
        props.setProperty(SchemaPrivilegesPermittedAuthorityProviderAlgorithm.PROP_USER_SCHEMA_MAPPINGS, "root@localhost=test, user1@127.0.0.1=db_dal_admin, user1@=test, user1@=test1");
        algorithm.setProps(props);
        algorithm.init(Collections.emptyMap(), Collections.singletonList(new ShardingSphereUser("root", "", "localhost")));
        Optional<ShardingSpherePrivileges> privileges1 = algorithm.findPrivileges(new Grantee("root", "localhost"));
        assertTrue(privileges1.isPresent());
        assertTrue(privileges1.get().hasPrivileges("test"));
        algorithm.refresh(Collections.emptyMap(), Collections.singletonList(new ShardingSphereUser("user1", "", "127.0.0.1")));
        Optional<ShardingSpherePrivileges> privileges2 = algorithm.findPrivileges(new Grantee("user1", "127.0.0.1"));
        assertTrue(privileges2.isPresent());
        assertTrue(privileges2.get().hasPrivileges("test1"));
    }
}
