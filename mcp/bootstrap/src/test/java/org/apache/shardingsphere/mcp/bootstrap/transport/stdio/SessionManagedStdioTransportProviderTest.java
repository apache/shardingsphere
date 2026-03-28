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

package org.apache.shardingsphere.mcp.bootstrap.transport.stdio;

import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerTransport;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPSessionCloser;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SessionManagedStdioTransportProviderTest {
    
    @Test
    void assertProtocolVersions() {
        SessionManagedStdioTransportProvider provider = createProvider(mock(MCPRuntimeContext.class), mock(MCPSessionCloser.class), mock(Runnable.class));
        List<String> actual = provider.protocolVersions();
        assertThat(actual, is(List.of(MCPTransportConstants.PROTOCOL_VERSION)));
    }
    
    @Test
    void assertSetSessionFactory() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        MCPSessionCloser sessionCloser = mock(MCPSessionCloser.class);
        Runnable closeCallback = mock(Runnable.class);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        when(runtimeContext.getSessionManager()).thenReturn(sessionManager);
        when(sessionFactory.create(any(McpServerTransport.class))).thenReturn(session);
        when(session.getId()).thenReturn("session-id");
        SessionManagedStdioTransportProvider provider = createProvider(runtimeContext, sessionCloser, closeCallback);
        provider.setSessionFactory(sessionFactory);
        verify(sessionManager).createSession("session-id");
    }
    
    @Test
    void assertCloseGracefully() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        MCPSessionCloser sessionCloser = mock(MCPSessionCloser.class);
        Runnable closeCallback = mock(Runnable.class);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        when(runtimeContext.getSessionManager()).thenReturn(mock(MCPSessionManager.class));
        when(sessionFactory.create(any(McpServerTransport.class))).thenReturn(session);
        when(session.getId()).thenReturn("session-id");
        when(session.closeGracefully()).thenReturn(Mono.empty());
        SessionManagedStdioTransportProvider provider = createProvider(runtimeContext, sessionCloser, closeCallback);
        provider.setSessionFactory(sessionFactory);
        provider.closeGracefully().block();
        verify(sessionCloser).closeSession("session-id");
        verify(closeCallback).run();
    }
    
    @Test
    void assertCloseGracefullyWithoutActiveSession() {
        MCPSessionCloser sessionCloser = mock(MCPSessionCloser.class);
        Runnable closeCallback = mock(Runnable.class);
        SessionManagedStdioTransportProvider provider = createProvider(mock(MCPRuntimeContext.class), sessionCloser, closeCallback);
        provider.closeGracefully().block();
        verifyNoInteractions(sessionCloser);
        verify(closeCallback).run();
    }
    
    private SessionManagedStdioTransportProvider createProvider(final MCPRuntimeContext runtimeContext, final MCPSessionCloser sessionCloser, final Runnable closeCallback) {
        return new SessionManagedStdioTransportProvider(runtimeContext, sessionCloser, MCPTransportJsonMapperFactory.create(),
                new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), closeCallback);
    }
}
