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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP session manager.
 */
public final class MCPSessionManager {
    
    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();
    
    private final Set<String> closedSessionIds = ConcurrentHashMap.newKeySet();
    
    /**
     * Create a new session.
     *
     * @param sessionId session identifier
     * @return created session context
     * @throws IllegalStateException when the session cannot be created or recovered
     */
    public SessionContext createSession(final String sessionId) {
        String actualSessionId = normalizeValue(sessionId, "sessionId");
        ShardingSpherePreconditions.checkState(!closedSessionIds.contains(actualSessionId), () -> new IllegalStateException("Session recovery is not supported."));
        SessionContext result = new SessionContext(actualSessionId);
        ShardingSpherePreconditions.checkState(null == sessions.putIfAbsent(actualSessionId, result), () -> new IllegalStateException("Session already exists."));
        return result;
    }
    
    /**
     * Find a session by identifier.
     *
     * @param sessionId session identifier
     * @return session context when present
     */
    public Optional<SessionContext> findSession(final String sessionId) {
        return Optional.ofNullable(sessions.get(normalizeValue(sessionId, "sessionId")));
    }
    
    /**
     * Bind a logical database to the session.
     *
     * @param sessionId session identifier
     * @param databaseName logical database name
     * @throws IllegalStateException when an active transaction tries to switch databases
     */
    public void bindDatabase(final String sessionId, final String databaseName) {
        SessionContext sessionContext = requireSession(sessionId);
        String actualDatabaseName = normalizeValue(databaseName, "databaseName");
        if (TransactionState.ACTIVE == sessionContext.getTransactionState()
                && !sessionContext.getBoundDatabase().isEmpty()
                && !sessionContext.getBoundDatabase().equals(actualDatabaseName)) {
            throw new IllegalStateException("Cross-database transaction switching is not supported.");
        }
        sessionContext.bindDatabase(actualDatabaseName);
    }
    
    /**
     * Transition the session into transaction state.
     *
     * @param sessionId session identifier
     * @param databaseName logical database name
     */
    public void beginTransaction(final String sessionId, final String databaseName) {
        SessionContext sessionContext = requireSession(sessionId);
        bindDatabase(sessionId, databaseName);
        sessionContext.beginTransaction();
    }
    
    /**
     * Record a savepoint name in the current session.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     */
    public void rememberSavepoint(final String sessionId, final String savepointName) {
        requireSession(sessionId).rememberSavepoint(normalizeValue(savepointName, "savepointName"));
    }
    
    /**
     * Commit the active transaction for the session.
     *
     * @param sessionId session identifier
     */
    public void commitTransaction(final String sessionId) {
        requireSession(sessionId).commitTransaction();
    }
    
    /**
     * Roll back the active transaction for the session.
     *
     * @param sessionId session identifier
     */
    public void rollbackTransaction(final String sessionId) {
        requireSession(sessionId).rollbackTransaction();
    }
    
    /**
     * Roll back to one named savepoint in the active transaction.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     */
    public void rollbackToSavepoint(final String sessionId, final String savepointName) {
        requireSession(sessionId).rollbackToSavepoint(normalizeValue(savepointName, "savepointName"));
    }
    
    /**
     * Release one named savepoint in the active transaction.
     *
     * @param sessionId session identifier
     * @param savepointName savepoint name
     */
    public void releaseSavepoint(final String sessionId, final String savepointName) {
        requireSession(sessionId).releaseSavepoint(normalizeValue(savepointName, "savepointName"));
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
        SessionContext sessionContext = sessions.remove(actualSessionId);
        if (null != sessionContext) {
            sessionContext.rollbackPendingWork();
            sessionContext.close();
        }
        closedSessionIds.add(actualSessionId);
    }
    
    private SessionContext requireSession(final String sessionId) {
        return findSession(sessionId).orElseThrow(() -> new IllegalStateException("Session does not exist."));
    }
    
    private static String normalizeValue(final String value, final String fieldName) {
        String result = value.trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return result;
    }
    
    /**
     * Mutable in-memory session context used by the skeleton runtime.
     */
    @Getter
    public static final class SessionContext {
        
        private final String sessionId;
        
        private final Set<String> savepoints = new LinkedHashSet<>();
        
        private boolean autocommit = true;
        
        private TransactionState transactionState = TransactionState.IDLE;
        
        private String boundDatabase = "";
        
        private boolean closed;
        
        private SessionContext(final String sessionId) {
            this.sessionId = sessionId;
        }
        
        /**
         * Get the registered savepoint names.
         *
         * @return savepoint snapshot
         */
        public Set<String> getSavepoints() {
            return new LinkedHashSet<>(savepoints);
        }
        
        private void bindDatabase(final String databaseName) {
            boundDatabase = databaseName;
        }
        
        private void beginTransaction() {
            if (TransactionState.ACTIVE == transactionState) {
                throw new IllegalStateException("Transaction already active.");
            }
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
            if (!savepoints.contains(savepointName)) {
                throw new IllegalStateException("Savepoint does not exist.");
            }
        }
        
        private void releaseSavepoint(final String savepointName) {
            requireActiveTransaction();
            if (!savepoints.remove(savepointName)) {
                throw new IllegalStateException("Savepoint does not exist.");
            }
        }
        
        private void rollbackPendingWork() {
            autocommit = true;
            transactionState = TransactionState.IDLE;
            savepoints.clear();
            boundDatabase = "";
        }
        
        private void requireActiveTransaction() {
            if (TransactionState.ACTIVE != transactionState) {
                throw new IllegalStateException("No active transaction.");
            }
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
