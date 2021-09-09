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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.node;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.schema.ClusterSchema;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class StatusNodeTest {
    
    @Test
    public void assertGetProxyNodePath() {
        assertThat(StatusNode.getComputeNodePath("testId"), is("/status/compute_nodes/testId"));
    }
    
    @Test
    public void assertGetDataNodesPath() {
        assertThat(StatusNode.getStorageNodePath(), is("/status/storage_nodes"));
    }
    
    @Test
    public void assertGetClusterSchema() {
        Optional<ClusterSchema> actual = StatusNode.getClusterSchema("/status/storage_nodes/replica_query_db/replica_ds_0");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSchemaName(), is("replica_query_db"));
        assertThat(actual.get().getDataSourceName(), is("replica_ds_0"));
    }
    
    @Test
    public void assertGetClusterSchemaForIpDataSourceName() {
        Optional<ClusterSchema> actual = StatusNode.getClusterSchema("/status/storage_nodes/replica_query_db/127.0.0.1");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSchemaName(), is("replica_query_db"));
        assertThat(actual.get().getDataSourceName(), is("127.0.0.1"));
    }
    
    @Test
    public void assertGetDataSourcePath() {
        assertThat(StatusNode.getDataSourcePath("replica_query_db", "replica_ds_0"), is("/status/storage_nodes/replica_query_db/replica_ds_0"));
    }
    
    @Test
    public void assertGetPrivilegeNodePath() {
        assertThat(StatusNode.getPrivilegeNodePath(), is("/status/privilegenode"));
    }
}
