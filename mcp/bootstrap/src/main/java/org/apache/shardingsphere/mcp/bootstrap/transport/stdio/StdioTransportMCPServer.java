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
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPRuntimeTransport;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPSessionCloser;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPSyncServerFactory;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * SDK-backed STDIO MCP transport server.
 */
public final class StdioTransportMCPServer implements MCPRuntimeTransport {
    
    private final ManagedStdioTransportProvider transportProvider;
    
    private final MCPSyncServerFactory syncServerFactory;
    
    private final CountDownLatch terminationLatch = new CountDownLatch(1);
    
    private McpSyncServer syncServer;
    
    private boolean running;
    
    public StdioTransportMCPServer(final MCPRuntimeContext runtimeContext) {
        McpJsonMapper jsonMapper = MCPTransportJsonMapperFactory.create();
        transportProvider = new ManagedStdioTransportProvider(
                runtimeContext, new MCPSessionCloser(runtimeContext), jsonMapper, new LifecycleAwareInputStream(System.in, () -> terminationLatch.countDown()), System.out,
                () -> terminationLatch.countDown());
        syncServerFactory = new MCPSyncServerFactory(runtimeContext, jsonMapper);
    }
    
    @Override
    public void start() {
        if (running) {
            return;
        }
        syncServer = syncServerFactory.create(transportProvider);
        running = true;
    }
    
    @Override
    public void stop() {
        if (null != syncServer) {
            syncServer.closeGracefully();
            syncServer = null;
        }
        running = false;
        terminationLatch.countDown();
    }
    
    @Override
    public void awaitTermination() throws InterruptedException {
        terminationLatch.await();
    }
    
    private static final class ManagedStdioTransportProvider implements McpServerTransportProvider {
        
        private final MCPRuntimeContext runtimeContext;
        
        private final MCPSessionCloser sessionCloser;
        
        private final StdioServerTransportProvider delegate;
        
        private final Runnable closeCallback;
        
        private String activeSessionId;
        
        private ManagedStdioTransportProvider(final MCPRuntimeContext runtimeContext, final MCPSessionCloser sessionCloser, final McpJsonMapper jsonMapper,
                                              final InputStream inputStream, final OutputStream outputStream, final Runnable closeCallback) {
            this.runtimeContext = runtimeContext;
            this.sessionCloser = sessionCloser;
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
                runtimeContext.getSessionManager().createSession(activeSessionId);
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
            sessionCloser.closeSession(activeSessionId);
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
        public int read() throws IOException {
            int result = super.read();
            if (-1 == result) {
                closeCallback.run();
            }
            return result;
        }
        
        @Override
        public int read(final byte[] bytes, final int offset, final int length) throws IOException {
            int result = super.read(bytes, offset, length);
            if (-1 == result) {
                closeCallback.run();
            }
            return result;
        }
        
        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                closeCallback.run();
            }
        }
    }
}
