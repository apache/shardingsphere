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
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
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
import static org.mockito.Mockito.when;

class SessionManagedStdioTransportProviderTest {
    
    @Test
    void assertProtocolVersions() {
        SessionManagedStdioTransportProvider provider = createProvider(new MCPSessionManager(Collections.emptyMap()));
        List<String> actual = provider.protocolVersions();
        assertThat(actual, is(MCPTransportConstants.SUPPORTED_PROTOCOL_VERSIONS));
    }
    
    @Test
    void assertSetSessionFactory() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        when(sessionFactory.create(any(McpServerTransport.class))).thenReturn(session);
        when(session.getId()).thenReturn("session-id");
        SessionManagedStdioTransportProvider provider = createProvider(sessionManager);
        provider.setSessionFactory(sessionFactory);
        assertTrue(sessionManager.hasSession("session-id"));
    }
    
    @Test
    void assertCloseManagedSessionWhenTransportCloseGracefully() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        List<String> actualClosedSessionIds = new LinkedList<>();
        sessionManager.addSessionCloseListener(actualClosedSessionIds::add);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        AtomicReference<McpServerTransport> transport = new AtomicReference<>();
        when(sessionFactory.create(any(McpServerTransport.class))).thenAnswer(invocation -> {
            transport.set(invocation.getArgument(0, McpServerTransport.class));
            return session;
        });
        when(session.getId()).thenReturn("session-id");
        SessionManagedStdioTransportProvider provider = createProvider(sessionManager);
        provider.setSessionFactory(sessionFactory);
        transport.get().closeGracefully().block();
        assertThat(actualClosedSessionIds, is(List.of("session-id")));
        assertFalse(sessionManager.hasSession("session-id"));
    }
    
    @Test
    void assertSetSessionFactoryAfterTransportClose() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        List<String> actualClosedSessionIds = new LinkedList<>();
        sessionManager.addSessionCloseListener(actualClosedSessionIds::add);
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
        SessionManagedStdioTransportProvider provider = createProvider(sessionManager);
        provider.setSessionFactory(sessionFactory);
        firstTransport.get().close();
        provider.setSessionFactory(sessionFactory);
        assertThat(actualClosedSessionIds, is(List.of("session-id-1")));
        assertFalse(sessionManager.hasSession("session-id-1"));
        assertTrue(sessionManager.hasSession("session-id-2"));
        assertNotNull(secondTransport.get());
    }
    
    @Test
    void assertCloseManagedSessionWhenTransportClose() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        List<String> actualClosedSessionIds = new LinkedList<>();
        sessionManager.addSessionCloseListener(actualClosedSessionIds::add);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        AtomicReference<McpServerTransport> transport = new AtomicReference<>();
        when(sessionFactory.create(any(McpServerTransport.class))).thenAnswer(invocation -> {
            transport.set(invocation.getArgument(0, McpServerTransport.class));
            return session;
        });
        when(session.getId()).thenReturn("session-id");
        SessionManagedStdioTransportProvider provider = createProvider(sessionManager);
        provider.setSessionFactory(sessionFactory);
        transport.get().close();
        assertThat(actualClosedSessionIds, is(List.of("session-id")));
        assertFalse(sessionManager.hasSession("session-id"));
    }
    
    @Test
    void assertCloseManagedSessionOnlyOnceWhenTransportClosedRepeatedly() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        List<String> actualClosedSessionIds = new LinkedList<>();
        sessionManager.addSessionCloseListener(actualClosedSessionIds::add);
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        AtomicReference<McpServerTransport> transport = new AtomicReference<>();
        when(sessionFactory.create(any(McpServerTransport.class))).thenAnswer(invocation -> {
            transport.set(invocation.getArgument(0, McpServerTransport.class));
            return session;
        });
        when(session.getId()).thenReturn("session-id");
        SessionManagedStdioTransportProvider provider = createProvider(sessionManager);
        provider.setSessionFactory(sessionFactory);
        transport.get().close();
        transport.get().closeGracefully().block();
        assertThat(actualClosedSessionIds, is(List.of("session-id")));
        assertFalse(sessionManager.hasSession("session-id"));
    }
    
    private SessionManagedStdioTransportProvider createProvider(final MCPSessionManager sessionManager) {
        InputStream originalInput = System.in;
        try {
            System.setIn(InputStream.nullInputStream());
            return new SessionManagedStdioTransportProvider(sessionManager, MCPTransportJsonMapperFactory.create());
        } finally {
            System.setIn(originalInput);
        }
    }
}
