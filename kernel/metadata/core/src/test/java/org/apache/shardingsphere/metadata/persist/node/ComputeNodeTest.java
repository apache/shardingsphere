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

package org.apache.shardingsphere.metadata.persist.node;

import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ComputeNodeTest {
    
    @Test
    void assertGetOnlineNodePath() {
        assertThat(ComputeNode.getOnlineNodePath(InstanceType.PROXY), is("/nodes/compute_nodes/online/proxy"));
        assertThat(ComputeNode.getOnlineNodePath(InstanceType.JDBC), is("/nodes/compute_nodes/online/jdbc"));
    }
    
    @Test
    void assertGetOnlineInstanceNodePath() {
        assertThat(ComputeNode.getOnlineInstanceNodePath("foo_instance_1", InstanceType.PROXY), is("/nodes/compute_nodes/online/proxy/foo_instance_1"));
        assertThat(ComputeNode.getOnlineInstanceNodePath("foo_instance_2", InstanceType.JDBC), is("/nodes/compute_nodes/online/jdbc/foo_instance_2"));
    }
    
    @Test
    void assertGetProcessTriggerNodePatch() {
        assertThat(ComputeNode.getShowProcessListTriggerNodePath(), is("/nodes/compute_nodes/show_process_list_trigger"));
    }
    
    @Test
    void assertGetProcessTriggerInstanceIdNodePath() {
        assertThat(ComputeNode.getProcessTriggerInstanceNodePath("foo_instance", "foo_process_id"),
                is("/nodes/compute_nodes/show_process_list_trigger/foo_instance:foo_process_id"));
        assertThat(ComputeNode.getProcessTriggerInstanceNodePath("foo_instance", "foo_process_id"),
                is("/nodes/compute_nodes/show_process_list_trigger/foo_instance:foo_process_id"));
    }
    
    @Test
    void assertGetInstanceLabelsNodePath() {
        assertThat(ComputeNode.getInstanceLabelsNodePath("foo_instance"), is("/nodes/compute_nodes/labels/foo_instance"));
    }
    
    @Test
    void assertGetInstanceWorkerIdNodePath() {
        assertThat(ComputeNode.getInstanceWorkerIdNodePath("foo_instance"), is("/nodes/compute_nodes/worker_id/foo_instance"));
    }
    
    @Test
    void assertGetInstanceWorkerIdRootNodePath() {
        assertThat(ComputeNode.getInstanceWorkerIdRootNodePath(), is("/nodes/compute_nodes/worker_id"));
    }
    
    @Test
    void assertGetInstanceIdByComputeNodePath() {
        assertThat(ComputeNode.getInstanceIdByComputeNode("/nodes/compute_nodes/status/foo_instance_1"), is("foo_instance_1"));
        assertThat(ComputeNode.getInstanceIdByComputeNode("/nodes/compute_nodes/worker_id/foo_instance_2"), is("foo_instance_2"));
        assertThat(ComputeNode.getInstanceIdByComputeNode("/nodes/compute_nodes/labels/foo_instance_3"), is("foo_instance_3"));
    }
    
    @Test
    void assertGetInstanceStatusNodePath() {
        assertThat(ComputeNode.getInstanceStatusNodePath("foo_instance"), is("/nodes/compute_nodes/status/foo_instance"));
    }
    
    @Test
    void assertGetComputeNodePath() {
        assertThat(ComputeNode.getComputeNodePath(), is("/nodes/compute_nodes"));
    }
}
