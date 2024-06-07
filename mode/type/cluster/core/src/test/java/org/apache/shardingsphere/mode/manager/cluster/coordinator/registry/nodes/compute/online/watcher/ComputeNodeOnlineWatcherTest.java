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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.nodes.compute.online.watcher;

import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.nodes.compute.online.event.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.nodes.compute.online.event.InstanceOnlineEvent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComputeNodeOnlineWatcherTest {
    
    @Test
    void assertComputeNodeOnline() {
        Optional<GovernanceEvent> actual = new ComputeNodeOnlineWatcher()
                .createGovernanceEvent(new DataChangedEvent("/nodes/compute_nodes/online/proxy/foo_instance_id", "{attribute: 127.0.0.1@3307,version: 1}", Type.ADDED));
        assertTrue(actual.isPresent());
        InstanceOnlineEvent event = (InstanceOnlineEvent) actual.get();
        assertThat(event.getInstanceMetaData().getId(), is("foo_instance_id"));
        assertThat(event.getInstanceMetaData().getIp(), is("127.0.0.1"));
        assertThat(event.getInstanceMetaData().getType(), is(InstanceType.PROXY));
        assertThat(event.getInstanceMetaData().getVersion(), is("1"));
        assertThat(event.getInstanceMetaData().getAttributes(), is("127.0.0.1@3307"));
    }
    
    @Test
    void assertComputeNodeOffline() {
        Optional<GovernanceEvent> actual = new ComputeNodeOnlineWatcher()
                .createGovernanceEvent(new DataChangedEvent("/nodes/compute_nodes/online/proxy/foo_instance_id", "{attribute: 127.0.0.1@3307,version: 1}", Type.DELETED));
        assertTrue(actual.isPresent());
        InstanceOfflineEvent event = (InstanceOfflineEvent) actual.get();
        assertThat(event.getInstanceMetaData().getId(), is("foo_instance_id"));
        assertThat(event.getInstanceMetaData().getIp(), is("127.0.0.1"));
        assertThat(event.getInstanceMetaData().getType(), is(InstanceType.PROXY));
        assertThat(event.getInstanceMetaData().getVersion(), is("1"));
        assertThat(event.getInstanceMetaData().getAttributes(), is("127.0.0.1@3307"));
    }
}
