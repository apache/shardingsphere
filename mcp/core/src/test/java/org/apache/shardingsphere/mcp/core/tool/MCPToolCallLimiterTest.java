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
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPToolCallLimiterTest {
    
    @Test
    void assertAcquire() {
        final MCPToolCallLimiter limiter = new MCPToolCallLimiter(2);
        assertDoesNotThrow(() -> limiter.acquire("session-1", "database_gateway_search_metadata"));
        assertDoesNotThrow(() -> limiter.acquire("session-1", "database_gateway_execute_query"));
    }
    
    @Test
    void assertAcquireWithDifferentSessions() {
        final MCPToolCallLimiter limiter = new MCPToolCallLimiter(1);
        limiter.acquire("session-1", "database_gateway_search_metadata");
        assertDoesNotThrow(() -> limiter.acquire("session-2", "database_gateway_search_metadata"));
    }
    
    @Test
    void assertAcquireWithExceededLimit() {
        final MCPToolCallLimiter limiter = new MCPToolCallLimiter(1);
        limiter.acquire("session-1", "database_gateway_search_metadata");
        final MCPToolCallLimitExceededException actual = assertThrows(MCPToolCallLimitExceededException.class, () -> limiter.acquire("session-1", "database_gateway_execute_query"));
        assertThat(actual.getSessionId(), is("session-1"));
        assertThat(actual.getToolName(), is("database_gateway_execute_query"));
        assertThat(actual.getMaxToolCallsPerSession(), is(1));
    }
    
    @Test
    void assertAcquireWithBlankSession() {
        final MCPToolCallLimiter limiter = new MCPToolCallLimiter(1);
        limiter.acquire(" ", "database_gateway_search_metadata");
        final MCPToolCallLimitExceededException actual = assertThrows(MCPToolCallLimitExceededException.class, () -> limiter.acquire(" ", "database_gateway_execute_query"));
        assertThat(actual.getSessionId(), is("anonymous"));
    }
    
    @Test
    void assertReleaseSession() {
        final MCPToolCallLimiter limiter = new MCPToolCallLimiter(1);
        limiter.acquire("session-1", "database_gateway_search_metadata");
        limiter.releaseSession("session-1");
        assertDoesNotThrow(() -> limiter.acquire("session-1", "database_gateway_execute_query"));
    }
    
    @Test
    void assertCreateWithInvalidLimit() {
        final IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> new MCPToolCallLimiter(0));
        assertThat(actual.getMessage(), is("Maximum tool calls per MCP session must be positive."));
    }
}
