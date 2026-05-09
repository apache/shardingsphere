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

import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

final class LLMMCPActionExecutor {

    private final MCPInteractionClient mcpInteractionClient;

    LLMMCPActionExecutor(final MCPInteractionClient mcpInteractionClient) {
        this.mcpInteractionClient = mcpInteractionClient;
    }

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
            return mcpInteractionClient.getPrompt(Objects.toString(args.get("name"), "").trim(), LLMMCPJsonValues.castToMap(args.getOrDefault("arguments", Map.of())));
        }
        return MCPInteractionActionNames.COMPLETE.equals(actionName) ? complete(args) : mcpInteractionClient.call(actionName, args);
    }

    private Map<String, Object> readResource(final Map<String, Object> args) throws IOException, InterruptedException {
        String resourceUri = Objects.toString(args.get("uri"), "").trim();
        if (resourceUri.isEmpty()) {
            throw new IllegalArgumentException("Resource URI is required.");
        }
        return mcpInteractionClient.readResource(resourceUri);
    }

    private Map<String, Object> complete(final Map<String, Object> args) throws IOException, InterruptedException {
        Map<String, Object> reference = normalizeCompletionReference(args);
        String argumentName = normalizeCompletionArgumentName(args);
        if (reference.isEmpty() || argumentName.isEmpty()) {
            return createCompletionRecovery(args, reference, argumentName);
        }
        return mcpInteractionClient.complete(reference, argumentName,
                Objects.toString(args.getOrDefault("argument_value", ""), ""),
                LLMMCPJsonValues.castToStringMap(args.getOrDefault("context_arguments", Map.of())));
    }

    private Map<String, Object> normalizeCompletionReference(final Map<String, Object> args) {
        Object reference = args.get("reference");
        if (!(reference instanceof Map)) {
            return normalizeInlineCompletionReference(args);
        }
        Map<String, Object> result = new LinkedHashMap<>(LLMMCPJsonValues.castToMap(reference));
        normalizeCompletionReferenceType(result);
        return result;
    }

    private Map<String, Object> normalizeInlineCompletionReference(final Map<String, Object> args) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        String referenceType = Objects.toString(args.get("reference_type"), "").trim();
        String promptName = Objects.toString(args.getOrDefault("prompt_name", args.get("name")), "").trim();
        String resourceUri = Objects.toString(args.getOrDefault("resource_uri", args.get("uri")), "").trim();
        if (!referenceType.isEmpty()) {
            result.put("type", referenceType);
        } else if (!promptName.isEmpty()) {
            result.put("type", "ref/prompt");
        } else if (!resourceUri.isEmpty()) {
            result.put("type", "ref/resource");
        }
        if (!promptName.isEmpty()) {
            result.put("name", promptName);
        }
        if (!resourceUri.isEmpty()) {
            result.put("uri", resourceUri);
        }
        normalizeCompletionReferenceType(result);
        return result;
    }

    private String normalizeCompletionArgumentName(final Map<String, Object> args) {
        String result = Objects.toString(args.get("argument_name"), "").trim();
        if (!result.isEmpty()) {
            return result;
        }
        for (Entry<String, Object> entry : args.entrySet()) {
            if (entry.getKey().toLowerCase(Locale.ENGLISH).contains("argument_name")) {
                result = Objects.toString(entry.getValue(), "").trim();
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }
        return "";
    }

    private void normalizeCompletionReferenceType(final Map<String, Object> result) {
        String referenceType = Objects.toString(result.get("type"), "").trim().toLowerCase(Locale.ENGLISH);
        if ("prompt".equals(referenceType)) {
            result.put("type", "ref/prompt");
        } else if ("resource".equals(referenceType)) {
            result.put("type", "ref/resource");
        }
    }

    private Map<String, Object> createCompletionRecovery(final Map<String, Object> args, final Map<String, Object> reference, final String argumentName) {
        Map<String, Object> retryArguments = new LinkedHashMap<>(4, 1F);
        if (!reference.isEmpty()) {
            retryArguments.put("reference", reference);
        }
        if (!argumentName.isEmpty()) {
            retryArguments.put("argument_name", argumentName);
        }
        retryArguments.put("argument_value", Objects.toString(args.getOrDefault("argument_value", ""), ""));
        retryArguments.put("context_arguments", args.getOrDefault("context_arguments", Map.of()));
        return Map.of(
                "response_mode", "recovery",
                "error_code", "invalid_tool_arguments",
                "message", "mcp_complete requires a reference object and argument_name.",
                "recovery", Map.of(
                        "recoverable", true,
                        "model_action", "Retry mcp_complete with the prompt or resource reference from the user request, prompt payload, or previous MCP response.",
                        "required_fields", List.of("reference", "argument_name"),
                        "next_actions", List.of(Map.of(
                                "order", 1,
                                "type", "completion",
                                "title", "Retry completion",
                                "requires_user_approval", false,
                                "tool_name", MCPInteractionActionNames.COMPLETE,
                                "arguments", retryArguments))));
    }
}
