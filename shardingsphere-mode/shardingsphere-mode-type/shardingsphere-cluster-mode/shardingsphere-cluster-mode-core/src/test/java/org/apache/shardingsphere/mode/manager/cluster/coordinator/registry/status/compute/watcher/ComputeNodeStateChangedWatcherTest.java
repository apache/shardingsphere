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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.watcher;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.StateEvent;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.ComputeNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.WorkerIdEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ComputeNodeStateChangedWatcherTest {
    
    @Test
    public void assertCreateEventWhenEnabled() {
        Optional<GovernanceEvent> actual = new ComputeNodeStateChangedWatcher().createGovernanceEvent(new DataChangedEvent("/nodes/compute_nodes/attributes/127.0.0.1@3307/status", 
                YamlEngine.marshal(Arrays.asList(ComputeNodeStatus.CIRCUIT_BREAK.name())), Type.ADDED));
        assertTrue(actual.isPresent());
        assertThat(((StateEvent) actual.get()).getStatus(), is(Arrays.asList(ComputeNodeStatus.CIRCUIT_BREAK.name())));
        assertThat(((StateEvent) actual.get()).getInstanceId(), is("127.0.0.1@3307"));
    }
    
    @Test
    public void assertCreateEventWhenDisabled() {
        Optional<GovernanceEvent> actual = new ComputeNodeStateChangedWatcher()
                .createGovernanceEvent(new DataChangedEvent("/nodes/compute_nodes/attributes/127.0.0.1@3307/status", "", Type.UPDATED));
        assertTrue(actual.isPresent());
        assertTrue(((StateEvent) actual.get()).getStatus().isEmpty());
        assertThat(((StateEvent) actual.get()).getInstanceId(), is("127.0.0.1@3307"));
    }
    
    @Test
    public void assertCreateAddWorkerIdEvent() {
        Optional<GovernanceEvent> actual = new ComputeNodeStateChangedWatcher()
                .createGovernanceEvent(new DataChangedEvent("/nodes/compute_nodes/attributes/127.0.0.1@3307/worker_id", "123", Type.ADDED));
        assertTrue(actual.isPresent());
        assertThat(((WorkerIdEvent) actual.get()).getWorkerId(), is(123L));
        assertThat(((WorkerIdEvent) actual.get()).getInstanceId(), is("127.0.0.1@3307"));
    }
    
    @Test
    public void assertCreateUpdateWorkerIdEvent() {
        Optional<GovernanceEvent> actual = new ComputeNodeStateChangedWatcher()
                .createGovernanceEvent(new DataChangedEvent("/nodes/compute_nodes/attributes/127.0.0.1@3307/worker_id", "123", Type.UPDATED));
        assertTrue(actual.isPresent());
        assertThat(((WorkerIdEvent) actual.get()).getWorkerId(), is(123L));
        assertThat(((WorkerIdEvent) actual.get()).getInstanceId(), is("127.0.0.1@3307"));
    }
    
    @Test
    public void assertCreateAddLabelEvent() {
        Optional<GovernanceEvent> actual = new ComputeNodeStateChangedWatcher()
                .createGovernanceEvent(new DataChangedEvent("/nodes/compute_nodes/attributes/127.0.0.1@3307/labels", 
                        YamlEngine.marshal(Arrays.asList("label_1", "label_2")), Type.ADDED));
        assertTrue(actual.isPresent());
        assertThat(((LabelsEvent) actual.get()).getLabels(), is(Arrays.asList("label_1", "label_2")));
        assertThat(((LabelsEvent) actual.get()).getInstanceId(), is("127.0.0.1@3307"));
    }
    
    @Test
    public void assertCreateUpdateLabelsEvent() {
        Optional<GovernanceEvent> actual = new ComputeNodeStateChangedWatcher()
                .createGovernanceEvent(new DataChangedEvent("/nodes/compute_nodes/attributes/127.0.0.1@3307/labels", 
                        YamlEngine.marshal(Arrays.asList("label_1", "label_2")), Type.UPDATED));
        assertTrue(actual.isPresent());
        assertThat(((LabelsEvent) actual.get()).getLabels(), is(Arrays.asList("label_1", "label_2")));
        assertThat(((LabelsEvent) actual.get()).getInstanceId(), is("127.0.0.1@3307"));
    }
}
