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
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.mcp.core.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StreamableHttpMCPServletTest {
    
    private static final String ACCEPT = "application/json, text/event-stream";
    
    @Test
    void assertProtocolVersions() throws ReflectiveOperationException {
        StreamableHttpMCPServlet actual = createServlet(mock(HttpServletStreamableServerTransportProvider.class), mock(MCPSessionManager.class),
                mock(MCPSessionExecutionCoordinator.class));
        assertThat(actual.protocolVersions(), is(MCPTransportConstants.SUPPORTED_PROTOCOL_VERSIONS));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("supportedProtocolVersions")
    void assertSetSessionFactoryWithSupportedProtocolVersion(final String name, final String requestedProtocolVersion) throws ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        McpStreamableServerSession.Factory sessionFactory = mock(McpStreamableServerSession.Factory.class);
        McpStreamableServerSession session = mock(McpStreamableServerSession.class);
        when(session.getId()).thenReturn("session-id");
        McpStreamableServerSession.McpStreamableServerSessionInit expectedInit = new McpStreamableServerSession.McpStreamableServerSessionInit(session,
                Mono.just(new InitializeResult(MCPTransportConstants.PROTOCOL_VERSION, McpSchema.ServerCapabilities.builder().tools(Boolean.FALSE).build(),
                        new McpSchema.Implementation(MCPTransportConstants.SERVER_NAME, "development"), "runtime")));
        when(sessionFactory.startSession(any(McpSchema.InitializeRequest.class))).thenReturn(expectedInit);
        StreamableHttpMCPServlet actual = createServlet(delegate, sessionManager, mock(MCPSessionExecutionCoordinator.class));
        actual.setSessionFactory(sessionFactory);
        ArgumentCaptor<McpStreamableServerSession.Factory> actualFactory = ArgumentCaptor.forClass(McpStreamableServerSession.Factory.class);
        verify(delegate).setSessionFactory(actualFactory.capture());
        McpSchema.InitializeRequest expectedInitializeRequest = new McpSchema.InitializeRequest(requestedProtocolVersion,
                new McpSchema.ClientCapabilities(Map.of(), null, null, null), new McpSchema.Implementation("foo_client", "1.0.0"));
        assertThat(actualFactory.getValue().startSession(expectedInitializeRequest), is(expectedInit));
        verify(sessionFactory).startSession(expectedInitializeRequest);
        verify(sessionManager).createSession("session-id");
    }
    
    private static Stream<Arguments> supportedProtocolVersions() {
        return Stream.of(
                Arguments.of("latest protocol version", MCPTransportConstants.PROTOCOL_VERSION),
                Arguments.of("compatible protocol version", ProtocolVersions.MCP_2025_06_18));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unsupportedProtocolVersions")
    void assertSetSessionFactoryWithNegotiatedProtocolVersion(final String name, final String requestedProtocolVersion) throws ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        McpStreamableServerSession.Factory sessionFactory = mock(McpStreamableServerSession.Factory.class);
        McpStreamableServerSession session = mock(McpStreamableServerSession.class);
        when(session.getId()).thenReturn("session-id");
        McpStreamableServerSession.McpStreamableServerSessionInit expectedInit = new McpStreamableServerSession.McpStreamableServerSessionInit(session,
                Mono.just(new InitializeResult(MCPTransportConstants.PROTOCOL_VERSION, McpSchema.ServerCapabilities.builder().tools(Boolean.FALSE).build(),
                        new McpSchema.Implementation(MCPTransportConstants.SERVER_NAME, "development"), "runtime")));
        when(sessionFactory.startSession(any(McpSchema.InitializeRequest.class))).thenReturn(expectedInit);
        StreamableHttpMCPServlet actual = createServlet(delegate, sessionManager, mock(MCPSessionExecutionCoordinator.class));
        actual.setSessionFactory(sessionFactory);
        ArgumentCaptor<McpStreamableServerSession.Factory> actualFactory = ArgumentCaptor.forClass(McpStreamableServerSession.Factory.class);
        verify(delegate).setSessionFactory(actualFactory.capture());
        McpSchema.InitializeRequest actualInitializeRequest = new McpSchema.InitializeRequest(requestedProtocolVersion,
                new McpSchema.ClientCapabilities(Map.of(), null, null, null), new McpSchema.Implementation("foo_client", "1.0.0"));
        actualFactory.getValue().startSession(actualInitializeRequest);
        ArgumentCaptor<McpSchema.InitializeRequest> negotiatedRequest = ArgumentCaptor.forClass(McpSchema.InitializeRequest.class);
        verify(sessionFactory).startSession(negotiatedRequest.capture());
        assertThat(negotiatedRequest.getValue().protocolVersion(), is(MCPTransportConstants.PROTOCOL_VERSION));
        assertThat(negotiatedRequest.getValue().capabilities(), is(actualInitializeRequest.capabilities()));
        assertThat(negotiatedRequest.getValue().clientInfo(), is(actualInitializeRequest.clientInfo()));
        assertThat(negotiatedRequest.getValue().meta(), is(actualInitializeRequest.meta()));
        verify(sessionManager).createSession("session-id");
    }
    
    private static Stream<Arguments> unsupportedProtocolVersions() {
        return Stream.of(
                Arguments.of("null protocol version", null),
                Arguments.of("blank protocol version", "  "),
                Arguments.of("unsupported protocol version", "2024-11-05"));
    }
    
    @Test
    void assertNotifyClients() throws ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        Mono<Void> expected = Mono.empty();
        when(delegate.notifyClients("notifications/tools/list_changed", Map.of("status", "ok"))).thenReturn(expected);
        StreamableHttpMCPServlet actual = createServlet(delegate, mock(MCPSessionManager.class), mock(MCPSessionExecutionCoordinator.class));
        assertThat(actual.notifyClients("notifications/tools/list_changed", Map.of("status", "ok")), is(expected));
    }
    
    @Test
    void assertNotifyClient() throws ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        Mono<Void> expected = Mono.empty();
        when(delegate.notifyClient("session-id", "notifications/resources/list_changed", Map.of("status", "ok"))).thenReturn(expected);
        StreamableHttpMCPServlet actual = createServlet(delegate, mock(MCPSessionManager.class), mock(MCPSessionExecutionCoordinator.class));
        assertThat(actual.notifyClient("session-id", "notifications/resources/list_changed", Map.of("status", "ok")), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("requestMethods")
    void assertServiceSetUtf8Encoding(final String name, final String requestMethod) throws ServletException, IOException, ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeader(HttpHeaders.MCP_SESSION_ID)).thenReturn(null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StreamableHttpMCPServlet actual = createServlet(delegate, mock(MCPSessionManager.class), mock(MCPSessionExecutionCoordinator.class));
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
    void assertServiceGetWithoutAcceptHeaderAndUtf8Encoding() throws ServletException, IOException, ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(delegate.closeGracefully()).thenReturn(Mono.empty());
        StreamableHttpMCPServlet actual = createServlet(delegate, mock(MCPSessionManager.class), mock(MCPSessionExecutionCoordinator.class));
        doAnswer(invocation -> assertDelegatedRequest(invocation.getArgument(0), invocation.getArgument(1), request, response))
                .when(delegate).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
        actual.service(request, response);
        verify(request).setCharacterEncoding("UTF-8");
        verify(response).setCharacterEncoding("UTF-8");
    }
    
    private Object assertDelegatedRequest(final HttpServletRequest actualRequest, final HttpServletResponse actualResponse, final HttpServletRequest request, final HttpServletResponse response) {
        assertThat(actualRequest, is(request));
        assertThat(actualResponse, is(response));
        assertThat(Thread.currentThread().getContextClassLoader(), is(StreamableHttpMCPServlet.class.getClassLoader()));
        return null;
    }
    
    @Test
    void assertServiceGetWithExistingAcceptHeader() throws ServletException, IOException, ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn("text/event-stream");
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(delegate.closeGracefully()).thenReturn(Mono.empty());
        StreamableHttpMCPServlet actual = createServlet(delegate, mock(MCPSessionManager.class), mock(MCPSessionExecutionCoordinator.class));
        doAnswer(invocation -> {
            assertThat(invocation.getArgument(0), is(request));
            assertThat(((HttpServletRequest) invocation.getArgument(0)).getHeader(HttpHeaders.ACCEPT), is("text/event-stream"));
            return null;
        }).when(delegate).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
        actual.service(request, response);
        verify(request).setCharacterEncoding("UTF-8");
        verify(response).setCharacterEncoding("UTF-8");
    }
    
    @Test
    void assertServicePostWithSessionHeaderSet() throws ServletException, IOException, ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(delegate.closeGracefully()).thenReturn(Mono.empty());
        StreamableHttpMCPServlet actual = createServlet(delegate, mock(MCPSessionManager.class), mock(MCPSessionExecutionCoordinator.class));
        doAnswer(invocation -> {
            ((HttpServletResponse) invocation.getArgument(1)).setHeader(HttpHeaders.MCP_SESSION_ID, "session-id");
            return null;
        }).when(delegate).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
        actual.service(request, response);
        verify(response).setHeader(HttpHeaders.MCP_SESSION_ID, "session-id");
        verify(response).setHeader(HttpHeaders.PROTOCOL_VERSION, MCPTransportConstants.PROTOCOL_VERSION);
    }
    
    @Test
    void assertServicePostWithJsonContentType() throws ServletException, IOException, ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/json; charset=UTF-8");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpServletResponse response = mock(HttpServletResponse.class);
        StreamableHttpMCPServlet actual = createServlet(delegate, mock(MCPSessionManager.class), mock(MCPSessionExecutionCoordinator.class));
        actual.service(request, response);
        verify(delegate).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }
    
    @Test
    void assertServicePostRejectUnsupportedContentType() throws ServletException, IOException, ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("text/plain");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StreamableHttpMCPServlet actual = createServlet(delegate, mock(MCPSessionManager.class), mock(MCPSessionExecutionCoordinator.class));
        actual.service(request, response);
        verify(response).sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Content-Type must be application/json.");
        verify(delegate, never()).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }
    
    @Test
    void assertServicePostWithNegotiatedProtocolHeader() throws ServletException, IOException, ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        McpStreamableServerSession.Factory sessionFactory = mock(McpStreamableServerSession.Factory.class);
        McpStreamableServerSession session = mock(McpStreamableServerSession.class);
        when(session.getId()).thenReturn("session-id");
        when(sessionFactory.startSession(any(McpSchema.InitializeRequest.class))).thenReturn(new McpStreamableServerSession.McpStreamableServerSessionInit(session,
                Mono.just(new InitializeResult(ProtocolVersions.MCP_2025_06_18, McpSchema.ServerCapabilities.builder().tools(Boolean.FALSE).build(),
                        new McpSchema.Implementation(MCPTransportConstants.SERVER_NAME, "development"), "runtime"))));
        StreamableHttpMCPServlet actual = createServlet(delegate, sessionManager, mock(MCPSessionExecutionCoordinator.class));
        actual.setSessionFactory(sessionFactory);
        ArgumentCaptor<McpStreamableServerSession.Factory> actualFactory = ArgumentCaptor.forClass(McpStreamableServerSession.Factory.class);
        verify(delegate).setSessionFactory(actualFactory.capture());
        actualFactory.getValue().startSession(new McpSchema.InitializeRequest(ProtocolVersions.MCP_2025_06_18,
                new McpSchema.ClientCapabilities(Map.of(), null, null, null), new McpSchema.Implementation("foo_client", "1.0.0")));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpServletResponse response = mock(HttpServletResponse.class);
        doAnswer(invocation -> {
            ((HttpServletResponse) invocation.getArgument(1)).setHeader(HttpHeaders.MCP_SESSION_ID, "session-id");
            return null;
        }).when(delegate).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
        actual.service(request, response);
        verify(response).setHeader(HttpHeaders.PROTOCOL_VERSION, ProtocolVersions.MCP_2025_06_18);
    }
    
    @Test
    void assertServicePostWithSessionHeaderAdded() throws ServletException, IOException, ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(delegate.closeGracefully()).thenReturn(Mono.empty());
        StreamableHttpMCPServlet actual = createServlet(delegate, mock(MCPSessionManager.class), mock(MCPSessionExecutionCoordinator.class));
        doAnswer(invocation -> {
            ((HttpServletResponse) invocation.getArgument(1)).addHeader(HttpHeaders.MCP_SESSION_ID, "session-id");
            return null;
        }).when(delegate).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
        actual.service(request, response);
        verify(response).addHeader(HttpHeaders.MCP_SESSION_ID, "session-id");
        verify(response).setHeader(HttpHeaders.PROTOCOL_VERSION, MCPTransportConstants.PROTOCOL_VERSION);
    }
    
    @Test
    void assertServicePostWithoutSessionHeader() throws ServletException, IOException, ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(delegate.closeGracefully()).thenReturn(Mono.empty());
        StreamableHttpMCPServlet actual = createServlet(delegate, mock(MCPSessionManager.class), mock(MCPSessionExecutionCoordinator.class));
        doAnswer(invocation -> {
            HttpServletResponse actualResponse = invocation.getArgument(1);
            actualResponse.setHeader("X-Test", "value");
            actualResponse.addHeader("X-Trace", "value");
            return null;
        }).when(delegate).service(any(HttpServletRequest.class), any(HttpServletResponse.class));
        actual.service(request, response);
        verify(response).setHeader("X-Test", "value");
        verify(response).addHeader("X-Trace", "value");
        verify(response, never()).setHeader(HttpHeaders.PROTOCOL_VERSION, MCPTransportConstants.PROTOCOL_VERSION);
    }
    
    @Test
    void assertServiceDeleteCloseSessionWhenStatusOk() throws ServletException, IOException, ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        MCPSessionExecutionCoordinator sessionExecutionCoordinator = mock(MCPSessionExecutionCoordinator.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getHeader(HttpHeaders.MCP_SESSION_ID)).thenReturn("session-id");
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(delegate.closeGracefully()).thenReturn(Mono.empty());
        StreamableHttpMCPServlet actual = createServlet(delegate, mock(MCPSessionManager.class), sessionExecutionCoordinator);
        actual.service(request, response);
        verify(sessionExecutionCoordinator).closeSession("session-id");
    }
    
    @Test
    void assertServiceDeleteSkipCloseSessionWhenStatusNotOk() throws ServletException, IOException, ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        MCPSessionExecutionCoordinator sessionExecutionCoordinator = mock(MCPSessionExecutionCoordinator.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(ACCEPT);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getHeader(HttpHeaders.MCP_SESSION_ID)).thenReturn("session-id");
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(404);
        when(delegate.closeGracefully()).thenReturn(Mono.empty());
        StreamableHttpMCPServlet actual = createServlet(delegate, mock(MCPSessionManager.class), sessionExecutionCoordinator);
        actual.service(request, response);
        verify(sessionExecutionCoordinator, never()).closeSession("session-id");
    }
    
    @Test
    void assertCloseGracefully() throws ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        MCPSessionExecutionCoordinator sessionExecutionCoordinator = mock(MCPSessionExecutionCoordinator.class);
        when(delegate.closeGracefully()).thenReturn(Mono.empty());
        StreamableHttpMCPServlet actual = createServlet(delegate, mock(MCPSessionManager.class), sessionExecutionCoordinator);
        assertDoesNotThrow(() -> actual.closeGracefully().block());
        assertDoesNotThrow(() -> actual.closeGracefully().block());
        verify(delegate).closeGracefully();
        verify(sessionExecutionCoordinator).closeAllSessions();
    }
    
    @Test
    void assertDestroy() throws ReflectiveOperationException {
        HttpServletStreamableServerTransportProvider delegate = mock(HttpServletStreamableServerTransportProvider.class);
        MCPSessionExecutionCoordinator sessionExecutionCoordinator = mock(MCPSessionExecutionCoordinator.class);
        when(delegate.closeGracefully()).thenReturn(Mono.empty());
        StreamableHttpMCPServlet actual = createServlet(delegate, mock(MCPSessionManager.class), sessionExecutionCoordinator);
        actual.destroy();
        verify(delegate).closeGracefully();
        verify(sessionExecutionCoordinator).closeAllSessions();
    }
    
    private StreamableHttpMCPServlet createServlet(final HttpServletStreamableServerTransportProvider delegate, final MCPSessionManager sessionManager,
                                                   final MCPSessionExecutionCoordinator sessionExecutionCoordinator) throws ReflectiveOperationException {
        return createServlet(delegate, sessionManager, sessionExecutionCoordinator,
                new HttpTransportConfiguration("127.0.0.1", 18088, "/mcp"));
    }
    
    private StreamableHttpMCPServlet createServlet(final HttpServletStreamableServerTransportProvider delegate, final MCPSessionManager sessionManager,
                                                   final MCPSessionExecutionCoordinator sessionExecutionCoordinator, final HttpTransportConfiguration config) throws ReflectiveOperationException {
        StreamableHttpMCPServlet result = new StreamableHttpMCPServlet(sessionManager, MCPTransportJsonMapperFactory.create(), config);
        setField(result, "delegate", delegate);
        setField(result, "sessionExecutionCoordinator", sessionExecutionCoordinator);
        return result;
    }
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
    }
}
