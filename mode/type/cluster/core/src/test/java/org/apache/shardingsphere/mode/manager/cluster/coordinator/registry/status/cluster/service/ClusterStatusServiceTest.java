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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.cluster.service;

import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.infra.state.cluster.ClusterStateContext;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClusterStatusServiceTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    @Test
    void assertPersistClusterState() {
        ClusterStatusService clusterStatusService = new ClusterStatusService(repository);
        clusterStatusService.persistClusterState(new ClusterStateContext());
        verify(repository).persist(ComputeNode.getClusterStatusNodePath(), ClusterState.OK.name());
    }
    
    @Test
    void assertLoadClusterStatus() {
        new ClusterStatusService(repository).loadClusterStatus();
        verify(repository).getDirectly(ComputeNode.getClusterStatusNodePath());
    }
}
