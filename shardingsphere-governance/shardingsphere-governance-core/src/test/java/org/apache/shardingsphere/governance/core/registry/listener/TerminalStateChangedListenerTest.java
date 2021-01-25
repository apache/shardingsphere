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

package org.apache.shardingsphere.governance.core.registry.listener;

import org.apache.shardingsphere.governance.core.registry.RegistryCenterNodeStatus;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.infra.state.StateEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class TerminalStateChangedListenerTest {
    
    private TerminalStateChangedListener terminalStateChangedListener;
    
    @Mock
    private RegistryRepository registryRepository;
    
    @Before
    public void setUp() {
        terminalStateChangedListener = new TerminalStateChangedListener(registryRepository);
    }
    
    @Test
    public void assertCreateEventWhenEnabled() {
        Optional<StateEvent> actual = terminalStateChangedListener.createEvent(new DataChangedEvent("/test_ds", "", Type.UPDATED));
        assertTrue(actual.isPresent());
        assertFalse(actual.get().isOn());
    }
    
    @Test
    public void assertCreateEventWhenDisabled() {
        Optional<StateEvent> actual = terminalStateChangedListener.createEvent(new DataChangedEvent("/test_ds", RegistryCenterNodeStatus.DISABLED.name(), Type.UPDATED));
        assertTrue(actual.isPresent());
        assertTrue(actual.get().isOn());
    }
}
