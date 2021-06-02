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

package org.apache.shardingsphere.governance.core.registry.state.node;

import org.apache.shardingsphere.governance.core.schema.GovernanceSchema;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class StatesNodeTest {
    
    @Test
    public void assertGetProxyNodePath() {
        assertThat(StatesNode.getProxyNodePath("testId"), is("/states/proxynodes/testId"));
    }
    
    @Test
    public void assertGetDataNodesPath() {
        assertThat(StatesNode.getDataNodesPath(), is("/states/datanodes"));
    }
    
    @Test
    public void assertGetGovernanceSchema() {
        Optional<GovernanceSchema> actual = StatesNode.getGovernanceSchema("/states/datanodes/replica_query_db/replica_ds_0");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSchemaName(), is("replica_query_db"));
        assertThat(actual.get().getDataSourceName(), is("replica_ds_0"));
    }
    
    @Test
    public void assertGetGovernanceSchemaForIpDataSourceName() {
        Optional<GovernanceSchema> actual = StatesNode.getGovernanceSchema("/states/datanodes/replica_query_db/127.0.0.1");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSchemaName(), is("replica_query_db"));
        assertThat(actual.get().getDataSourceName(), is("127.0.0.1"));
    }
    
    @Test
    public void assertGetSchemaPath() {
        assertThat(StatesNode.getSchemaPath("replica_query_db"), is("/states/datanodes/replica_query_db"));
    }
    
    @Test
    public void assertGetDataSourcePath() {
        assertThat(StatesNode.getDataSourcePath("replica_query_db", "replica_ds_0"), is("/states/datanodes/replica_query_db/replica_ds_0"));
    }
    
    @Test
    public void assertGetAllSchemaPaths() {
        Collection<String> schemaPaths = StatesNode.getAllSchemaPaths(Arrays.asList("replica_query_db", "sharding_db"));
        assertThat(schemaPaths.size(), is(4));
        assertThat(schemaPaths, hasItem("/states/datanodes/replica_query_db"));
        assertThat(schemaPaths, hasItem("/states/datanodes/sharding_db"));
        assertThat(schemaPaths, hasItem("/states/primarynodes/replica_query_db"));
        assertThat(schemaPaths, hasItem("/states/primarynodes/sharding_db"));
    }
    
    @Test
    public void assertGetPrivilegeNodePath() {
        assertThat(StatesNode.getPrivilegeNodePath(), is("/states/privilegenode"));
    }
}
