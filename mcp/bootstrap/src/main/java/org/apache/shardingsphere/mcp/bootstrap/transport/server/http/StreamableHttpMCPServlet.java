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

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.HttpHeaders;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStreamableServerSession;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.ServerTransportSecurityValidatorFactory;
import org.apache.shardingsphere.mcp.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

final class StreamableHttpMCPServlet extends HttpServlet implements McpStreamableServerTransportProvider {
    
    private static final long serialVersionUID = -2320345528569140021L;
    
    private static final String SESSION_HEADER = HttpHeaders.MCP_SESSION_ID;
    
    private static final String PROTOCOL_HEADER = HttpHeaders.PROTOCOL_VERSION;
    
    private static final String ACCEPT_HEADER = HttpHeaders.ACCEPT;
    
    private static final String DEFAULT_ACCEPT = "application/json, text/event-stream";
    
    private final HttpServletStreamableServerTransportProvider delegate;
    
    private final MCPSessionManager sessionManager;
    
    private final MCPSessionExecutionCoordinator sessionExecutionCoordinator;
    
    private final AtomicBoolean closed;
    
    StreamableHttpMCPServlet(final MCPSessionManager sessionManager, final McpJsonMapper jsonMapper, final String bindHost, final String accessToken, final String endpointPath) {
        this(createDelegate(sessionManager, jsonMapper, bindHost, accessToken, endpointPath), sessionManager, new MCPSessionExecutionCoordinator(sessionManager));
    }
    
    StreamableHttpMCPServlet(final HttpServletStreamableServerTransportProvider delegate, final MCPSessionManager sessionManager,
                             final MCPSessionExecutionCoordinator sessionExecutionCoordinator) {
        this.delegate = delegate;
        this.sessionManager = sessionManager;
        this.sessionExecutionCoordinator = sessionExecutionCoordinator;
        closed = new AtomicBoolean();
    }
    
    private static HttpServletStreamableServerTransportProvider createDelegate(final MCPSessionManager sessionManager, final McpJsonMapper jsonMapper,
                                                                               final String bindHost, final String accessToken, final String endpointPath) {
        return HttpServletStreamableServerTransportProvider.builder().jsonMapper(jsonMapper).mcpEndpoint(endpointPath)
                .securityValidator(ServerTransportSecurityValidatorFactory.create(sessionManager, bindHost, accessToken)).build();
    }
    
    @Override
    public List<String> protocolVersions() {
        return MCPTransportConstants.SUPPORTED_PROTOCOL_VERSIONS;
    }
    
    @Override
    public void setSessionFactory(final McpStreamableServerSession.Factory sessionFactory) {
        delegate.setSessionFactory(initializeRequest -> {
            McpSchema.InitializeRequest actualInitializeRequest = negotiateInitializeRequest(initializeRequest);
            McpStreamableServerSession.McpStreamableServerSessionInit result = sessionFactory.startSession(actualInitializeRequest);
            sessionManager.createSession(result.session().getId());
            return result;
        });
    }
    
    private McpSchema.InitializeRequest negotiateInitializeRequest(final McpSchema.InitializeRequest initializeRequest) {
        String negotiatedProtocolVersion = negotiateProtocolVersion(initializeRequest.protocolVersion());
        return negotiatedProtocolVersion.equals(initializeRequest.protocolVersion())
                ? initializeRequest
                : new McpSchema.InitializeRequest(negotiatedProtocolVersion, initializeRequest.capabilities(), initializeRequest.clientInfo(), initializeRequest.meta());
    }
    
    private String negotiateProtocolVersion(final String requestedProtocolVersion) {
        String actualRequestedProtocolVersion = Objects.toString(requestedProtocolVersion, "").trim();
        return protocolVersions().contains(actualRequestedProtocolVersion) ? actualRequestedProtocolVersion : MCPTransportConstants.PROTOCOL_VERSION;
    }
    
    @Override
    public Mono<Void> notifyClients(final String method, final Object params) {
        return delegate.notifyClients(method, params);
    }
    
    @Override
    public Mono<Void> notifyClient(final String sessionId, final String method, final Object params) {
        return delegate.notifyClient(sessionId, method, params);
    }
    
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        serviceRequest(withDefaultAcceptHeader(request), response);
    }
    
    private void serviceWithApplicationClassLoader(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            delegate.service(request, response);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
    
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        serviceRequest(withDefaultAcceptHeader(request), withInitializeProtocolHeader(response));
    }
    
    private void serviceRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        setUtf8Encoding(request, response);
        serviceWithApplicationClassLoader(request, response);
    }
    
    @Override
    protected void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        String sessionId = Objects.toString(request.getHeader(SESSION_HEADER), "").trim();
        serviceRequest(withDefaultAcceptHeader(request), response);
        if (200 == response.getStatus()) {
            sessionExecutionCoordinator.closeSession(sessionId);
        }
    }
    
    private void setUtf8Encoding(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    }
    
    @Override
    public Mono<Void> closeGracefully() {
        return closed.compareAndSet(false, true) ? delegate.closeGracefully().doFinally(ignored -> sessionExecutionCoordinator.closeAllSessions()) : Mono.empty();
    }
    
    @Override
    public void destroy() {
        try {
            closeGracefully().block();
        } finally {
            super.destroy();
        }
    }
    
    private HttpServletRequest withDefaultAcceptHeader(final HttpServletRequest request) {
        if (!Objects.toString(request.getHeader(ACCEPT_HEADER), "").trim().isEmpty()) {
            return request;
        }
        return new HttpServletRequestWrapper(request) {
            
            @Override
            public String getHeader(final String name) {
                return ACCEPT_HEADER.equalsIgnoreCase(name) ? DEFAULT_ACCEPT : super.getHeader(name);
            }
            
            @Override
            public Enumeration<String> getHeaders(final String name) {
                return ACCEPT_HEADER.equalsIgnoreCase(name) ? Collections.enumeration(List.of(DEFAULT_ACCEPT)) : super.getHeaders(name);
            }
            
            @Override
            public Enumeration<String> getHeaderNames() {
                Set<String> result = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
                result.add(ACCEPT_HEADER);
                return Collections.enumeration(result);
            }
        };
    }
    
    private HttpServletResponse withInitializeProtocolHeader(final HttpServletResponse response) {
        return new HttpServletResponseWrapper(response) {
            
            @Override
            public void setHeader(final String name, final String value) {
                super.setHeader(name, value);
                addNegotiatedProtocolHeader(name);
            }
            
            @Override
            public void addHeader(final String name, final String value) {
                super.addHeader(name, value);
                addNegotiatedProtocolHeader(name);
            }
            
            private void addNegotiatedProtocolHeader(final String name) {
                if (SESSION_HEADER.equalsIgnoreCase(name)) {
                    super.setHeader(PROTOCOL_HEADER, MCPTransportConstants.PROTOCOL_VERSION);
                }
            }
        };
    }
}
