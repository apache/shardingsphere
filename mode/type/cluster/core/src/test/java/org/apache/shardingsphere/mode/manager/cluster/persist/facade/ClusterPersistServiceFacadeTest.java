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

package org.apache.shardingsphere.mode.manager.cluster.persist.facade;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.ClusterComputeNodePersistService;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.ClusterMetaDataManagerPersistService;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.ClusterProcessPersistService;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClusterPersistServiceFacadeTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContextManager metaDataContextManager;
    
    @Test
    void assertConstructServices() {
        ClusterPersistServiceFacade actual = new ClusterPersistServiceFacade(metaDataContextManager, repository);
        assertThat(actual.getMetaDataManagerService(), isA(ClusterMetaDataManagerPersistService.class));
        assertThat(actual.getComputeNodeService(), isA(ClusterComputeNodePersistService.class));
        assertThat(actual.getProcessService(), isA(ClusterProcessPersistService.class));
    }
    
    @Test
    void assertCloseOfflineInstance() {
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        when(instanceMetaData.getId()).thenReturn("foo_instance");
        when(instanceMetaData.getType()).thenReturn(InstanceType.PROXY);
        when(metaDataContextManager.getComputeNodeInstanceContext().getInstance()).thenReturn(new ComputeNodeInstance(instanceMetaData));
        new ClusterPersistServiceFacade(metaDataContextManager, repository).close();
        verify(repository).delete("/nodes/compute_nodes/online/proxy/foo_instance");
    }
}
