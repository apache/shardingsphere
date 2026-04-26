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

package org.apache.shardingsphere.mcp.session;

import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.tool.handler.execute.MCPJdbcTransactionResourceManager;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MCP session manager.
 */
public final class MCPSessionManager {
    
    @Getter
    private final MCPJdbcTransactionResourceManager transactionResourceManager;
    
    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();
    
    public MCPSessionManager(final Map<String, RuntimeDatabaseConfiguration> databases) {
        transactionResourceManager = new MCPJdbcTransactionResourceManager(databases);
    }
    
    /**
     * Create a new session.
     *
     * @param sessionId session id
     */
    public void createSession(final String sessionId) {
        createSession(sessionId, "");
    }
    
    /**
     * Create a new session with negotiated protocol version.
     *
     * @param sessionId session id
     * @param protocolVersion negotiated protocol version
     */
    public void createSession(final String sessionId, final String protocolVersion) {
        ShardingSpherePreconditions.checkState(null == sessions.putIfAbsent(sessionId, new SessionContext(protocolVersion)), () -> new IllegalStateException("Session already exists."));
    }
    
    /**
     * Determine whether a session exists.
     *
     * @param sessionId session id
     * @return session exists or not
     */
    public boolean hasSession(final String sessionId) {
        return sessions.containsKey(sessionId);
    }
    
    /**
     * Find negotiated protocol version for session.
     *
     * @param sessionId session id
     * @return negotiated protocol version
     */
    public Optional<String> findProtocolVersion(final String sessionId) {
        return findSessionContext(sessionId).map(SessionContext::getProtocolVersion).filter(each -> !each.isEmpty());
    }
    
    /**
     * Close the session and rollback any pending work.
     *
     * @param sessionId session identifier
     */
    public void closeSession(final String sessionId) {
        Optional<SessionContext> sessionContext = findSessionContext(sessionId);
        if (sessionContext.isEmpty()) {
            return;
        }
        try {
            transactionResourceManager.closeSession(sessionId);
        } finally {
            sessions.remove(sessionId, sessionContext.get());
        }
    }
    
    /**
     * Close all current sessions.
     */
    public void closeAllSessions() {
        for (String each : new LinkedHashSet<>(sessions.keySet())) {
            closeSession(each);
        }
    }
    
    Optional<SessionContext> findSessionContext(final String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
    
    SessionContext getRequiredSessionContext(final String sessionId) {
        return findSessionContext(sessionId).orElseThrow(MCPSessionNotExistedException::new);
    }
    
    Set<String> getSessionIds() {
        return new LinkedHashSet<>(sessions.keySet());
    }
    
    static final class SessionContext {
        
        @Getter
        private final String protocolVersion;
        
        private final ReentrantLock executionLock = new ReentrantLock(true);
        
        private SessionContext(final String protocolVersion) {
            this.protocolVersion = protocolVersion;
        }
        
        ReentrantLock getExecutionLock() {
            return executionLock;
        }
    }
}
