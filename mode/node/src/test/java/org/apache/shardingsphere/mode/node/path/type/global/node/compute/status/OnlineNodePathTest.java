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

package org.apache.shardingsphere.mode.node.path.type.global.node.compute.status;

import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnlineNodePathTest {
    
    @Test
    void assertToPath() {
        assertThat(NodePathGenerator.toPath(new OnlineNodePath(null, null)), is("/nodes/compute_nodes/online"));
        assertThat(NodePathGenerator.toPath(new OnlineNodePath(InstanceType.JDBC, null)), is("/nodes/compute_nodes/online/jdbc"));
        assertThat(NodePathGenerator.toPath(new OnlineNodePath(InstanceType.PROXY, "foo_instance_id")), is("/nodes/compute_nodes/online/proxy/foo_instance_id"));
    }
    
    @Test
    void assertCreateInstanceTypeSearchCriteria() {
        assertThat(NodePathSearcher.get("/nodes/compute_nodes/online/jdbc/foo_instance_id", OnlineNodePath.createInstanceTypeSearchCriteria()), is("jdbc"));
        assertFalse(NodePathSearcher.find("/nodes/compute_nodes/online/jdbc", OnlineNodePath.createInstanceTypeSearchCriteria()).isPresent());
    }
    
    @Test
    void assertCreateInstanceIdSearchCriteria() {
        assertThat(NodePathSearcher.get("/nodes/compute_nodes/online/jdbc/foo_instance_id", OnlineNodePath.createInstanceIdSearchCriteria()), is("foo_instance_id"));
        assertFalse(NodePathSearcher.find("/nodes/compute_nodes/online/jdbc", OnlineNodePath.createInstanceIdSearchCriteria()).isPresent());
        assertTrue(NodePathSearcher.isMatchedPath("/nodes/compute_nodes/online/proxy/foo_instance_id", OnlineNodePath.createInstanceIdSearchCriteria()));
        assertFalse(NodePathSearcher.isMatchedPath("/nodes/compute_nodes/online/proxy", OnlineNodePath.createInstanceIdSearchCriteria()));
    }
}
