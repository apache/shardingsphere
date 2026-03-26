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
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStreamableServerSession;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeServices;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPRuntimeTransport;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPSyncServerFactory;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SDK-backed HTTP listener for the MCP Streamable HTTP runtime.
 */
public final class StreamableHttpMCPServer implements MCPRuntimeTransport {
    
    private static final String SESSION_HEADER = "MCP-Session-Id";
    
    private static final String PROTOCOL_HEADER = "MCP-Protocol-Version";
    
    private static final String JSON_CONTENT_TYPE = "application/json";
    
    private static final String ACCEPT_HEADER = "Accept";
    
    private static final String DEFAULT_ACCEPT = "application/json, text/event-stream";
    
    private static final String ORIGIN_HEADER = "Origin";
    
    private static final String MISSING_SESSION_MESSAGE = "Session ID required in mcp-session-id header";
    
    private static final String PROTOCOL_MISMATCH_MESSAGE = "Protocol version mismatch.";
    
    private static final String INVALID_ORIGIN_MESSAGE = "Origin is not allowed for the current binding.";
    
    private final HttpTransportConfiguration transportConfiguration;
    
    private final MCPSessionManager sessionManager;
    
    private final DatabaseRuntime databaseRuntime;
    
    private final MetadataRefreshCoordinator metadataRefreshCoordinator;
    
    private final McpJsonMapper jsonMapper;
    
    private final MCPSyncServerFactory syncServerFactory;
    
    private Tomcat tomcat;
    
    private Connector connector;
    
    private Path baseDirectory;
    
    private SdkStreamableHttpServlet transportProvider;
    
    private McpSyncServer syncServer;
    
    private boolean running;
    
    /**
     * Construct one HTTP MCP server with caller-provided runtime metadata.
     *
     * @param transportConfiguration HTTP transport configuration
     * @param sessionManager session manager
     * @param runtimeServices runtime services
     * @param metadataCatalog metadata catalog
     * @param databaseRuntime database runtime
     */
    public StreamableHttpMCPServer(final HttpTransportConfiguration transportConfiguration, final MCPSessionManager sessionManager, final MCPRuntimeServices runtimeServices,
                                   final MetadataCatalog metadataCatalog, final DatabaseRuntime databaseRuntime) {
        this.transportConfiguration = transportConfiguration;
        this.sessionManager = sessionManager;
        this.databaseRuntime = databaseRuntime;
        metadataRefreshCoordinator = runtimeServices.getMetadataRefreshCoordinator();
        jsonMapper = MCPSyncServerFactory.createJsonMapper();
        syncServerFactory = new MCPSyncServerFactory(runtimeServices, jsonMapper, metadataCatalog, databaseRuntime);
    }
    
    /**
     * Start the HTTP listener.
     *
     * @throws IOException when the listener cannot be created
     */
    @Override
    public void start() throws IOException {
        if (running) {
            return;
        }
        transportProvider = new SdkStreamableHttpServlet(sessionManager, databaseRuntime, metadataRefreshCoordinator, jsonMapper,
                transportConfiguration.getBindHost(), transportConfiguration.getEndpointPath());
        syncServer = createSyncServer();
        try {
            tomcat = new Tomcat();
            baseDirectory = Files.createTempDirectory("shardingsphere-mcp-tomcat");
            connector = new Connector();
            connector.setPort(transportConfiguration.getPort());
            connector.setProperty("address", transportConfiguration.getBindHost());
            tomcat.setBaseDir(baseDirectory.toString());
            tomcat.setConnector(connector);
            Context context = tomcat.addContext("", baseDirectory.toString());
            ((StandardContext) context).setClearReferencesRmiTargets(false);
            Wrapper servletWrapper = Tomcat.addServlet(context, "mcp-streamable-http", transportProvider);
            servletWrapper.setAsyncSupported(true);
            context.addServletMappingDecoded(transportConfiguration.getEndpointPath(), "mcp-streamable-http");
            tomcat.start();
            running = true;
        } catch (final LifecycleException ex) {
            stop();
            throw new IOException("Failed to start embedded Tomcat runtime.", ex);
        }
    }
    
    /**
     * Stop the HTTP listener.
     */
    public void stop() {
        if (null != syncServer) {
            syncServer.closeGracefully();
            syncServer = null;
        }
        if (null != transportProvider) {
            transportProvider = null;
        }
        if (null != tomcat) {
            try {
                tomcat.stop();
            } catch (final LifecycleException ignored) {
            }
            try {
                tomcat.destroy();
            } catch (final LifecycleException ignored) {
            }
            tomcat = null;
        }
        connector = null;
        deleteBaseDirectory();
        running = false;
    }
    
    @Override
    public void close() {
        stop();
    }
    
    /**
     * Get the local port that the listener actually bound to.
     *
     * @return bound port
     */
    public int getLocalPort() {
        return null == connector ? transportConfiguration.getPort() : connector.getLocalPort();
    }
    
    private McpSyncServer createSyncServer() {
        return syncServerFactory.create(transportProvider);
    }
    
    private void deleteBaseDirectory() {
        if (null == baseDirectory) {
            return;
        }
        try {
            Files.walk(baseDirectory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(each -> {
                        try {
                            Files.deleteIfExists(each);
                        } catch (final IOException ignored) {
                        }
                    });
        } catch (final IOException ignored) {
        }
        baseDirectory = null;
    }
    
    private static final class SdkStreamableHttpServlet extends HttpServlet implements McpStreamableServerTransportProvider {
        
        private final MCPSessionManager sessionManager;
        
        private final DatabaseRuntime databaseRuntime;
        
        private final MetadataRefreshCoordinator metadataRefreshCoordinator;
        
        private final HttpServletStreamableServerTransportProvider delegate;
        
        private final Set<String> activeSessionIds = ConcurrentHashMap.newKeySet();
        
        private final AtomicBoolean closed = new AtomicBoolean();
        
        private final String bindHost;
        
        private SdkStreamableHttpServlet(final MCPSessionManager sessionManager, final DatabaseRuntime databaseRuntime,
                                         final MetadataRefreshCoordinator metadataRefreshCoordinator,
                                         final McpJsonMapper jsonMapper, final String bindHost, final String endpointPath) {
            this.sessionManager = sessionManager;
            this.databaseRuntime = databaseRuntime;
            this.metadataRefreshCoordinator = metadataRefreshCoordinator;
            this.bindHost = bindHost;
            delegate = HttpServletStreamableServerTransportProvider.builder()
                    .jsonMapper(jsonMapper)
                    .mcpEndpoint(endpointPath)
                    .contextExtractor(this::createTransportContext)
                    .build();
        }
        
        @Override
        public List<String> protocolVersions() {
            return List.of(MCPTransportConstants.PROTOCOL_VERSION);
        }
        
        @Override
        public void setSessionFactory(final McpStreamableServerSession.Factory sessionFactory) {
            delegate.setSessionFactory(initializeRequest -> {
                McpSchema.InitializeRequest actualInitializeRequest = normalizeInitializeRequest(initializeRequest);
                McpStreamableServerSession.McpStreamableServerSessionInit result = sessionFactory.startSession(actualInitializeRequest);
                String sessionId = result.session().getId();
                sessionManager.createSession(sessionId);
                activeSessionIds.add(sessionId);
                return result;
            });
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
        public reactor.core.publisher.Mono<Void> closeGracefully() {
            if (!closed.compareAndSet(false, true)) {
                return reactor.core.publisher.Mono.empty();
            }
            return delegate.closeGracefully().doFinally(ignored -> closeManagedSessions());
        }
        
        @Override
        protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
            Optional<ResponseStatus> validationFailure = validateFollowUpRequest(request);
            if (validationFailure.isPresent()) {
                writeResponse(response, validationFailure.get());
                return;
            }
            serviceWithApplicationClassLoader(withDefaultAcceptHeader(request), response);
        }
        
        @Override
        protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
            String sessionId = getHeader(request, SESSION_HEADER);
            if (!sessionId.isEmpty()) {
                Optional<ResponseStatus> validationFailure = validateFollowUpRequest(request);
                if (validationFailure.isPresent()) {
                    writeResponse(response, validationFailure.get());
                    return;
                }
                serviceWithApplicationClassLoader(withDefaultAcceptHeader(request), response);
                return;
            }
            Map<String, String> headers = extractHeaders(request);
            Optional<ResponseStatus> initializationFailure = validateInitializeRequest(headers);
            if (initializationFailure.isPresent()) {
                writeResponse(response, initializationFailure.get());
                return;
            }
            serviceWithApplicationClassLoader(withDefaultAcceptHeader(request), new InitializeResponseHeaderWrapper(response));
        }
        
        @Override
        protected void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
            Optional<ResponseStatus> validationFailure = validateFollowUpRequest(request);
            if (validationFailure.isPresent()) {
                writeResponse(response, validationFailure.get());
                return;
            }
            String sessionId = getHeader(request, SESSION_HEADER);
            serviceWithApplicationClassLoader(withDefaultAcceptHeader(request), response);
            if (200 == response.getStatus() && activeSessionIds.remove(sessionId)) {
                metadataRefreshCoordinator.clearSession(sessionId);
                databaseRuntime.closeSession(sessionId);
                sessionManager.closeSession(sessionId);
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
        
        private HttpServletRequest withDefaultAcceptHeader(final HttpServletRequest request) {
            if (!getHeader(request, ACCEPT_HEADER).isEmpty()) {
                return request;
            }
            return new AcceptHeaderRequestWrapper(request);
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
        
        private McpTransportContext createTransportContext(final HttpServletRequest request) {
            return McpTransportContext.create(Collections.emptyMap());
        }
        
        private Optional<ResponseStatus> validateInitializeRequest(final Map<String, String> headers) {
            return validateOrigin(headers);
        }
        
        private Optional<ResponseStatus> validateFollowUpRequest(final HttpServletRequest request) {
            String sessionId = getHeader(request, SESSION_HEADER);
            if (sessionId.isEmpty()) {
                return Optional.of(new ResponseStatus(400, MISSING_SESSION_MESSAGE));
            }
            Map<String, String> headers = extractHeaders(request);
            Optional<ResponseStatus> originFailure = validateOrigin(headers);
            if (originFailure.isPresent()) {
                return originFailure;
            }
            if (!sessionManager.hasSession(sessionId)) {
                return Optional.of(new ResponseStatus(404, "Session does not exist."));
            }
            String actualProtocolVersion = normalizeProtocolVersion(getHeader(request, PROTOCOL_HEADER));
            String negotiatedProtocolVersion = MCPTransportConstants.PROTOCOL_VERSION;
            if (!negotiatedProtocolVersion.equals(actualProtocolVersion)) {
                return Optional.of(new ResponseStatus(400, PROTOCOL_MISMATCH_MESSAGE));
            }
            return Optional.empty();
        }
        
        private Optional<ResponseStatus> validateOrigin(final Map<String, String> headers) {
            if (!isLocalBinding()) {
                return Optional.empty();
            }
            String origin = getHeader(headers, ORIGIN_HEADER);
            if (origin.isEmpty()) {
                return Optional.empty();
            }
            try {
                String host = Optional.ofNullable(URI.create(origin).getHost()).orElse("");
                return isLoopbackHost(host) ? Optional.empty() : Optional.of(new ResponseStatus(403, INVALID_ORIGIN_MESSAGE));
            } catch (final IllegalArgumentException ignored) {
                return Optional.of(new ResponseStatus(403, INVALID_ORIGIN_MESSAGE));
            }
        }
        
        private boolean isLocalBinding() {
            return isLoopbackHost(bindHost);
        }
        
        private boolean isLoopbackHost(final String host) {
            String actualHost = null == host ? "" : host.trim().toLowerCase(Locale.ENGLISH);
            return "127.0.0.1".equals(actualHost) || "localhost".equals(actualHost) || "::1".equals(actualHost);
        }
        
        private Map<String, String> extractHeaders(final HttpServletRequest request) {
            Map<String, String> result = new LinkedHashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String each = headerNames.nextElement();
                String value = request.getHeader(each);
                if (null != value && !value.trim().isEmpty()) {
                    result.put(each, value.trim());
                }
            }
            return result;
        }
        
        private String getHeader(final HttpServletRequest request, final String headerName) {
            return getHeader(extractHeaders(request), headerName);
        }
        
        private String getHeader(final Map<String, String> headers, final String headerName) {
            String result = headers.get(headerName);
            if (null != result) {
                return result.trim();
            }
            for (Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(headerName)) {
                    return entry.getValue().trim();
                }
            }
            return "";
        }
        
        private McpSchema.InitializeRequest normalizeInitializeRequest(final McpSchema.InitializeRequest initializeRequest) {
            String actualProtocolVersion = normalizeProtocolVersion(initializeRequest.protocolVersion());
            if (actualProtocolVersion.equals(initializeRequest.protocolVersion())) {
                return initializeRequest;
            }
            return new McpSchema.InitializeRequest(actualProtocolVersion, initializeRequest.capabilities(), initializeRequest.clientInfo(), initializeRequest.meta());
        }
        
        private void writeResponse(final HttpServletResponse response, final ResponseStatus responseStatus) throws IOException {
            response.setStatus(responseStatus.getStatusCode());
            response.setContentType(JSON_CONTENT_TYPE);
            response.getWriter().write(JsonUtils.toJsonString(Map.of("message", responseStatus.getMessage())));
            response.getWriter().flush();
        }
        
        private String normalizeProtocolVersion(final String rawProtocolVersion) {
            String actualProtocolVersion = null == rawProtocolVersion ? "" : rawProtocolVersion.trim();
            return actualProtocolVersion.isEmpty() ? MCPTransportConstants.PROTOCOL_VERSION : actualProtocolVersion;
        }
        
        private void closeManagedSessions() {
            Set<String> sessionIds = new LinkedHashSet<>(activeSessionIds);
            activeSessionIds.clear();
            for (String each : sessionIds) {
                metadataRefreshCoordinator.clearSession(each);
                databaseRuntime.closeSession(each);
                sessionManager.closeSession(each);
            }
        }
        
    }
    
    private static final class InitializeResponseHeaderWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
        
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
    
    private static final class ResponseStatus {
        
        @Getter
        private final int statusCode;
        
        @Getter
        private final String message;
        
        private ResponseStatus(final int statusCode, final String message) {
            this.statusCode = statusCode;
            this.message = message;
        }
    }
    
    private static final class AcceptHeaderRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        
        private AcceptHeaderRequestWrapper(final HttpServletRequest request) {
            super(request);
        }
        
        @Override
        public String getHeader(final String name) {
            if (ACCEPT_HEADER.equalsIgnoreCase(name)) {
                return DEFAULT_ACCEPT;
            }
            return super.getHeader(name);
        }
        
        @Override
        public Enumeration<String> getHeaders(final String name) {
            if (ACCEPT_HEADER.equalsIgnoreCase(name)) {
                return Collections.enumeration(List.of(DEFAULT_ACCEPT));
            }
            return super.getHeaders(name);
        }
        
        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> result = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
            result.add(ACCEPT_HEADER);
            return Collections.enumeration(result);
        }
    }
}
