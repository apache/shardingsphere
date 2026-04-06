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
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.tool.MCPToolController;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
final class MCPToolCallHandler {
    
    private final MCPToolController toolController;
    
    McpSchema.CallToolResult handle(final McpSyncServerExchange exchange, final McpSchema.CallToolRequest request) {
        Map<String, Object> arguments = Optional.ofNullable(request.arguments()).orElse(Map.of());
        Map<String, Object> payload = toolController.handle(exchange.sessionId(), request.name(), arguments).toPayload();
        return CallToolResult.builder().structuredContent(payload).addTextContent(JsonUtils.toJsonString(payload)).isError(isError(payload)).build();
    }
    
    private boolean isError(final Map<String, Object> payload) {
        return payload.containsKey("error_code");
    }
}
