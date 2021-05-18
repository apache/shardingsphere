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
import org.apache.shardingsphere.governance.core.registry.listener.event.schema.SchemaChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaChangedListenerTest extends GovernanceListenerTest {
    
    private static final String META_DATA_FILE = "yaml/schema.yaml";
    
    private SchemaChangedListener schemaChangedListener;
    
    @Before
    public void setUp() {
        schemaChangedListener = new SchemaChangedListener(getRegistryCenterRepository(), Arrays.asList("sharding_db", "readwrite_splitting_db", "encrypt_db"));
    }
    
    @Test
    public void assertCreateEvent() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/sharding_db/schema", readYAML(META_DATA_FILE), Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(SchemaChangedEvent.class));
        assertTrue(((SchemaChangedEvent) actual.get()).getSchema().getAllTableNames().contains("t_order"));
    }
}
