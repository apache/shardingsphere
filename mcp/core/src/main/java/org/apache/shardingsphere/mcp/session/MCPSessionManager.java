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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP session manager.
 */
public final class MCPSessionManager {
    
    private final Map<String, MCPSessionContext> sessions = new ConcurrentHashMap<>();
    
    /**
     * Create a new session.
     *
     * @param sessionId session identifier
     * @return created session context
     */
    public MCPSessionContext createSession(final String sessionId) {
        String actualSessionId = normalizeValue(sessionId, "sessionId");
        MCPSessionContext result = new MCPSessionContext(actualSessionId);
        ShardingSpherePreconditions.checkState(null == sessions.putIfAbsent(actualSessionId, result), () -> new IllegalStateException("Session already exists."));
        return result;
    }
    
    /**
     * Transition the session into transaction state.
     *
     * @param sessionId session identifier
     * @param databaseName logical database name
     */
    public void beginTransaction(final String sessionId, final String databaseName) {
        getSession(sessionId).beginTransaction(normalizeValue(databaseName, "databaseName"));
    }
    
    /**
     * Record a savepoint name in the current session.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     */
    public void rememberSavepoint(final String sessionId, final String savepointName) {
        getSession(sessionId).rememberSavepoint(normalizeValue(savepointName, "savepointName"));
    }
    
    /**
     * Commit the active transaction for the session.
     *
     * @param sessionId session identifier
     */
    public void commitTransaction(final String sessionId) {
        getSession(sessionId).commitTransaction();
    }
    
    /**
     * Roll back the active transaction for the session.
     *
     * @param sessionId session identifier
     */
    public void rollbackTransaction(final String sessionId) {
        getSession(sessionId).rollbackTransaction();
    }
    
    /**
     * Roll back to one named savepoint in the active transaction.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     */
    public void rollbackToSavepoint(final String sessionId, final String savepointName) {
        getSession(sessionId).rollbackToSavepoint(normalizeValue(savepointName, "savepointName"));
    }
    
    /**
     * Release one named savepoint in the active transaction.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     */
    public void releaseSavepoint(final String sessionId, final String savepointName) {
        getSession(sessionId).releaseSavepoint(normalizeValue(savepointName, "savepointName"));
    }
    
    MCPSessionContext getSession(final String sessionId) {
        return Optional.ofNullable(sessions.get(normalizeValue(sessionId, "sessionId"))).orElseThrow(() -> new IllegalStateException("Session does not exist."));
    }
    
    /**
     * Determine whether a session exists.
     *
     * @param sessionId session identifier
     * @return {@code true} when the session exists
     */
    public boolean hasSession(final String sessionId) {
        return sessions.containsKey(normalizeValue(sessionId, "sessionId"));
    }
    
    /**
     * Close the session and rollback any pending work.
     *
     * @param sessionId session identifier
     */
    public void closeSession(final String sessionId) {
        String actualSessionId = normalizeValue(sessionId, "sessionId");
        MCPSessionContext sessionContext = sessions.remove(actualSessionId);
        if (null != sessionContext) {
            sessionContext.rollbackPendingWork();
            sessionContext.close();
        }
    }
    
    private String normalizeValue(final String value, final String fieldName) {
        String result = Objects.toString(value, "").trim();
        ShardingSpherePreconditions.checkNotEmpty(result, () -> new IllegalArgumentException(fieldName + " cannot be empty."));
        return result;
    }
    
    /**
     * MCP session context.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class MCPSessionContext {
        
        private final String sessionId;
        
        private final Set<String> savepoints = new LinkedHashSet<>();
        
        private boolean autocommit = true;
        
        private TransactionState transactionState = TransactionState.IDLE;
        
        private String boundDatabase = "";
        
        private boolean closed;
        
        private void beginTransaction(final String databaseName) {
            ShardingSpherePreconditions.checkState(TransactionState.ACTIVE != transactionState || boundDatabase.isEmpty() || boundDatabase.equals(databaseName),
                    () -> new IllegalStateException("Cross-database transaction switching is not supported."));
            boundDatabase = databaseName;
            ShardingSpherePreconditions.checkState(TransactionState.ACTIVE != transactionState, () -> new IllegalStateException("Transaction already active."));
            autocommit = false;
            transactionState = TransactionState.ACTIVE;
        }
        
        private void rememberSavepoint(final String savepointName) {
            requireActiveTransaction();
            savepoints.add(savepointName);
        }
        
        private void commitTransaction() {
            requireActiveTransaction();
            rollbackPendingWork();
        }
        
        private void rollbackTransaction() {
            requireActiveTransaction();
            rollbackPendingWork();
        }
        
        private void rollbackToSavepoint(final String savepointName) {
            requireActiveTransaction();
            ShardingSpherePreconditions.checkContains(savepoints, savepointName, () -> new IllegalStateException("Savepoint does not exist."));
        }
        
        private void releaseSavepoint(final String savepointName) {
            requireActiveTransaction();
            ShardingSpherePreconditions.checkState(savepoints.remove(savepointName), () -> new IllegalStateException("Savepoint does not exist."));
        }
        
        private void rollbackPendingWork() {
            autocommit = true;
            transactionState = TransactionState.IDLE;
            savepoints.clear();
            boundDatabase = "";
        }
        
        private void requireActiveTransaction() {
            ShardingSpherePreconditions.checkState(TransactionState.ACTIVE == transactionState, () -> new IllegalStateException("No active transaction."));
        }
        
        private void close() {
            closed = true;
        }
    }
    
    /**
     * Transaction states tracked by the session manager.
     */
    public enum TransactionState {
        
        IDLE, ACTIVE
    }
}
