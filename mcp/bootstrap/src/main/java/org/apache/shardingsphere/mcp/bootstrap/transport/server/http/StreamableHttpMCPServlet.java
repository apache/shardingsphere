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
import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import io.modelcontextprotocol.server.transport.ServerTransportSecurityValidator;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStreamableServerSession;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.StreamableHttpMCPRequestValidator.ResponseStatus;
import org.apache.shardingsphere.mcp.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

final class StreamableHttpMCPServlet extends HttpServlet implements McpStreamableServerTransportProvider {
    
    private static final long serialVersionUID = -2320345528569140021L;
    
    private static final String SESSION_HEADER = "MCP-Session-Id";
    
    private static final String PROTOCOL_HEADER = "MCP-Protocol-Version";
    
    private static final String JSON_CONTENT_TYPE = "application/json";
    
    private static final String ACCEPT_HEADER = "Accept";
    
    private static final String DEFAULT_ACCEPT = "application/json, text/event-stream";
    
    private final HttpServletStreamableServerTransportProvider delegate;
    
    private final MCPSessionManager sessionManager;
    
    private final MCPSessionExecutionCoordinator sessionExecutionCoordinator;
    
    private final StreamableHttpMCPRequestValidator requestValidator;
    
    private final ServerTransportSecurityValidator securityValidator;
    
    private final AtomicBoolean closed;
    
    StreamableHttpMCPServlet(final MCPSessionManager sessionManager, final McpJsonMapper jsonMapper, final String bindHost, final String accessToken, final String endpointPath) {
        delegate = HttpServletStreamableServerTransportProvider.builder().jsonMapper(jsonMapper).mcpEndpoint(endpointPath).securityValidator(ServerTransportSecurityValidator.NOOP).build();
        this.sessionManager = sessionManager;
        sessionExecutionCoordinator = new MCPSessionExecutionCoordinator(sessionManager);
        requestValidator = new StreamableHttpMCPRequestValidator(sessionManager);
        securityValidator = new CompositeServerTransportSecurityValidator(List.of(AccessTokenSecurityValidator.create(accessToken), LoopbackOriginSecurityValidator.create(bindHost)));
        closed = new AtomicBoolean();
    }
    
    @Override
    public List<String> protocolVersions() {
        return Collections.singletonList(MCPTransportConstants.PROTOCOL_VERSION);
    }
    
    @Override
    public void setSessionFactory(final McpStreamableServerSession.Factory sessionFactory) {
        delegate.setSessionFactory(initializeRequest -> {
            McpStreamableServerSession.McpStreamableServerSessionInit result = sessionFactory.startSession(normalizeInitializeRequest(initializeRequest));
            sessionManager.createSession(result.session().getId());
            return result;
        });
    }
    
    private McpSchema.InitializeRequest normalizeInitializeRequest(final McpSchema.InitializeRequest initializeRequest) {
        String protocolVersion = normalizeProtocolVersion(initializeRequest.protocolVersion());
        return protocolVersion.equals(initializeRequest.protocolVersion())
                ? initializeRequest
                : new McpSchema.InitializeRequest(protocolVersion, initializeRequest.capabilities(), initializeRequest.clientInfo(), initializeRequest.meta());
    }
    
    private String normalizeProtocolVersion(final String rawProtocolVersion) {
        String protocolVersion = Objects.toString(rawProtocolVersion, "").trim();
        return protocolVersion.isEmpty() ? MCPTransportConstants.PROTOCOL_VERSION : protocolVersion;
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
        String sessionId = Objects.toString(request.getHeader(SESSION_HEADER), "").trim();
        doExecute(request, response, sessionId);
    }
    
    private Optional<ResponseStatus> doExecute(final HttpServletRequest request, final HttpServletResponse response, final String sessionId) throws IOException, ServletException {
        Optional<ResponseStatus> result = validateSecurity(request);
        if (result.isEmpty()) {
            result = requestValidator.validateSessionId(sessionId);
        }
        if (result.isEmpty()) {
            result = requestValidator.validateSessionRequest(request, sessionId);
        }
        if (result.isPresent()) {
            writeResponse(response, result.get());
        } else {
            serviceWithApplicationClassLoader(withDefaultAcceptHeader(request), response);
        }
        return result;
    }
    
    private Optional<ResponseStatus> validateSecurity(final HttpServletRequest request) {
        try {
            securityValidator.validateHeaders(extractHeaders(request));
            return Optional.empty();
        } catch (final ServerTransportSecurityException ex) {
            return Optional.of(new ResponseStatus(ex.getStatusCode(), ex.getMessage()));
        }
    }
    
    private Map<String, List<String>> extractHeaders(final HttpServletRequest request) {
        Collection<String> headerNames = Collections.list(request.getHeaderNames());
        Map<String, List<String>> result = new LinkedHashMap<>(headerNames.size(), 1F);
        for (String each : headerNames) {
            result.put(each, Collections.list(request.getHeaders(each)));
        }
        return result;
    }
    
    private void writeResponse(final HttpServletResponse response, final ResponseStatus responseStatus) throws IOException {
        response.setStatus(responseStatus.getStatusCode());
        response.setContentType(JSON_CONTENT_TYPE);
        response.getWriter().write(JsonUtils.toJsonString(Map.of("message", responseStatus.getMessage())));
        response.getWriter().flush();
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
        String sessionId = Objects.toString(request.getHeader(SESSION_HEADER), "").trim();
        if (sessionId.isEmpty()) {
            doInit(request, response);
        } else {
            doExecute(request, response, sessionId);
        }
    }
    
    private void doInit(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        Optional<ResponseStatus> validationFailure = validateSecurity(request);
        if (validationFailure.isPresent()) {
            writeResponse(response, validationFailure.get());
        } else {
            serviceWithApplicationClassLoader(withDefaultAcceptHeader(request), withInitializeProtocolHeader(response));
        }
    }
    
    @Override
    protected void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        String sessionId = Objects.toString(request.getHeader(SESSION_HEADER), "").trim();
        Optional<ResponseStatus> validationFailure = doExecute(request, response, sessionId);
        if (validationFailure.isEmpty() && 200 == response.getStatus()) {
            sessionExecutionCoordinator.closeSession(sessionId);
        }
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
