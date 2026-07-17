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

import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.HttpHeaders;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.InitializeResult;
import io.modelcontextprotocol.spec.McpStreamableServerSession;
import io.modelcontextprotocol.spec.ProtocolVersions;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.SessionAttributionSourceConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StreamableHttpMCPServletTest {
    
    private static final String ACCEPT = "application/json, text/event-stream";
    
    private MockedConstruction<HttpServletStreamableServerTransportProvider> mockedDelegates;
    
    @BeforeEach
    void setUp() {
        mockedDelegates = mockConstruction(HttpServletStreamableServerTransportProvider.class,
                (mock, context) -> when(mock.closeGracefully()).thenReturn(Mono.empty()));
    }
    
    @AfterEach
    void tearDown() {
        mockedDelegates.close();
    }
    
    @Test
    void assertProtocolVersions() {
        StreamableHttpMCPServlet actual = createServlet(mock(MCPSessionManager.class));
        assertThat(actual.protocolVersions(), is(MCPTransportConstants.SUPPORTED_PROTOCOL_VERSIONS));
    }
    
    @Test
    void assertSetSessionFactoryWithSupportedProtocolVersion() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        McpStreamableServerSession.Factory sessionFactory = mock(McpStreamableServerSession.Factory.class);
        McpStreamableServerSession session = mock(McpStreamableServerSession.class);
        McpStreamableServerSession.McpStreamableServerSessionInit expectedInit = new McpStreamableServerSession.McpStreamableServerSessionInit(session,
                Mono.just(new InitializeResult(MCPTransportConstants.PROTOCOL_VERSION, McpSchema.ServerCapabilities.builder().tools(Boolean.FALSE).build(),
                        new McpSchema.Implementation(MCPTransportConstants.SERVER_NAME, "development"), "runtime")));
        when(sessionFactory.startSession(any(McpSchema.InitializeRequest.class))).thenReturn(expectedInit);
        StreamableHttpMCPServlet actual = createServlet(sessionManager);
        actual.setSessionFactory(sessionFactory);
        ArgumentCaptor<McpStreamableServerSession.Factory> actualFactory = ArgumentCaptor.forClass(McpStreamableServerSession.Factory.class);
        verify(getDelegate()).setSessionFactory(actualFactory.capture());
        McpSchema.InitializeRequest expectedInitializeRequest = new McpSchema.InitializeRequest(MCPTransportConstants.PROTOCOL_VERSION,
                new McpSchema.ClientCapabilities(Map.of(), null, null, null), new McpSchema.Implementation("foo_client", "1.0.0"));
        assertThat(actualFactory.getValue().startSession(expectedInitializeRequest), is(expectedInit));
        verify(sessionFactory).startSession(expectedInitializeRequest);
        verify(sessionManager, never()).createSession(any());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unsupportedProtocolVersions")
    void assertSetSessionFactoryWithNegotiatedProtocolVersion(final String name, final String requestedProtocolVersion) {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        McpStreamableServerSession.Factory sessionFactory = mock(McpStreamableServerSession.Factory.class);
        McpStreamableServerSession session = mock(McpStreamableServerSession.class);
        McpStreamableServerSession.McpStreamableServerSessionInit expectedInit = new McpStreamableServerSession.McpStreamableServerSessionInit(session,
                Mono.just(new InitializeResult(MCPTransportConstants.PROTOCOL_VERSION, McpSchema.ServerCapabilities.builder().tools(Boolean.FALSE).build(),
                        new McpSchema.Implementation(MCPTransportConstants.SERVER_NAME, "development"), "runtime")));
        when(sessionFactory.startSession(any(McpSchema.InitializeRequest.class))).thenReturn(expectedInit);
        StreamableHttpMCPServlet actual = createServlet(sessionManager);
        actual.setSessionFactory(sessionFactory);
        ArgumentCaptor<McpStreamableServerSession.Factory> actualFactory = ArgumentCaptor.forClass(McpStreamableServerSession.Factory.class);
        verify(getDelegate()).setSessionFactory(actualFactory.capture());
        McpSchema.InitializeRequest actualInitializeRequest = new McpSchema.InitializeRequest(requestedProtocolVersion,
                new McpSchema.ClientCapabilities(Map.of(), null, null, null), new McpSchema.Implementation("foo_client", "1.0.0"));
        actualFactory.getValue().startSession(actualInitializeRequest);
        ArgumentCaptor<McpSchema.InitializeRequest> negotiatedRequest = ArgumentCaptor.forClass(McpSchema.InitializeRequest.class);
        verify(sessionFactory).startSession(negotiatedRequest.capture());
        assertThat(negotiatedRequest.getValue().protocolVersion(), is(MCPTransportConstants.PROTOCOL_VERSION));
        assertThat(negotiatedRequest.getValue().capabilities(), is(actualInitializeRequest.capabilities()));
        assertThat(negotiatedRequest.getValue().clientInfo(), is(actualInitializeRequest.clientInfo()));
        assertThat(negotiatedRequest.getValue().meta(), is(actualInitializeRequest.meta()));
        verify(sessionManager, never()).createSession(any());
    }
    
    private static Stream<Arguments> unsupportedProtocolVersions() {
        return Stream.of(
                Arguments.of("null protocol version", null),
                Arguments.of("blank protocol version", "  "),
                Arguments.of("legacy protocol version", ProtocolVersions.MCP_2025_06_18),
                Arguments.of("unsupported protocol version", "2024-11-05"));
    }
    
    @Test
    void assertNotifyClients() {
        StreamableHttpMCPServlet actual = createServlet(mock(MCPSessionManager.class));
        HttpServletStreamableServerTransportProvider delegate = getDelegate();
        Mono<Void> expected = Mono.empty();
        when(delegate.notifyClients("notifications/tools/list_changed", Map.of("status", "ok"))).thenReturn(expected);
        assertThat(actual.notifyClients("notifications/tools/list_changed", Map.of("status", "ok")), is(expected));
    }
    
    @Test
    void assertNotifyClient() {
        StreamableHttpMCPServlet actual = createServlet(mock(MCPSessionManager.class));
        HttpServletStreamableServerTransportProvider delegate = getDelegate();
        Mono<Void> expected = Mono.empty();
        when(delegate.notifyClient("session-id", "notifications/resources/list_changed", Map.of("status", "ok"))).thenReturn(expected);
        assertThat(actual.notifyClient("session-id", "notifications/resources/list_changed", Map.of("status", "ok")), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("requestMethods")
    void assertServiceSetUtf8Encoding(final String name, final String requestMethod) throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeader(HttpHeaders.MCP_SESSION_ID)).thenReturn(null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StreamableHttpMCPServlet actual = createServlet(mock(MCPSessionManager.class));
        actual.service(request, response);
        verify(request).setCharacterEncoding("UTF-8");
        verify(response).setCharacterEncoding("UTF-8");
    }
    
    private static Stream<Arguments> requestMethods() {
        return Stream.of(
                Arguments.of("get request", "GET"),
                Arguments.of("post request", "POST"),
                Arguments.of("delete request", "DELETE"));
    }
    
    @Test
    void assertServiceGetWithoutAcceptHeaderAndUtf8Encoding() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StreamableHttpMCPServlet actual = createServlet(mock(MCPSessionManager.class));
        actual.service(request, response);
        verify(request).setCharacterEncoding("UTF-8");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Accept must include text/event-stream.");
        verify(getDelegate(), never()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }
    
    @Test
    void assertServiceGetRejectsOriginBeforeAccept() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of("Origin")));
        when(request.getHeaders("Origin")).thenReturn(Collections.enumeration(List.of("https://example.com")));
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
        createServlet(mock(MCPSessionManager.class)).service(request, response);
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, never()).sendError(HttpServletResponse.SC_BAD_REQUEST, "Accept must include text/event-stream.");
        verify(getDelegate(), never()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }
    
    @Test
    void assertRejectUnsupportedEventStreamGet() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn("text/event-stream");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StreamableHttpMCPServlet actual = createServlet(mock(MCPSessionManager.class));
        actual.service(request, response);
        verify(request).setCharacterEncoding("UTF-8");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP GET event streams are not supported.");
        verify(getDelegate(), never()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }
    
    @Test
    void assertServicePostWithSessionHeaderSet() throws ServletException, IOException {
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);
        doAnswer(invocation -> {
            if (HttpHeaders.MCP_SESSION_ID.equals(invocation.getArgument(0))) {
                assertTrue(sessionManager.hasSession(invocation.getArgument(1)));
            }
            return null;
        }).when(response).setHeader(anyString(), anyString());
        StreamableHttpMCPServlet actual = createServlet(sessionManager);
        doAnswer(invocation -> {
            ((HttpServletResponse) invocation.getArgument(1)).setHeader(HttpHeaders.MCP_SESSION_ID, "session-id");
            return null;
        }).when(getDelegate()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
        actual.service(request, response);
        verify(response).setHeader(HttpHeaders.MCP_SESSION_ID, "session-id");
        verify(response).setHeader(HttpHeaders.PROTOCOL_VERSION, MCPTransportConstants.PROTOCOL_VERSION);
        assertSessionIdentity(sessionManager.getRequiredSessionIdentity("session-id"), "", "", Map.of());
    }
    
    @Test
    void assertServicePostRegisterAttributedSession() throws ServletException, IOException {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenAnswer(ignored -> Collections.enumeration(List.of("X-Test-Subject", "X-Test-Source", "X-Test-ATTR-Region")));
        when(request.getHeader("X-Test-Subject")).thenReturn("subject");
        when(request.getHeader("X-Test-Source")).thenReturn("gateway");
        when(request.getHeader("X-Test-ATTR-Region")).thenReturn("ap-south");
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);
        HttpTransportConfiguration config = new HttpTransportConfiguration("127.0.0.1", 18088, "/mcp",
                new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-"));
        StreamableHttpMCPServlet actual = createServlet(sessionManager, config);
        doAnswer(invocation -> {
            ((HttpServletResponse) invocation.getArgument(1)).setHeader(HttpHeaders.MCP_SESSION_ID, "session-id");
            return null;
        }).when(getDelegate()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
        actual.service(request, response);
        ArgumentCaptor<MCPSessionIdentity> sessionIdentity = ArgumentCaptor.forClass(MCPSessionIdentity.class);
        verify(sessionManager).createSession(sessionIdentity.capture());
        assertSessionIdentity(sessionIdentity.getValue(), "subject", "gateway", Map.of("region", "ap-south"));
    }
    
    @Test
    void assertServicePostRollbackSessionWhenInitializationFails() throws ServletException, IOException {
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        StreamableHttpMCPServlet actual = createServlet(sessionManager);
        doAnswer(invocation -> {
            ((HttpServletResponse) invocation.getArgument(1)).setHeader(HttpHeaders.MCP_SESSION_ID, "session-id");
            return null;
        }).when(getDelegate()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
        actual.service(request, response);
        assertFalse(sessionManager.hasSession("session-id"));
    }
    
    @Test
    void assertServicePostWithJsonContentType() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/json; charset=UTF-8");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);
        StreamableHttpMCPServlet actual = createServlet(mock(MCPSessionManager.class));
        actual.service(request, response);
        verify(getDelegate()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }
    
    @Test
    void assertServicePostRejectUnsupportedContentType() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("text/plain");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StreamableHttpMCPServlet actual = createServlet(mock(MCPSessionManager.class));
        actual.service(request, response);
        verify(response).sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Content-Type must be application/json.");
        verify(getDelegate(), never()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }
    
    @Test
    void assertServicePostRejectsOriginBeforeContentType() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("text/plain");
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of("Origin")));
        when(request.getHeaders("Origin")).thenReturn(Collections.enumeration(List.of("https://example.com")));
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
        createServlet(mock(MCPSessionManager.class)).service(request, response);
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, never()).sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Content-Type must be application/json.");
        verify(getDelegate(), never()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }
    
    @Test
    void assertServicePostWithNegotiatedProtocolHeader() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);
        StreamableHttpMCPServlet actual = createServlet(mock(MCPSessionManager.class));
        doAnswer(invocation -> {
            ((HttpServletResponse) invocation.getArgument(1)).setHeader(HttpHeaders.MCP_SESSION_ID, "session-id");
            return null;
        }).when(getDelegate()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
        actual.service(request, response);
        verify(response).setHeader(HttpHeaders.PROTOCOL_VERSION, MCPTransportConstants.PROTOCOL_VERSION);
    }
    
    @Test
    void assertServicePostWithSessionHeaderAdded() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);
        StreamableHttpMCPServlet actual = createServlet(mock(MCPSessionManager.class));
        doAnswer(invocation -> {
            ((HttpServletResponse) invocation.getArgument(1)).addHeader(HttpHeaders.MCP_SESSION_ID, "session-id");
            return null;
        }).when(getDelegate()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
        actual.service(request, response);
        verify(response).addHeader(HttpHeaders.MCP_SESSION_ID, "session-id");
        verify(response).setHeader(HttpHeaders.PROTOCOL_VERSION, MCPTransportConstants.PROTOCOL_VERSION);
    }
    
    @Test
    void assertServicePostWithoutSessionHeader() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpServletResponse response = mock(HttpServletResponse.class);
        StreamableHttpMCPServlet actual = createServlet(mock(MCPSessionManager.class));
        doAnswer(invocation -> {
            HttpServletResponse actualResponse = invocation.getArgument(1);
            actualResponse.setHeader("X-Test", "value");
            actualResponse.addHeader("X-Trace", "value");
            return null;
        }).when(getDelegate()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
        actual.service(request, response);
        verify(response).setHeader("X-Test", "value");
        verify(response).addHeader("X-Trace", "value");
        verify(response, never()).setHeader(HttpHeaders.PROTOCOL_VERSION, MCPTransportConstants.PROTOCOL_VERSION);
    }
    
    @Test
    void assertServiceDeleteCloseSessionWhenStatusOk() throws ServletException, IOException {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        List<String> actualClosedSessionIds = new LinkedList<>();
        sessionManager.addSessionCloseListener(actualClosedSessionIds::add);
        sessionManager.createSession(new MCPSessionIdentity("session-id", "", "", Map.of()));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getHeader(HttpHeaders.MCP_SESSION_ID)).thenReturn("session-id");
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(200);
        StreamableHttpMCPServlet actual = createServlet(sessionManager);
        actual.service(request, response);
        assertThat(actualClosedSessionIds, is(List.of("session-id")));
        assertFalse(sessionManager.hasSession("session-id"));
    }
    
    @Test
    void assertServiceDeleteSkipCloseSessionWhenStatusNotOk() throws ServletException, IOException {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        sessionManager.createSession(new MCPSessionIdentity("session-id", "", "", Map.of()));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getHeader(HttpHeaders.MCP_SESSION_ID)).thenReturn("session-id");
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(404);
        StreamableHttpMCPServlet actual = createServlet(sessionManager);
        actual.service(request, response);
        assertTrue(sessionManager.hasSession("session-id"));
    }
    
    @Test
    void assertCloseGracefully() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        List<String> actualClosedSessionIds = new LinkedList<>();
        sessionManager.addSessionCloseListener(actualClosedSessionIds::add);
        sessionManager.createSession(new MCPSessionIdentity("session-id", "", "", Map.of()));
        StreamableHttpMCPServlet actual = createServlet(sessionManager);
        assertDoesNotThrow(() -> actual.closeGracefully().block());
        assertDoesNotThrow(() -> actual.closeGracefully().block());
        verify(getDelegate()).closeGracefully();
        assertThat(actualClosedSessionIds, is(List.of("session-id")));
    }
    
    @Test
    void assertCloseGracefullyWaitsForActiveRequest() throws IOException, ServletException, InterruptedException {
        CountDownLatch requestEntered = new CountDownLatch(1);
        CountDownLatch releaseRequest = new CountDownLatch(1);
        CountDownLatch closeSubscriptionStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getMethod()).thenReturn("POST");
            when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
            when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
            HttpServletResponse response = mock(HttpServletResponse.class);
            StreamableHttpMCPServlet actual = createServlet(new MCPSessionManager(Map.of()));
            doAnswer(invocation -> {
                requestEntered.countDown();
                assertTrue(releaseRequest.await(5L, TimeUnit.SECONDS));
                return null;
            }).when(getDelegate()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
            Future<?> requestFuture = executor.submit(() -> {
                actual.service(request, response);
                return null;
            });
            assertTrue(requestEntered.await(5L, TimeUnit.SECONDS));
            assertFalse(requestFuture.isDone());
            Mono<Void> closeOperation = actual.closeGracefully();
            Future<?> closeFuture = executor.submit(() -> {
                closeSubscriptionStarted.countDown();
                closeOperation.block();
                return null;
            });
            assertTrue(closeSubscriptionStarted.await(5L, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> closeFuture.get(100L, TimeUnit.MILLISECONDS));
            releaseRequest.countDown();
            assertDoesNotThrow(() -> requestFuture.get(5L, TimeUnit.SECONDS));
            assertDoesNotThrow(() -> closeFuture.get(5L, TimeUnit.SECONDS));
        } finally {
            releaseRequest.countDown();
            executor.shutdownNow();
        }
    }
    
    @Test
    void assertRejectRequestAfterClose() throws ServletException, IOException {
        StreamableHttpMCPServlet actual = createServlet(new MCPSessionManager(Map.of()));
        actual.closeGracefully().block();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        HttpServletResponse response = mock(HttpServletResponse.class);
        actual.service(request, response);
        verify(response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Server is shutting down");
        verify(getDelegate(), never()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }
    
    @Test
    void assertDestroy() {
        MCPSessionManager sessionManager = new MCPSessionManager(Collections.emptyMap());
        List<String> actualClosedSessionIds = new LinkedList<>();
        sessionManager.addSessionCloseListener(actualClosedSessionIds::add);
        sessionManager.createSession(new MCPSessionIdentity("session-id", "", "", Map.of()));
        StreamableHttpMCPServlet actual = createServlet(sessionManager);
        actual.destroy();
        verify(getDelegate()).closeGracefully();
        assertThat(actualClosedSessionIds, is(List.of("session-id")));
    }
    
    private void assertSessionIdentity(final MCPSessionIdentity actual, final String subject, final String source, final Map<String, String> attributes) {
        assertThat(actual.getSessionId(), is("session-id"));
        assertThat(actual.getSubject(), is(subject));
        assertThat(actual.getSource(), is(source));
        assertThat(actual.getAttributes(), is(attributes));
    }
    
    private StreamableHttpMCPServlet createServlet(final MCPSessionManager sessionManager) {
        return createServlet(sessionManager, new HttpTransportConfiguration("127.0.0.1", 18088, "/mcp"));
    }
    
    private StreamableHttpMCPServlet createServlet(final MCPSessionManager sessionManager, final HttpTransportConfiguration config) {
        return new StreamableHttpMCPServlet(sessionManager, MCPTransportJsonMapperFactory.create(), config);
    }
    
    private HttpServletStreamableServerTransportProvider getDelegate() {
        return mockedDelegates.constructed().getFirst();
    }
}
