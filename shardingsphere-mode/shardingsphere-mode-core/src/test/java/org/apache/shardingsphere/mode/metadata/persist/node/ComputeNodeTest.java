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

package org.apache.shardingsphere.mode.metadata.persist.node;

import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ComputeNodeTest {
    
    @Test
    public void assertGetOnlineNodePath() {
        assertThat(ComputeNode.getOnlineNodePath(InstanceType.PROXY), is("/nodes/compute_nodes/online/proxy"));
        assertThat(ComputeNode.getOnlineNodePath(InstanceType.JDBC), is("/nodes/compute_nodes/online/jdbc"));
    }
    
    @Test
    public void assertGetOnlineInstanceNodePath() {
        assertThat(ComputeNode.getOnlineInstanceNodePath("127.0.0.1@3307", InstanceType.PROXY), is("/nodes/compute_nodes/online/proxy/127.0.0.1@3307"));
        assertThat(ComputeNode.getOnlineInstanceNodePath("127.0.0.1@3307", InstanceType.JDBC), is("/nodes/compute_nodes/online/jdbc/127.0.0.1@3307"));
    }
    
    @Test
    public void assertGetInstanceLabelsNodePath() {
        assertThat(ComputeNode.getInstanceLabelsNodePath("127.0.0.1@3307"), is("/nodes/compute_nodes/attributes/127.0.0.1@3307/labels"));
    }
    
    @Test
    public void assertGetAttributesNodePath() {
        assertThat(ComputeNode.getAttributesNodePath(), is("/nodes/compute_nodes/attributes"));
    }
    
    @Test
    public void assertGetInstanceWorkerIdNodePath() {
        assertThat(ComputeNode.getInstanceWorkerIdNodePath("127.0.0.1@3307"), is("/nodes/compute_nodes/attributes/127.0.0.1@3307/worker_id"));
    }
    
    @Test
    public void assertGetInstanceIdByAttributes() {
        assertThat(ComputeNode.getInstanceIdByAttributes("/nodes/compute_nodes/attributes/127.0.0.1@3307/status"), is("127.0.0.1@3307"));
        assertThat(ComputeNode.getInstanceIdByAttributes("/nodes/compute_nodes/attributes/127.0.0.1@3308/worker_id"), is("127.0.0.1@3308"));
        assertThat(ComputeNode.getInstanceIdByAttributes("/nodes/compute_nodes/attributes/127.0.0.1@3309/labels"), is("127.0.0.1@3309"));
    }
    
    @Test
    public void assertGetInstanceStatusNodePath() {
        assertThat(ComputeNode.getInstanceStatusNodePath("127.0.0.1@3307"), is("/nodes/compute_nodes/attributes/127.0.0.1@3307/status"));
    }
}
