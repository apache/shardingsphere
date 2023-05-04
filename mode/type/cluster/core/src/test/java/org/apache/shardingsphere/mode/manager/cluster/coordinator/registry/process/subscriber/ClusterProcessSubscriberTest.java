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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.subscriber;

import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.process.event.ShowProcessListRequestEvent;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClusterProcessSubscriberTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    private final EventBusContext eventBusContext = new EventBusContext();
    
    private ClusterProcessSubscriber clusterProcessListSubscriber;
    
    @BeforeEach
    void setUp() {
        clusterProcessListSubscriber = new ClusterProcessSubscriber(repository, eventBusContext);
    }
    
    @Test
    void assertPostShowProcessListData() {
        when(repository.getChildrenKeys(ComputeNode.getOnlineNodePath(InstanceType.JDBC))).thenReturn(Collections.emptyList());
        when(repository.getChildrenKeys(ComputeNode.getOnlineNodePath(InstanceType.PROXY))).thenReturn(Collections.singletonList("abc"));
        when(repository.getDirectly(any())).thenReturn(null);
        clusterProcessListSubscriber.postShowProcessListData(new ShowProcessListRequestEvent());
        verify(repository).persist(any(), any());
    }
}
