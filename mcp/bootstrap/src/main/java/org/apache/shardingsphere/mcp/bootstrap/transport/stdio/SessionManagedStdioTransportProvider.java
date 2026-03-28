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
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPSessionCloser;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Session managed stdio transport provider.
 */
final class SessionManagedStdioTransportProvider extends StdioServerTransportProvider {
    
    private final MCPRuntimeContext runtimeContext;
    
    private final MCPSessionCloser sessionCloser;
    
    private final Runnable closeCallback;
    
    private String activeSessionId;
    
    SessionManagedStdioTransportProvider(final MCPRuntimeContext runtimeContext, final MCPSessionCloser sessionCloser, final McpJsonMapper jsonMapper,
                                         final InputStream inputStream, final OutputStream outputStream, final Runnable closeCallback) {
        super(jsonMapper, inputStream, outputStream);
        this.runtimeContext = runtimeContext;
        this.sessionCloser = sessionCloser;
        this.closeCallback = closeCallback;
    }
    
    @Override
    public List<String> protocolVersions() {
        return List.of(MCPTransportConstants.PROTOCOL_VERSION);
    }
    
    @Override
    public void setSessionFactory(final McpServerSession.Factory sessionFactory) {
        super.setSessionFactory(transport -> createManagedSession(sessionFactory, transport));
    }
    
    private McpServerSession createManagedSession(final McpServerSession.Factory sessionFactory, final McpServerTransport transport) {
        McpServerSession result = sessionFactory.create(transport);
        activeSessionId = result.getId();
        runtimeContext.getSessionManager().createSession(activeSessionId);
        return result;
    }
    
    @Override
    public Mono<Void> closeGracefully() {
        return super.closeGracefully().doFinally(ignored -> {
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
