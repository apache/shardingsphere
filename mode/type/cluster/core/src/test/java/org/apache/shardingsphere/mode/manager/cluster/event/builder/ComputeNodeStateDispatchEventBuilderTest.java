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

import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.LabelsEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.ComputeNodeInstanceStateChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComputeNodeStateDispatchEventBuilderTest {
    
    @Test
    void assertCreateEventWhenDisabled() {
        Optional<DispatchEvent> actual = new ComputeNodeStateDispatchEventBuilder()
                .build(new DataChangedEvent("/nodes/compute_nodes/status/foo_instance_id", InstanceState.CIRCUIT_BREAK.name(), Type.ADDED));
        assertTrue(actual.isPresent());
        assertThat(((ComputeNodeInstanceStateChangedEvent) actual.get()).getStatus(), is(InstanceState.CIRCUIT_BREAK.name()));
        assertThat(((ComputeNodeInstanceStateChangedEvent) actual.get()).getInstanceId(), is("foo_instance_id"));
    }
    
    @Test
    void assertCreateEventWhenEnabled() {
        Optional<DispatchEvent> actual = new ComputeNodeStateDispatchEventBuilder()
                .build(new DataChangedEvent("/nodes/compute_nodes/status/foo_instance_id", "", Type.UPDATED));
        assertTrue(actual.isPresent());
        assertTrue(((ComputeNodeInstanceStateChangedEvent) actual.get()).getStatus().isEmpty());
        assertThat(((ComputeNodeInstanceStateChangedEvent) actual.get()).getInstanceId(), is("foo_instance_id"));
    }
    
    @Test
    void assertCreateAddLabelEvent() {
        Optional<DispatchEvent> actual = new ComputeNodeStateDispatchEventBuilder()
                .build(new DataChangedEvent("/nodes/compute_nodes/labels/foo_instance_id",
                        YamlEngine.marshal(Arrays.asList("label_1", "label_2")), Type.ADDED));
        assertTrue(actual.isPresent());
        assertThat(((LabelsEvent) actual.get()).getLabels(), is(Arrays.asList("label_1", "label_2")));
        assertThat(((LabelsEvent) actual.get()).getInstanceId(), is("foo_instance_id"));
    }
    
    @Test
    void assertCreateUpdateLabelsEvent() {
        Optional<DispatchEvent> actual = new ComputeNodeStateDispatchEventBuilder()
                .build(new DataChangedEvent("/nodes/compute_nodes/labels/foo_instance_id",
                        YamlEngine.marshal(Arrays.asList("label_1", "label_2")), Type.UPDATED));
        assertTrue(actual.isPresent());
        assertThat(((LabelsEvent) actual.get()).getLabels(), is(Arrays.asList("label_1", "label_2")));
        assertThat(((LabelsEvent) actual.get()).getInstanceId(), is("foo_instance_id"));
    }
}
