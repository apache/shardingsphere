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

package org.apache.shardingsphere.governance.core.registry;

import org.apache.shardingsphere.governance.core.registry.schema.GovernanceSchema;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class RegistryCenterNodeTest {
    
    private final RegistryCenterNode registryCenterNode = new RegistryCenterNode();
    
    @Test
    public void assertGetProxyNodePath() {
        assertThat(registryCenterNode.getProxyNodePath("testId"), is("/states/proxynodes/testId"));
    }
    
    @Test
    public void assertGetDataNodesPath() {
        assertThat(registryCenterNode.getDataNodesPath(), is("/states/datanodes"));
    }
    
    @Test
    public void assertGetGovernanceSchema() {
        Optional<GovernanceSchema> actual = registryCenterNode.getGovernanceSchema("/states/datanodes/replica_query_db/replica_ds_0");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSchemaName(), is("replica_query_db"));
        assertThat(actual.get().getDataSourceName(), is("replica_ds_0"));
    }

    @Test
    public void assertGetGovernanceSchemaForIpDataSourceName() {
        Optional<GovernanceSchema> actual = registryCenterNode.getGovernanceSchema("/states/datanodes/replica_query_db/127.0.0.1");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSchemaName(), is("replica_query_db"));
        assertThat(actual.get().getDataSourceName(), is("127.0.0.1"));
    }
    
    @Test
    public void assertGetSchemaPath() {
        assertThat(registryCenterNode.getSchemaPath("replica_query_db"), is("/states/datanodes/replica_query_db"));
    }
    
    @Test
    public void assertGetDataSourcePath() {
        assertThat(registryCenterNode.getDataSourcePath("replica_query_db", "replica_ds_0"), is("/states/datanodes/replica_query_db/replica_ds_0"));
    }
    
    @Test
    public void assertGetAllSchemaPaths() {
        Collection<String> schemaPaths = registryCenterNode.getAllSchemaPaths(Arrays.asList("replica_query_db", "sharding_db"));
        assertThat(schemaPaths.size(), is(4));
        assertThat(schemaPaths, hasItem("/states/datanodes/replica_query_db"));
        assertThat(schemaPaths, hasItem("/states/datanodes/sharding_db"));
        assertThat(schemaPaths, hasItem("/states/primarynodes/replica_query_db"));
        assertThat(schemaPaths, hasItem("/states/primarynodes/sharding_db"));
    }
    
    @Test
    public void assertGetRulePath() {
        assertThat(registryCenterNode.getRulePath(DefaultSchema.LOGIC_NAME), is("/metadata/logic_db/rules"));
    }
    
    @Test
    public void assertGetUsersNodePath() {
        assertThat(registryCenterNode.getUsersNode(), is("/users"));
    }
    
    @Test
    public void assertGetGlobalRuleNodePath() {
        assertThat(registryCenterNode.getGlobalRuleNode(), is("/rules"));
    }
    
    @Test
    public void assertGetPropsPath() {
        assertThat(registryCenterNode.getPropsPath(), is("/props"));
    }
    
    @Test
    public void assertGetSchemaName() {
        assertThat(registryCenterNode.getSchemaName("/metadata/logic_db/rules"), is(DefaultSchema.LOGIC_NAME));
    }
    
    @Test
    public void assertGetAllSchemaConfigPaths() {
        Collection<String> actual = registryCenterNode.getAllSchemaConfigPaths(Collections.singletonList(DefaultSchema.LOGIC_NAME));
        assertThat(actual.size(), is(4));
        assertThat(actual, hasItems("/metadata"));
        assertThat(actual, hasItems("/metadata/logic_db/rules"));
        assertThat(actual, hasItems("/metadata/logic_db/dataSources"));
        assertThat(actual, hasItems("/metadata/logic_db/schema"));
    }

    @Test
    public void assertGetSchemaNamePath() {
        assertThat(registryCenterNode.getSchemaNamePath("sharding_db"), is("/metadata/sharding_db"));
    }

    @Test
    public void assertGetTablePath() {
        assertThat(registryCenterNode.getMetadataSchemaPath("sharding_db"), is("/metadata/sharding_db/schema"));
    }
    
    @Test
    public void assertGetPrivilegeNodePath() {
        assertThat(registryCenterNode.getPrivilegeNodePath(), is("/states/privilegenode"));
    }
}
