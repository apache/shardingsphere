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

package org.apache.shardingsphere.mode.state;

import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class StateServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    void assertPersistClusterStateWithoutPath() {
        StateService stateService = new StateService(repository);
        stateService.persist(ClusterState.OK);
        verify(repository).persist(ComputeNode.getClusterStateNodePath(), ClusterState.OK.name());
    }
    
    @Test
    void assertPersistClusterStateWithPath() {
        StateService stateService = new StateService(repository);
        when(repository.getDirectly("/nodes/compute_nodes/status")).thenReturn(ClusterState.OK.name());
        stateService.persist(ClusterState.OK);
        verify(repository, times(0)).persist(ComputeNode.getClusterStateNodePath(), ClusterState.OK.name());
    }
    
    @Test
    void assertLoadClusterState() {
        new StateService(repository).load();
        verify(repository).getDirectly(ComputeNode.getClusterStateNodePath());
    }
}
