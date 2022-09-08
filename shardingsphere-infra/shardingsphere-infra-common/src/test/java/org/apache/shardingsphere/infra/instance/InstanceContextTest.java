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
import org.apache.shardingsphere.infra.schedule.ScheduleContext;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.fixture.WorkerIdGeneratorFixture;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.infra.state.StateType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class InstanceContextTest {
    
    private final ModeConfiguration modeConfig = new ModeConfiguration("Standalone", null, false);
    
    private final LockContext lockContext = mock(LockContext.class);
    
    private final EventBusContext eventBusContext = new EventBusContext();
    
    @Test
    public void assertUpdateInstanceStatus() {
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        when(instanceMetaData.getId()).thenReturn("foo_instance_id");
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(instanceMetaData), new WorkerIdGeneratorFixture(Long.MIN_VALUE),
                modeConfig, lockContext, eventBusContext, mock(ScheduleContext.class));
        StateType actual = context.getInstance().getState().getCurrentState();
        assertThat(actual, is(StateType.OK));
        context.updateInstanceStatus(instanceMetaData.getId(), Collections.singleton(StateType.CIRCUIT_BREAK.name()));
        actual = context.getInstance().getState().getCurrentState();
        assertThat(actual, is(StateType.CIRCUIT_BREAK));
        context.updateInstanceStatus(instanceMetaData.getId(), Collections.emptyList());
        actual = context.getInstance().getState().getCurrentState();
        assertThat(actual, is(StateType.OK));
    }
    
    @Test
    public void assertGetWorkerId() {
        ComputeNodeInstance computeNodeInstance = mock(ComputeNodeInstance.class);
        when(computeNodeInstance.getWorkerId()).thenReturn(0L);
        InstanceContext context = new InstanceContext(computeNodeInstance, new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfig,
                lockContext, eventBusContext, mock(ScheduleContext.class));
        assertThat(context.getWorkerId(), is(0L));
    }
    
    @Test
    public void assertGenerateWorkerId() {
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfig,
                lockContext, eventBusContext, mock(ScheduleContext.class));
        long actual = context.generateWorkerId(new Properties());
        assertThat(actual, is(Long.MIN_VALUE));
    }
    
    @Test
    public void assertUpdateLabel() {
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        when(instanceMetaData.getId()).thenReturn("foo_instance_id");
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(instanceMetaData), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfig,
                lockContext, eventBusContext, mock(ScheduleContext.class));
        Set<String> expected = new LinkedHashSet<>(Arrays.asList("label_1", "label_2"));
        context.updateLabel("foo_instance_id", expected);
        Collection<String> actual = context.getInstance().getLabels();
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertGetInstance() {
        ComputeNodeInstance expected = new ComputeNodeInstance(mock(InstanceMetaData.class));
        InstanceContext context = new InstanceContext(expected, new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfig,
                lockContext, eventBusContext, mock(ScheduleContext.class));
        ComputeNodeInstance actual = context.getInstance();
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertGetState() {
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfig,
                lockContext, eventBusContext, mock(ScheduleContext.class));
        StateContext actual = context.getInstance().getState();
        assertNotNull(actual);
    }
    
    @Test
    public void assertGetModeConfiguration() {
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfig,
                lockContext, eventBusContext, mock(ScheduleContext.class));
        ModeConfiguration actual = context.getModeConfiguration();
        assertThat(actual, is(modeConfig));
    }
    
    @Test
    public void assertIsCluster() {
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfig,
                lockContext, eventBusContext, mock(ScheduleContext.class));
        assertFalse(context.isCluster());
        InstanceContext clusterContext = new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new WorkerIdGeneratorFixture(Long.MIN_VALUE),
                new ModeConfiguration("Cluster", null, false), lockContext, eventBusContext, mock(ScheduleContext.class));
        assertTrue(clusterContext.isCluster());
    }
}
