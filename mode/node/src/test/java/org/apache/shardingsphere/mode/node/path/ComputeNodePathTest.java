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

package org.apache.shardingsphere.mode.node.path;

import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.mode.node.path.metadata.ComputeNodePath;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ComputeNodePathTest {
    
    @Test
    void assertGetRootPath() {
        assertThat(ComputeNodePath.getRootPath(), is("/nodes/compute_nodes"));
    }
    
    @Test
    void assertGetOnlineRootPath() {
        assertThat(ComputeNodePath.getOnlineRootPath(), is("/nodes/compute_nodes/online"));
    }
    
    @Test
    void assertGetOnlinePathWithInstanceType() {
        assertThat(ComputeNodePath.getOnlinePath(InstanceType.PROXY), is("/nodes/compute_nodes/online/proxy"));
        assertThat(ComputeNodePath.getOnlinePath(InstanceType.JDBC), is("/nodes/compute_nodes/online/jdbc"));
    }
    
    @Test
    void assertGetOnlinePathWithInstanceId() {
        assertThat(ComputeNodePath.getOnlinePath("foo_instance_1", InstanceType.PROXY), is("/nodes/compute_nodes/online/proxy/foo_instance_1"));
        assertThat(ComputeNodePath.getOnlinePath("foo_instance_2", InstanceType.JDBC), is("/nodes/compute_nodes/online/jdbc/foo_instance_2"));
    }
    
    @Test
    void assertGetShowProcessListTriggerRootPath() {
        assertThat(ComputeNodePath.getShowProcessListTriggerRootPath(), is("/nodes/compute_nodes/show_process_list_trigger"));
    }
    
    @Test
    void assertGetShowProcessListTriggerPathWithInstanceId() {
        assertThat(ComputeNodePath.getShowProcessListTriggerPath("foo_instance", "foo_process_id"), is("/nodes/compute_nodes/show_process_list_trigger/foo_instance:foo_process_id"));
        assertThat(ComputeNodePath.getShowProcessListTriggerPath("foo_instance", "foo_process_id"), is("/nodes/compute_nodes/show_process_list_trigger/foo_instance:foo_process_id"));
    }
    
    @Test
    void assertGetKillProcessTriggerRootPath() {
        assertThat(ComputeNodePath.getKillProcessTriggerRootPath(), is("/nodes/compute_nodes/kill_process_trigger"));
    }
    
    @Test
    void assertGetKillProcessTriggerPathWithInstanceId() {
        assertThat(ComputeNodePath.getKillProcessTriggerPath("foo_instance", "foo_process_id"), is("/nodes/compute_nodes/kill_process_trigger/foo_instance:foo_process_id"));
    }
    
    @Test
    void assertGetStatePath() {
        assertThat(ComputeNodePath.getStatePath("foo_instance"), is("/nodes/compute_nodes/status/foo_instance"));
    }
    
    @Test
    void assertGetWorkerIdRootPath() {
        assertThat(ComputeNodePath.getWorkerIdRootPath(), is("/nodes/compute_nodes/worker_id"));
    }
    
    @Test
    void assertGetWorkerIdPathWithInstanceId() {
        assertThat(ComputeNodePath.getWorkerIdPath("foo_instance"), is("/nodes/compute_nodes/worker_id/foo_instance"));
    }
    
    @Test
    void assertGetLabelsPath() {
        assertThat(ComputeNodePath.getLabelsPath("foo_instance"), is("/nodes/compute_nodes/labels/foo_instance"));
    }
    
    @Test
    void assertFindInstanceId() {
        assertThat(ComputeNodePath.findInstanceId("/nodes/compute_nodes/status/foo_instance_1"), is(Optional.of("foo_instance_1")));
        assertThat(ComputeNodePath.findInstanceId("/nodes/compute_nodes/worker_id/foo_instance_2"), is(Optional.of("foo_instance_2")));
        assertThat(ComputeNodePath.findInstanceId("/nodes/compute_nodes/labels/foo_instance_3"), is(Optional.of("foo_instance_3")));
        assertFalse(ComputeNodePath.findInstanceId("/nodes/compute_nodes/invalid/foo_instance_4").isPresent());
    }
}
