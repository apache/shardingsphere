/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.registry.state.listener;

import io.shardingsphere.orchestration.internal.registry.state.node.StateNodeStatus;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent.ChangedType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class InstanceStateChangedListenerTest {
    
    private InstanceStateChangedListener instanceStateChangedListener;
    
    @Mock
    private RegistryCenter regCenter;
    
    @Before
    public void setUp() {
        instanceStateChangedListener = new InstanceStateChangedListener("test", regCenter);
    }
    
    @Test
    public void assertCreateShardingOrchestrationEventWhenEnabled() {
        assertFalse(instanceStateChangedListener.createShardingOrchestrationEvent(new DataChangedEvent("test/test_ds", "", ChangedType.UPDATED)).isCircuitBreak());
    }
    
    @Test
    public void assertCreateShardingOrchestrationEventWhenDisabled() {
        assertTrue(instanceStateChangedListener.createShardingOrchestrationEvent(new DataChangedEvent("test/test_ds", StateNodeStatus.DISABLED.name(), ChangedType.UPDATED)).isCircuitBreak());
    }
}
