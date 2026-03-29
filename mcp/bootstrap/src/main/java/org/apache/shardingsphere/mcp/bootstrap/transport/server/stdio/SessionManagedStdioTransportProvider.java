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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.stdio;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerTransport;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.session.MCPSessionLifecycleRegistry;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

final class SessionManagedStdioTransportProvider extends StdioServerTransportProvider {
    
    private final MCPSessionLifecycleRegistry managedSessions;
    
    SessionManagedStdioTransportProvider(final MCPRuntimeContext runtimeContext, final McpJsonMapper jsonMapper) {
        super(jsonMapper);
        managedSessions = runtimeContext.getSessionLifecycleRegistry();
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
        SessionClosingTransport managedTransport = new SessionClosingTransport(transport);
        McpServerSession session = sessionFactory.create(managedTransport);
        String sessionId = session.getId();
        managedSessions.create(sessionId);
        managedTransport.bindSessionId(sessionId);
        return session;
    }
    
    @RequiredArgsConstructor
    private final class SessionClosingTransport implements McpServerTransport {
        
        private final McpServerTransport delegate;
        
        private final AtomicReference<String> sessionId = new AtomicReference<>();
        
        private final AtomicBoolean closed = new AtomicBoolean();
        
        private void bindSessionId(final String sessionId) {
            this.sessionId.set(sessionId);
        }
        
        @Override
        public Mono<Void> sendMessage(final JSONRPCMessage message) {
            return delegate.sendMessage(message);
        }
        
        @Override
        public <T> T unmarshalFrom(final Object data, final TypeRef<T> typeRef) {
            return delegate.unmarshalFrom(data, typeRef);
        }
        
        @Override
        public Mono<Void> closeGracefully() {
            return delegate.closeGracefully().doFinally(ignored -> closeManagedSession());
        }
        
        @Override
        public void close() {
            try {
                delegate.close();
            } finally {
                closeManagedSession();
            }
        }
        
        private void closeManagedSession() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            managedSessions.close(sessionId.get());
        }
    }
}
