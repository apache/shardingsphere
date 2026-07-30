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

package org.apache.shardingsphere.mcp.core.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.MCPJdbcTransactionResourceManager;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.List;

/**
 * MCP session manager.
 */
public final class MCPSessionManager {
    
    @Getter
    private final MCPJdbcTransactionResourceManager transactionResourceManager;
    
    private final Map<String, SessionState> sessions = new ConcurrentHashMap<>();
    
    private final List<Consumer<String>> sessionCloseListeners = new CopyOnWriteArrayList<>();
    
    public MCPSessionManager(final Map<String, RuntimeDatabaseConfiguration> databases) {
        transactionResourceManager = new MCPJdbcTransactionResourceManager(databases);
    }
    
    /**
     * Create a new session.
     *
     * @param sessionIdentity session identity
     */
    public void createSession(final MCPSessionIdentity sessionIdentity) {
        ShardingSpherePreconditions.checkState(null == sessions.putIfAbsent(sessionIdentity.getSessionId(), new SessionState(sessionIdentity)),
                () -> new IllegalStateException("Session already exists."));
    }
    
    /**
     * Find session identity.
     *
     * @param sessionId session id
     * @return session identity
     */
    public Optional<MCPSessionIdentity> findSessionIdentity(final String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId)).map(each -> each.identity);
    }
    
    /**
     * Get required session identity.
     *
     * @param sessionId session id
     * @return session identity
     */
    public MCPSessionIdentity getRequiredSessionIdentity(final String sessionId) {
        return getRequiredSessionState(sessionId).identity;
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
     * Add a callback invoked during session close, before the session identifier becomes reusable.
     *
     * @param sessionCloseListener session close listener
     */
    public void addSessionCloseListener(final Consumer<String> sessionCloseListener) {
        sessionCloseListeners.add(sessionCloseListener);
    }
    
    /**
     * Close the session and rollback any pending work.
     *
     * @param sessionId session identifier
     */
    void closeSession(final String sessionId) {
        SessionState sessionState = sessions.get(sessionId);
        if (null == sessionState) {
            return;
        }
        try {
            transactionResourceManager.closeSession(sessionId);
        } finally {
            if (sessionState == sessions.get(sessionId)) {
                try {
                    notifySessionCloseListeners(sessionId);
                } finally {
                    sessions.remove(sessionId, sessionState);
                }
            }
        }
    }
    
    ReentrantLock findExecutionLock(final String sessionId) {
        SessionState sessionState = sessions.get(sessionId);
        return null == sessionState ? null : sessionState.executionLock;
    }
    
    ReentrantLock getRequiredExecutionLock(final String sessionId) {
        ReentrantLock result = findExecutionLock(sessionId);
        if (null == result) {
            throw new MCPSessionNotExistedException();
        }
        return result;
    }
    
    Set<String> getSessionIds() {
        return new LinkedHashSet<>(sessions.keySet());
    }
    
    private SessionState getRequiredSessionState(final String sessionId) {
        SessionState result = sessions.get(sessionId);
        if (null == result) {
            throw new MCPSessionNotExistedException();
        }
        return result;
    }
    
    private void notifySessionCloseListeners(final String sessionId) {
        for (Consumer<String> each : sessionCloseListeners) {
            each.accept(sessionId);
        }
    }
    
    @RequiredArgsConstructor
    private static final class SessionState {
        
        private final ReentrantLock executionLock = new ReentrantLock(true);
        
        private final MCPSessionIdentity identity;
    }
}
