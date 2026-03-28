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

package org.apache.shardingsphere.mcp.bootstrap.transport.session;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP sessions registry.
 */
public final class MCPSessionRegistry {
    
    private final MCPSessionManager sessionManager;
    
    private final MCPSessionCloser sessionCloser;
    
    private final Set<String> activeSessionIds = ConcurrentHashMap.newKeySet();
    
    public MCPSessionRegistry(final MCPRuntimeContext runtimeContext) {
        sessionManager = runtimeContext.getSessionManager();
        sessionCloser = new MCPSessionCloser(runtimeContext);
    }
    
    /**
     * Create session.
     *
     * @param sessionId session ID
     */
    public void create(final String sessionId) {
        sessionManager.createSession(sessionId);
        activeSessionIds.add(sessionId);
    }
    
    /**
     * Close session.
     *
     * @param sessionId session ID
     */
    public void close(final String sessionId) {
        if (activeSessionIds.remove(sessionId)) {
            sessionCloser.closeSession(sessionId);
        }
    }
    
    /**
     * Close all sessions.
     */
    public void closeAll() {
        for (String each : new LinkedHashSet<>(activeSessionIds)) {
            close(each);
        }
    }
}
