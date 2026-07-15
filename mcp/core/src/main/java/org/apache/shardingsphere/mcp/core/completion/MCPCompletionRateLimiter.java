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

package org.apache.shardingsphere.mcp.core.completion;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.support.security.MCPRuntimeProtectionPolicy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-session fixed-window rate limiter for MCP completion requests.
 */
final class MCPCompletionRateLimiter {
    
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1L);
    
    private final int maxRequestsPerWindow;
    
    private final Clock clock;
    
    private final Map<String, CompletionWindow> sessionWindows = new ConcurrentHashMap<>();
    
    MCPCompletionRateLimiter() {
        this(MCPRuntimeProtectionPolicy.getMaxCompletionRequestsPerMinute(), Clock.systemUTC());
    }
    
    MCPCompletionRateLimiter(final int maxRequestsPerWindow, final Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.clock = clock;
    }
    
    void acquire(final String sessionId) {
        Instant now = clock.instant();
        sessionWindows.compute(sessionId, (ignored, currentWindow) -> acquire(sessionId, currentWindow, now));
    }
    
    private CompletionWindow acquire(final String sessionId, final CompletionWindow currentWindow, final Instant now) {
        if (null == currentWindow || !now.isBefore(currentWindow.startedAt().plus(WINDOW_DURATION))) {
            return new CompletionWindow(now, 1);
        }
        if (currentWindow.requestCount() >= maxRequestsPerWindow) {
            throw new MCPUnavailableException(String.format(
                    "Completion request limit of %d per minute exceeded for MCP session `%s`; retry after the current 60-second window ends.", maxRequestsPerWindow, sessionId));
        }
        return new CompletionWindow(currentWindow.startedAt(), currentWindow.requestCount() + 1);
    }
    
    void releaseSession(final String sessionId) {
        sessionWindows.remove(sessionId);
    }
    
    private record CompletionWindow(Instant startedAt, int requestCount) {
    }
}
