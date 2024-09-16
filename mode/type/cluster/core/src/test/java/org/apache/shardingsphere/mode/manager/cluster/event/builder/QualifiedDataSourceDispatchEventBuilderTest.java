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
    
    private final QualifiedDataSourceDispatchEventBuilder builder = new QualifiedDataSourceDispatchEventBuilder();
    
    @Test
    void assertGetSubscribedKey() {
        assertThat(builder.getSubscribedKey(), is("/nodes/qualified_data_sources"));
    }
    
    @Test
    void assertBuildQualifiedDataSourceStateEvent() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/nodes/qualified_data_sources/foo_db.foo_group.foo_ds", "state: ENABLED", Type.ADDED));
        assertTrue(actual.isPresent());
        assertThat(((QualifiedDataSourceStateEvent) actual.get()).getQualifiedDataSource().getDatabaseName(), is("foo_db"));
        assertThat(((QualifiedDataSourceStateEvent) actual.get()).getQualifiedDataSource().getGroupName(), is("foo_group"));
        assertThat(((QualifiedDataSourceStateEvent) actual.get()).getQualifiedDataSource().getDataSourceName(), is("foo_ds"));
        assertThat(((QualifiedDataSourceStateEvent) actual.get()).getStatus().getState(), is(DataSourceState.ENABLED));
    }
    
    @Test
    void assertBuildWithEmptyValue() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/nodes/qualified_data_sources/foo_db.foo_group.foo_ds", "", Type.ADDED));
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertBuildWithoutQualifiedDataSource() {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent("/nodes/qualified_data_sources", "state: DISABLED", Type.ADDED));
        assertFalse(actual.isPresent());
    }
}
