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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.service;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.instance.utils.IpUtils;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ComputeNodeStatusServiceTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    @Test
    public void assertRegisterOnline() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        new ComputeNodeStatusService(repository).registerOnline(instanceMetaData);
        verify(repository).persistEphemeral(eq("/nodes/compute_nodes/online/proxy/" + instanceMetaData.getId()), anyString());
    }
    
    @Test
    public void assertPersistInstanceLabels() {
        ComputeNodeStatusService computeNodeStatusService = new ComputeNodeStatusService(repository);
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        final String instanceId = instanceMetaData.getId();
        computeNodeStatusService.persistInstanceLabels(instanceId, Collections.singletonList("test"));
        verify(repository, times(1)).persistEphemeral(ComputeNode.getInstanceLabelsNodePath(instanceId), YamlEngine.marshal(Collections.singletonList("test")));
        computeNodeStatusService.persistInstanceLabels(instanceId, Collections.emptyList());
        verify(repository, times(1)).persistEphemeral(ComputeNode.getInstanceLabelsNodePath(instanceId), YamlEngine.marshal(Collections.emptyList()));
    }
    
    @Test
    public void assertPersistInstanceWorkerId() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        final String instanceId = instanceMetaData.getId();
        new ComputeNodeStatusService(repository).persistInstanceWorkerId(instanceId, 100);
        verify(repository).persistEphemeral(ComputeNode.getInstanceWorkerIdNodePath(instanceId), String.valueOf(100));
    }
    
    @Test
    public void assertLoadInstanceLabels() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        final String instanceId = instanceMetaData.getId();
        new ComputeNodeStatusService(repository).loadInstanceLabels(instanceId);
        verify(repository).getDirectly(ComputeNode.getInstanceLabelsNodePath(instanceId));
    }
    
    @Test
    public void assertLoadInstanceStatus() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        final String instanceId = instanceMetaData.getId();
        new ComputeNodeStatusService(repository).loadInstanceStatus(instanceId);
        verify(repository).getDirectly(ComputeNode.getInstanceStatusNodePath(instanceId));
    }
    
    @Test
    public void assertLoadInstanceWorkerId() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        final String instanceId = instanceMetaData.getId();
        new ComputeNodeStatusService(repository).loadInstanceWorkerId(instanceId);
        verify(repository).getDirectly(ComputeNode.getInstanceWorkerIdNodePath(instanceId));
    }
    
    @Test
    public void assertLoadAllComputeNodeInstances() {
        when(repository.getChildrenKeys("/nodes/compute_nodes/online/jdbc")).thenReturn(Collections.singletonList("foo_instance_3307"));
        when(repository.getChildrenKeys("/nodes/compute_nodes/online/proxy")).thenReturn(Collections.singletonList("foo_instance_3308"));
        when(repository.getDirectly("/nodes/compute_nodes/online/proxy/foo_instance_3308")).thenReturn("127.0.0.1@3308");
        List<ComputeNodeInstance> actual = new ArrayList<>(new ComputeNodeStatusService(repository).loadAllComputeNodeInstances());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getMetaData().getId(), is("foo_instance_3307"));
        assertThat(actual.get(0).getMetaData().getIp(), is(IpUtils.getIp()));
        assertThat(actual.get(1).getMetaData().getId(), is("foo_instance_3308"));
        assertThat(actual.get(1).getMetaData().getIp(), is("127.0.0.1"));
        assertThat(actual.get(1).getMetaData().getType(), is(InstanceType.PROXY));
        assertThat(((ProxyInstanceMetaData) actual.get(1).getMetaData()).getPort(), is(3308));
    }
    
    @Test
    public void assertLoadComputeNodeInstance() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        ComputeNodeInstance actual = new ComputeNodeStatusService(repository).loadComputeNodeInstance(instanceMetaData);
        assertThat(actual.getMetaData(), is(instanceMetaData));
    }
    
    @Test
    public void assertGetUsedWorkerIds() {
        new ComputeNodeStatusService(repository).getAssignedWorkerIds();
        verify(repository).getChildrenKeys(ComputeNode.getInstanceWorkerIdRootNodePath());
    }
}
