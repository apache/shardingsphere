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
    public void assertGetInstancesNodeFullPath() {
        assertThat(registryCenterNode.getInstancesNodeFullPath("testId"), is("/registry/instances/testId"));
    }
    
    @Test
    public void assertGetDataSourcesNodeFullRootPath() {
        assertThat(registryCenterNode.getDataSourcesNodeFullRootPath(), is("/registry/datasources"));
    }
    
    @Test
    public void assertGetGovernanceSchema() {
        assertThat(registryCenterNode.getGovernanceSchema("/registry/datasources/primary_replica_db/replica_ds_0").get().getSchemaName(), is("primary_replica_db"));
    }
    
    @Test
    public void assertGetDataSourcesNodeSchemaPath() {
        assertThat(registryCenterNode.getDataSourcesNodeSchemaPath("primary_replica_db"), is("/registry/datasources/primary_replica_db"));
    }
    
    @Test
    public void assertGetDataSourcesNodeDataSourcePath() {
        assertThat(registryCenterNode.getDataSourcesNodeDataSourcePath("primary_replica_db", "replica_ds_0"), is("/registry/datasources/primary_replica_db/replica_ds_0"));
    }
    
    @Test
    public void assertGetAllDataSourcesSchemaPaths() {
        Collection<String> schemaPaths = registryCenterNode.getAllDataSourcesSchemaPaths(Arrays.asList("primary_replica_db", "sharding_db"));
        assertThat(schemaPaths.size(), is(2));
        assertThat(schemaPaths, hasItem("/registry/datasources/primary_replica_db"));
        assertThat(schemaPaths, hasItem("/registry/datasources/sharding_db"));
    }
}
