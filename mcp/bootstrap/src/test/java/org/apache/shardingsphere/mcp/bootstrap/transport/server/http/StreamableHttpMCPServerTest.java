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
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.OAuthIntrospectionConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPSyncServerFactory;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StreamableHttpMCPServerTest {
    
    @Test
    void assertGetLocalPortBeforeStart() {
        assertThat(createServer(18080).getLocalPort(), is(18080));
    }
    
    @Test
    void assertStartAndStop() throws IOException {
        MCPSyncServerFactory syncServerFactory = mock(MCPSyncServerFactory.class);
        McpSyncServer syncServer = mock(McpSyncServer.class);
        StreamableHttpMCPServlet transportServlet = createTransportServlet();
        when(syncServerFactory.create(transportServlet)).thenReturn(syncServer);
        StreamableHttpMCPServer actual = createServer(createConfig(0), syncServerFactory, transportServlet);
        actual.start();
        assertThat(actual.getLocalPort(), greaterThan(0));
        actual.stop();
        verify(syncServerFactory).create(transportServlet);
        verify(syncServer).closeGracefully();
        assertThat(actual.getLocalPort(), is(0));
    }
    
    @Test
    void assertStartOnce() throws IOException {
        MCPSyncServerFactory syncServerFactory = mock(MCPSyncServerFactory.class);
        McpSyncServer syncServer = mock(McpSyncServer.class);
        StreamableHttpMCPServlet transportServlet = createTransportServlet();
        when(syncServerFactory.create(transportServlet)).thenReturn(syncServer);
        StreamableHttpMCPServer actual = createServer(createConfig(0), syncServerFactory, transportServlet);
        actual.start();
        actual.start();
        actual.stop();
        verify(syncServerFactory).create(transportServlet);
    }
    
    @Test
    void assertStartWithLifecycleFailure() {
        MCPSyncServerFactory syncServerFactory = mock(MCPSyncServerFactory.class);
        McpSyncServer syncServer = mock(McpSyncServer.class);
        StreamableHttpMCPServlet transportServlet = createTransportServlet();
        when(syncServerFactory.create(transportServlet)).thenReturn(syncServer);
        try (MockedConstruction<Tomcat> ignoredMockedTomcat = mockConstruction(Tomcat.class, (mock, context) -> {
            when(mock.addContext(anyString(), anyString())).thenReturn(new StandardContext());
            doThrow(new LifecycleException("mocked lifecycle failure")).when(mock).start();
        })) {
            StreamableHttpMCPServer actual = createServer(createConfig(0), syncServerFactory, transportServlet);
            IOException ex = assertThrows(IOException.class, actual::start);
            assertThat(ex.getMessage(), is("Failed to start embedded Tomcat runtime."));
        }
        verify(syncServer).closeGracefully();
    }
    
    @Test
    void assertStopWithoutStart() {
        assertDoesNotThrow(() -> createServer(18080).stop());
    }
    
    private StreamableHttpMCPServer createServer(final int port) {
        return createServer(createConfig(port), mock(MCPSyncServerFactory.class), createTransportServlet());
    }
    
    private StreamableHttpMCPServer createServer(final HttpTransportConfiguration config, final MCPSyncServerFactory syncServerFactory,
                                                 final StreamableHttpMCPServlet transportServlet) {
        StreamableHttpMCPServer result = new StreamableHttpMCPServer(config, createRuntimeContext());
        try {
            setField(result, "syncServerFactory", syncServerFactory);
            setField(result, "transportServlet", transportServlet);
            return result;
        } catch (final ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
    
    private HttpTransportConfiguration createConfig(final int port) {
        return new HttpTransportConfiguration(true, "127.0.0.1", false, "", port, "/mcp", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "",
                new OAuthIntrospectionConfiguration());
    }
    
    private StreamableHttpMCPServlet createTransportServlet() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        try {
            StreamableHttpMCPServlet result = new StreamableHttpMCPServlet(sessionManager, MCPTransportJsonMapperFactory.create(), createConfig(0));
            setField(result, "delegate", mock(HttpServletStreamableServerTransportProvider.class));
            setField(result, "sessionExecutionCoordinator", new MCPSessionExecutionCoordinator(sessionManager));
            return result;
        } catch (final ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
    
    private MCPRuntimeContext createRuntimeContext() {
        return new MCPRuntimeContext(new MCPSessionManager(Collections.emptyMap()), new MCPDatabaseCapabilityProvider(Collections.emptyMap()), "http");
    }
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
    }
}
