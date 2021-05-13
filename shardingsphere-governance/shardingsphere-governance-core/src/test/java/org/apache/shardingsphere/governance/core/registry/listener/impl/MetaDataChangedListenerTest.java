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

package org.apache.shardingsphere.governance.core.registry.listener.impl;

import org.apache.shardingsphere.governance.core.registry.listener.event.GovernanceEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.metadata.MetaDataDeletedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.metadata.MetaDataPersistedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class MetaDataChangedListenerTest extends GovernanceListenerTest {
    
    private MetaDataChangedListener metaDataChangedListener;
    
    @Before
    public void setUp() {
        metaDataChangedListener = new MetaDataChangedListener(getRegistryCenterRepository(), Arrays.asList("sharding_db", "readwrite_splitting_db"));
    }
    
    @Test
    public void assertCreateEventWithInvalidPath() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/sharding_db", "encrypt_db", Type.UPDATED);
        Optional<GovernanceEvent> actual = metaDataChangedListener.createEvent(dataChangedEvent);
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertCreateAddedEvent() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata", "sharding_db,readwrite_splitting_db,encrypt_db", Type.UPDATED);
        Optional<GovernanceEvent> actual = metaDataChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MetaDataPersistedEvent.class));
        assertThat(((MetaDataPersistedEvent) actual.get()).getSchemaName(), is("encrypt_db"));
    }
    
    @Test
    public void assertCreateDeletedEvent() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata", "sharding_db", Type.UPDATED);
        Optional<GovernanceEvent> actual = metaDataChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MetaDataDeletedEvent.class));
        assertThat(((MetaDataDeletedEvent) actual.get()).getSchemaName(), is("readwrite_splitting_db"));
    }
}
