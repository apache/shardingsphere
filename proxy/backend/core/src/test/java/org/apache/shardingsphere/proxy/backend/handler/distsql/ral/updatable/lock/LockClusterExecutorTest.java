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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.lock;

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.LockClusterStatement;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.lock.exception.LockedClusterException;
import org.apache.shardingsphere.mode.state.ShardingSphereState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LockClusterExecutorTest {
    
    private final LockClusterExecutor executor = new LockClusterExecutor();
    
    @SuppressWarnings("resource")
    @Test
    void assertExecuteUpdateWithLockedCluster() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getStateContext().getState()).thenReturn(ShardingSphereState.UNAVAILABLE);
        assertThrows(LockedClusterException.class, () -> executor.executeUpdate(new LockClusterStatement(new AlgorithmSegment("FOO", new Properties()), null), contextManager));
    }
    
    @SuppressWarnings("resource")
    @Test
    void assertExecuteUpdateWithWrongAlgorithm() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getStateContext().getState()).thenReturn(ShardingSphereState.OK);
        assertThrows(ServiceProviderNotFoundException.class, () -> executor.executeUpdate(new LockClusterStatement(new AlgorithmSegment("FOO", new Properties()), null), contextManager));
    }
    
    @SuppressWarnings("resource")
    @Test
    void assertExecuteUpdateWithUsingTimeout() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getStateContext().getState()).thenReturn(ShardingSphereState.OK);
        assertDoesNotThrow(() -> executor.executeUpdate(new LockClusterStatement(new AlgorithmSegment("WRITE", new Properties()), 2000L), contextManager));
    }
}
