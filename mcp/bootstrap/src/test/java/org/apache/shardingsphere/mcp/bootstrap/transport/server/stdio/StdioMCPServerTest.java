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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.stdio;

import io.modelcontextprotocol.server.McpSyncServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPSyncServerFactory;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StdioMCPServerTest {
    
    @Test
    void assertStart() {
        MCPSyncServerFactory syncServerFactory = mock(MCPSyncServerFactory.class);
        SessionManagedStdioTransportProvider transportProvider = mock(SessionManagedStdioTransportProvider.class);
        McpSyncServer syncServer = mock(McpSyncServer.class);
        when(syncServerFactory.create(transportProvider)).thenReturn(syncServer);
        createServer(syncServerFactory, transportProvider).start();
        verify(syncServerFactory).create(transportProvider);
    }
    
    @Test
    void assertStartOnce() {
        MCPSyncServerFactory syncServerFactory = mock(MCPSyncServerFactory.class);
        SessionManagedStdioTransportProvider transportProvider = mock(SessionManagedStdioTransportProvider.class);
        McpSyncServer syncServer = mock(McpSyncServer.class);
        when(syncServerFactory.create(transportProvider)).thenReturn(syncServer);
        StdioMCPServer actual = createServer(syncServerFactory, transportProvider);
        actual.start();
        actual.start();
        verify(syncServerFactory).create(transportProvider);
    }
    
    @Test
    void assertStop() {
        MCPSyncServerFactory syncServerFactory = mock(MCPSyncServerFactory.class);
        SessionManagedStdioTransportProvider transportProvider = mock(SessionManagedStdioTransportProvider.class);
        McpSyncServer syncServer = mock(McpSyncServer.class);
        when(syncServerFactory.create(transportProvider)).thenReturn(syncServer);
        StdioMCPServer actual = createServer(syncServerFactory, transportProvider);
        actual.start();
        actual.stop();
        verify(syncServer).closeGracefully();
    }
    
    @Test
    void assertStartAfterStop() {
        MCPSyncServerFactory syncServerFactory = mock(MCPSyncServerFactory.class);
        SessionManagedStdioTransportProvider transportProvider = mock(SessionManagedStdioTransportProvider.class);
        McpSyncServer firstSyncServer = mock(McpSyncServer.class);
        McpSyncServer secondSyncServer = mock(McpSyncServer.class);
        when(syncServerFactory.create(transportProvider)).thenReturn(firstSyncServer, secondSyncServer);
        StdioMCPServer actual = createServer(syncServerFactory, transportProvider);
        actual.start();
        actual.stop();
        actual.start();
        verify(syncServerFactory, times(2)).create(transportProvider);
        verify(firstSyncServer).closeGracefully();
    }
    
    @Test
    void assertStopWithoutStart() {
        assertDoesNotThrow(() -> new StdioMCPServer(createRuntimeContext()).stop());
    }
    
    private StdioMCPServer createServer(final MCPSyncServerFactory syncServerFactory, final SessionManagedStdioTransportProvider transportProvider) {
        StdioMCPServer result = new StdioMCPServer(createRuntimeContext());
        try {
            setField(result, "syncServerFactory", syncServerFactory);
            setField(result, "transportProvider", transportProvider);
            return result;
        } catch (final ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
    
    private MCPRuntimeContext createRuntimeContext() {
        return new MCPRuntimeContext(new MCPSessionManager(Collections.emptyMap()), new MCPDatabaseCapabilityProvider(Collections.emptyMap()), "stdio");
    }
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
    }
}
