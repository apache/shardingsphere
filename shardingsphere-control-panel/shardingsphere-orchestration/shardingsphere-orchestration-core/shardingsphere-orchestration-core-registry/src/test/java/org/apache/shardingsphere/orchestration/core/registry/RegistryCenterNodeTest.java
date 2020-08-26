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

package org.apache.shardingsphere.orchestration.core.registry;

import org.junit.Test;

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
    public void assertGetDataSourcesNodeFullPath() {
        assertThat(registryCenterNode.getDataSourcesNodeFullPath("sharding_db"), is("/registry/datasources/sharding_db"));
    }
    
    @Test
    public void assertGetOrchestrationShardingSchema() {
        assertThat(registryCenterNode.getOrchestrationShardingSchema("/registry/datasources/master_slave_db.slave_ds_0").getSchemaName(), is("master_slave_db"));
    }
}
