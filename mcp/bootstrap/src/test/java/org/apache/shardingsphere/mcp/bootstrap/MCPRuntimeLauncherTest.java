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

package org.apache.shardingsphere.mcp.bootstrap;

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportType;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPRuntimeServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.stdio.StdioMCPServer;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

class MCPRuntimeLauncherTest {
    
    @Test
    void assertLaunchWithHttpTransport() throws IOException {
        try (
                MockedConstruction<MCPSessionManager> ignoredMockedSessionManager = mockConstruction(MCPSessionManager.class);
                MockedConstruction<MCPDatabaseCapabilityProvider> ignoredMockedCapabilityProvider = mockConstruction(MCPDatabaseCapabilityProvider.class);
                MockedConstruction<StreamableHttpMCPServer> mockedHttpServer = mockConstruction(StreamableHttpMCPServer.class);
                MockedConstruction<StdioMCPServer> mockedStdioServer = mockConstruction(StdioMCPServer.class)) {
            MCPRuntimeServer actual = new MCPRuntimeLauncher("conf/mcp-http.yaml").launch(createLaunchConfiguration(true));
            assertThat(actual, is(mockedHttpServer.constructed().get(0)));
            assertThat(mockedHttpServer.constructed().size(), is(1));
            assertThat(mockedStdioServer.constructed().size(), is(0));
            verify(actual).start();
        }
    }
    
    @Test
    void assertLaunchWithStdioTransport() throws IOException {
        try (
                MockedConstruction<MCPSessionManager> ignoredMockedSessionManager = mockConstruction(MCPSessionManager.class);
                MockedConstruction<MCPDatabaseCapabilityProvider> ignoredMockedCapabilityProvider = mockConstruction(MCPDatabaseCapabilityProvider.class);
                MockedConstruction<StreamableHttpMCPServer> mockedHttpServer = mockConstruction(StreamableHttpMCPServer.class);
                MockedConstruction<StdioMCPServer> mockedStdioServer = mockConstruction(StdioMCPServer.class)) {
            MCPRuntimeServer actual = new MCPRuntimeLauncher("conf/mcp-http.yaml").launch(createLaunchConfiguration(false));
            assertThat(actual, isA(StdioMCPServer.class));
            assertThat(actual, is(mockedStdioServer.constructed().get(0)));
            assertThat(mockedHttpServer.constructed().size(), is(0));
            assertThat(mockedStdioServer.constructed().size(), is(1));
            verify(actual).start();
        }
    }
    
    @Test
    void assertLaunchWithoutRuntimeDatabases() throws IOException {
        try (
                MockedConstruction<MCPSessionManager> ignoredMockedSessionManager = mockConstruction(MCPSessionManager.class);
                MockedConstruction<MCPDatabaseCapabilityProvider> ignoredMockedCapabilityProvider = mockConstruction(MCPDatabaseCapabilityProvider.class);
                MockedConstruction<StreamableHttpMCPServer> mockedHttpServer = mockConstruction(StreamableHttpMCPServer.class);
                MockedConstruction<StdioMCPServer> mockedStdioServer = mockConstruction(StdioMCPServer.class)) {
            MCPRuntimeServer actual = new MCPRuntimeLauncher("conf/mcp-http.yaml").launch(createLaunchConfiguration(true, Map.of()));
            assertThat(actual, is(mockedHttpServer.constructed().get(0)));
            assertThat(mockedHttpServer.constructed().size(), is(1));
            assertThat(mockedStdioServer.constructed().size(), is(0));
            verify(actual).start();
        }
    }
    
    @Test
    void assertLaunchWithNullConfiguration() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> new MCPRuntimeLauncher("conf/mcp-http.yaml").launch(null));
        assertThat(actual.getMessage(), is("MCP launch configuration cannot be null."));
    }
    
    @Test
    void assertLaunchWithHttpStartFailure() {
        IOException startFailure = new IOException("mocked http failure");
        try (
                MockedConstruction<MCPSessionManager> ignoredMockedSessionManager = mockConstruction(MCPSessionManager.class);
                MockedConstruction<MCPDatabaseCapabilityProvider> ignoredMockedCapabilityProvider = mockConstruction(MCPDatabaseCapabilityProvider.class);
                MockedConstruction<StreamableHttpMCPServer> mockedHttpServer = mockConstruction(StreamableHttpMCPServer.class, (mock, context) -> doThrow(startFailure).when(mock).start())) {
            IOException actual = assertThrows(IOException.class, () -> new MCPRuntimeLauncher("conf/mcp-http.yaml").launch(createLaunchConfiguration(true)));
            assertThat(actual.getMessage(), is("Failed to start HTTP server."));
            assertThat(actual.getCause(), is(startFailure));
            verify(mockedHttpServer.constructed().get(0)).stop();
        }
    }
    
    @Test
    void assertLaunchWithStdioStartFailure() {
        IOException startFailure = new IOException("mocked stdio failure");
        try (
                MockedConstruction<MCPSessionManager> ignoredMockedSessionManager = mockConstruction(MCPSessionManager.class);
                MockedConstruction<MCPDatabaseCapabilityProvider> ignoredMockedCapabilityProvider = mockConstruction(MCPDatabaseCapabilityProvider.class);
                MockedConstruction<StdioMCPServer> mockedStdioServer = mockConstruction(StdioMCPServer.class, (mock, context) -> doThrow(startFailure).when(mock).start())) {
            IOException actual = assertThrows(IOException.class, () -> new MCPRuntimeLauncher("conf/mcp-http.yaml").launch(createLaunchConfiguration(false)));
            assertThat(actual.getMessage(), is("Failed to start STDIO server."));
            assertThat(actual.getCause(), is(startFailure));
            verify(mockedStdioServer.constructed().get(0)).stop();
        }
    }
    
    private MCPLaunchConfiguration createLaunchConfiguration(final boolean httpEnabled) {
        return createLaunchConfiguration(httpEnabled, Map.of("logic_db", mock(RuntimeDatabaseConfiguration.class)));
    }
    
    private MCPLaunchConfiguration createLaunchConfiguration(final boolean httpEnabled, final Map<String, RuntimeDatabaseConfiguration> databases) {
        return new MCPLaunchConfiguration(httpEnabled ? MCPTransportType.STREAMABLE_HTTP : MCPTransportType.STDIO, new HttpTransportConfiguration("127.0.0.1", 18080, "/mcp"), databases);
    }
}
