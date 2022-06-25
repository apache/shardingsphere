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
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.fixture.WorkerIdGeneratorFixture;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.infra.state.StateType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class InstanceContextTest {
    
    private final ModeConfiguration modeConfig = new ModeConfiguration("Memory", null, false);
    
    private final LockContext lockContext = mock(LockContext.class);
    
    @Test
    public void assertUpdateInstanceStatus() {
        InstanceDefinition instanceDefinition = mock(InstanceDefinition.class);
        when(instanceDefinition.getInstanceId()).thenReturn("foo_instance_id");
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(instanceDefinition), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfig, lockContext);
        StateType actual = context.getInstance().getState().getCurrentState();
        assertThat(actual, is(StateType.OK));
        context.updateInstanceStatus(instanceDefinition.getInstanceId(), Collections.singleton(StateType.CIRCUIT_BREAK.name()));
        actual = context.getInstance().getState().getCurrentState();
        assertThat(actual, is(StateType.CIRCUIT_BREAK));
        context.updateInstanceStatus(instanceDefinition.getInstanceId(), Collections.emptyList());
        actual = context.getInstance().getState().getCurrentState();
        assertThat(actual, is(StateType.OK));
    }
    
    @Test
    public void assertUpdateWorkerId() {
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(mock(InstanceDefinition.class)), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfig, lockContext);
        long actual = context.getWorkerId();
        assertThat(actual, is(Long.MIN_VALUE));
        Random random = new Random();
        Long expected = random.nextLong();
        context.updateWorkerId(expected);
        actual = context.getWorkerId();
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertUpdateLabel() {
        InstanceDefinition instanceDefinition = mock(InstanceDefinition.class);
        when(instanceDefinition.getInstanceId()).thenReturn("foo_instance_id");
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(instanceDefinition), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfig, lockContext);
        Set<String> expected = new LinkedHashSet<>(Arrays.asList("label_1", "label_2"));
        context.updateLabel("foo_instance_id", expected);
        Collection<String> actual = context.getInstance().getLabels();
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertGetInstance() {
        ComputeNodeInstance expected = new ComputeNodeInstance(mock(InstanceDefinition.class));
        InstanceContext context = new InstanceContext(expected, new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfig, lockContext);
        ComputeNodeInstance actual = context.getInstance();
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertGetState() {
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(mock(InstanceDefinition.class)), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfig, lockContext);
        StateContext actual = context.getInstance().getState();
        assertNotNull(actual);
    }
    
    @Test
    public void assertGetWorkerIdGenerator() {
        WorkerIdGeneratorFixture expected = new WorkerIdGeneratorFixture(Long.MIN_VALUE);
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(mock(InstanceDefinition.class)), expected, modeConfig, lockContext);
        WorkerIdGeneratorFixture actual = (WorkerIdGeneratorFixture) context.getWorkerIdGenerator();
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertGetModeConfiguration() {
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(mock(InstanceDefinition.class)), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfig, lockContext);
        ModeConfiguration actual = context.getModeConfiguration();
        assertThat(actual, is(modeConfig));
    }
    
    @Test
    public void assertIsCluster() {
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(mock(InstanceDefinition.class)), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfig, lockContext);
        assertFalse(context.isCluster());
        InstanceContext clusterContext = new InstanceContext(new ComputeNodeInstance(mock(InstanceDefinition.class)), new WorkerIdGeneratorFixture(Long.MIN_VALUE),
                new ModeConfiguration("Cluster", null, false), lockContext);
        assertTrue(clusterContext.isCluster());
    }
}
