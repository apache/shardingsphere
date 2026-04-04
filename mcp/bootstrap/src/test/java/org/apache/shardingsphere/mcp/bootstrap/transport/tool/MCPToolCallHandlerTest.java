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

package org.apache.shardingsphere.mcp.bootstrap.transport.tool;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.tool.MCPToolPayloadResolver;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPToolCallHandlerTest {
    
    @Test
    void assertHandle() {
        MCPToolPayloadResolver toolPayloadResolver = mock(MCPToolPayloadResolver.class);
        MCPToolCallHandler toolCallHandler = new MCPToolCallHandler(toolPayloadResolver);
        McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
        Map<String, Object> payload = Map.of("result_kind", "result_set");
        when(exchange.sessionId()).thenReturn("session-1");
        when(toolPayloadResolver.resolve("session-1", "execute_query", Map.of("sql", "SELECT 1"))).thenReturn(payload);
        McpSchema.CallToolResult actual = toolCallHandler.handle(exchange, new McpSchema.CallToolRequest("execute_query", Map.of("sql", "SELECT 1")));
        verify(toolPayloadResolver).resolve("session-1", "execute_query", Map.of("sql", "SELECT 1"));
        assertFalse(actual.isError());
        assertThat(actual.structuredContent(), is(payload));
        assertThat(actual.content().get(0), isA(TextContent.class));
        assertThat(((TextContent) actual.content().get(0)).text(), is(JsonUtils.toJsonString(payload)));
    }
    
    @Test
    void assertHandleWithError() {
        MCPToolPayloadResolver toolPayloadResolver = mock(MCPToolPayloadResolver.class);
        MCPToolCallHandler toolCallHandler = new MCPToolCallHandler(toolPayloadResolver);
        McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
        Map<String, Object> payload = Map.of("error_code", "invalid_request", "message", "Unsupported tool.");
        when(exchange.sessionId()).thenReturn("session-1");
        when(toolPayloadResolver.resolve("session-1", "unsupported_tool", Map.of())).thenReturn(payload);
        McpSchema.CallToolResult actual = toolCallHandler.handle(exchange, new McpSchema.CallToolRequest("unsupported_tool", null));
        verify(toolPayloadResolver).resolve("session-1", "unsupported_tool", Map.of());
        assertTrue(actual.isError());
        assertThat(actual.structuredContent(), is(payload));
        assertThat(actual.content().get(0), isA(TextContent.class));
        assertThat(((TextContent) actual.content().get(0)).text(), is(JsonUtils.toJsonString(payload)));
    }
}
