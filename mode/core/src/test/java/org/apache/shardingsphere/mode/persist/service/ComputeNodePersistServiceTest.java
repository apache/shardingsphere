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
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.instance.yaml.YamlComputeNodeData;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComputeNodePersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    void assertRegisterOnline() {
        ComputeNodeInstance computeNodeInstance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo_instance_id", 3307));
        computeNodeInstance.getLabels().add("test");
        new ComputeNodePersistService(repository).registerOnline(computeNodeInstance);
        verify(repository).persistEphemeral(eq("/nodes/compute_nodes/online/proxy/" + computeNodeInstance.getMetaData().getId()), anyString());
        verify(repository).persistEphemeral(ComputeNode.getComputeNodeStateNodePath(computeNodeInstance.getMetaData().getId()), InstanceState.OK.name());
        verify(repository).persistEphemeral(ComputeNode.getInstanceLabelsNodePath(computeNodeInstance.getMetaData().getId()), YamlEngine.marshal(Collections.singletonList("test")));
    }
    
    @Test
    void assertPersistInstanceLabels() {
        ComputeNodePersistService computeNodePersistService = new ComputeNodePersistService(repository);
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        final String instanceId = instanceMetaData.getId();
        computeNodePersistService.persistInstanceLabels(instanceId, Collections.singletonList("test"));
        verify(repository).persistEphemeral(ComputeNode.getInstanceLabelsNodePath(instanceId), YamlEngine.marshal(Collections.singletonList("test")));
        computeNodePersistService.persistInstanceLabels(instanceId, Collections.emptyList());
        verify(repository).persistEphemeral(ComputeNode.getInstanceLabelsNodePath(instanceId), YamlEngine.marshal(Collections.emptyList()));
    }
    
    @Test
    void assertPersistInstanceWorkerId() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        final String instanceId = instanceMetaData.getId();
        new ComputeNodePersistService(repository).persistInstanceWorkerId(instanceId, 100);
        verify(repository).persistEphemeral(ComputeNode.getInstanceWorkerIdNodePath(instanceId), String.valueOf(100));
    }
    
    @Test
    void assertLoadInstanceLabels() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        final String instanceId = instanceMetaData.getId();
        new ComputeNodePersistService(repository).loadInstanceLabels(instanceId);
        verify(repository).query(ComputeNode.getInstanceLabelsNodePath(instanceId));
    }
    
    @Test
    void assertLoadComputeNodeState() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        final String instanceId = instanceMetaData.getId();
        new ComputeNodePersistService(repository).loadComputeNodeState(instanceId);
        verify(repository).query(ComputeNode.getComputeNodeStateNodePath(instanceId));
    }
    
    @Test
    void assertLoadInstanceWorkerId() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        final String instanceId = instanceMetaData.getId();
        new ComputeNodePersistService(repository).loadInstanceWorkerId(instanceId);
        verify(repository).query(ComputeNode.getInstanceWorkerIdNodePath(instanceId));
    }
    
    @Test
    void assertLoadAllComputeNodeInstances() {
        when(repository.getChildrenKeys("/nodes/compute_nodes/online/jdbc")).thenReturn(Collections.singletonList("foo_instance_3307"));
        when(repository.getChildrenKeys("/nodes/compute_nodes/online/proxy")).thenReturn(Collections.singletonList("foo_instance_3308"));
        YamlComputeNodeData yamlComputeNodeData0 = new YamlComputeNodeData();
        yamlComputeNodeData0.setAttribute("127.0.0.1");
        yamlComputeNodeData0.setVersion("foo_version");
        when(repository.query("/nodes/compute_nodes/online/jdbc/foo_instance_3307")).thenReturn(YamlEngine.marshal(yamlComputeNodeData0));
        YamlComputeNodeData yamlComputeNodeData1 = new YamlComputeNodeData();
        yamlComputeNodeData1.setAttribute("127.0.0.1@3308");
        yamlComputeNodeData1.setVersion("foo_version");
        when(repository.query("/nodes/compute_nodes/online/proxy/foo_instance_3308")).thenReturn(YamlEngine.marshal(yamlComputeNodeData1));
        List<ComputeNodeInstance> actual = new ArrayList<>(new ComputeNodePersistService(repository).loadAllComputeNodeInstances());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getMetaData().getId(), is("foo_instance_3307"));
        assertThat(actual.get(0).getMetaData().getIp(), is("127.0.0.1"));
        assertThat(actual.get(1).getMetaData().getId(), is("foo_instance_3308"));
        assertThat(actual.get(1).getMetaData().getIp(), is("127.0.0.1"));
        assertThat(actual.get(1).getMetaData().getType(), is(InstanceType.PROXY));
        assertThat(((ProxyInstanceMetaData) actual.get(1).getMetaData()).getPort(), is(3308));
    }
    
    @Test
    void assertLoadComputeNodeInstance() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        ComputeNodeInstance actual = new ComputeNodePersistService(repository).loadComputeNodeInstance(instanceMetaData);
        assertThat(actual.getMetaData(), is(instanceMetaData));
    }
    
    @Test
    void assertGetUsedWorkerIds() {
        new ComputeNodePersistService(repository).getAssignedWorkerIds();
        verify(repository).getChildrenKeys(ComputeNode.getInstanceWorkerIdRootNodePath());
    }
    
    @Test
    void assertUpdateComputeNodeState() {
        new ComputeNodePersistService(repository).updateComputeNodeState("foo_instance_id", InstanceState.OK);
        verify(repository).persistEphemeral(ComputeNode.getComputeNodeStateNodePath("foo_instance_id"), InstanceState.OK.name());
    }
    
    @Test
    void assertOffline() {
        ComputeNodeInstance computeNodeInstance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo_instance_id", 3307));
        new ComputeNodePersistService(repository).offline(computeNodeInstance);
        verify(repository).delete("/nodes/compute_nodes/online/proxy/" + computeNodeInstance.getMetaData().getId());
    }
}
