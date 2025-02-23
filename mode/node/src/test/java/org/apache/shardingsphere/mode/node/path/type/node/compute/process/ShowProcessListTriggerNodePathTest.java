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

package org.apache.shardingsphere.mode.node.path.type.node.compute.process;

import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShowProcessListTriggerNodePathTest {
    
    @Test
    void assertToPath() {
        assertThat(NodePathGenerator.toPath(new ShowProcessListTriggerNodePath(null), false), is("/nodes/compute_nodes/show_process_list_trigger"));
        assertThat(NodePathGenerator.toPath(new ShowProcessListTriggerNodePath(new InstanceProcessNodeValue("foo_instance_id", "foo_process_id")), false),
                is("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id:foo_process_id"));
    }
    
    @Test
    void assertCreateInstanceIdSearchCriteria() {
        assertThat(NodePathSearcher.find("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id:foo_process_id",
                ShowProcessListTriggerNodePath.createInstanceIdSearchCriteria()), is(Optional.of("foo_instance_id")));
        assertFalse(NodePathSearcher.find("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id", ShowProcessListTriggerNodePath.createInstanceIdSearchCriteria()).isPresent());
        assertTrue(NodePathSearcher.isMatchedPath("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id:foo_process_id", ShowProcessListTriggerNodePath.createInstanceIdSearchCriteria()));
        assertFalse(NodePathSearcher.isMatchedPath("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id", ShowProcessListTriggerNodePath.createInstanceIdSearchCriteria()));
    }
    
    @Test
    void assertCreateProcessIdSearchCriteria() {
        assertThat(NodePathSearcher.find("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id:foo_process_id",
                ShowProcessListTriggerNodePath.createProcessIdSearchCriteria()), is(Optional.of("foo_process_id")));
        assertFalse(NodePathSearcher.find("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id", ShowProcessListTriggerNodePath.createProcessIdSearchCriteria()).isPresent());
        assertTrue(NodePathSearcher.isMatchedPath("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id:foo_process_id", ShowProcessListTriggerNodePath.createProcessIdSearchCriteria()));
        assertFalse(NodePathSearcher.isMatchedPath("/nodes/compute_nodes/show_process_list_trigger/foo_instance_id", ShowProcessListTriggerNodePath.createProcessIdSearchCriteria()));
    }
}
