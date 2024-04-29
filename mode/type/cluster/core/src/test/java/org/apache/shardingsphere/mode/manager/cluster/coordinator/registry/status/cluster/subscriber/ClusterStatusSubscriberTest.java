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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.cluster.subscriber;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.cluster.event.ClusterStatusChangedEvent;
import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClusterStatusSubscriberTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    @Test
    void assertUpdate() {
        ClusterStatusSubscriber clusterStatusSubscriber = new ClusterStatusSubscriber(repository, new EventBusContext());
        ClusterStatusChangedEvent event = new ClusterStatusChangedEvent(ClusterState.OK);
        clusterStatusSubscriber.update(event);
        verify(repository).persist(ComputeNode.getClusterStatusNodePath(), ClusterState.OK.name());
    }
}
