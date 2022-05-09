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

package org.apache.shardingsphere.authority.provider.schema;

import org.apache.shardingsphere.authority.factory.AuthorityProviderAlgorithmFactory;
import org.apache.shardingsphere.authority.model.AuthorityRegistry;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public final class SchemaPermittedPrivilegesProviderAlgorithmTest {
    
    @Test
    public void assertBuildAuthorityRegistry() {
        SchemaPermittedPrivilegesProviderAlgorithm algorithm = createAuthorityProviderAlgorithm();
        AuthorityRegistry actual = algorithm.buildAuthorityRegistry(Collections.emptyMap(), Collections.singletonList(new ShardingSphereUser("user1", "", "127.0.0.2")));
        Optional<ShardingSpherePrivileges> privileges = actual.findPrivileges(new Grantee("user1", "127.0.0.2"));
        assertTrue(privileges.isPresent());
        assertTrue(privileges.get().hasPrivileges("test"));
    }
    
    private SchemaPermittedPrivilegesProviderAlgorithm createAuthorityProviderAlgorithm() {
        return (SchemaPermittedPrivilegesProviderAlgorithm) AuthorityProviderAlgorithmFactory.newInstance(
                new ShardingSphereAlgorithmConfiguration("SCHEMA_PERMITTED", createProperties()));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty(SchemaPermittedPrivilegesProviderAlgorithm.PROP_USER_SCHEMA_MAPPINGS, "root@localhost=test, user1@127.0.0.1=db_dal_admin, user1@=test, user1@=test1");
        return result;
    }
}
