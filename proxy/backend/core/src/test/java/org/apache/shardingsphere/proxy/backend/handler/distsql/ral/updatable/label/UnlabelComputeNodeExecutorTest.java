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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.label;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.UnlabelComputeNodeStatement;
import org.apache.shardingsphere.infra.instance.ClusterInstanceRegistry;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.persist.facade.ClusterPersistServiceFacade;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.ClusterComputeNodePersistService;
import org.apache.shardingsphere.mode.persist.PersistServiceFacade;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UnlabelComputeNodeExecutorTest {
    
    private final UnlabelComputeNodeExecutor executor = (UnlabelComputeNodeExecutor) TypedSPILoader.getService(DistSQLUpdateExecutor.class, UnlabelComputeNodeStatement.class);
    
    @Test
    void assertDoNothingWhenInstanceAbsent() {
        UnlabelComputeNodeStatement sqlStatement = new UnlabelComputeNodeStatement("instance-id", Collections.singletonList("label_a"));
        ContextManager contextManager = mock(ContextManager.class);
        ComputeNodeInstanceContext instanceContext = mock(ComputeNodeInstanceContext.class);
        ClusterInstanceRegistry clusterInstanceRegistry = mock(ClusterInstanceRegistry.class);
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(instanceContext);
        when(instanceContext.getClusterInstanceRegistry()).thenReturn(clusterInstanceRegistry);
        when(clusterInstanceRegistry.find("instance-id")).thenReturn(Optional.empty());
        PersistServiceFacade persistServiceFacade = mock(PersistServiceFacade.class);
        when(contextManager.getPersistServiceFacade()).thenReturn(persistServiceFacade);
        executor.executeUpdate(sqlStatement, contextManager);
        verifyNoInteractions(persistServiceFacade);
    }
    
    @Test
    void assertClearLabelsWhenStatementLabelsEmpty() {
        UnlabelComputeNodeStatement sqlStatement = new UnlabelComputeNodeStatement("instance-id", Collections.emptyList());
        ContextManager contextManager = mock(ContextManager.class);
        ComputeNodeInstance computeNodeInstance = mock(ComputeNodeInstance.class);
        ClusterComputeNodePersistService computeNodeService = mockContextManager(contextManager, computeNodeInstance);
        ComputeNodeInstanceContext instanceContext = contextManager.getComputeNodeInstanceContext();
        when(instanceContext.getClusterInstanceRegistry().find("instance-id")).thenReturn(Optional.of(computeNodeInstance));
        executor.executeUpdate(sqlStatement, contextManager);
        verify(computeNodeService).persistLabels("instance-id", Collections.emptyList());
    }
    
    @Test
    void assertRemoveSpecifiedLabels() {
        UnlabelComputeNodeStatement sqlStatement = new UnlabelComputeNodeStatement("instance-id", Collections.singletonList("label_b"));
        ContextManager contextManager = mock(ContextManager.class);
        ComputeNodeInstance computeNodeInstance = mock(ComputeNodeInstance.class);
        when(computeNodeInstance.getLabels()).thenReturn(Arrays.asList("label_a", "label_b"));
        ClusterComputeNodePersistService computeNodeService = mockContextManager(contextManager, computeNodeInstance);
        ComputeNodeInstanceContext instanceContext = contextManager.getComputeNodeInstanceContext();
        when(instanceContext.getClusterInstanceRegistry().find("instance-id")).thenReturn(Optional.of(computeNodeInstance));
        executor.executeUpdate(sqlStatement, contextManager);
        verify(computeNodeService).persistLabels("instance-id", Collections.singletonList("label_a"));
    }
    
    private ClusterComputeNodePersistService mockContextManager(final ContextManager contextManager, final ComputeNodeInstance computeNodeInstance) {
        ComputeNodeInstanceContext instanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(instanceContext.getClusterInstanceRegistry().find("instance-id")).thenReturn(Optional.of(computeNodeInstance));
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(instanceContext);
        PersistServiceFacade persistServiceFacade = mock(PersistServiceFacade.class);
        ClusterPersistServiceFacade clusterPersistServiceFacade = mock(ClusterPersistServiceFacade.class);
        when(persistServiceFacade.getModeFacade()).thenReturn(clusterPersistServiceFacade);
        when(contextManager.getPersistServiceFacade()).thenReturn(persistServiceFacade);
        ClusterComputeNodePersistService result = mock(ClusterComputeNodePersistService.class);
        when(clusterPersistServiceFacade.getComputeNodeService()).thenReturn(result);
        return result;
    }
}
