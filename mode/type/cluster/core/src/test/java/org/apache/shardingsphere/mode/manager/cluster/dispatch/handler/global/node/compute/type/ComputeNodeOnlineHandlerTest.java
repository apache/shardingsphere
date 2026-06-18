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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.node.compute.type;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.mode.manager.cluster.persist.facade.ClusterPersistServiceFacade;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComputeNodeOnlineHandlerTest {
    
    private GlobalDataChangedEventHandler handler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ClusterPersistServiceFacade clusterPersistServiceFacade;
    
    @BeforeEach
    void setUp() {
        handler = ShardingSphereServiceLoader.getServiceInstances(GlobalDataChangedEventHandler.class).stream()
                .filter(each -> "/nodes/compute_nodes/online".equals(NodePathGenerator.toPath(each.getSubscribedNodePath()))).findFirst().orElse(null);
        when(contextManager.getPersistServiceFacade().getModeFacade()).thenReturn(clusterPersistServiceFacade);
    }
    
    @Test
    void assertHandleWithInvalidInstanceOnlinePath() {
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/online/foo", "{attribute: 127.0.0.1@3307,version: 1}", Type.ADDED));
        verify(contextManager.getComputeNodeInstanceContext(), never()).getClusterInstanceRegistry();
    }
    
    @Test
    void assertHandleWithInstanceOnlineEvent() {
        ComputeNodeInstance computeNodeInstance = mock(ComputeNodeInstance.class);
        when(clusterPersistServiceFacade.getComputeNodeService().loadInstance(any())).thenReturn(computeNodeInstance);
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/online/proxy/foo_instance_id", "{attribute: 127.0.0.1@3307,version: 1}", Type.ADDED));
        verify(contextManager.getComputeNodeInstanceContext().getClusterInstanceRegistry()).add(computeNodeInstance);
    }
    
    @Test
    void assertHandleWithInstanceOfflineEvent() {
        handler.handle(contextManager, new DataChangedEvent("/nodes/compute_nodes/online/proxy/foo_instance_id", "{attribute: 127.0.0.1@3307,version: 1}", Type.DELETED));
        verify(contextManager.getComputeNodeInstanceContext().getClusterInstanceRegistry()).delete(any());
    }
}
