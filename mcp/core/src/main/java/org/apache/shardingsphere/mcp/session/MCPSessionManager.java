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

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * MCP session manager.
 */
@Getter
public final class MCPSessionManager {
    
    private final Set<String> sessions = new ConcurrentSkipListSet<>();
    
    /**
     * Create a new session.
     *
     * @param sessionId session identifier
     */
    public void createSession(final String sessionId) {
        ShardingSpherePreconditions.checkState(sessions.add(sessionId), () -> new IllegalStateException("Session already exists."));
    }
    
    /**
     * Determine whether a session exists.
     *
     * @param sessionId session identifier
     * @return {@code true} when the session exists
     */
    public boolean hasSession(final String sessionId) {
        return sessions.contains(sessionId);
    }
    
    /**
     * Close the session and rollback any pending work.
     *
     * @param sessionId session identifier
     */
    public void closeSession(final String sessionId) {
        sessions.remove(sessionId);
    }
}
