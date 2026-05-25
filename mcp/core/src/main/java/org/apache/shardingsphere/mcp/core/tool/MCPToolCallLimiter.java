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

package org.apache.shardingsphere.mcp.core.tool;

import org.apache.shardingsphere.mcp.core.protocol.exception.MCPToolCallLimitExceededException;
import org.apache.shardingsphere.mcp.support.security.MCPClientSafetyPolicy;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MCP tool call limiter.
 */
public final class MCPToolCallLimiter {
    
    private final int maxToolCallsPerSession;
    
    private final Map<String, AtomicInteger> sessionToolCallCounts = new ConcurrentHashMap<>();
    
    public MCPToolCallLimiter() {
        maxToolCallsPerSession = MCPClientSafetyPolicy.getMaxToolCallsPerSession();
    }
    
    /**
     * Acquire one tool call budget slot.
     *
     * @param sessionId session identifier
     * @param toolName tool name
     * @throws MCPToolCallLimitExceededException when session tool call quota is exhausted
     */
    public void acquire(final String sessionId, final String toolName) {
        String actualSessionId = normalizeSessionId(sessionId);
        int callCount = sessionToolCallCounts.computeIfAbsent(actualSessionId, ignored -> new AtomicInteger()).incrementAndGet();
        if (callCount > maxToolCallsPerSession) {
            throw new MCPToolCallLimitExceededException(actualSessionId, Objects.toString(toolName, ""), maxToolCallsPerSession);
        }
    }
    
    /**
     * Release all tracked budget state for one session.
     *
     * @param sessionId session identifier
     */
    public void releaseSession(final String sessionId) {
        sessionToolCallCounts.remove(normalizeSessionId(sessionId));
    }
    
    private String normalizeSessionId(final String sessionId) {
        String result = Objects.toString(sessionId, "").trim();
        return result.isEmpty() ? "anonymous" : result;
    }
}
