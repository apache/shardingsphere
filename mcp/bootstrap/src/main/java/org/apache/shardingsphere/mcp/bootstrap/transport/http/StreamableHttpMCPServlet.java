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

package org.apache.shardingsphere.mcp.bootstrap.transport.http;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStreamableServerSession;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPSessionCloser;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Streamable HTTP MCP servlet.
 */
final class StreamableHttpMCPServlet extends HttpServlet implements McpStreamableServerTransportProvider {
    
    private static final long serialVersionUID = -2320345528569140021L;
    
    private static final String SESSION_HEADER = "MCP-Session-Id";
    
    private static final String PROTOCOL_HEADER = "MCP-Protocol-Version";
    
    private static final String JSON_CONTENT_TYPE = "application/json";
    
    private static final String ACCEPT_HEADER = "Accept";
    
    private static final String DEFAULT_ACCEPT = "application/json, text/event-stream";
    
    private final MCPRuntimeContext runtimeContext;
    
    private final HttpServletStreamableServerTransportProvider delegate;
    
    private final Set<String> activeSessionIds = ConcurrentHashMap.newKeySet();
    
    private final AtomicBoolean closed = new AtomicBoolean();
    
    private final MCPHttpRequestValidator requestValidator;
    
    private final MCPSessionCloser sessionCloser;
    
    StreamableHttpMCPServlet(final MCPRuntimeContext runtimeContext, final McpJsonMapper jsonMapper, final String bindHost, final String endpointPath) {
        this.runtimeContext = runtimeContext;
        requestValidator = new MCPHttpRequestValidator(runtimeContext, bindHost);
        sessionCloser = new MCPSessionCloser(runtimeContext);
        delegate = HttpServletStreamableServerTransportProvider.builder().jsonMapper(jsonMapper).mcpEndpoint(endpointPath).contextExtractor(request -> McpTransportContext.create(Collections.emptyMap())).build();
    }
    
    @Override
    public List<String> protocolVersions() {
        return Collections.singletonList(MCPTransportConstants.PROTOCOL_VERSION);
    }
    
    @Override
    public void setSessionFactory(final McpStreamableServerSession.Factory sessionFactory) {
        delegate.setSessionFactory(initializeRequest -> {
            McpStreamableServerSession.McpStreamableServerSessionInit result = sessionFactory.startSession(normalizeInitializeRequest(initializeRequest));
            String sessionId = result.session().getId();
            runtimeContext.getSessionManager().createSession(sessionId);
            activeSessionIds.add(sessionId);
            return result;
        });
    }
    
    private McpSchema.InitializeRequest normalizeInitializeRequest(final McpSchema.InitializeRequest initializeRequest) {
        String actualProtocolVersion = normalizeProtocolVersion(initializeRequest.protocolVersion());
        return actualProtocolVersion.equals(initializeRequest.protocolVersion())
                ? initializeRequest
                : new McpSchema.InitializeRequest(actualProtocolVersion, initializeRequest.capabilities(), initializeRequest.clientInfo(), initializeRequest.meta());
    }
    
    private String normalizeProtocolVersion(final String rawProtocolVersion) {
        String actualProtocolVersion = null == rawProtocolVersion ? "" : rawProtocolVersion.trim();
        return actualProtocolVersion.isEmpty() ? MCPTransportConstants.PROTOCOL_VERSION : actualProtocolVersion;
    }
    
    @Override
    public reactor.core.publisher.Mono<Void> notifyClients(final String method, final Object params) {
        return delegate.notifyClients(method, params);
    }
    
    @Override
    public reactor.core.publisher.Mono<Void> notifyClient(final String sessionId, final String method, final Object params) {
        return delegate.notifyClient(sessionId, method, params);
    }
    
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> headers = requestValidator.extractHeaders(request);
        Optional<MCPHttpRequestValidator.ResponseStatus> validationFailure = requestValidator.validateFollowUpRequest(headers);
        if (validationFailure.isPresent()) {
            writeResponse(response, validationFailure.get());
        } else {
            serviceWithApplicationClassLoader(withDefaultAcceptHeader(request), response);
        }
    }
    
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> headers = requestValidator.extractHeaders(request);
        String sessionId = requestValidator.getHeader(headers, SESSION_HEADER);
        if (!sessionId.isEmpty()) {
            Optional<MCPHttpRequestValidator.ResponseStatus> validationFailure = requestValidator.validateFollowUpRequest(headers);
            if (validationFailure.isPresent()) {
                writeResponse(response, validationFailure.get());
            } else {
                serviceWithApplicationClassLoader(withDefaultAcceptHeader(request), response);
            }
            return;
        }
        Optional<MCPHttpRequestValidator.ResponseStatus> initializationFailure = requestValidator.validateInitializeRequest(headers);
        if (initializationFailure.isPresent()) {
            writeResponse(response, initializationFailure.get());
        } else {
            serviceWithApplicationClassLoader(withDefaultAcceptHeader(request), new InitializeResponseHeaderWrapper(response));
        }
    }
    
    @Override
    protected void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> headers = requestValidator.extractHeaders(request);
        Optional<MCPHttpRequestValidator.ResponseStatus> validationFailure = requestValidator.validateFollowUpRequest(headers);
        if (validationFailure.isPresent()) {
            writeResponse(response, validationFailure.get());
            return;
        }
        String sessionId = requestValidator.getHeader(headers, SESSION_HEADER);
        serviceWithApplicationClassLoader(withDefaultAcceptHeader(request), response);
        if (200 == response.getStatus() && activeSessionIds.remove(sessionId)) {
            sessionCloser.closeSession(sessionId);
        }
    }
    
    private void writeResponse(final HttpServletResponse response, final MCPHttpRequestValidator.ResponseStatus responseStatus) throws IOException {
        response.setStatus(responseStatus.getStatusCode());
        response.setContentType(JSON_CONTENT_TYPE);
        response.getWriter().write(JsonUtils.toJsonString(Map.of("message", responseStatus.getMessage())));
        response.getWriter().flush();
    }
    
    private void serviceWithApplicationClassLoader(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            delegate.service(request, response);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
    
    private HttpServletRequest withDefaultAcceptHeader(final HttpServletRequest request) {
        String acceptHeader = request.getHeader(ACCEPT_HEADER);
        return null == acceptHeader || acceptHeader.trim().isEmpty() ? new AcceptHeaderRequestWrapper(request) : request;
    }
    
    @Override
    public reactor.core.publisher.Mono<Void> closeGracefully() {
        return closed.compareAndSet(false, true) ? delegate.closeGracefully().doFinally(ignored -> closeManagedSessions()) : reactor.core.publisher.Mono.empty();
    }
    
    private void closeManagedSessions() {
        Set<String> sessionIds = new LinkedHashSet<>(activeSessionIds);
        activeSessionIds.clear();
        for (String each : sessionIds) {
            sessionCloser.closeSession(each);
        }
    }
    
    @Override
    public void destroy() {
        try {
            closeGracefully().block();
        } finally {
            super.destroy();
        }
    }
    
    private static final class InitializeResponseHeaderWrapper extends HttpServletResponseWrapper {
        
        private InitializeResponseHeaderWrapper(final HttpServletResponse response) {
            super(response);
        }
        
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
        
        private void addNegotiatedProtocolHeader(final String name, final String value) {
            if (SESSION_HEADER.equalsIgnoreCase(name) && null != value && !value.trim().isEmpty()) {
                super.setHeader(PROTOCOL_HEADER, MCPTransportConstants.PROTOCOL_VERSION);
            }
        }
    }
    
    private static final class AcceptHeaderRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        
        private AcceptHeaderRequestWrapper(final HttpServletRequest request) {
            super(request);
        }
        
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
    }
}
