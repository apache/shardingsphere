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

package org.apache.shardingsphere.proxy.frontend.postgresql;

import lombok.SneakyThrows;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.executor.ConnectionThreadExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLPortalContextRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLFrontendEngineTest {
    
    @Test
    void assertRelease() {
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        int connectionId = 1;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        PostgreSQLPortalContextRegistry.getInstance().get(connectionId);
        PostgreSQLFrontendEngine frontendEngine = new PostgreSQLFrontendEngine();
        ConnectionThreadExecutorGroup.getInstance().register(connectionId);
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(connectionId);
        frontendEngine.release(connectionSession);
        assertTrue(getPortalContexts().isEmpty());
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private ConcurrentMap<Integer, PortalContext> getPortalContexts() {
        return (ConcurrentMap<Integer, PortalContext>) Plugins.getMemberAccessor()
                .get(PostgreSQLPortalContextRegistry.class.getDeclaredField("portalContexts"), PostgreSQLPortalContextRegistry.getInstance());
    }
}
