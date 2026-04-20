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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.util.List;
import java.util.Map;

/**
 * MCP transport payload utility methods.
 */
public final class MCPTransportPayloadUtils {
    
    public static final String JSON_CONTENT_TYPE = "application/json";
    
    private MCPTransportPayloadUtils() {
    }
    
    /**
     * Create MCP tool result for a structured payload.
     *
     * @param payload MCP payload
     * @return MCP tool result
     */
    public static McpSchema.CallToolResult createCallToolResult(final Map<String, Object> payload) {
        return CallToolResult.builder().structuredContent(payload).addTextContent(JsonUtils.toJsonString(payload)).isError(payload.containsKey("error_code")).build();
    }
    
    /**
     * Create MCP resource result for a structured payload.
     *
     * @param uri resource URI
     * @param payload MCP payload
     * @return MCP resource result
     */
    public static McpSchema.ReadResourceResult createReadResourceResult(final String uri, final Map<String, Object> payload) {
        return new McpSchema.ReadResourceResult(List.of(new McpSchema.TextResourceContents(uri, JSON_CONTENT_TYPE, JsonUtils.toJsonString(payload))));
    }
}
