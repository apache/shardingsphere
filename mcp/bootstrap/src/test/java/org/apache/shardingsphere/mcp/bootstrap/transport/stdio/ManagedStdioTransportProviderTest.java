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
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPSessionCloser;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ManagedStdioTransportProviderTest {
    
    @Test
    void assertProtocolVersions() {
        ManagedStdioTransportProvider provider = new ManagedStdioTransportProvider(
                mock(MCPRuntimeContext.class), mock(MCPSessionCloser.class), mock(McpServerTransportProvider.class), mock(Runnable.class));
        List<String> actual = provider.protocolVersions();
        assertThat(actual, is(List.of(MCPTransportConstants.PROTOCOL_VERSION)));
    }
    
    @Test
    void assertSetSessionFactory() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        MCPSessionCloser sessionCloser = mock(MCPSessionCloser.class);
        McpServerTransportProvider delegate = mock(McpServerTransportProvider.class);
        Runnable closeCallback = mock(Runnable.class);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        McpServerTransport transport = mock(McpServerTransport.class);
        when(runtimeContext.getSessionManager()).thenReturn(sessionManager);
        when(sessionFactory.create(transport)).thenReturn(session);
        when(session.getId()).thenReturn("session-id");
        ManagedStdioTransportProvider provider = new ManagedStdioTransportProvider(runtimeContext, sessionCloser, delegate, closeCallback);
        provider.setSessionFactory(sessionFactory);
        McpServerSession actual = captureSessionFactory(delegate).create(transport);
        assertThat(actual, is(session));
        verify(sessionManager).createSession("session-id");
    }
    
    @Test
    void assertNotifyClients() {
        McpServerTransportProvider delegate = mock(McpServerTransportProvider.class);
        Mono<Void> expected = Mono.empty();
        when(delegate.notifyClients("method", "params")).thenReturn(expected);
        ManagedStdioTransportProvider provider = new ManagedStdioTransportProvider(
                mock(MCPRuntimeContext.class), mock(MCPSessionCloser.class), delegate, mock(Runnable.class));
        Mono<Void> actual = provider.notifyClients("method", "params");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertNotifyClient() {
        McpServerTransportProvider delegate = mock(McpServerTransportProvider.class);
        Mono<Void> expected = Mono.empty();
        when(delegate.notifyClient("session-id", "method", "params")).thenReturn(expected);
        ManagedStdioTransportProvider provider = new ManagedStdioTransportProvider(
                mock(MCPRuntimeContext.class), mock(MCPSessionCloser.class), delegate, mock(Runnable.class));
        Mono<Void> actual = provider.notifyClient("session-id", "method", "params");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCloseGracefully() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        MCPSessionCloser sessionCloser = mock(MCPSessionCloser.class);
        McpServerTransportProvider delegate = mock(McpServerTransportProvider.class);
        Runnable closeCallback = mock(Runnable.class);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        McpServerTransport transport = mock(McpServerTransport.class);
        when(runtimeContext.getSessionManager()).thenReturn(mock(MCPSessionManager.class));
        when(sessionFactory.create(transport)).thenReturn(session);
        when(session.getId()).thenReturn("session-id");
        when(delegate.closeGracefully()).thenReturn(Mono.empty());
        ManagedStdioTransportProvider provider = new ManagedStdioTransportProvider(runtimeContext, sessionCloser, delegate, closeCallback);
        provider.setSessionFactory(sessionFactory);
        captureSessionFactory(delegate).create(transport);
        provider.closeGracefully().block();
        verify(sessionCloser).closeSession("session-id");
        verify(closeCallback).run();
    }
    
    @Test
    void assertCloseGracefullyWithoutActiveSession() {
        MCPSessionCloser sessionCloser = mock(MCPSessionCloser.class);
        McpServerTransportProvider delegate = mock(McpServerTransportProvider.class);
        Runnable closeCallback = mock(Runnable.class);
        when(delegate.closeGracefully()).thenReturn(Mono.empty());
        ManagedStdioTransportProvider provider = new ManagedStdioTransportProvider(mock(MCPRuntimeContext.class), sessionCloser, delegate, closeCallback);
        provider.closeGracefully().block();
        verifyNoInteractions(sessionCloser);
        verify(closeCallback).run();
    }
    
    private McpServerSession.Factory captureSessionFactory(final McpServerTransportProvider delegate) {
        ArgumentCaptor<McpServerSession.Factory> result = ArgumentCaptor.forClass(McpServerSession.Factory.class);
        verify(delegate).setSessionFactory(result.capture());
        return result.getValue();
    }
}
