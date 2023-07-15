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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.watcher;

import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.event.schema.TableMetaDataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetaDataChangedWatcherTest {
    
    @Test
    void assertCreateEventWithInvalidPath() {
        String key = "/metadata_invalid/sharding_db/sharding_schema";
        String value = "encrypt_db";
        Optional<GovernanceEvent> actual = createEvent(key, value, Type.UPDATED);
        assertFalse(actual.isPresent());
        actual = createEvent(key, value, Type.ADDED);
        assertFalse(actual.isPresent());
        actual = createEvent(key, value, Type.DELETED);
        assertFalse(actual.isPresent());
        actual = createEvent(key, value, Type.IGNORED);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertCreateAddedEvent() {
        String key = "/metadata/sharding_db";
        String value = "encrypt_db";
        Optional<GovernanceEvent> actual = createEvent(key, value, Type.UPDATED);
        assertTrue(actual.isPresent());
        actual = createEvent(key, value, Type.ADDED);
        assertTrue(actual.isPresent());
        actual = createEvent(key, value, Type.IGNORED);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertEmptyValue() {
        String key = "/metadata/sharding_db/data_sources";
        Optional<GovernanceEvent> actual = createEvent(key, null, Type.UPDATED);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertCreateDatabaseDeletedEvent() {
        String key = "/metadata/sharding_db";
        String value = "encrypt_db";
        Optional<GovernanceEvent> actual = createEvent(key, value, Type.DELETED);
        assertTrue(actual.isPresent());
    }
    
    @Test
    void assertCreateDataSourceChangedEvent() {
        String key = "/metadata/sharding_db/versions/0/data_sources/units";
        String value = "{}";
        Optional<GovernanceEvent> actual = createEvent(key, value, Type.UPDATED);
        assertTrue(actual.isPresent());
    }
    
    @Test
    void assertCreateDataSourceChangedEventWithAddEvent() {
        String key = "/metadata/sharding_db/versions/0/data_sources/units";
        String value = "{}";
        Optional<GovernanceEvent> actual = createEvent(key, value, Type.ADDED);
        assertTrue(actual.isPresent());
    }
    
    @Test
    void assertCreateRuleChangedEvent() {
        String key = "/metadata/sharding_db/versions/0/rules";
        Optional<GovernanceEvent> actual = createEvent(key, "[]", Type.UPDATED);
        assertTrue(actual.isPresent());
    }
    
    @Test
    void assertCreateRuleChangedEventWithAddEvent() {
        String key = "/metadata/sharding_db/versions/0/rules";
        Optional<GovernanceEvent> actual = createEvent(key, "[]", Type.ADDED);
        assertTrue(actual.isPresent());
    }
    
    @Test
    void assertCreateTableSchemaChangedEvent() {
        String key = "/metadata/sharding_db/schemas/sharding_schema/tables/t_order";
        Optional<GovernanceEvent> actual = createEvent(key, "{}", Type.UPDATED);
        assertTrue(actual.isPresent());
    }
    
    @Test
    void assertCreateTableSchemaDeletedEvent() {
        String key = "/metadata/sharding_db/schemas/sharding_schema/tables/t_order";
        Optional<GovernanceEvent> actual = createEvent(key, "{}", Type.DELETED);
        assertTrue(actual.isPresent());
        assertThat(((TableMetaDataChangedEvent) actual.get()).getDeletedTable(), is("t_order"));
    }
    
    @Test
    void assertCreateViewMetaDataChangedEvent() {
        String key = "/metadata/sharding_db/schemas/sharding_schema/views/foo_view";
        Optional<GovernanceEvent> actual = createEvent(key, "{}", Type.UPDATED);
        assertTrue(actual.isPresent());
    }
    
    @Test
    void assertCreateViewMetaDataDeletedEvent() {
        String key = "/metadata/sharding_db/schemas/sharding_schema/views/foo_view";
        Optional<GovernanceEvent> actual = createEvent(key, "{}", Type.DELETED);
        assertTrue(actual.isPresent());
    }
    
    private Optional<GovernanceEvent> createEvent(final String key, final String value, final Type type) {
        DataChangedEvent dataChangedEvent = new DataChangedEvent(key, value, type);
        return new MetaDataChangedWatcher().createGovernanceEvent(dataChangedEvent);
    }
}
