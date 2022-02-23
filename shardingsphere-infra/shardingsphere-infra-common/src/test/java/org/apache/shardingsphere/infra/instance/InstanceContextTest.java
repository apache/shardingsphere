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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.fixture.WorkerIdGeneratorFixture;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.infra.state.StateType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class InstanceContextTest {

    private final ModeConfiguration modeConfiguration = new ModeConfiguration("Memory", null, false);

    @Test
    public void assertUpdateInstanceStatus() {
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfiguration);
        StateType actual = context.getState().getCurrentState();
        assertThat(actual, is(StateType.OK));
        context.updateInstanceStatus(Lists.newArrayList(StateType.CIRCUIT_BREAK.name()));
        actual = context.getState().getCurrentState();
        assertThat(actual, is(StateType.CIRCUIT_BREAK));
        context.updateInstanceStatus(Lists.newArrayList());
        actual = context.getState().getCurrentState();
        assertThat(actual, is(StateType.OK));
    }

    @Test
    public void assertUpdateWorkerId() {
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfiguration);
        Long actual = context.getWorkerId();
        assertThat(actual, is(Long.MIN_VALUE));
        Random random = new Random();
        Long expected = random.nextLong();
        context.updateWorkerId(expected);
        actual = context.getWorkerId();
        assertThat(actual, is(expected));
    }

    @Test
    public void assertUpdateLabel() {
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfiguration);
        Collection<String> expected = Arrays.asList("label_1", "label_2");
        context.updateLabel(expected);
        Collection<String> actual = context.getInstance().getLabels();
        assertThat(actual, is(expected));
    }

    @Test
    public void assertGetInstance() {
        ComputeNodeInstance expected = new ComputeNodeInstance();
        InstanceContext context = new InstanceContext(expected, new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfiguration);
        ComputeNodeInstance actual = context.getInstance();
        assertThat(actual, is(expected));
    }

    @Test
    public void assertGetState() {
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfiguration);
        StateContext actual = context.getState();
        assertNotNull(actual);
    }

    @Test
    public void assertGetWorkerIdGenerator() {
        WorkerIdGeneratorFixture expected = new WorkerIdGeneratorFixture(Long.MIN_VALUE);
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(), expected, modeConfiguration);
        WorkerIdGeneratorFixture actual = (WorkerIdGeneratorFixture) context.getWorkerIdGenerator();
        assertThat(actual, is(expected));
    }

    @Test
    public void assertGetModeConfiguration() {
        InstanceContext context = new InstanceContext(new ComputeNodeInstance(), new WorkerIdGeneratorFixture(Long.MIN_VALUE), modeConfiguration);
        ModeConfiguration actual = context.getModeConfiguration();
        assertThat(actual, is(modeConfiguration));
    }
}
