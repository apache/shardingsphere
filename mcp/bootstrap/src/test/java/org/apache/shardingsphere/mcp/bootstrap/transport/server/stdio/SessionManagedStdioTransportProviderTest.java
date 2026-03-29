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

import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerTransport;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.session.MCPSessionLifecycleRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SessionManagedStdioTransportProviderTest {
    
    @Test
    void assertProtocolVersions() {
        SessionManagedStdioTransportProvider provider = new SessionManagedStdioTransportProvider(mock(MCPRuntimeContext.class), MCPTransportJsonMapperFactory.create());
        List<String> actual = provider.protocolVersions();
        assertThat(actual, is(List.of(MCPTransportConstants.PROTOCOL_VERSION)));
    }
    
    @Test
    void assertSetSessionFactory() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        MCPSessionLifecycleRegistry sessionLifecycleRegistry = mock(MCPSessionLifecycleRegistry.class);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        when(runtimeContext.getSessionLifecycleRegistry()).thenReturn(sessionLifecycleRegistry);
        when(sessionFactory.create(any(McpServerTransport.class))).thenReturn(session);
        when(session.getId()).thenReturn("session-id");
        SessionManagedStdioTransportProvider provider = new SessionManagedStdioTransportProvider(runtimeContext, MCPTransportJsonMapperFactory.create());
        provider.setSessionFactory(sessionFactory);
        verify(sessionLifecycleRegistry).create("session-id");
    }
    
    @Test
    void assertSetSessionFactoryWhenSessionAlreadyActive() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        MCPSessionLifecycleRegistry sessionLifecycleRegistry = mock(MCPSessionLifecycleRegistry.class);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        when(runtimeContext.getSessionLifecycleRegistry()).thenReturn(sessionLifecycleRegistry);
        when(sessionFactory.create(any(McpServerTransport.class))).thenReturn(session);
        when(session.getId()).thenReturn("session-id");
        SessionManagedStdioTransportProvider provider = new SessionManagedStdioTransportProvider(runtimeContext, MCPTransportJsonMapperFactory.create());
        provider.setSessionFactory(sessionFactory);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> provider.setSessionFactory(sessionFactory));
        assertThat(actual.getMessage(), is("STDIO transport supports only one active session at a time."));
        verify(sessionLifecycleRegistry).create("session-id");
        verifyNoMoreInteractions(sessionLifecycleRegistry);
    }
    
    @Test
    void assertCloseManagedSessionWhenTransportCloseGracefully() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        MCPSessionLifecycleRegistry sessionLifecycleRegistry = mock(MCPSessionLifecycleRegistry.class);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        AtomicReference<McpServerTransport> transport = new AtomicReference<>();
        when(runtimeContext.getSessionLifecycleRegistry()).thenReturn(sessionLifecycleRegistry);
        when(sessionFactory.create(any(McpServerTransport.class))).thenAnswer(invocation -> {
            transport.set(invocation.getArgument(0, McpServerTransport.class));
            return session;
        });
        when(session.getId()).thenReturn("session-id");
        SessionManagedStdioTransportProvider provider = new SessionManagedStdioTransportProvider(runtimeContext, MCPTransportJsonMapperFactory.create());
        provider.setSessionFactory(sessionFactory);
        clearInvocations(runtimeContext, sessionLifecycleRegistry);
        transport.get().closeGracefully().block();
        verify(sessionLifecycleRegistry).close("session-id");
        verifyNoInteractions(runtimeContext);
    }
    
    @Test
    void assertSetSessionFactoryAfterTransportClose() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        MCPSessionLifecycleRegistry sessionLifecycleRegistry = mock(MCPSessionLifecycleRegistry.class);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession firstSession = mock(McpServerSession.class);
        McpServerSession secondSession = mock(McpServerSession.class);
        AtomicReference<McpServerTransport> firstTransport = new AtomicReference<>();
        AtomicReference<McpServerTransport> secondTransport = new AtomicReference<>();
        AtomicInteger invocationCount = new AtomicInteger();
        when(runtimeContext.getSessionLifecycleRegistry()).thenReturn(sessionLifecycleRegistry);
        when(sessionFactory.create(any(McpServerTransport.class))).thenAnswer(invocation -> {
            McpServerTransport actual = invocation.getArgument(0, McpServerTransport.class);
            if (0 == invocationCount.getAndIncrement()) {
                firstTransport.set(actual);
                return firstSession;
            }
            secondTransport.set(actual);
            return secondSession;
        });
        when(firstSession.getId()).thenReturn("session-id-1");
        when(secondSession.getId()).thenReturn("session-id-2");
        SessionManagedStdioTransportProvider provider = new SessionManagedStdioTransportProvider(runtimeContext, MCPTransportJsonMapperFactory.create());
        provider.setSessionFactory(sessionFactory);
        firstTransport.get().close();
        provider.setSessionFactory(sessionFactory);
        verify(sessionLifecycleRegistry).create("session-id-1");
        verify(sessionLifecycleRegistry).close("session-id-1");
        verify(sessionLifecycleRegistry).create("session-id-2");
        assertNotNull(secondTransport.get());
    }
    
    @Test
    void assertCloseManagedSessionWhenTransportClose() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        MCPSessionLifecycleRegistry sessionLifecycleRegistry = mock(MCPSessionLifecycleRegistry.class);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        AtomicReference<McpServerTransport> transport = new AtomicReference<>();
        when(runtimeContext.getSessionLifecycleRegistry()).thenReturn(sessionLifecycleRegistry);
        when(sessionFactory.create(any(McpServerTransport.class))).thenAnswer(invocation -> {
            transport.set(invocation.getArgument(0, McpServerTransport.class));
            return session;
        });
        when(session.getId()).thenReturn("session-id");
        SessionManagedStdioTransportProvider provider = new SessionManagedStdioTransportProvider(runtimeContext, MCPTransportJsonMapperFactory.create());
        provider.setSessionFactory(sessionFactory);
        clearInvocations(runtimeContext, sessionLifecycleRegistry);
        transport.get().close();
        verify(sessionLifecycleRegistry).close("session-id");
        verifyNoInteractions(runtimeContext);
    }
    
    @Test
    void assertCloseManagedSessionOnlyOnceWhenTransportClosedRepeatedly() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class);
        MCPSessionLifecycleRegistry sessionLifecycleRegistry = mock(MCPSessionLifecycleRegistry.class);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        AtomicReference<McpServerTransport> transport = new AtomicReference<>();
        when(runtimeContext.getSessionLifecycleRegistry()).thenReturn(sessionLifecycleRegistry);
        when(sessionFactory.create(any(McpServerTransport.class))).thenAnswer(invocation -> {
            transport.set(invocation.getArgument(0, McpServerTransport.class));
            return session;
        });
        when(session.getId()).thenReturn("session-id");
        SessionManagedStdioTransportProvider provider = new SessionManagedStdioTransportProvider(runtimeContext, MCPTransportJsonMapperFactory.create());
        provider.setSessionFactory(sessionFactory);
        clearInvocations(runtimeContext, sessionLifecycleRegistry);
        transport.get().close();
        transport.get().closeGracefully().block();
        verify(sessionLifecycleRegistry).close("session-id");
        verifyNoInteractions(runtimeContext);
        verifyNoMoreInteractions(sessionLifecycleRegistry);
    }
}
