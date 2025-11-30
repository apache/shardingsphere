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

import org.apache.shardingsphere.distsql.statement.type.ral.updatable.UnlockClusterStatement;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.lock.exception.NotLockedClusterException;
import org.apache.shardingsphere.mode.state.ShardingSphereState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnlockClusterExecutorTest {
    
    private final UnlockClusterExecutor executor = new UnlockClusterExecutor();
    
    @Test
    void assertExecuteUpdateWithNotLockedCluster() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getStateContext().getState()).thenReturn(ShardingSphereState.OK);
        assertThrows(NotLockedClusterException.class, () -> executor.executeUpdate(new UnlockClusterStatement(null), contextManager));
    }
    
    @Test
    void assertExecuteUpdateWithUsingTimeout() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getStateContext().getState()).thenReturn(ShardingSphereState.UNAVAILABLE);
        assertDoesNotThrow(() -> executor.executeUpdate(new UnlockClusterStatement(2000L), contextManager));
    }
}
