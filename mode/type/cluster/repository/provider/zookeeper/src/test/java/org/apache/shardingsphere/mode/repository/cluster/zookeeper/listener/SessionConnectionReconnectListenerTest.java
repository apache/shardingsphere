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

package org.apache.shardingsphere.mode.repository.cluster.zookeeper.listener;

import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.ClusterComputeNodePersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionConnectionReconnectListenerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComputeNodeInstanceContext computeNodeInstanceContext;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ClusterPersistRepository repository;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CuratorFramework client;
    
    @Test
    void assertChangeToConnectedState() throws InterruptedException {
        new SessionConnectionReconnectListener(computeNodeInstanceContext, repository).stateChanged(client, ConnectionState.CONNECTED);
        verify(client.getZookeeperClient(), never()).blockUntilConnectedOrTimedOut();
    }
    
    @Test
    void assertChangeToLostStateWithGenerateWorkerId() throws InterruptedException {
        ClusterComputeNodePersistService computeNodePersistService = mock(ClusterComputeNodePersistService.class);
        when(client.getZookeeperClient().blockUntilConnectedOrTimedOut()).thenReturn(false, true);
        getSessionConnectionReconnectListener(computeNodePersistService).stateChanged(client, ConnectionState.LOST);
        verify(computeNodeInstanceContext).generateWorkerId(new Properties());
        verify(computeNodePersistService).registerOnline(any());
    }
    
    @Test
    void assertChangeToLostStateWithoutGenerateWorkerId() throws InterruptedException {
        ClusterComputeNodePersistService computeNodePersistService = mock(ClusterComputeNodePersistService.class);
        when(client.getZookeeperClient().blockUntilConnectedOrTimedOut()).thenReturn(true);
        when(computeNodeInstanceContext.getInstance().getWorkerId()).thenReturn(-1);
        getSessionConnectionReconnectListener(computeNodePersistService).stateChanged(client, ConnectionState.LOST);
        verify(computeNodeInstanceContext, never()).generateWorkerId(new Properties());
        verify(computeNodePersistService).registerOnline(any());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private SessionConnectionReconnectListener getSessionConnectionReconnectListener(final ClusterComputeNodePersistService computeNodePersistService) {
        SessionConnectionReconnectListener result = new SessionConnectionReconnectListener(computeNodeInstanceContext, repository);
        Plugins.getMemberAccessor().set(SessionConnectionReconnectListener.class.getDeclaredField("computeNodePersistService"), result, computeNodePersistService);
        return result;
    }
}
