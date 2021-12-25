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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.watcher;

import org.apache.shardingsphere.infra.state.StateEvent;
import org.apache.shardingsphere.infra.instance.Instance;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ComputeNodeStateChangedWatcherTest {
    
    private String originalClusterInstanceId;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        originalClusterInstanceId = Instance.getInstance().getId();
        Field field = Instance.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(Instance.getInstance(), "127.0.0.1@3307");
    }
    
    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        Field field = Instance.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(Instance.getInstance(), originalClusterInstanceId);
    }
    
    @Test
    public void assertCreateEventWhenEnabled() {
        Optional<StateEvent> actual = new ComputeNodeStateChangedWatcher().createGovernanceEvent(new DataChangedEvent("/status/compute_nodes/circuit_breaker/127.0.0.1@3307", "", Type.ADDED));
        assertTrue(actual.isPresent());
        assertTrue(actual.get().isOn());
    }
    
    @Test
    public void assertCreateEventWhenDisabled() {
        Optional<StateEvent> actual = new ComputeNodeStateChangedWatcher().createGovernanceEvent(new DataChangedEvent("/status/compute_nodes/circuit_breaker/127.0.0.1@3307", "", Type.DELETED));
        assertTrue(actual.isPresent());
        assertFalse(actual.get().isOn());
    }
}
