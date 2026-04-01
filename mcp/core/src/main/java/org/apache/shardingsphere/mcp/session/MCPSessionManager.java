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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionResourceManager;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * MCP session manager.
 */
@RequiredArgsConstructor
public final class MCPSessionManager {
    
    private final MCPJdbcTransactionResourceManager transactionResourceManager;
    
    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();
    
    /**
     * Create a new session.
     *
     * @param sessionId session id
     */
    public void createSession(final String sessionId) {
        ShardingSpherePreconditions.checkState(null == sessions.putIfAbsent(sessionId, new SessionContext()), () -> new IllegalStateException("Session already exists."));
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
     * Execute one operation while holding the session execution lock.
     *
     * @param sessionId session id
     * @param operation guarded operation
     * @param <T> return type
     * @return operation result
     */
    public <T> T executeWithSessionLock(final String sessionId, final Supplier<T> operation) {
        SessionContext sessionContext = getRequiredSessionContext(sessionId);
        sessionContext.executionLock.lock();
        try {
            ShardingSpherePreconditions.checkState(sessionContext == sessions.get(sessionId), MCPSessionNotExistedException::new);
            return operation.get();
        } finally {
            sessionContext.executionLock.unlock();
        }
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
        sessionContext.get().executionLock.lock();
        try {
            if (sessionContext.get() != sessions.get(sessionId)) {
                return;
            }
            try {
                transactionResourceManager.closeSession(sessionId);
            } finally {
                sessions.remove(sessionId, sessionContext.get());
            }
        } finally {
            sessionContext.get().executionLock.unlock();
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
    
    private Optional<SessionContext> findSessionContext(final String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
    
    private SessionContext getRequiredSessionContext(final String sessionId) {
        return findSessionContext(sessionId).orElseThrow(MCPSessionNotExistedException::new);
    }
    
    private static final class SessionContext {
        
        private final ReentrantLock executionLock = new ReentrantLock(true);
    }
}
