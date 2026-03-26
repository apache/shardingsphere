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

package org.apache.shardingsphere.mcp.bootstrap.transport.stdio;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeServices;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPSyncServerFactory;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPRuntimeTransport;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * SDK-backed STDIO MCP transport server.
 */
public final class StdioTransportMCPServer implements MCPRuntimeTransport {
    
    private final MCPSessionManager sessionManager;
    
    private final DatabaseRuntime databaseRuntime;
    
    private final MetadataRefreshCoordinator metadataRefreshCoordinator;
    
    private final ManagedStdioTransportProvider transportProvider;
    
    private final MCPSyncServerFactory syncServerFactory;
    
    private final CountDownLatch terminationLatch = new CountDownLatch(1);
    
    private McpSyncServer syncServer;
    
    private boolean running;
    
    /**
     * Construct one STDIO MCP transport server.
     *
     * @param sessionManager session manager
     * @param runtimeServices runtime services
     * @param metadataCatalog metadata catalog
     * @param databaseRuntime database runtime
     */
    public StdioTransportMCPServer(final MCPSessionManager sessionManager, final MCPRuntimeServices runtimeServices,
                                   final MetadataCatalog metadataCatalog, final DatabaseRuntime databaseRuntime) {
        this(sessionManager, runtimeServices, metadataCatalog, databaseRuntime, System.in, System.out);
    }
    
    StdioTransportMCPServer(final MCPSessionManager sessionManager, final MCPRuntimeServices runtimeServices,
                            final MetadataCatalog metadataCatalog, final DatabaseRuntime databaseRuntime,
                            final InputStream inputStream, final OutputStream outputStream) {
        this.sessionManager = sessionManager;
        this.databaseRuntime = databaseRuntime;
        metadataRefreshCoordinator = runtimeServices.getMetadataRefreshCoordinator();
        McpJsonMapper jsonMapper = MCPSyncServerFactory.createJsonMapper();
        transportProvider = new ManagedStdioTransportProvider(sessionManager, databaseRuntime, metadataRefreshCoordinator,
                jsonMapper, new LifecycleAwareInputStream(inputStream, this::signalTermination), outputStream, this::signalTermination);
        syncServerFactory = new MCPSyncServerFactory(runtimeServices, jsonMapper, metadataCatalog, databaseRuntime);
    }
    
    /**
     * Start the STDIO transport.
     */
    @Override
    public void start() {
        if (running) {
            return;
        }
        syncServer = syncServerFactory.create(transportProvider);
        running = true;
    }
    
    /**
     * Stop the STDIO transport.
     */
    public void stop() {
        if (null != syncServer) {
            syncServer.closeGracefully();
            syncServer = null;
        }
        running = false;
        signalTermination();
    }
    
    @Override
    public void awaitTermination() throws InterruptedException {
        terminationLatch.await();
    }
    
    @Override
    public void close() {
        stop();
    }
    
    private void signalTermination() {
        terminationLatch.countDown();
    }
    
    private static final class ManagedStdioTransportProvider implements McpServerTransportProvider {
        
        private final MCPSessionManager sessionManager;
        
        private final DatabaseRuntime databaseRuntime;
        
        private final MetadataRefreshCoordinator metadataRefreshCoordinator;
        
        private final StdioServerTransportProvider delegate;
        
        private final Runnable closeCallback;
        
        private String activeSessionId;
        
        private ManagedStdioTransportProvider(final MCPSessionManager sessionManager, final DatabaseRuntime databaseRuntime,
                                              final MetadataRefreshCoordinator metadataRefreshCoordinator, final McpJsonMapper jsonMapper,
                                              final InputStream inputStream, final OutputStream outputStream, final Runnable closeCallback) {
            this.sessionManager = sessionManager;
            this.databaseRuntime = databaseRuntime;
            this.metadataRefreshCoordinator = metadataRefreshCoordinator;
            this.closeCallback = closeCallback;
            delegate = new StdioServerTransportProvider(jsonMapper, inputStream, outputStream);
        }
        
        @Override
        public List<String> protocolVersions() {
            return List.of(MCPTransportConstants.PROTOCOL_VERSION);
        }
        
        @Override
        public void setSessionFactory(final McpServerSession.Factory sessionFactory) {
            delegate.setSessionFactory(transport -> {
                McpServerSession result = sessionFactory.create(transport);
                activeSessionId = result.getId();
                sessionManager.createSession(activeSessionId);
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
            return delegate.closeGracefully().doFinally(ignored -> {
                closeManagedSession();
                closeCallback.run();
            });
        }
        
        private void closeManagedSession() {
            if (null == activeSessionId || activeSessionId.isEmpty()) {
                return;
            }
            metadataRefreshCoordinator.clearSession(activeSessionId);
            databaseRuntime.closeSession(activeSessionId);
            sessionManager.closeSession(activeSessionId);
            activeSessionId = null;
        }
        
    }
    
    private static final class LifecycleAwareInputStream extends FilterInputStream {
        
        private final Runnable closeCallback;
        
        private LifecycleAwareInputStream(final InputStream inputStream, final Runnable closeCallback) {
            super(inputStream);
            this.closeCallback = closeCallback;
        }
        
        @Override
        public int read() throws java.io.IOException {
            int result = super.read();
            if (-1 == result) {
                closeCallback.run();
            }
            return result;
        }
        
        @Override
        public int read(final byte[] bytes, final int offset, final int length) throws java.io.IOException {
            int result = super.read(bytes, offset, length);
            if (-1 == result) {
                closeCallback.run();
            }
            return result;
        }
        
        @Override
        public void close() throws java.io.IOException {
            try {
                super.close();
            } finally {
                closeCallback.run();
            }
        }
    }
}
