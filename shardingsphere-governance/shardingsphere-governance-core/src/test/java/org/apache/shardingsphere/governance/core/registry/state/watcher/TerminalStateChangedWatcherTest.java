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

package org.apache.shardingsphere.governance.core.registry.state.watcher;

import org.apache.shardingsphere.governance.core.registry.state.ResourceState;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.infra.state.StateEvent;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class TerminalStateChangedWatcherTest {
    
    @Test
    public void assertCreateEventWhenEnabled() {
        Optional<StateEvent> actual = new TerminalStateChangedWatcher().createGovernanceEvent(new DataChangedEvent("/test_ds", "", Type.UPDATED));
        assertTrue(actual.isPresent());
        assertFalse(actual.get().isOn());
    }
    
    @Test
    public void assertCreateEventWhenDisabled() {
        Optional<StateEvent> actual = new TerminalStateChangedWatcher().createGovernanceEvent(new DataChangedEvent("/test_ds", ResourceState.DISABLED.name(), Type.UPDATED));
        assertTrue(actual.isPresent());
        assertTrue(actual.get().isOn());
    }
}
