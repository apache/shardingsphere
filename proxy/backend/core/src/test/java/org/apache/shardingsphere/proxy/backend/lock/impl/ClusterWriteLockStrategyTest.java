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

package org.apache.shardingsphere.proxy.backend.lock.impl;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.state.ShardingSphereState;
import org.apache.shardingsphere.mode.state.StatePersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.lock.spi.ClusterLockStrategy;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ClusterWriteLockStrategyTest {
    
    private final ClusterLockStrategy clusterLockStrategy = TypedSPILoader.getService(ClusterLockStrategy.class, "WRITE");
    
    @Test
    void assertLock() {
        StatePersistService stateService = mock(StatePersistService.class);
        ProxyContext proxyContext = mock(ProxyContext.class, RETURNS_DEEP_STUBS);
        when(proxyContext.getContextManager().getPersistServiceFacade().getStateService()).thenReturn(stateService);
        when(ProxyContext.getInstance()).thenReturn(proxyContext);
        clusterLockStrategy.lock();
        verify(stateService).update(ShardingSphereState.READ_ONLY);
    }
}
