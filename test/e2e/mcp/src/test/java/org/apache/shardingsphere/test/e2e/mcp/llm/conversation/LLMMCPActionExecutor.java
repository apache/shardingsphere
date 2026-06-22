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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class LLMMCPActionExecutor {
    
    private final MCPInteractionClient mcpInteractionClient;
    
    Map<String, Object> executeSafely(final String actionName, final Map<String, Object> args) throws InterruptedException {
        try {
            return execute(actionName, args);
        } catch (final IOException | IllegalStateException ex) {
            throw new IllegalStateException(String.format("MCP action `%s` failed: %s", actionName, ex.getMessage()), ex);
        }
    }
    
    private Map<String, Object> execute(final String actionName, final Map<String, Object> args) throws IOException, InterruptedException {
        if (MCPInteractionActionNames.LIST_RESOURCES.equals(actionName)) {
            return mcpInteractionClient.listResources();
        }
        if (MCPInteractionActionNames.READ_RESOURCE.equals(actionName)) {
            return readResource(args);
        }
        if (MCPInteractionActionNames.LIST_PROMPTS.equals(actionName)) {
            return mcpInteractionClient.listPrompts();
        }
        if (MCPInteractionActionNames.GET_PROMPT.equals(actionName)) {
            return mcpInteractionClient.getPrompt(getRequiredString(args.get("name"), "Prompt name is required."), getOptionalObjectMap(args, "arguments", "Prompt arguments must be an object."));
        }
        return MCPInteractionActionNames.COMPLETE.equals(actionName) ? complete(args) : mcpInteractionClient.call(actionName, args);
    }
    
    private Map<String, Object> readResource(final Map<String, Object> args) throws IOException, InterruptedException {
        return mcpInteractionClient.readResource(getRequiredString(args.get("uri"), "Resource URI is required."));
    }
    
    private Map<String, Object> complete(final Map<String, Object> args) throws IOException, InterruptedException {
        Map<String, Object> reference = requireCompletionReference(args.get("ref"));
        Map<String, Object> argument = getRequiredObjectMap(args.get("argument"), "mcp_complete requires argument.");
        return mcpInteractionClient.complete(reference,
                getRequiredString(argument.get("name"), "mcp_complete argument.name is required."),
                getOptionalString(argument, "value", "mcp_complete argument.value must be a string."),
                getCompletionContextArguments(args));
    }
    
    private Map<String, Object> requireCompletionReference(final Object rawReference) {
        Map<String, Object> result = getRequiredObjectMap(rawReference, "mcp_complete requires ref.");
        String type = getRequiredString(result.get("type"), "mcp_complete ref.type is required.");
        if (!"ref/prompt".equals(type) && !"ref/resource".equals(type)) {
            throw new IllegalArgumentException("mcp_complete ref.type must be ref/prompt or ref/resource.");
        }
        if ("ref/prompt".equals(type)) {
            getRequiredString(result.get("name"), "mcp_complete prompt ref requires name.");
        }
        if ("ref/resource".equals(type)) {
            getRequiredString(result.get("uri"), "mcp_complete resource ref requires uri.");
        }
        return result;
    }
    
    private Map<String, String> getCompletionContextArguments(final Map<String, Object> args) {
        if (!args.containsKey("context")) {
            return Map.of();
        }
        Map<String, Object> context = getOptionalObjectMap(args, "context", "mcp_complete context must be an object.");
        if (!context.containsKey("arguments")) {
            throw new IllegalArgumentException("mcp_complete context.arguments is required.");
        }
        Map<String, Object> rawArguments = getOptionalObjectMap(context, "arguments", "mcp_complete context.arguments must be an object.");
        Map<String, String> result = new LinkedHashMap<>(rawArguments.size(), 1F);
        for (Entry<String, Object> entry : rawArguments.entrySet()) {
            if (!(entry.getValue() instanceof String)) {
                throw new IllegalArgumentException("mcp_complete context.arguments values must be strings.");
            }
            result.put(entry.getKey(), (String) entry.getValue());
        }
        return result;
    }
    
    private Map<String, Object> getRequiredObjectMap(final Object value, final String errorMessage) {
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return LLMMCPJsonValues.castToMap(value);
    }
    
    private Map<String, Object> getOptionalObjectMap(final Map<String, Object> args, final String argumentName, final String errorMessage) {
        if (!args.containsKey(argumentName)) {
            return Map.of();
        }
        Object value = args.get(argumentName);
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return LLMMCPJsonValues.castToMap(value);
    }
    
    private String getRequiredString(final Object value, final String errorMessage) {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(errorMessage);
        }
        String result = ((String) value).trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return result;
    }
    
    private String getOptionalString(final Map<String, Object> args, final String argumentName, final String errorMessage) {
        if (!args.containsKey(argumentName)) {
            return "";
        }
        Object value = args.get(argumentName);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return (String) value;
    }
}
