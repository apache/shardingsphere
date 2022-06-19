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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.watcher;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.event.DistributedAckLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.event.DistributedAckLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.event.DistributedLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.event.DistributedLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DistributedLockChangedWatcherTest {
    
    private final DistributedLockChangedWatcher watcher = new DistributedLockChangedWatcher();
    
    @Test
    public void assertGetWatchingKeys() {
        Collection<String> keys = watcher.getWatchingKeys();
        assertThat(keys.size(), is(1));
        assertThat("/lock/distributed/locks", is(keys.iterator().next()));
    }
    
    @Test
    public void assertGetWatchingTypes() {
        Collection<DataChangedEvent.Type> types = watcher.getWatchingTypes();
        assertThat(types.size(), is(2));
        Iterator<DataChangedEvent.Type> iterator = types.iterator();
        assertThat(iterator.next(), is(DataChangedEvent.Type.ADDED));
        assertThat(iterator.next(), is(DataChangedEvent.Type.DELETED));
    }
    
    @Test
    public void assertLocksCreateGovernanceEvent() {
        String eventKey = "/lock/distributed/locks/lockName/leases/c_l_0000000";
        DataChangedEvent addDataChangedEvent = new DataChangedEvent(eventKey, "127.0.0.1@3307", DataChangedEvent.Type.ADDED);
        Optional<GovernanceEvent> addGovernanceEvent = watcher.createGovernanceEvent(addDataChangedEvent);
        assertTrue(addGovernanceEvent.isPresent());
        assertThat(addGovernanceEvent.get(), instanceOf(DistributedLockedEvent.class));
        assertThat(((DistributedLockedEvent) addGovernanceEvent.get()).getLockedName(), is("lockName"));
        DataChangedEvent deleteDataChangedEvent = new DataChangedEvent(eventKey, "127.0.0.1@3307", DataChangedEvent.Type.DELETED);
        Optional<GovernanceEvent> deleteGovernanceEvent = watcher.createGovernanceEvent(deleteDataChangedEvent);
        assertTrue(deleteGovernanceEvent.isPresent());
        assertThat(deleteGovernanceEvent.get(), instanceOf(DistributedLockReleasedEvent.class));
        assertThat(((DistributedLockReleasedEvent) deleteGovernanceEvent.get()).getLockedName(), is("lockName"));
        DataChangedEvent updateDataChangedEvent = new DataChangedEvent(eventKey, "127.0.0.1@3307", DataChangedEvent.Type.UPDATED);
        Optional<GovernanceEvent> updateGovernanceEvent = watcher.createGovernanceEvent(updateDataChangedEvent);
        assertFalse(updateGovernanceEvent.isPresent());
        DataChangedEvent ignoredDataChangedEvent = new DataChangedEvent(eventKey, "127.0.0.1@3307", DataChangedEvent.Type.IGNORED);
        Optional<GovernanceEvent> ignoredGovernanceEvent = watcher.createGovernanceEvent(ignoredDataChangedEvent);
        assertFalse(ignoredGovernanceEvent.isPresent());
    }
    
    @Test
    public void assertLocksAckCreateGovernanceEvent() {
        String eventKey = "/lock/distributed/locks/lockName/ack/127.0.0.1@3307";
        DataChangedEvent addDataChangedEvent = new DataChangedEvent(eventKey, "127.0.0.1@3307", DataChangedEvent.Type.ADDED);
        Optional<GovernanceEvent> addGovernanceEvent = watcher.createGovernanceEvent(addDataChangedEvent);
        assertTrue(addGovernanceEvent.isPresent());
        assertThat(addGovernanceEvent.get(), instanceOf(DistributedAckLockedEvent.class));
        assertThat(((DistributedAckLockedEvent) addGovernanceEvent.get()).getLockName(), is("lockName"));
        assertThat(((DistributedAckLockedEvent) addGovernanceEvent.get()).getLockedInstance(), is("127.0.0.1@3307"));
        DataChangedEvent deleteDataChangedEvent = new DataChangedEvent(eventKey, "127.0.0.1@3307", DataChangedEvent.Type.DELETED);
        Optional<GovernanceEvent> deleteGovernanceEvent = watcher.createGovernanceEvent(deleteDataChangedEvent);
        assertTrue(deleteGovernanceEvent.isPresent());
        assertThat(deleteGovernanceEvent.get(), instanceOf(DistributedAckLockReleasedEvent.class));
        assertThat(((DistributedAckLockReleasedEvent) deleteGovernanceEvent.get()).getLockName(), is("lockName"));
        assertThat(((DistributedAckLockReleasedEvent) deleteGovernanceEvent.get()).getLockedInstance(), is("127.0.0.1@3307"));
        DataChangedEvent updateDataChangedEvent = new DataChangedEvent(eventKey, "127.0.0.1@3307", DataChangedEvent.Type.UPDATED);
        Optional<GovernanceEvent> updateGovernanceEvent = watcher.createGovernanceEvent(updateDataChangedEvent);
        assertFalse(updateGovernanceEvent.isPresent());
        DataChangedEvent ignoredDataChangedEvent = new DataChangedEvent(eventKey, "127.0.0.1@3307", DataChangedEvent.Type.IGNORED);
        Optional<GovernanceEvent> ignoredGovernanceEvent = watcher.createGovernanceEvent(ignoredDataChangedEvent);
        assertFalse(ignoredGovernanceEvent.isPresent());
    }
}
