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

package org.apache.shardingsphere.mode.manager.cluster.event.builder;

import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.mode.event.dispatch.assisted.CreateDatabaseListenerAssistedEvent;
import org.apache.shardingsphere.mode.event.dispatch.assisted.DropDatabaseListenerAssistedEvent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListenerAssistedDispatchEventBuilderTest {
    
    private final ListenerAssistedDispatchEventBuilder builder = new ListenerAssistedDispatchEventBuilder();
    
    @Test
    void assertGetSubscribedKey() {
        assertThat(builder.getSubscribedKey(), is("/states/listener_assisted"));
    }
    
    @Test
    void assertBuildCreateDatabaseListenerAssistedEvent() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/states/listener_assisted/foo_db", "{databaseName: foo_db, listenerAssistedType: CREATE_DATABASE}", Type.ADDED));
        assertTrue(actual.isPresent());
        assertThat(((CreateDatabaseListenerAssistedEvent) actual.get()).getDatabaseName(), is("foo_db"));
    }
    
    @Test
    void assertBuildDropDatabaseListenerAssistedEvent() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/states/listener_assisted/foo_db", "{databaseName: foo_db, listenerAssistedType: DROP_DATABASE}", Type.ADDED));
        assertTrue(actual.isPresent());
        assertThat(((DropDatabaseListenerAssistedEvent) actual.get()).getDatabaseName(), is("foo_db"));
    }
    
    @Test
    void assertBuildWithoutDatabase() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/states/listener_assisted", "", Type.ADDED));
        assertFalse(actual.isPresent());
    }
}
