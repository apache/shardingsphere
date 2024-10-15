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
import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ComputeNodeInstanceContextTest {
    
    private final ModeConfiguration modeConfig = new ModeConfiguration("Standalone", null);
    
    private final LockContext<?> lockContext = mock(LockContext.class);
    
    private final EventBusContext eventBusContext = new EventBusContext();
    
    @Test
    void assertInit() {
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), modeConfig, eventBusContext);
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        context.init(workerIdGenerator, lockContext);
        context.generateWorkerId(new Properties());
        verify(workerIdGenerator).generate(new Properties());
        assertThat(context.getLockContext(), is(lockContext));
    }
    
    @Test
    void assertUpdateStatusWithoutInstanceState() {
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(new ComputeNodeInstance(instanceMetaData), mock(WorkerIdGenerator.class), modeConfig, lockContext, eventBusContext);
        context.updateStatus("id", "INVALID");
        verify(instanceMetaData, times(0)).getId();
    }
    
    @Test
    void assertUpdateStatusWithOtherInstance() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3306);
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(new ComputeNodeInstance(instanceMetaData), mock(WorkerIdGenerator.class), modeConfig, lockContext, eventBusContext);
        context.addComputeNodeInstance(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        context.updateStatus("bar_instance_id", InstanceState.CIRCUIT_BREAK.name());
        assertThat(context.getInstance().getState().getCurrentState(), is(InstanceState.OK));
    }
    
    @Test
    void assertUpdateStatusWithCurrentInstance() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3306);
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(new ComputeNodeInstance(instanceMetaData), mock(WorkerIdGenerator.class), modeConfig, lockContext, eventBusContext);
        context.addComputeNodeInstance(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        context.updateStatus("foo_instance_id", InstanceState.CIRCUIT_BREAK.name());
        assertThat(context.getInstance().getState().getCurrentState(), is(InstanceState.CIRCUIT_BREAK));
    }
    
    @Test
    void assertUpdateWorkerIdWithOtherInstance() {
        ComputeNodeInstance instance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo_instance_id", 3306));
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(instance, mock(WorkerIdGenerator.class), modeConfig, lockContext, eventBusContext);
        context.addComputeNodeInstance(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        context.updateWorkerId("bar_instance_id", 10);
        assertThat(context.getWorkerId(), is(-1));
        assertThat(context.getAllClusterInstances().iterator().next().getWorkerId(), is(10));
    }
    
    @Test
    void assertUpdateWorkerIdWithCurrentInstance() {
        ComputeNodeInstance instance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo_instance_id", 3306));
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(instance, mock(WorkerIdGenerator.class), modeConfig, lockContext, eventBusContext);
        context.updateWorkerId("foo_instance_id", 10);
        assertThat(context.getWorkerId(), is(10));
    }
    
    @Test
    void assertGenerateWorkerId() {
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)), mock(WorkerIdGenerator.class), modeConfig, lockContext, eventBusContext);
        assertThat(context.generateWorkerId(new Properties()), is(0));
    }
    
    @Test
    void assertUpdateLabel() {
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        when(instanceMetaData.getId()).thenReturn("foo_instance_id");
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(new ComputeNodeInstance(instanceMetaData), mock(WorkerIdGenerator.class), modeConfig, lockContext, eventBusContext);
        Collection<String> expected = Arrays.asList("label_1", "label_2");
        context.updateLabel("foo_instance_id", expected);
        Collection<String> actual = context.getInstance().getLabels();
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertGetInstance() {
        ComputeNodeInstance expected = new ComputeNodeInstance(mock(InstanceMetaData.class));
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(expected, mock(WorkerIdGenerator.class), modeConfig, lockContext, eventBusContext);
        ComputeNodeInstance actual = context.getInstance();
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertGetState() {
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)), mock(WorkerIdGenerator.class), modeConfig, lockContext, eventBusContext);
        assertNotNull(context.getInstance().getState());
    }
    
    @Test
    void assertGetModeConfiguration() {
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)), mock(WorkerIdGenerator.class), modeConfig, lockContext, eventBusContext);
        assertThat(context.getModeConfiguration(), is(modeConfig));
    }
}
