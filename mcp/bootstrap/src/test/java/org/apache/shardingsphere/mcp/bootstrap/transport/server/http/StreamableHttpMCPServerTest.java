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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http;

import io.modelcontextprotocol.server.McpSyncServer;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPSyncServerFactory;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StreamableHttpMCPServerTest {
    
    private MockedConstruction<MCPSyncServerFactory> mockedSyncServerFactories;
    
    private MockedConstruction<StreamableHttpMCPServlet> mockedTransportServlets;
    
    @BeforeEach
    void setUp() {
        mockedSyncServerFactories = mockConstruction(MCPSyncServerFactory.class);
        mockedTransportServlets = mockConstruction(StreamableHttpMCPServlet.class);
    }
    
    @AfterEach
    void tearDown() {
        mockedTransportServlets.close();
        mockedSyncServerFactories.close();
    }
    
    @Test
    void assertGetLocalPortBeforeStart() {
        assertThat(createServer(18080).getLocalPort(), is(18080));
    }
    
    @Test
    void assertStartAndStop() throws IOException {
        McpSyncServer syncServer = mock(McpSyncServer.class);
        StreamableHttpMCPServer actual = createServer(0);
        MCPSyncServerFactory syncServerFactory = getSyncServerFactory();
        StreamableHttpMCPServlet transportServlet = getTransportServlet();
        when(syncServerFactory.create(transportServlet)).thenReturn(syncServer);
        actual.start();
        assertThat(actual.getLocalPort(), greaterThan(0));
        actual.stop();
        verify(syncServerFactory).create(transportServlet);
        verify(syncServer).closeGracefully();
        assertThat(actual.getLocalPort(), is(0));
    }
    
    @Test
    void assertStartOnce() throws IOException {
        McpSyncServer syncServer = mock(McpSyncServer.class);
        StreamableHttpMCPServer actual = createServer(0);
        MCPSyncServerFactory syncServerFactory = getSyncServerFactory();
        StreamableHttpMCPServlet transportServlet = getTransportServlet();
        when(syncServerFactory.create(transportServlet)).thenReturn(syncServer);
        actual.start();
        actual.start();
        actual.stop();
        verify(syncServerFactory).create(transportServlet);
    }
    
    @Test
    void assertStartWithLifecycleFailure() {
        McpSyncServer syncServer = mock(McpSyncServer.class);
        try (MockedConstruction<Tomcat> ignoredMockedTomcat = mockConstruction(Tomcat.class, (mock, context) -> {
            when(mock.addContext(anyString(), anyString())).thenReturn(new StandardContext());
            doThrow(new LifecycleException("mocked lifecycle failure")).when(mock).start();
        })) {
            StreamableHttpMCPServer actual = createServer(0);
            when(getSyncServerFactory().create(getTransportServlet())).thenReturn(syncServer);
            IOException ex = assertThrows(IOException.class, actual::start);
            assertThat(ex.getMessage(), is("Failed to start embedded Tomcat runtime."));
        }
        verify(syncServer).closeGracefully();
    }
    
    @Test
    void assertStopWithoutStart() {
        assertDoesNotThrow(() -> createServer(18080).stop());
    }
    
    @Test
    void assertStopClosesTomcatBeforeSyncServer() throws IOException, LifecycleException {
        McpSyncServer syncServer = mock(McpSyncServer.class);
        try (MockedConstruction<Tomcat> mockedTomcat = mockConstruction(Tomcat.class, (mock, context) -> when(mock.addContext(anyString(), anyString())).thenReturn(new StandardContext()))) {
            StreamableHttpMCPServer actual = createServer(0);
            when(getSyncServerFactory().create(getTransportServlet())).thenReturn(syncServer);
            actual.start();
            actual.stop();
            Tomcat tomcat = mockedTomcat.constructed().get(0);
            InOrder inOrder = inOrder(tomcat, syncServer);
            inOrder.verify(tomcat).stop();
            inOrder.verify(tomcat).destroy();
            inOrder.verify(syncServer).closeGracefully();
        }
    }
    
    private StreamableHttpMCPServer createServer(final int port) {
        return new StreamableHttpMCPServer(createConfig(port), createRuntimeContext());
    }
    
    private MCPSyncServerFactory getSyncServerFactory() {
        return mockedSyncServerFactories.constructed().getFirst();
    }
    
    private StreamableHttpMCPServlet getTransportServlet() {
        return mockedTransportServlets.constructed().getFirst();
    }
    
    private HttpTransportConfiguration createConfig(final int port) {
        return new HttpTransportConfiguration("127.0.0.1", port, "/mcp");
    }
    
    private MCPRuntimeContext createRuntimeContext() {
        return new MCPRuntimeContext(new MCPSessionManager(Collections.emptyMap()), new MCPDatabaseCapabilityProvider(Collections.emptyMap()), MCPTransportType.HTTP);
    }
}
