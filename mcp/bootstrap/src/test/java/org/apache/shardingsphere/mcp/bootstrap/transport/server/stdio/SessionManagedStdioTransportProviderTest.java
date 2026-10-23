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

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCNotification;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerTransport;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
    
    @Test
    void assertSendMessage() throws IOException {
        McpJsonMapper jsonMapper = mock(McpJsonMapper.class);
        JSONRPCNotification message = new JSONRPCNotification("2.0", "foo_method", Map.of());
        String expectedMessage = "{\"jsonrpc\":\"2.0\",\"method\":\"foo_method\",\"params\":{}}";
        when(jsonMapper.writeValueAsString(message)).thenReturn(expectedMessage);
        ByteArrayOutputStream actualOutput = new ByteArrayOutputStream();
        try (PipedOutputStream inputWriter = new PipedOutputStream(); PipedInputStream input = new PipedInputStream(inputWriter); PrintStream output = new PrintStream(actualOutput)) {
            InputStream originalInput = System.in;
            PrintStream originalOutput = System.out;
            SessionManagedStdioTransportProvider provider;
            try {
                System.setIn(input);
                System.setOut(output);
                provider = new SessionManagedStdioTransportProvider(new MCPSessionManager(Map.of()), jsonMapper);
            } finally {
                System.setIn(originalInput);
                System.setOut(originalOutput);
            }
            McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
            McpServerSession session = mock(McpServerSession.class);
            when(sessionFactory.create(any(McpServerTransport.class))).thenReturn(session);
            when(session.getId()).thenReturn("foo_session");
            provider.setSessionFactory(sessionFactory);
            ArgumentCaptor<McpServerTransport> transportCaptor = ArgumentCaptor.forClass(McpServerTransport.class);
            verify(sessionFactory).create(transportCaptor.capture());
            try {
                transportCaptor.getValue().sendMessage(message).block(Duration.ofSeconds(5L));
                String expectedOutput = expectedMessage + "\n";
                String actual = Flux.interval(Duration.ofMillis(10L)).map(ignored -> actualOutput.toString(StandardCharsets.UTF_8))
                        .filter(expectedOutput::equals).next().block(Duration.ofSeconds(5L));
                assertThat(actual, is(expectedOutput));
                verify(jsonMapper).writeValueAsString(message);
            } finally {
                input.close();
                transportCaptor.getValue().closeGracefully().block(Duration.ofSeconds(5L));
            }
        }
    }
    
    @Test
    void assertUnmarshalFrom() {
        McpJsonMapper jsonMapper = mock(McpJsonMapper.class);
        Map<String, Object> data = Map.of("foo_key", "foo_value");
        TypeRef<Map<String, Object>> typeRef = new TypeRef<>() {
        };
        Map<String, Object> expected = Map.of("foo_result", "foo_value");
        when(jsonMapper.convertValue(data, typeRef)).thenReturn(expected);
        InputStream originalInput = System.in;
        SessionManagedStdioTransportProvider provider;
        try {
            System.setIn(InputStream.nullInputStream());
            provider = new SessionManagedStdioTransportProvider(new MCPSessionManager(Map.of()), jsonMapper);
        } finally {
            System.setIn(originalInput);
        }
        McpServerSession.Factory sessionFactory = mock(McpServerSession.Factory.class);
        McpServerSession session = mock(McpServerSession.class);
        when(sessionFactory.create(any(McpServerTransport.class))).thenReturn(session);
        when(session.getId()).thenReturn("foo_session");
        provider.setSessionFactory(sessionFactory);
        ArgumentCaptor<McpServerTransport> transportCaptor = ArgumentCaptor.forClass(McpServerTransport.class);
        verify(sessionFactory).create(transportCaptor.capture());
        try {
            Map<String, Object> actual = transportCaptor.getValue().unmarshalFrom(data, typeRef);
            assertThat(actual, sameInstance(expected));
        } finally {
            transportCaptor.getValue().closeGracefully().block(Duration.ofSeconds(5L));
        }
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
