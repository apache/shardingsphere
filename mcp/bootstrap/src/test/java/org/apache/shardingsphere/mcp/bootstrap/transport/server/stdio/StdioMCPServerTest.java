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
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPSyncServerFactory;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StdioMCPServerTest {
    
    private MockedConstruction<MCPSyncServerFactory> mockedSyncServerFactories;
    
    private MockedConstruction<SessionManagedStdioTransportProvider> mockedTransportProviders;
    
    @BeforeEach
    void setUp() {
        mockedSyncServerFactories = mockConstruction(MCPSyncServerFactory.class);
        mockedTransportProviders = mockConstruction(SessionManagedStdioTransportProvider.class);
    }
    
    @AfterEach
    void tearDown() {
        mockedTransportProviders.close();
        mockedSyncServerFactories.close();
    }
    
    @Test
    void assertStart() {
        McpSyncServer syncServer = mock(McpSyncServer.class);
        StdioMCPServer actual = createServer();
        MCPSyncServerFactory syncServerFactory = getSyncServerFactory();
        SessionManagedStdioTransportProvider transportProvider = getTransportProvider();
        when(syncServerFactory.create(transportProvider)).thenReturn(syncServer);
        actual.start();
        verify(syncServerFactory).create(transportProvider);
    }
    
    @Test
    void assertStartOnce() {
        McpSyncServer syncServer = mock(McpSyncServer.class);
        StdioMCPServer actual = createServer();
        MCPSyncServerFactory syncServerFactory = getSyncServerFactory();
        SessionManagedStdioTransportProvider transportProvider = getTransportProvider();
        when(syncServerFactory.create(transportProvider)).thenReturn(syncServer);
        actual.start();
        actual.start();
        verify(syncServerFactory).create(transportProvider);
    }
    
    @Test
    void assertStop() {
        McpSyncServer syncServer = mock(McpSyncServer.class);
        StdioMCPServer actual = createServer();
        MCPSyncServerFactory syncServerFactory = getSyncServerFactory();
        SessionManagedStdioTransportProvider transportProvider = getTransportProvider();
        when(syncServerFactory.create(transportProvider)).thenReturn(syncServer);
        actual.start();
        actual.stop();
        verify(syncServer).closeGracefully();
    }
    
    @Test
    void assertStartAfterStop() {
        McpSyncServer firstSyncServer = mock(McpSyncServer.class);
        McpSyncServer secondSyncServer = mock(McpSyncServer.class);
        StdioMCPServer actual = createServer();
        MCPSyncServerFactory syncServerFactory = getSyncServerFactory();
        SessionManagedStdioTransportProvider transportProvider = getTransportProvider();
        when(syncServerFactory.create(transportProvider)).thenReturn(firstSyncServer, secondSyncServer);
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
    
    private StdioMCPServer createServer() {
        return new StdioMCPServer(createRuntimeContext());
    }
    
    private MCPSyncServerFactory getSyncServerFactory() {
        return mockedSyncServerFactories.constructed().getFirst();
    }
    
    private SessionManagedStdioTransportProvider getTransportProvider() {
        return mockedTransportProviders.constructed().getFirst();
    }
    
    private MCPRuntimeContext createRuntimeContext() {
        return new MCPRuntimeContext(new MCPSessionManager(Collections.emptyMap()), new MCPDatabaseCapabilityProvider(Collections.emptyMap()), MCPTransportType.STDIO);
    }
}
