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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.ServerTransportSecurityValidatorFactory;
import org.apache.shardingsphere.mcp.core.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

final class StreamableHttpMCPServlet extends HttpServlet implements McpStreamableServerTransportProvider {
    
    private static final long serialVersionUID = -2320345528569140021L;
    
    private static final String SESSION_HEADER = HttpHeaders.MCP_SESSION_ID;
    
    private static final String PROTOCOL_HEADER = HttpHeaders.PROTOCOL_VERSION;
    
    private static final String JSON_CONTENT_TYPE = "application/json";
    
    private final HttpServletStreamableServerTransportProvider delegate;
    
    private final MCPSessionManager sessionManager;
    
    private final MCPSessionExecutionCoordinator sessionExecutionCoordinator;
    
    private final Map<String, String> sessionProtocolVersions;
    
    private final AtomicBoolean closed;
    
    StreamableHttpMCPServlet(final MCPSessionManager sessionManager, final McpJsonMapper jsonMapper, final HttpTransportConfiguration config) {
        delegate = createDelegate(sessionManager, jsonMapper, config.getBindHost(), config.getEndpointPath());
        this.sessionManager = sessionManager;
        sessionExecutionCoordinator = new MCPSessionExecutionCoordinator(sessionManager);
        sessionProtocolVersions = new ConcurrentHashMap<>();
        sessionManager.addSessionCloseListener(sessionProtocolVersions::remove);
        closed = new AtomicBoolean();
    }
    
    private static HttpServletStreamableServerTransportProvider createDelegate(final MCPSessionManager sessionManager, final McpJsonMapper jsonMapper,
                                                                               final String bindHost, final String endpointPath) {
        return HttpServletStreamableServerTransportProvider.builder().jsonMapper(jsonMapper).mcpEndpoint(endpointPath)
                .securityValidator(ServerTransportSecurityValidatorFactory.create(sessionManager, bindHost)).build();
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
            String sessionId = result.session().getId();
            sessionManager.createSession(sessionId);
            sessionProtocolVersions.put(sessionId, actualInitializeRequest.protocolVersion());
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
        serviceRequest(request, response);
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
        if (!isJsonContentType(request)) {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Content-Type must be application/json.");
            return;
        }
        serviceRequest(request, withInitializeProtocolHeader(response));
    }
    
    private boolean isJsonContentType(final HttpServletRequest request) {
        String contentType = Objects.toString(request.getContentType(), "").trim();
        return contentType.isEmpty() || JSON_CONTENT_TYPE.equalsIgnoreCase(contentType.split(";", 2)[0].trim());
    }
    
    private void serviceRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        setUtf8Encoding(request, response);
        serviceWithApplicationClassLoader(request, response);
    }
    
    @Override
    protected void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        String sessionId = Objects.toString(request.getHeader(SESSION_HEADER), "").trim();
        serviceRequest(request, response);
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
    
    private HttpServletResponse withInitializeProtocolHeader(final HttpServletResponse response) {
        return new HttpServletResponseWrapper(response) {
            
            @Override
            public void setHeader(final String name, final String value) {
                super.setHeader(name, value);
                addNegotiatedProtocolHeader(name, value);
            }
            
            @Override
            public void addHeader(final String name, final String value) {
                super.addHeader(name, value);
                addNegotiatedProtocolHeader(name, value);
            }
            
            private void addNegotiatedProtocolHeader(final String name, final String sessionId) {
                if (SESSION_HEADER.equalsIgnoreCase(name)) {
                    super.setHeader(PROTOCOL_HEADER, findNegotiatedProtocolVersion(sessionId));
                }
            }
        };
    }
    
    private String findNegotiatedProtocolVersion(final String sessionId) {
        return sessionProtocolVersions.getOrDefault(Objects.toString(sessionId, ""), MCPTransportConstants.PROTOCOL_VERSION);
    }
}
