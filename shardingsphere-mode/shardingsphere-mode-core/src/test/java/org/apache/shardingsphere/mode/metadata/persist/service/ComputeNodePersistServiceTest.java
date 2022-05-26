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

package org.apache.shardingsphere.mode.metadata.persist.service;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ComputeNodePersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    public void assertPersistInstanceLabels() {
        ComputeNodePersistService computeNodePersistService = new ComputeNodePersistService(repository);
        InstanceDefinition instanceDefinition = new InstanceDefinition(InstanceType.PROXY, 3307);
        final String instanceId = instanceDefinition.getInstanceId().getId();
        computeNodePersistService.persistInstanceLabels(instanceId, Collections.singletonList("test"), true);
        verify(repository, times(1)).persist(ComputeNode.getInstanceLabelsNodePath(instanceId), YamlEngine.marshal(Collections.singletonList("test")));
        computeNodePersistService.persistInstanceLabels(instanceId, Collections.emptyList(), true);
        verify(repository, times(0)).persist(ComputeNode.getInstanceLabelsNodePath(instanceId), YamlEngine.marshal(Collections.emptyList()));
    }
    
    @Test
    public void assertPersistInstanceWorkerId() {
        InstanceDefinition instanceDefinition = new InstanceDefinition(InstanceType.PROXY, 3307);
        final String instanceId = instanceDefinition.getInstanceId().getId();
        new ComputeNodePersistService(repository).persistInstanceWorkerId(instanceId, 100L);
        verify(repository).persist(ComputeNode.getInstanceWorkerIdNodePath(instanceId), String.valueOf(100L));
    }
    
    @Test
    public void assertPersistInstanceXaRecoveryId() {
        InstanceDefinition instanceDefinition = new InstanceDefinition(InstanceType.PROXY, 3307);
        final String instanceId = instanceDefinition.getInstanceId().getId();
        new ComputeNodePersistService(repository).persistInstanceXaRecoveryId(instanceId, instanceId);
        verify(repository).getChildrenKeys(ComputeNode.getXaRecoveryIdNodePath());
        verify(repository).persist(ComputeNode.getInstanceXaRecoveryIdNodePath(instanceId, instanceId), "");
    }
    
    @Test
    public void assertLoadInstanceLabels() {
        InstanceDefinition instanceDefinition = new InstanceDefinition(InstanceType.PROXY, 3307);
        final String instanceId = instanceDefinition.getInstanceId().getId();
        new ComputeNodePersistService(repository).loadInstanceLabels(instanceId);
        verify(repository).get(ComputeNode.getInstanceLabelsNodePath(instanceId));
    }
    
    @Test
    public void assertLoadInstanceStatus() {
        InstanceDefinition instanceDefinition = new InstanceDefinition(InstanceType.PROXY, 3307);
        final String instanceId = instanceDefinition.getInstanceId().getId();
        new ComputeNodePersistService(repository).loadInstanceStatus(instanceId);
        verify(repository).get(ComputeNode.getInstanceStatusNodePath(instanceId));
    }
    
    @Test
    public void assertLoadInstanceWorkerId() {
        InstanceDefinition instanceDefinition = new InstanceDefinition(InstanceType.PROXY, 3307);
        final String instanceId = instanceDefinition.getInstanceId().getId();
        new ComputeNodePersistService(repository).loadInstanceWorkerId(instanceId);
        verify(repository).get(ComputeNode.getInstanceWorkerIdNodePath(instanceId));
    }
    
    @Test
    public void assertLoadInstanceXaRecoveryId() {
        InstanceDefinition instanceDefinition = new InstanceDefinition(InstanceType.PROXY, 3307);
        final String instanceId = instanceDefinition.getInstanceId().getId();
        new ComputeNodePersistService(repository).loadXaRecoveryId(instanceId);
        verify(repository).getChildrenKeys(ComputeNode.getXaRecoveryIdNodePath());
        
    }
    
    @Test
    public void assertLoadAllComputeNodeInstances() {
        when(repository.getChildrenKeys("/nodes/compute_nodes/online/proxy")).thenReturn(Collections.singletonList("127.0.0.1@3307"));
        when(repository.getChildrenKeys("/nodes/compute_nodes/online/jdbc")).thenReturn(Collections.singletonList("127.0.0.1@3308"));
        Collection<ComputeNodeInstance> actual = new ComputeNodePersistService(repository).loadAllComputeNodeInstances();
        assertThat(actual.size(), is(2));
    }
    
    @Test
    public void assertLoadComputeNodeInstance() {
        InstanceDefinition instanceDefinition = new InstanceDefinition(InstanceType.PROXY, 3307);
        ComputeNodeInstance actual = new ComputeNodePersistService(repository).loadComputeNodeInstance(instanceDefinition);
        assertThat(actual.getInstanceDefinition(), is(instanceDefinition));
    }
}
