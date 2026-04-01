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

import java.util.Optional;
import java.util.function.Supplier;

/**
 * MCP session execution coordinator.
 */
@RequiredArgsConstructor
public final class MCPSessionExecutionCoordinator {
    
    private final MCPSessionManager sessionManager;
    
    /**
     * Execute one operation while holding the session execution lock.
     *
     * @param sessionId session id
     * @param operation guarded operation
     * @param <T> return type
     * @return operation result
     */
    public <T> T executeWithSessionLock(final String sessionId, final Supplier<T> operation) {
        MCPSessionManager.SessionContext sessionContext = sessionManager.getRequiredSessionContext(sessionId);
        sessionContext.getExecutionLock().lock();
        try {
            ShardingSpherePreconditions.checkState(isCurrentSessionContext(sessionId, sessionContext), MCPSessionNotExistedException::new);
            return operation.get();
        } finally {
            sessionContext.getExecutionLock().unlock();
        }
    }
    
    /**
     * Close one session.
     *
     * @param sessionId session id
     */
    public void closeSession(final String sessionId) {
        Optional<MCPSessionManager.SessionContext> sessionContext = sessionManager.findSessionContext(sessionId);
        if (sessionContext.isEmpty()) {
            return;
        }
        sessionContext.get().getExecutionLock().lock();
        try {
            if (isCurrentSessionContext(sessionId, sessionContext.get())) {
                sessionManager.closeSession(sessionId);
            }
        } finally {
            sessionContext.get().getExecutionLock().unlock();
        }
    }
    
    /**
     * Close all current sessions.
     */
    public void closeAllSessions() {
        for (String each : sessionManager.getSessionIds()) {
            closeSession(each);
        }
    }
    
    private boolean isCurrentSessionContext(final String sessionId, final MCPSessionManager.SessionContext sessionContext) {
        return sessionContext == sessionManager.findSessionContext(sessionId).orElse(null);
    }
}
