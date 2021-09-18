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

import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.ComputeNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.schema.ClusterSchema;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class StatusNodeTest {
    
    @Test
    public void assertGetComputeNodeRootPath() {
        assertThat(StatusNode.getComputeNodeRootPath(), is("/status/compute_nodes"));
    }
    
    @Test
    public void assertGetComputeNodeStatusPath() {
        assertThat(StatusNode.getComputeNodeStatusPath(ComputeNodeStatus.CIRCUIT_BREAKER), is("/status/compute_nodes/circuit_breaker"));
    }
    
    @Test
    public void assertGetComputeNodeStatusPathWithInstanceId() {
        assertThat(StatusNode.getComputeNodeStatusPath(ComputeNodeStatus.ONLINE, "127.0.0.0@3307"), is("/status/compute_nodes/online/127.0.0.0@3307"));
    }
    
    @Test
    public void assertGetStorageNodeRootPath() {
        assertThat(StatusNode.getStorageNodeRootPath(), is("/status/storage_nodes"));
    }
    
    @Test
    public void assertGetStorageNodeStatusPath() {
        assertThat(StatusNode.getStorageNodeStatusPath(StorageNodeStatus.DISABLE), is("/status/storage_nodes/disable"));
    }
    
    @Test
    public void assertGetStorageNodePathWithSchema() {
        assertThat(StatusNode.getStorageNodeStatusPath(StorageNodeStatus.PRIMARY, new ClusterSchema("replica_query_db.replica_ds_0")), 
                is("/status/storage_nodes/primary/replica_query_db.replica_ds_0"));
    }
    
    @Test
    public void assertFindClusterSchema() {
        Optional<ClusterSchema> actual = StatusNode.findClusterSchema(StorageNodeStatus.DISABLE, "/status/storage_nodes/disable/replica_query_db.replica_ds_0");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSchemaName(), is("replica_query_db"));
        assertThat(actual.get().getDataSourceName(), is("replica_ds_0"));
    }
    
    @Test
    public void assertGetPrivilegeNodePath() {
        assertThat(StatusNode.getPrivilegeNodePath(), is("/status/privilegenode"));
    }
}
