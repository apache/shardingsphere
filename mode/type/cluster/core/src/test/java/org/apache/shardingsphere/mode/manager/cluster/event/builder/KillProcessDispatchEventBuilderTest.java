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

package org.apache.shardingsphere.mode.manager.cluster.event.builder;

import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.KillLocalProcessCompletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.KillLocalProcessEvent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KillProcessDispatchEventBuilderTest {
    
    private final KillProcessDispatchEventBuilder builder = new KillProcessDispatchEventBuilder();
    
    @Test
    void assertGetSubscribedKey() {
        assertThat(builder.getSubscribedKey(), is(ComputeNode.getKillProcessTriggerNodePath()));
    }
    
    @Test
    void assertGetSubscribedTypes() {
        assertThat(builder.getSubscribedTypes().size(), is(2));
    }
    
    @Test
    void assertBuildKillLocalProcessEvent() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/nodes/compute_nodes/kill_process_trigger/foo_instance_id:foo_pid", "", Type.ADDED));
        assertTrue(actual.isPresent());
        assertThat(((KillLocalProcessEvent) actual.get()).getInstanceId(), is("foo_instance_id"));
        assertThat(((KillLocalProcessEvent) actual.get()).getProcessId(), is("foo_pid"));
    }
    
    @Test
    void assertBuildReportKillLocalProcessCompletedEvent() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/nodes/compute_nodes/kill_process_trigger/foo_instance_id:foo_pid", "", Type.DELETED));
        assertTrue(actual.isPresent());
        assertThat(((KillLocalProcessCompletedEvent) actual.get()).getProcessId(), is("foo_pid"));
    }
    
    @Test
    void assertBuildWithUpdateReportKillLocalProcessCompletedEvent() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/nodes/compute_nodes/kill_process_trigger/foo_instance_id:foo_pid", "", Type.UPDATED));
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertBuildWithInvalidKillProcessListTriggerEventKey() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/nodes/compute_nodes/kill_process_trigger/foo_instance_id", "", Type.ADDED));
        assertFalse(actual.isPresent());
    }
}
