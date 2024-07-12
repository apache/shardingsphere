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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ComputeNodeInstanceContextTest {
    
    private final ModeConfiguration modeConfig = new ModeConfiguration("Standalone", null);
    
    private final LockContext lockContext = mock(LockContext.class);
    
    private final EventBusContext eventBusContext = new EventBusContext();
    
    @Test
    void assertUpdateComputeNodeState() {
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        when(instanceMetaData.getId()).thenReturn("foo_instance_id");
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(instanceMetaData), mock(WorkerIdGenerator.class), modeConfig, lockContext, eventBusContext);
        InstanceState actual = context.getInstance().getState().getCurrentState();
        assertThat(actual, is(InstanceState.OK));
        context.updateStatus(instanceMetaData.getId(), InstanceState.CIRCUIT_BREAK.name());
        actual = context.getInstance().getState().getCurrentState();
        assertThat(actual, is(InstanceState.CIRCUIT_BREAK));
        context.updateStatus(instanceMetaData.getId(), InstanceState.OK.name());
        actual = context.getInstance().getState().getCurrentState();
        assertThat(actual, is(InstanceState.OK));
    }
    
    @Test
    void assertGetWorkerId() {
        ComputeNodeInstance computeNodeInstance = mock(ComputeNodeInstance.class);
        when(computeNodeInstance.getWorkerId()).thenReturn(0);
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(computeNodeInstance, mock(WorkerIdGenerator.class), modeConfig, lockContext, eventBusContext);
        assertThat(context.getWorkerId(), is(0));
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
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(instanceMetaData), mock(WorkerIdGenerator.class), modeConfig, lockContext, eventBusContext);
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
    
    @Test
    void assertIsCluster() {
        ComputeNodeInstanceContext context = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)), mock(WorkerIdGenerator.class), modeConfig, lockContext, eventBusContext);
        assertFalse(context.isCluster());
        ComputeNodeInstanceContext clusterContext = new ComputeNodeInstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), mock(WorkerIdGenerator.class),
                new ModeConfiguration("Cluster", null), lockContext, eventBusContext);
        assertTrue(clusterContext.isCluster());
    }
}
