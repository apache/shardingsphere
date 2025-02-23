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

package org.apache.shardingsphere.mode.node.path.type.node.compute;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ComputeNodePathParserTest {
    
    @Test
    void assertFindInstanceId() {
        assertThat(ComputeNodePathParser.findInstanceId("/nodes/compute_nodes/status/foo_instance_1"), is(Optional.of("foo_instance_1")));
        assertThat(ComputeNodePathParser.findInstanceId("/nodes/compute_nodes/worker_id/foo_instance_2"), is(Optional.of("foo_instance_2")));
        assertThat(ComputeNodePathParser.findInstanceId("/nodes/compute_nodes/labels/foo_instance_3"), is(Optional.of("foo_instance_3")));
        assertFalse(ComputeNodePathParser.findInstanceId("/nodes/compute_nodes/invalid/foo_instance_4").isPresent());
    }
}
