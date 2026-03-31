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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP session manager.
 */
@Getter
public final class MCPSessionManager {
    
    private final Map<String, MCPSessionContext> sessions = new ConcurrentHashMap<>();
    
    /**
     * Create a new session.
     *
     * @param sessionId session identifier
     * @return created session context
     */
    public MCPSessionContext createSession(final String sessionId) {
        MCPSessionContext result = new MCPSessionContext(sessionId);
        ShardingSpherePreconditions.checkState(null == sessions.putIfAbsent(sessionId, result), () -> new IllegalStateException("Session already exists."));
        return result;
    }
    
    MCPSessionContext getSession(final String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId)).orElseThrow(() -> new IllegalStateException("Session does not exist."));
    }
    
    /**
     * Determine whether a session exists.
     *
     * @param sessionId session identifier
     * @return {@code true} when the session exists
     */
    public boolean hasSession(final String sessionId) {
        return sessions.containsKey(sessionId);
    }
    
    /**
     * Close the session and rollback any pending work.
     *
     * @param sessionId session identifier
     */
    public void closeSession(final String sessionId) {
        MCPSessionContext sessionContext = sessions.remove(sessionId);
        if (null != sessionContext) {
            sessionContext.close();
        }
    }
    
    /**
     * MCP session context.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class MCPSessionContext {
        
        private final String sessionId;
        
        private boolean closed;
        
        private void close() {
            closed = true;
        }
    }
}
