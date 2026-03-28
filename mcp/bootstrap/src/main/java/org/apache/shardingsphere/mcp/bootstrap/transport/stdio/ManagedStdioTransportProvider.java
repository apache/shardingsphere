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
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerTransport;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPSessionCloser;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * STDIO transport provider with session lifecycle management.
 */
final class ManagedStdioTransportProvider implements McpServerTransportProvider {
    
    private final MCPRuntimeContext runtimeContext;
    
    private final MCPSessionCloser sessionCloser;
    
    private final McpServerTransportProvider delegate;
    
    private final Runnable closeCallback;
    
    private String activeSessionId;
    
    ManagedStdioTransportProvider(final MCPRuntimeContext runtimeContext, final MCPSessionCloser sessionCloser, final McpJsonMapper jsonMapper,
                                  final InputStream inputStream, final OutputStream outputStream, final Runnable closeCallback) {
        this(runtimeContext, sessionCloser, new StdioServerTransportProvider(jsonMapper, inputStream, outputStream), closeCallback);
    }
    
    ManagedStdioTransportProvider(final MCPRuntimeContext runtimeContext, final MCPSessionCloser sessionCloser,
                                  final McpServerTransportProvider delegate, final Runnable closeCallback) {
        this.runtimeContext = runtimeContext;
        this.sessionCloser = sessionCloser;
        this.delegate = delegate;
        this.closeCallback = closeCallback;
    }
    
    @Override
    public List<String> protocolVersions() {
        return List.of(MCPTransportConstants.PROTOCOL_VERSION);
    }
    
    @Override
    public void setSessionFactory(final McpServerSession.Factory sessionFactory) {
        delegate.setSessionFactory(transport -> createManagedSession(sessionFactory, transport));
    }
    
    private McpServerSession createManagedSession(final McpServerSession.Factory sessionFactory, final McpServerTransport transport) {
        McpServerSession result = sessionFactory.create(transport);
        activeSessionId = result.getId();
        runtimeContext.getSessionManager().createSession(activeSessionId);
        return result;
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
    public Mono<Void> closeGracefully() {
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
