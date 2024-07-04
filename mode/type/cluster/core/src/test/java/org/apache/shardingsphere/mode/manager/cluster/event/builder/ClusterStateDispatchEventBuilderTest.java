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

import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.cluster.ClusterStateEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterStateDispatchEventBuilderTest {
    
    @Test
    void assertCreateEventWhenReadOnly() {
        Optional<DispatchEvent> actual = new ClusterStateDispatchEventBuilder()
                .build(new DataChangedEvent("/nodes/compute_nodes/status", ClusterState.READ_ONLY.name(), Type.UPDATED));
        assertTrue(actual.isPresent());
        assertThat(((ClusterStateEvent) actual.get()).getClusterState(), is(ClusterState.READ_ONLY));
    }
    
    @Test
    void assertCreateEventWhenUnavailable() {
        Optional<DispatchEvent> actual = new ClusterStateDispatchEventBuilder()
                .build(new DataChangedEvent("/nodes/compute_nodes/status", ClusterState.UNAVAILABLE.name(), Type.UPDATED));
        assertTrue(actual.isPresent());
        assertThat(((ClusterStateEvent) actual.get()).getClusterState(), is(ClusterState.UNAVAILABLE));
    }
    
    @Test
    void assertCreateEventWhenEnabled() {
        Optional<DispatchEvent> actual = new ClusterStateDispatchEventBuilder()
                .build(new DataChangedEvent("/nodes/compute_nodes/status", ClusterState.OK.name(), Type.UPDATED));
        assertTrue(actual.isPresent());
        assertThat(((ClusterStateEvent) actual.get()).getClusterState(), is(ClusterState.OK));
    }
}
