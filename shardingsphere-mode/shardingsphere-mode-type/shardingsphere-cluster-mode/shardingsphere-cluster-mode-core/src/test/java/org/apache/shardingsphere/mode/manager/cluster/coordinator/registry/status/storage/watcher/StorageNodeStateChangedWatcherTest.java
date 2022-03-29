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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.watcher;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.PrimaryStateChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class StorageNodeStateChangedWatcherTest {
    
    @Test
    public void assertCreatePrimaryStateChangedEvent() {
        Optional<GovernanceEvent> actual = new StorageNodeStateChangedWatcher().createGovernanceEvent(
                new DataChangedEvent("/nodes/storage_nodes/attributes/replica_query_db.readwrite_ds.replica_ds_0", "role: primary\nstatus: enable\n", Type.ADDED));
        assertTrue(actual.isPresent());
        PrimaryStateChangedEvent actualEvent = (PrimaryStateChangedEvent) actual.get();
        assertThat(actualEvent.getQualifiedSchema().getSchemaName(), is("replica_query_db"));
        assertThat(actualEvent.getQualifiedSchema().getGroupName(), is("readwrite_ds"));
        assertThat(actualEvent.getQualifiedSchema().getDataSourceName(), is("replica_ds_0"));
    }
    
    @Test
    public void assertCreateEnabledStateChangedEvent() {
        Optional<GovernanceEvent> actual = new StorageNodeStateChangedWatcher().createGovernanceEvent(
                new DataChangedEvent("/nodes/storage_nodes/attributes/replica_query_db.readwrite_ds.replica_ds_0", "role: member\nstatus: enable\n", Type.ADDED));
        assertTrue(actual.isPresent());
        DisabledStateChangedEvent actualEvent = (DisabledStateChangedEvent) actual.get();
        assertThat(actualEvent.getQualifiedSchema().getSchemaName(), is("replica_query_db"));
        assertThat(actualEvent.getQualifiedSchema().getGroupName(), is("readwrite_ds"));
        assertThat(actualEvent.getQualifiedSchema().getDataSourceName(), is("replica_ds_0"));
        assertFalse(actualEvent.isDisabled());
    }
    
    @Test
    public void assertCreateDisabledStateChangedEvent() {
        Optional<GovernanceEvent> actual = new StorageNodeStateChangedWatcher().createGovernanceEvent(
                new DataChangedEvent("/nodes/storage_nodes/attributes/replica_query_db.readwrite_ds.replica_ds_0", "role: member\nstatus: disable\n", Type.DELETED));
        assertTrue(actual.isPresent());
        DisabledStateChangedEvent actualEvent = (DisabledStateChangedEvent) actual.get();
        assertThat(actualEvent.getQualifiedSchema().getSchemaName(), is("replica_query_db"));
        assertThat(actualEvent.getQualifiedSchema().getGroupName(), is("readwrite_ds"));
        assertThat(actualEvent.getQualifiedSchema().getDataSourceName(), is("replica_ds_0"));
        assertTrue(actualEvent.isDisabled());
    }
    
    @Test
    public void assertCreateEmptyEvent() {
        Optional<GovernanceEvent> actual = new StorageNodeStateChangedWatcher().createGovernanceEvent(
                new DataChangedEvent("/nodes/storage_nodes/attributes/replica_query_db.readwrite_ds.replica_ds_0", "", Type.ADDED));
        assertFalse(actual.isPresent());
    }
}
