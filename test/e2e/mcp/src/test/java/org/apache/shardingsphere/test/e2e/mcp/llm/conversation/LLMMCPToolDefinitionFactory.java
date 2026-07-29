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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class LLMMCPToolDefinitionFactory {
    
    private static final String EXECUTE_UPDATE_TOOL_NAME = "database_gateway_execute_update";
    
    List<Map<String, Object>> createFromRemote(final List<Map<String, Object>> advertisedTools, final boolean includeUpdatePreview) {
        List<Map<String, Object>> result = new LinkedList<>();
        result.add(createReadResourceToolDefinition());
        boolean readOnlyToolFound = false;
        boolean updatePreviewToolFound = false;
        for (Map<String, Object> each : advertisedTools) {
            String toolName = Objects.toString(each.get("name"), "").trim();
            boolean readOnly = isReadOnly(each);
            boolean updatePreview = includeUpdatePreview && EXECUTE_UPDATE_TOOL_NAME.equals(toolName);
            if (!readOnly && !updatePreview) {
                continue;
            }
            result.add(createRemoteToolDefinition(each, toolName));
            readOnlyToolFound |= readOnly;
            updatePreviewToolFound |= updatePreview;
        }
        if (!readOnlyToolFound) {
            throw new IllegalStateException("MCP runtime did not advertise any read-only tools.");
        }
        if (includeUpdatePreview && !updatePreviewToolFound) {
            throw new IllegalStateException("MCP runtime did not advertise required preview tool: " + EXECUTE_UPDATE_TOOL_NAME);
        }
        return result;
    }
    
    private boolean isReadOnly(final Map<String, Object> advertisedTool) {
        Object annotations = advertisedTool.get("annotations");
        return annotations instanceof Map && Boolean.TRUE.equals(((Map<?, ?>) annotations).get("readOnlyHint"));
    }
    
    private Map<String, Object> createRemoteToolDefinition(final Map<String, Object> advertisedTool, final String toolName) {
        Object inputSchema = advertisedTool.get("inputSchema");
        if (toolName.isEmpty() || !(inputSchema instanceof Map)) {
            throw new IllegalStateException("MCP runtime advertised tool without a name or inputSchema.");
        }
        return Map.of("type", "function", "function", Map.of(
                "name", toolName,
                "description", Objects.toString(advertisedTool.get("description"), ""),
                "parameters", LLMMCPJsonValues.castToMap(inputSchema)));
    }
    
    private Map<String, Object> createReadResourceToolDefinition() {
        return Map.of("type", "function", "function", Map.of(
                "name", LLMConversationRunner.READ_RESOURCE_TOOL_NAME,
                "description", "Read one resource from the live MCP server.",
                "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of("uri", Map.of("type", "string", "description", "Resource URI to read.")),
                        "required", List.of("uri"),
                        "additionalProperties", false)));
    }
}
