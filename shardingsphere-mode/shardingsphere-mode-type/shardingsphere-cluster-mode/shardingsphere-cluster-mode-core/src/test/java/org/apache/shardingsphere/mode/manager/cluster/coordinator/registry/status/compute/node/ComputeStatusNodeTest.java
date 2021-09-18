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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.node;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.ComputeNodeStatus;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ComputeStatusNodeTest {
    
    @Test
    public void assertGetRootPath() {
        assertThat(ComputeStatusNode.getRootPath(), is("/status/compute_nodes"));
    }
    
    @Test
    public void assertGetStatusPath() {
        assertThat(ComputeStatusNode.getStatusPath(ComputeNodeStatus.CIRCUIT_BREAKER), is("/status/compute_nodes/circuit_breaker"));
    }
    
    @Test
    public void assertGetStatusPathWithInstanceId() {
        assertThat(ComputeStatusNode.getStatusPath(ComputeNodeStatus.ONLINE, "127.0.0.0@3307"), is("/status/compute_nodes/online/127.0.0.0@3307"));
    }
}
