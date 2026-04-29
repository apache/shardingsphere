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
import org.junit.jupiter.api.Test;

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
        new StdioMCPServer(syncServerFactory, transportProvider).start();
        verify(syncServerFactory).create(transportProvider);
    }
    
    @Test
    void assertStartOnce() {
        MCPSyncServerFactory syncServerFactory = mock(MCPSyncServerFactory.class);
        SessionManagedStdioTransportProvider transportProvider = mock(SessionManagedStdioTransportProvider.class);
        McpSyncServer syncServer = mock(McpSyncServer.class);
        when(syncServerFactory.create(transportProvider)).thenReturn(syncServer);
        StdioMCPServer actual = new StdioMCPServer(syncServerFactory, transportProvider);
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
        StdioMCPServer actual = new StdioMCPServer(syncServerFactory, transportProvider);
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
        StdioMCPServer actual = new StdioMCPServer(syncServerFactory, transportProvider);
        actual.start();
        actual.stop();
        actual.start();
        verify(syncServerFactory, times(2)).create(transportProvider);
        verify(firstSyncServer).closeGracefully();
    }
    
    @Test
    void assertStopWithoutStart() {
        assertDoesNotThrow(() -> new StdioMCPServer(mock(MCPSyncServerFactory.class), mock(SessionManagedStdioTransportProvider.class)).stop());
    }
}
