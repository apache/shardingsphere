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

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolDefinitionRegistry;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

final class LLMMCPToolDefinitionFactory {
    
    List<Map<String, Object>> create(final Collection<String> allowedToolNames) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (String each : allowedToolNames) {
            result.add(createToolDefinition(each));
        }
        return result;
    }
    
    private Map<String, Object> createToolDefinition(final String toolName) {
        if (MCPInteractionActionNames.LIST_RESOURCES.equals(toolName)) {
            return createListResourcesToolDefinition();
        }
        if (MCPInteractionActionNames.READ_RESOURCE.equals(toolName)) {
            return createReadResourceToolDefinition();
        }
        if (MCPInteractionActionNames.LIST_PROMPTS.equals(toolName)) {
            return createListPromptsToolDefinition();
        }
        if (MCPInteractionActionNames.GET_PROMPT.equals(toolName)) {
            return createGetPromptToolDefinition();
        }
        return MCPInteractionActionNames.COMPLETE.equals(toolName) ? createCompleteToolDefinition() : createOfficialToolDefinition(toolName);
    }
    
    private Map<String, Object> createListResourcesToolDefinition() {
        return Map.of("type", "function", "function", Map.of(
                "name", MCPInteractionActionNames.LIST_RESOURCES,
                "description", "Bridge to MCP resources/list for application-driven context discovery.",
                "parameters", createEmptyObjectSchema()));
    }
    
    private Map<String, Object> createReadResourceToolDefinition() {
        return Map.of("type", "function", "function", Map.of(
                "name", MCPInteractionActionNames.READ_RESOURCE,
                "description", "Bridge to MCP resources/read for application-driven context retrieval.",
                "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of("uri", Map.of("type", "string", "description", "Resource URI to read.")),
                        "required", List.of("uri"),
                        "additionalProperties", false)));
    }
    
    private Map<String, Object> createListPromptsToolDefinition() {
        return Map.of("type", "function", "function", Map.of(
                "name", MCPInteractionActionNames.LIST_PROMPTS,
                "description", "Bridge to MCP prompts/list for task guide discovery.",
                "parameters", createEmptyObjectSchema()));
    }
    
    private Map<String, Object> createGetPromptToolDefinition() {
        return Map.of("type", "function", "function", Map.of(
                "name", MCPInteractionActionNames.GET_PROMPT,
                "description", "Bridge to MCP prompts/get for retrieving a task guide template.",
                "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "name", Map.of("type", "string", "description", "Prompt name to retrieve."),
                                "arguments", Map.of("type", "object", "description", "Prompt arguments.")),
                        "required", List.of("name"),
                        "additionalProperties", false)));
    }
    
    private Map<String, Object> createCompleteToolDefinition() {
        return Map.of("type", "function", "function", Map.of(
                "name", MCPInteractionActionNames.COMPLETE,
                "description", "Bridge to MCP completion/complete for descriptor-backed argument suggestions.",
                "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "ref", createCompletionReferenceSchema(),
                                "argument", createCompletionArgumentSchema(),
                                "context", createCompletionContextSchema()),
                        "required", List.of("ref", "argument"),
                        "additionalProperties", false)));
    }
    
    private Map<String, Object> createCompletionReferenceSchema() {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("type", "object");
        result.put("description", "MCP completion reference. Use type `ref/prompt` with prompt name, or type `ref/resource` with resource uri.");
        result.put("properties", Map.of(
                "type", Map.of("type", "string", "description", "Reference type.", "enum", List.of("ref/prompt", "ref/resource")),
                "name", Map.of("type", "string", "description", "Prompt name when type is `ref/prompt`."),
                "uri", Map.of("type", "string", "description", "Resource URI when type is `ref/resource`.")));
        result.put("required", List.of("type"));
        result.put("additionalProperties", false);
        return result;
    }
    
    private Map<String, Object> createCompletionArgumentSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "name", Map.of("type", "string", "description", "Argument name to complete."),
                        "value", Map.of("type", "string", "description", "Argument prefix.")),
                "required", List.of("name"),
                "additionalProperties", false);
    }
    
    private Map<String, Object> createCompletionContextSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of("arguments", Map.of("type", "object", "description", "Known arguments for contextual completion.")),
                "required", List.of("arguments"),
                "additionalProperties", false);
    }
    
    private Map<String, Object> createOfficialToolDefinition(final String toolName) {
        MCPToolDescriptor toolDescriptor = getToolDescriptor(toolName);
        return Map.of("type", "function", "function", Map.of(
                "name", toolDescriptor.getName(),
                "description", toolDescriptor.getDescription(),
                "parameters", toolDescriptor.getInputSchema()));
    }
    
    private MCPToolDescriptor getToolDescriptor(final String toolName) {
        for (MCPToolDescriptor each : ToolDefinitionRegistry.getSupportedToolDescriptors()) {
            if (toolName.equals(each.getName())) {
                return each;
            }
        }
        throw new IllegalArgumentException("Unsupported tool descriptor: " + toolName);
    }
    
    private Map<String, Object> createEmptyObjectSchema() {
        return Map.of("type", "object", "properties", Map.of(), "additionalProperties", false);
    }
    
}
