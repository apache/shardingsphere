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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.MCPJdbcTransactionResourceManager;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * MCP session manager.
 */
public final class MCPSessionManager {
    
    @Getter
    private final MCPJdbcTransactionResourceManager transactionResourceManager;
    
    private final Map<String, ReentrantLock> sessions = new ConcurrentHashMap<>();
    
    private final List<Consumer<String>> sessionCloseListeners = new CopyOnWriteArrayList<>();
    
    public MCPSessionManager(final Map<String, RuntimeDatabaseConfiguration> databases) {
        transactionResourceManager = new MCPJdbcTransactionResourceManager(databases);
    }
    
    /**
     * Create a new session.
     *
     * @param sessionId session id
     */
    public void createSession(final String sessionId) {
        ShardingSpherePreconditions.checkState(null == sessions.putIfAbsent(sessionId, new ReentrantLock(true)), () -> new IllegalStateException("Session already exists."));
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
     * Add a callback invoked after one session is closed.
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
    public void closeSession(final String sessionId) {
        ReentrantLock executionLock = findExecutionLock(sessionId);
        if (null == executionLock) {
            return;
        }
        try {
            transactionResourceManager.closeSession(sessionId);
        } finally {
            if (sessions.remove(sessionId, executionLock)) {
                notifySessionCloseListeners(sessionId);
            }
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
    
    ReentrantLock findExecutionLock(final String sessionId) {
        return sessions.get(sessionId);
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
    
    private void notifySessionCloseListeners(final String sessionId) {
        for (Consumer<String> each : sessionCloseListeners) {
            each.accept(sessionId);
        }
    }
}
