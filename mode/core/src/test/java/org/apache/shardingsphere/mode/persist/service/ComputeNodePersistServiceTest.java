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

package org.apache.shardingsphere.mode.persist.service;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.instance.yaml.YamlComputeNodeData;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComputeNodePersistServiceTest {
    
    private ComputeNodePersistService computeNodePersistService;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        computeNodePersistService = new ComputeNodePersistService(repository);
    }
    
    @Test
    void assertRegisterOnline() {
        ComputeNodeInstance computeNodeInstance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo_instance_id", 3307));
        computeNodeInstance.getLabels().add("test");
        computeNodePersistService.registerOnline(computeNodeInstance);
        verify(repository).persistEphemeral(eq("/nodes/compute_nodes/online/proxy/foo_instance_id"), anyString());
        verify(repository).persistEphemeral("/nodes/compute_nodes/status/foo_instance_id", InstanceState.OK.name());
        verify(repository).persistEphemeral("/nodes/compute_nodes/labels/foo_instance_id", YamlEngine.marshal(Collections.singletonList("test")));
    }
    
    @Test
    void assertPersistInstanceLabels() {
        String instanceId = new ProxyInstanceMetaData("foo_instance_id", 3307).getId();
        computeNodePersistService.persistInstanceLabels(instanceId, Collections.singletonList("test"));
        verify(repository).persistEphemeral("/nodes/compute_nodes/labels/foo_instance_id", YamlEngine.marshal(Collections.singletonList("test")));
    }
    
    @Test
    void assertPersistInstanceWorkerId() {
        String instanceId = new ProxyInstanceMetaData("foo_instance_id", 3307).getId();
        computeNodePersistService.persistInstanceWorkerId(instanceId, 100);
        verify(repository).persistEphemeral("/nodes/compute_nodes/worker_id/foo_instance_id", String.valueOf(100));
    }
    
    @Test
    void assertLoadEmptyInstanceLabels() {
        String instanceId = new ProxyInstanceMetaData("foo_instance_id", 3307).getId();
        when(repository.query("/nodes/compute_nodes/labels/foo_instance_id")).thenReturn("");
        assertTrue(computeNodePersistService.loadInstanceLabels(instanceId).isEmpty());
    }
    
    @Test
    void assertLoadInstanceLabels() {
        String instanceId = new ProxyInstanceMetaData("foo_instance_id", 3307).getId();
        when(repository.query("/nodes/compute_nodes/labels/foo_instance_id")).thenReturn("{xxx:xxx}");
        assertFalse(computeNodePersistService.loadInstanceLabels(instanceId).isEmpty());
    }
    
    @Test
    void assertLoadComputeNodeState() {
        String instanceId = new ProxyInstanceMetaData("foo_instance_id", 3307).getId();
        when(repository.query("/nodes/compute_nodes/status/foo_instance_id")).thenReturn("OK");
        assertThat(computeNodePersistService.loadComputeNodeState(instanceId), is("OK"));
    }
    
    @Test
    void assertLoadInstanceWorkerId() {
        String instanceId = new ProxyInstanceMetaData("foo_instance_id", 3307).getId();
        when(repository.query("/nodes/compute_nodes/worker_id/foo_instance_id")).thenReturn("1");
        assertThat(computeNodePersistService.loadInstanceWorkerId(instanceId), is(Optional.of(1)));
    }
    
    @Test
    void assertLoadWithEmptyInstanceWorkerId() {
        String instanceId = new ProxyInstanceMetaData("foo_instance_id", 3307).getId();
        when(repository.query("/nodes/compute_nodes/worker_id/foo_instance_id")).thenReturn("");
        assertFalse(computeNodePersistService.loadInstanceWorkerId(instanceId).isPresent());
    }
    
    @Test
    void assertLoadInstanceWorkerIdWithInvalidFormat() {
        String instanceId = new ProxyInstanceMetaData("foo_instance_id", 3307).getId();
        when(repository.query("/nodes/compute_nodes/worker_id/foo_instance_id")).thenReturn("a");
        assertFalse(computeNodePersistService.loadInstanceWorkerId(instanceId).isPresent());
    }
    
    @Test
    void assertLoadAllComputeNodeInstances() {
        when(repository.getChildrenKeys("/nodes/compute_nodes/online/jdbc")).thenReturn(Collections.singletonList("foo_instance_3307"));
        when(repository.getChildrenKeys("/nodes/compute_nodes/online/proxy")).thenReturn(Collections.singletonList("foo_instance_3308"));
        YamlComputeNodeData yamlComputeNodeData0 = new YamlComputeNodeData();
        yamlComputeNodeData0.setAttribute("127.0.0.1");
        yamlComputeNodeData0.setVersion("foo_version");
        when(repository.query("/nodes/compute_nodes/online/jdbc/foo_instance_3307")).thenReturn(YamlEngine.marshal(yamlComputeNodeData0));
        List<ComputeNodeInstance> actual = new ArrayList<>(computeNodePersistService.loadAllComputeNodeInstances());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getMetaData().getId(), is("foo_instance_3307"));
        assertThat(actual.get(0).getMetaData().getIp(), is("127.0.0.1"));
    }
    
    @Test
    void assertLoadComputeNodeInstance() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        ComputeNodeInstance actual = computeNodePersistService.loadComputeNodeInstance(instanceMetaData);
        assertThat(actual.getMetaData(), is(instanceMetaData));
    }
    
    @Test
    void assertGetUsedWorkerIds() {
        when(repository.getChildrenKeys("/nodes/compute_nodes/worker_id")).thenReturn(Arrays.asList("1", "2"));
        when(repository.query("/nodes/compute_nodes/worker_id/1")).thenReturn(null);
        when(repository.query("/nodes/compute_nodes/worker_id/2")).thenReturn("2");
        assertThat(computeNodePersistService.getAssignedWorkerIds(), is(Collections.singleton(2)));
    }
    
    @Test
    void assertUpdateComputeNodeState() {
        computeNodePersistService.updateComputeNodeState("foo_instance_id", InstanceState.OK);
        verify(repository).persistEphemeral("/nodes/compute_nodes/status/foo_instance_id", InstanceState.OK.name());
    }
    
    @Test
    void assertOffline() {
        computeNodePersistService.offline(new ComputeNodeInstance(new ProxyInstanceMetaData("foo_instance_id", 3307)));
        verify(repository).delete("/nodes/compute_nodes/online/proxy/foo_instance_id");
    }
}
