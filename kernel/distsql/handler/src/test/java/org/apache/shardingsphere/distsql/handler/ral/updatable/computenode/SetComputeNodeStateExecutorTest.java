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

package org.apache.shardingsphere.distsql.handler.ral.updatable.computenode;

import org.apache.shardingsphere.distsql.statement.type.ral.updatable.SetComputeNodeStateStatement;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.persist.facade.ClusterPersistServiceFacade;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SetComputeNodeStateExecutorTest {
    
    private final SetComputeNodeStateExecutor executor = new SetComputeNodeStateExecutor();
    
    @Test
    void assertExecuteUpdateWithAlreadyDisableInstance() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ComputeNodeInstanceContext instanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(instanceContext);
        ComputeNodeInstance currentInstance = new ComputeNodeInstance(new ProxyInstanceMetaData("current", "127.0.0.1@3307", "version"));
        when(instanceContext.getInstance()).thenReturn(currentInstance);
        ComputeNodeInstance targetInstance = new ComputeNodeInstance(new ProxyInstanceMetaData("instanceID", "127.0.0.1@3308", "version"));
        when(instanceContext.getClusterInstanceRegistry().find("instanceID")).thenReturn(Optional.of(targetInstance));
        ClusterPersistServiceFacade clusterPersistServiceFacade = mock(ClusterPersistServiceFacade.class, RETURNS_DEEP_STUBS);
        when(contextManager.getPersistServiceFacade().getModeFacade()).thenReturn(clusterPersistServiceFacade);
        executor.executeUpdate(new SetComputeNodeStateStatement("DISABLE", "instanceID"), contextManager);
        verify(clusterPersistServiceFacade.getComputeNodeService()).updateState("instanceID", InstanceState.CIRCUIT_BREAK);
    }
    
    @Test
    void assertDisableComputeNodeWithCurrentInstance() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ComputeNodeInstanceContext instanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(instanceContext);
        ComputeNodeInstance currentInstance = new ComputeNodeInstance(new ProxyInstanceMetaData("current", "127.0.0.1@3307", "version"));
        when(instanceContext.getInstance()).thenReturn(currentInstance);
        assertThrows(UnsupportedSQLOperationException.class, () -> executor.executeUpdate(new SetComputeNodeStateStatement("DISABLE", "current"), contextManager));
    }
    
    @Test
    void assertExecuteUpdateWithEnableInstance() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ComputeNodeInstanceContext instanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(instanceContext);
        ComputeNodeInstance targetInstance = new ComputeNodeInstance(new ProxyInstanceMetaData("instanceID", "127.0.0.1@3308", "version"));
        when(instanceContext.getClusterInstanceRegistry().find("instanceID")).thenReturn(Optional.of(targetInstance));
        ClusterPersistServiceFacade clusterPersistServiceFacade = mock(ClusterPersistServiceFacade.class, RETURNS_DEEP_STUBS);
        when(contextManager.getPersistServiceFacade().getModeFacade()).thenReturn(clusterPersistServiceFacade);
        executor.executeUpdate(new SetComputeNodeStateStatement("ENABLE", "instanceID"), contextManager);
        verify(clusterPersistServiceFacade.getComputeNodeService()).updateState("instanceID", InstanceState.OK);
    }
    
    @Test
    void assertEnableComputeNodeWithoutInstance() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ComputeNodeInstanceContext instanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(instanceContext);
        when(instanceContext.getClusterInstanceRegistry().find("missing")).thenReturn(Optional.empty());
        assertThrows(UnsupportedSQLOperationException.class, () -> executor.executeUpdate(new SetComputeNodeStateStatement("ENABLE", "missing"), contextManager));
    }
}
