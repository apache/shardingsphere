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
import org.apache.shardingsphere.mcp.tool.handler.execute.MCPJdbcTransactionResourceManager;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class SessionManagedStdioTransportProviderTest {
    
    @Test
    void assertProtocolVersions() {
        SessionManagedStdioTransportProvider provider = new SessionManagedStdioTransportProvider(new MCPSessionManager(Collections.emptyMap()), MCPTransportJsonMapperFactory.create());
        List<String> actual = provider.protocolVersions();
        assertThat(actual, is(List.of(MCPTransportConstants.PROTOCOL_VERSION)));
    }
    
    @Test
    void assertSetSessionFactory() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        when(sessionFactory.create(any(McpServerTransport.class))).thenReturn(session);
        when(session.getId()).thenReturn("session-id");
        SessionManagedStdioTransportProvider provider = new SessionManagedStdioTransportProvider(sessionManager, MCPTransportJsonMapperFactory.create());
        provider.setSessionFactory(sessionFactory);
        assertTrue(sessionManager.hasSession("session-id"));
    }
    
    @Test
    void assertCloseManagedSessionWhenTransportCloseGracefully() throws ReflectiveOperationException {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        MCPSessionManager sessionManager = createSessionManager(transactionResourceManager);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        AtomicReference<McpServerTransport> transport = new AtomicReference<>();
        when(sessionFactory.create(any(McpServerTransport.class))).thenAnswer(invocation -> {
            transport.set(invocation.getArgument(0, McpServerTransport.class));
            return session;
        });
        when(session.getId()).thenReturn("session-id");
        SessionManagedStdioTransportProvider provider = new SessionManagedStdioTransportProvider(sessionManager, MCPTransportJsonMapperFactory.create());
        provider.setSessionFactory(sessionFactory);
        transport.get().closeGracefully().block();
        verify(transactionResourceManager).closeSession("session-id");
        verifyNoMoreInteractions(transactionResourceManager);
        assertFalse(sessionManager.hasSession("session-id"));
    }
    
    @Test
    void assertSetSessionFactoryAfterTransportClose() throws ReflectiveOperationException {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        MCPSessionManager sessionManager = createSessionManager(transactionResourceManager);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession firstSession = mock(McpServerSession.class);
        McpServerSession secondSession = mock(McpServerSession.class);
        AtomicReference<McpServerTransport> firstTransport = new AtomicReference<>();
        AtomicReference<McpServerTransport> secondTransport = new AtomicReference<>();
        AtomicInteger invocationCount = new AtomicInteger();
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
        SessionManagedStdioTransportProvider provider = new SessionManagedStdioTransportProvider(sessionManager, MCPTransportJsonMapperFactory.create());
        provider.setSessionFactory(sessionFactory);
        firstTransport.get().close();
        provider.setSessionFactory(sessionFactory);
        verify(transactionResourceManager).closeSession("session-id-1");
        verifyNoMoreInteractions(transactionResourceManager);
        assertFalse(sessionManager.hasSession("session-id-1"));
        assertTrue(sessionManager.hasSession("session-id-2"));
        assertNotNull(secondTransport.get());
    }
    
    @Test
    void assertCloseManagedSessionWhenTransportClose() throws ReflectiveOperationException {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        MCPSessionManager sessionManager = createSessionManager(transactionResourceManager);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        AtomicReference<McpServerTransport> transport = new AtomicReference<>();
        when(sessionFactory.create(any(McpServerTransport.class))).thenAnswer(invocation -> {
            transport.set(invocation.getArgument(0, McpServerTransport.class));
            return session;
        });
        when(session.getId()).thenReturn("session-id");
        SessionManagedStdioTransportProvider provider = new SessionManagedStdioTransportProvider(sessionManager, MCPTransportJsonMapperFactory.create());
        provider.setSessionFactory(sessionFactory);
        transport.get().close();
        verify(transactionResourceManager).closeSession("session-id");
        verifyNoMoreInteractions(transactionResourceManager);
        assertFalse(sessionManager.hasSession("session-id"));
    }
    
    @Test
    void assertCloseManagedSessionOnlyOnceWhenTransportClosedRepeatedly() throws ReflectiveOperationException {
        MCPJdbcTransactionResourceManager transactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        MCPSessionManager sessionManager = createSessionManager(transactionResourceManager);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        AtomicReference<McpServerTransport> transport = new AtomicReference<>();
        when(sessionFactory.create(any(McpServerTransport.class))).thenAnswer(invocation -> {
            transport.set(invocation.getArgument(0, McpServerTransport.class));
            return session;
        });
        when(session.getId()).thenReturn("session-id");
        SessionManagedStdioTransportProvider provider = new SessionManagedStdioTransportProvider(sessionManager, MCPTransportJsonMapperFactory.create());
        provider.setSessionFactory(sessionFactory);
        transport.get().close();
        transport.get().closeGracefully().block();
        verify(transactionResourceManager).closeSession("session-id");
        verifyNoMoreInteractions(transactionResourceManager);
        assertFalse(sessionManager.hasSession("session-id"));
    }
    
    private MCPSessionManager createSessionManager(final MCPJdbcTransactionResourceManager transactionResourceManager) throws ReflectiveOperationException {
        MCPSessionManager result = new MCPSessionManager(Collections.emptyMap());
        Plugins.getMemberAccessor().set(MCPSessionManager.class.getDeclaredField("transactionResourceManager"), result, transactionResourceManager);
        return result;
    }
}
