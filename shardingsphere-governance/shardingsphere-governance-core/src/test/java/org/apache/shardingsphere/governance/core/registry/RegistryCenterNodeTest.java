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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
        assertThat(registryCenterNode.getGovernanceSchema("/states/datanodes/replica_query_db/replica_ds_0").get().getSchemaName(), is("replica_query_db"));
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
        assertThat(schemaPaths.size(), is(2));
        assertThat(schemaPaths, hasItem("/states/datanodes/replica_query_db"));
        assertThat(schemaPaths, hasItem("/states/datanodes/sharding_db"));
    }
}
