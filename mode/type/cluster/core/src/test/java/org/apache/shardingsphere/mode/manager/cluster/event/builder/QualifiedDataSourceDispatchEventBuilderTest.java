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

import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.state.storage.QualifiedDataSourceStateEvent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualifiedDataSourceDispatchEventBuilderTest {
    
    @Test
    void assertCreateEnabledQualifiedDataSourceChangedEvent() {
        Optional<DispatchEvent> actual = new QualifiedDataSourceDispatchEventBuilder().build(
                new DataChangedEvent("/nodes/qualified_data_sources/replica_query_db.readwrite_ds.replica_ds_0", "state: ENABLED\n", Type.ADDED));
        assertTrue(actual.isPresent());
        QualifiedDataSourceStateEvent actualEvent = (QualifiedDataSourceStateEvent) actual.get();
        assertThat(actualEvent.getQualifiedDataSource().getDatabaseName(), is("replica_query_db"));
        assertThat(actualEvent.getQualifiedDataSource().getGroupName(), is("readwrite_ds"));
        assertThat(actualEvent.getQualifiedDataSource().getDataSourceName(), is("replica_ds_0"));
        assertThat(actualEvent.getStatus().getState(), is(DataSourceState.ENABLED));
    }
    
    @Test
    void assertCreateDisabledQualifiedDataSourceChangedEvent() {
        Optional<DispatchEvent> actual = new QualifiedDataSourceDispatchEventBuilder().build(
                new DataChangedEvent("/nodes/qualified_data_sources/replica_query_db.readwrite_ds.replica_ds_0", "state: DISABLED\n", Type.DELETED));
        assertTrue(actual.isPresent());
        QualifiedDataSourceStateEvent actualEvent = (QualifiedDataSourceStateEvent) actual.get();
        assertThat(actualEvent.getQualifiedDataSource().getDatabaseName(), is("replica_query_db"));
        assertThat(actualEvent.getQualifiedDataSource().getGroupName(), is("readwrite_ds"));
        assertThat(actualEvent.getQualifiedDataSource().getDataSourceName(), is("replica_ds_0"));
        assertThat(actualEvent.getStatus().getState(), is(DataSourceState.DISABLED));
    }
    
    @Test
    void assertCreateEmptyEvent() {
        Optional<DispatchEvent> actual = new QualifiedDataSourceDispatchEventBuilder().build(
                new DataChangedEvent("/nodes/qualified_data_sources/replica_query_db.readwrite_ds.replica_ds_0", "", Type.ADDED));
        assertFalse(actual.isPresent());
    }
}
