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

package org.apache.shardingsphere.mode.node.path.type.global.node.compute.process;

import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KillProcessTriggerNodePathTest {
    
    @Test
    void assertToPath() {
        assertThat(NodePathGenerator.toPath(new KillProcessTriggerNodePath(null)), is("/nodes/compute_nodes/kill_process_trigger"));
        assertThat(NodePathGenerator.toPath(new KillProcessTriggerNodePath(new InstanceProcessNodeValue("foo_instance_id", "foo_process_id"))),
                is("/nodes/compute_nodes/kill_process_trigger/foo_instance_id:foo_process_id"));
    }
    
    @Test
    void assertCreateInstanceIdSearchCriteria() {
        assertThat(NodePathSearcher.get("/nodes/compute_nodes/kill_process_trigger/foo_instance_id:foo_process_id",
                KillProcessTriggerNodePath.createInstanceIdSearchCriteria()), is("foo_instance_id"));
        assertFalse(NodePathSearcher.find("/nodes/compute_nodes/kill_process_trigger/foo_instance_id", KillProcessTriggerNodePath.createInstanceIdSearchCriteria()).isPresent());
        assertTrue(NodePathSearcher.isMatchedPath("/nodes/compute_nodes/kill_process_trigger/foo_instance_id:foo_process_id", KillProcessTriggerNodePath.createInstanceIdSearchCriteria()));
        assertFalse(NodePathSearcher.isMatchedPath("/nodes/compute_nodes/kill_process_trigger/foo_instance_id", KillProcessTriggerNodePath.createInstanceIdSearchCriteria()));
    }
    
    @Test
    void assertCreateProcessIdSearchCriteria() {
        assertThat(NodePathSearcher.get("/nodes/compute_nodes/kill_process_trigger/foo_instance_id:foo_process_id", KillProcessTriggerNodePath.createProcessIdSearchCriteria()), is("foo_process_id"));
        assertFalse(NodePathSearcher.find("/nodes/compute_nodes/kill_process_trigger/foo_instance_id", KillProcessTriggerNodePath.createProcessIdSearchCriteria()).isPresent());
        assertTrue(NodePathSearcher.isMatchedPath("/nodes/compute_nodes/kill_process_trigger/foo_instance_id:foo_process_id", KillProcessTriggerNodePath.createProcessIdSearchCriteria()));
        assertFalse(NodePathSearcher.isMatchedPath("/nodes/compute_nodes/kill_process_trigger/foo_instance_id", KillProcessTriggerNodePath.createProcessIdSearchCriteria()));
    }
}
