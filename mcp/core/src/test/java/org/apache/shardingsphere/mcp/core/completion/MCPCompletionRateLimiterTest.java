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

import org.apache.shardingsphere.mcp.api.exception.MCPUnavailableException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPCompletionRateLimiterTest {
    
    private static final Instant WINDOW_START = Instant.parse("2026-07-14T00:00:00Z");
    
    @Test
    void assertAcquireWithinLimit() {
        MCPCompletionRateLimiter limiter = new MCPCompletionRateLimiter(2, Clock.fixed(WINDOW_START, ZoneOffset.UTC));
        assertDoesNotThrow(() -> limiter.acquire("session-1"));
        assertDoesNotThrow(() -> limiter.acquire("session-1"));
        MCPUnavailableException actual = assertThrows(MCPUnavailableException.class, () -> limiter.acquire("session-1"));
        assertThat(actual.getMessage(), is("Completion request limit of 2 per minute exceeded for MCP session `session-1`; retry after the current 60-second window ends."));
    }
    
    @Test
    void assertAcquireSeparatelyBySession() {
        MCPCompletionRateLimiter limiter = new MCPCompletionRateLimiter(1, Clock.fixed(WINDOW_START, ZoneOffset.UTC));
        limiter.acquire("session-1");
        assertDoesNotThrow(() -> limiter.acquire("session-2"));
    }
    
    @Test
    void assertResetExpiredWindow() {
        Clock clock = mock(Clock.class);
        when(clock.instant()).thenReturn(WINDOW_START, WINDOW_START, WINDOW_START.plusSeconds(60L));
        MCPCompletionRateLimiter limiter = new MCPCompletionRateLimiter(1, clock);
        limiter.acquire("session-1");
        assertThrows(MCPUnavailableException.class, () -> limiter.acquire("session-1"));
        assertDoesNotThrow(() -> limiter.acquire("session-1"));
    }
    
    @Test
    void assertReleaseSession() {
        MCPCompletionRateLimiter limiter = new MCPCompletionRateLimiter(1, Clock.fixed(WINDOW_START, ZoneOffset.UTC));
        limiter.acquire("session-1");
        limiter.releaseSession("session-1");
        assertDoesNotThrow(() -> limiter.acquire("session-1"));
    }
}
