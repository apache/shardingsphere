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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.function.Supplier;
import java.util.concurrent.locks.ReentrantLock;

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
        ReentrantLock executionLock = sessionManager.getRequiredExecutionLock(sessionId);
        executionLock.lock();
        try {
            ShardingSpherePreconditions.checkState(isCurrentExecutionLock(sessionId, executionLock), MCPSessionNotExistedException::new);
            return operation.get();
        } finally {
            executionLock.unlock();
        }
    }
    
    /**
     * Close one session.
     *
     * @param sessionId session id
     */
    public void closeSession(final String sessionId) {
        ReentrantLock executionLock = sessionManager.findExecutionLock(sessionId);
        if (null == executionLock) {
            return;
        }
        executionLock.lock();
        try {
            if (isCurrentExecutionLock(sessionId, executionLock)) {
                sessionManager.closeSession(sessionId);
            }
        } finally {
            executionLock.unlock();
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
    
    private boolean isCurrentExecutionLock(final String sessionId, final ReentrantLock executionLock) {
        return executionLock == sessionManager.findExecutionLock(sessionId);
    }
}
