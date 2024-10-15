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

package org.apache.shardingsphere.infra.instance;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ComputeNodeInstanceContextTest {
    
    @Test
    void assertInit() {
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), mock(ModeConfiguration.class), new EventBusContext());
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        LockContext<?> lockContext = mock(LockContext.class);
        context.init(workerIdGenerator, lockContext);
        context.generateWorkerId(new Properties());
        verify(workerIdGenerator).generate(new Properties());
        assertThat(context.getLockContext(), is(lockContext));
    }
    
    @Test
    void assertUpdateStatusWithInvalidInstanceState() {
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(instanceMetaData), mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), mock(LockContext.class), new EventBusContext());
        context.updateStatus("id", "INVALID");
        verify(instanceMetaData, times(0)).getId();
    }
    
    @Test
    void assertUpdateStatusWithCurrentInstance() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3306);
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(instanceMetaData), mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), mock(LockContext.class), new EventBusContext());
        context.addComputeNodeInstance(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        context.updateStatus("foo_instance_id", InstanceState.CIRCUIT_BREAK.name());
        assertThat(context.getInstance().getState().getCurrentState(), is(InstanceState.CIRCUIT_BREAK));
    }
    
    @Test
    void assertUpdateStatusWithOtherInstance() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3306);
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(instanceMetaData), mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), mock(LockContext.class), new EventBusContext());
        context.addComputeNodeInstance(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        context.updateStatus("bar_instance_id", InstanceState.CIRCUIT_BREAK.name());
        assertThat(context.getInstance().getState().getCurrentState(), is(InstanceState.OK));
    }
    
    @Test
    void assertUpdateLabelsWithCurrentInstance() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3306);
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(instanceMetaData), mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), mock(LockContext.class), new EventBusContext());
        context.updateLabels("foo_instance_id", Arrays.asList("label_1", "label_2"));
        assertThat(context.getInstance().getLabels(), is(Arrays.asList("label_1", "label_2")));
    }
    
    @Test
    void assertUpdateLabelsWithOtherInstance() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3306);
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(instanceMetaData), mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), mock(LockContext.class), new EventBusContext());
        context.addComputeNodeInstance(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        context.updateLabels("bar_instance_id", Arrays.asList("label_1", "label_2"));
        assertTrue(context.getInstance().getLabels().isEmpty());
        assertThat(context.getAllClusterInstances().iterator().next().getLabels(), is(Arrays.asList("label_1", "label_2")));
    }
    
    @Test
    void assertUpdateWorkerIdWithCurrentInstance() {
        ComputeNodeInstance instance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo_instance_id", 3306));
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(instance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), mock(LockContext.class), new EventBusContext());
        context.updateWorkerId("foo_instance_id", 10);
        assertThat(context.getWorkerId(), is(10));
    }
    
    @Test
    void assertUpdateWorkerIdWithOtherInstance() {
        ComputeNodeInstance instance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo_instance_id", 3306));
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(instance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), mock(LockContext.class), new EventBusContext());
        context.addComputeNodeInstance(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        context.updateWorkerId("bar_instance_id", 10);
        assertThat(context.getWorkerId(), is(-1));
        assertThat(context.getAllClusterInstances().iterator().next().getWorkerId(), is(10));
    }
    
    @Test
    void assertGenerateWorkerId() {
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)), mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), mock(LockContext.class), new EventBusContext());
        assertThat(context.generateWorkerId(new Properties()), is(0));
    }
    
    @Test
    void assertAddComputeNodeInstance() {
        ComputeNodeInstance instance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo_instance_id", 3306));
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(instance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), mock(LockContext.class), new EventBusContext());
        context.addComputeNodeInstance(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        assertFalse(context.getAllClusterInstances().isEmpty());
    }
    
    @Test
    void assertDeleteComputeNodeInstance() {
        ComputeNodeInstance instance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo_instance_id", 3306));
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(instance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), mock(LockContext.class), new EventBusContext());
        context.addComputeNodeInstance(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        context.deleteComputeNodeInstance(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        assertTrue(context.getAllClusterInstances().isEmpty());
    }
    
    @Test
    void assertGetComputeNodeInstanceById() {
        ComputeNodeInstance instance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo_instance_id", 3306));
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(instance, mock(WorkerIdGenerator.class), mock(ModeConfiguration.class), mock(LockContext.class), new EventBusContext());
        context.addComputeNodeInstance(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        Optional<ComputeNodeInstance> actual = context.getComputeNodeInstanceById("bar_instance_id");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getMetaData().getId(), is("bar_instance_id"));
    }
}
