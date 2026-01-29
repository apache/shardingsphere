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

package org.apache.shardingsphere.distsql.handler.ral.updatable.label;

import org.apache.shardingsphere.distsql.statement.type.ral.updatable.UnlabelComputeNodeStatement;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.persist.facade.ClusterPersistServiceFacade;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UnlabelComputeNodeExecutorTest {
    
    private final UnlabelComputeNodeExecutor executor = new UnlabelComputeNodeExecutor();
    
    @Test
    void assertExecuteWithEmptyLabels() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ComputeNodeInstanceContext instanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(instanceContext);
        ComputeNodeInstance computeNodeInstance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo", "127.0.0.1@3308", "version"));
        computeNodeInstance.getLabels().add("existing");
        when(instanceContext.getClusterInstanceRegistry().find("foo")).thenReturn(Optional.of(computeNodeInstance));
        ClusterPersistServiceFacade clusterPersistServiceFacade = mock(ClusterPersistServiceFacade.class, RETURNS_DEEP_STUBS);
        when(contextManager.getPersistServiceFacade().getModeFacade()).thenReturn(clusterPersistServiceFacade);
        executor.executeUpdate(new UnlabelComputeNodeStatement("foo", Collections.emptyList()), contextManager);
        verify(clusterPersistServiceFacade.getComputeNodeService()).persistLabels("foo", Collections.emptyList());
    }
    
    @Test
    void assertExecuteWithLabels() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ComputeNodeInstanceContext instanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(contextManager.getComputeNodeInstanceContext()).thenReturn(instanceContext);
        ComputeNodeInstance computeNodeInstance = new ComputeNodeInstance(new ProxyInstanceMetaData("foo", "127.0.0.1@3308", "version"));
        computeNodeInstance.getLabels().addAll(Arrays.asList("foo", "bar"));
        when(instanceContext.getClusterInstanceRegistry().find("foo")).thenReturn(Optional.of(computeNodeInstance));
        ClusterPersistServiceFacade clusterPersistServiceFacade = mock(ClusterPersistServiceFacade.class, RETURNS_DEEP_STUBS);
        when(contextManager.getPersistServiceFacade().getModeFacade()).thenReturn(clusterPersistServiceFacade);
        executor.executeUpdate(new UnlabelComputeNodeStatement("foo", Collections.singleton("bar")), contextManager);
        verify(clusterPersistServiceFacade.getComputeNodeService()).persistLabels("foo", Collections.singletonList("foo"));
    }
}
