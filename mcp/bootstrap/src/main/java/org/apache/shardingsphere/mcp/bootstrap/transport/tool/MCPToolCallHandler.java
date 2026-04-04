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
import org.apache.shardingsphere.mcp.tool.MCPToolPayloadResolver;
import org.apache.shardingsphere.mcp.tool.MCPToolPayloadResult;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
final class MCPToolCallHandler {
    
    private final MCPToolPayloadResolver toolPayloadResolver;
    
    McpSchema.CallToolResult handle(final McpSyncServerExchange exchange, final McpSchema.CallToolRequest request) {
        Map<String, Object> arguments = Optional.ofNullable(request.arguments()).orElse(Collections.emptyMap());
        MCPToolPayloadResult payloadResult = toolPayloadResolver.resolve(exchange.sessionId(), request.name(), arguments);
        return CallToolResult.builder()
                .structuredContent(payloadResult.getPayload()).addTextContent(JsonUtils.toJsonString(payloadResult.getPayload())).isError(!payloadResult.isSuccessful()).build();
    }
}
