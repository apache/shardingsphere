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
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ComputeNodeInstanceContextTest {
    
    @Test
    void assertInit() {
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), mock(ModeConfiguration.class), new EventBusContext());
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        context.init(workerIdGenerator);
        context.generateWorkerId(new Properties());
        verify(workerIdGenerator).generate(new Properties());
    }
    
    @Test
    void assertUpdateStatusWithInvalidInstanceState() {
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        ComputeNodeInstanceContext instanceContext = new ComputeNodeInstanceContext(new ComputeNodeInstance(instanceMetaData), mock(ModeConfiguration.class), new EventBusContext());
        instanceContext.init(mock(WorkerIdGenerator.class));
        instanceContext.updateStatus("id", "INVALID");
        verify(instanceMetaData, never()).getId();
    }
    
    @Test
    void assertUpdateStatusWithCurrentInstance() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3306);
        ComputeNodeInstanceContext instanceContext = new ComputeNodeInstanceContext(new ComputeNodeInstance(instanceMetaData), mock(ModeConfiguration.class), new EventBusContext());
        instanceContext.init(mock(WorkerIdGenerator.class));
        instanceContext.getClusterInstanceRegistry().add(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        instanceContext.updateStatus("foo_instance_id", InstanceState.CIRCUIT_BREAK.name());
        assertThat(instanceContext.getInstance().getState().getCurrentState(), is(InstanceState.CIRCUIT_BREAK));
    }
    
    @Test
    void assertUpdateStatusWithOtherInstance() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3306);
        ComputeNodeInstanceContext instanceContext = new ComputeNodeInstanceContext(new ComputeNodeInstance(instanceMetaData), mock(ModeConfiguration.class), new EventBusContext());
        instanceContext.init(mock(WorkerIdGenerator.class));
        instanceContext.getClusterInstanceRegistry().add(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        instanceContext.updateStatus("bar_instance_id", InstanceState.CIRCUIT_BREAK.name());
        assertThat(instanceContext.getInstance().getState().getCurrentState(), is(InstanceState.OK));
    }
    
    @Test
    void assertUpdateLabelsWithCurrentInstance() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3306);
        ComputeNodeInstanceContext instanceContext = new ComputeNodeInstanceContext(new ComputeNodeInstance(instanceMetaData), mock(ModeConfiguration.class), new EventBusContext());
        instanceContext.init(mock(WorkerIdGenerator.class));
        instanceContext.updateLabels("foo_instance_id", Arrays.asList("label_1", "label_2"));
        assertThat(instanceContext.getInstance().getLabels(), is(Arrays.asList("label_1", "label_2")));
    }
    
    @Test
    void assertUpdateLabelsWithOtherInstance() {
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3306);
        ComputeNodeInstanceContext instanceContext = new ComputeNodeInstanceContext(new ComputeNodeInstance(instanceMetaData), mock(ModeConfiguration.class), new EventBusContext());
        instanceContext.init(mock(WorkerIdGenerator.class));
        instanceContext.getClusterInstanceRegistry().add(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        instanceContext.updateLabels("bar_instance_id", Arrays.asList("label_1", "label_2"));
        assertTrue(instanceContext.getInstance().getLabels().isEmpty());
        assertThat(instanceContext.getClusterInstanceRegistry().getAllClusterInstances().iterator().next().getLabels(), is(Arrays.asList("label_1", "label_2")));
    }
    
    @Test
    void assertUpdateWorkerIdWithCurrentInstance() {
        ComputeNodeInstance instance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo_instance_id", 3306));
        ComputeNodeInstanceContext instanceContext = new ComputeNodeInstanceContext(instance, mock(ModeConfiguration.class), new EventBusContext());
        instanceContext.init(mock(WorkerIdGenerator.class));
        instanceContext.updateWorkerId("foo_instance_id", 10);
        assertThat(instanceContext.getWorkerId(), is(10));
    }
    
    @Test
    void assertUpdateWorkerIdWithOtherInstance() {
        ComputeNodeInstance instance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo_instance_id", 3306));
        ComputeNodeInstanceContext instanceContext = new ComputeNodeInstanceContext(instance, mock(ModeConfiguration.class), new EventBusContext());
        instanceContext.init(mock(WorkerIdGenerator.class));
        instanceContext.getClusterInstanceRegistry().add(new ComputeNodeInstance(new ProxyInstanceMetaData("bar_instance_id", 3307)));
        instanceContext.updateWorkerId("bar_instance_id", 10);
        assertThat(instanceContext.getWorkerId(), is(-1));
        assertThat(instanceContext.getClusterInstanceRegistry().getAllClusterInstances().iterator().next().getWorkerId(), is(10));
    }
    
    @Test
    void assertGenerateWorkerId() {
        ComputeNodeInstanceContext instanceContext = new ComputeNodeInstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), mock(ModeConfiguration.class), new EventBusContext());
        instanceContext.init(mock(WorkerIdGenerator.class));
        assertThat(instanceContext.generateWorkerId(new Properties()), is(0));
    }
}
