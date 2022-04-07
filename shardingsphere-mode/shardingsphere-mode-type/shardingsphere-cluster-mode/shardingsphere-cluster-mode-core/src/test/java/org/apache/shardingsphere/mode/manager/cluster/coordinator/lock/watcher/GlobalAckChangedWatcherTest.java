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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.watcher;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.AckLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.AckLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class GlobalAckChangedWatcherTest {
    
    @Test
    public void assertCreateGovernanceEvent() {
        DataChangedEvent addDataChangedEvent = new DataChangedEvent("/lock/global/ack/schema-127.0.0.1@3307", "127.0.0.1@3307", DataChangedEvent.Type.ADDED);
        Optional<GovernanceEvent> add = new GlobalAckChangedWatcher().createGovernanceEvent(addDataChangedEvent);
        assertTrue(add.isPresent());
        GovernanceEvent addEvent = add.get();
        assertTrue(addEvent instanceof AckLockedEvent);
        assertThat(((AckLockedEvent) addEvent).getLockName(), is("schema-127.0.0.1@3307"));
        DataChangedEvent deleteDataChangedEvent = new DataChangedEvent("/lock/global/ack/schema-127.0.0.1@3307", "127.0.0.1@3307", DataChangedEvent.Type.DELETED);
        Optional<GovernanceEvent> delete = new GlobalAckChangedWatcher().createGovernanceEvent(deleteDataChangedEvent);
        assertTrue(delete.isPresent());
        GovernanceEvent deleteEvent = delete.get();
        assertTrue(deleteEvent instanceof AckLockReleasedEvent);
        assertThat(((AckLockReleasedEvent) deleteEvent).getLockName(), is("schema-127.0.0.1@3307"));
    }
}
