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

package org.apache.shardingsphere.distsql.handler.executor.lock.impl;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.lock.spi.ClusterLockStrategy;
import org.apache.shardingsphere.mode.state.ShardingSphereState;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ClusterWriteLockStrategyTest {
    
    private final ClusterLockStrategy clusterLockStrategy = TypedSPILoader.getService(ClusterLockStrategy.class, "WRITE");
    
    @Test
    void assertLock() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        clusterLockStrategy.lock(contextManager);
        verify(contextManager.getPersistServiceFacade().getStateService()).update(ShardingSphereState.READ_ONLY);
    }
    
    @Test
    void assertLockWithDifferentContextManagers() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ContextManager anotherContextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        clusterLockStrategy.lock(contextManager);
        clusterLockStrategy.lock(anotherContextManager);
        verify(contextManager.getPersistServiceFacade().getStateService()).update(ShardingSphereState.READ_ONLY);
        verify(anotherContextManager.getPersistServiceFacade().getStateService()).update(ShardingSphereState.READ_ONLY);
    }
}
