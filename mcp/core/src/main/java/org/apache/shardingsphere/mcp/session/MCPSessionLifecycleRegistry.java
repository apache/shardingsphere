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
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinate MCP session lifecycle across transports and runtime services.
 */
@RequiredArgsConstructor
public final class MCPSessionLifecycleRegistry {
    
    private final DatabaseRuntime databaseRuntime;
    
    private final MCPSessionManager sessionManager;
    
    private final Set<String> activeSessionIds = ConcurrentHashMap.newKeySet();
    
    /**
     * Create and register one managed session.
     *
     * @param sessionId session identifier
     */
    public void create(final String sessionId) {
        String actualSessionId = Objects.toString(sessionId, "").trim();
        sessionManager.createSession(actualSessionId);
        activeSessionIds.add(actualSessionId);
    }
    
    /**
     * Close one managed session.
     *
     * @param sessionId session identifier
     */
    public void close(final String sessionId) {
        String actualSessionId = Objects.toString(sessionId, "").trim();
        if (actualSessionId.isEmpty()) {
            return;
        }
        if (activeSessionIds.remove(actualSessionId)) {
            cleanup(actualSessionId);
        }
    }
    
    /**
     * Close all managed sessions.
     */
    public void closeAll() {
        for (String each : new LinkedHashSet<>(activeSessionIds)) {
            close(each);
        }
    }
    
    /**
     * Clean up one session even when it is not tracked by the registry.
     *
     * @param sessionId session identifier
     */
    public void cleanup(final String sessionId) {
        String actualSessionId = Objects.toString(sessionId, "").trim();
        if (actualSessionId.isEmpty()) {
            return;
        }
        activeSessionIds.remove(actualSessionId);
        databaseRuntime.closeSession(actualSessionId);
        sessionManager.closeSession(actualSessionId);
    }
}
