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

package org.apache.shardingsphere.mcp.support.protocol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for model-facing MCP next action payloads.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPNextActionUtils {
    
    /**
     * Create a read-resource action.
     *
     * @param resourceUri resource URI
     * @param reason action reason
     * @return action payload
     */
    public static Map<String, Object> readResource(final String resourceUri, final String reason) {
        Map<String, Object> result = createBaseAction("resource_read", "Read resource", reason);
        result.put("resource_uri", resourceUri);
        return result;
    }
    
    /**
     * Create a tool-call action.
     *
     * @param toolName tool name
     * @param reason action reason
     * @param arguments tool arguments
     * @return action payload
     */
    public static Map<String, Object> callTool(final String toolName, final String reason, final Map<String, Object> arguments) {
        Map<String, Object> result = createBaseAction("tool_call", "Call " + toolName, reason);
        result.put("tool_name", toolName);
        result.put("arguments", arguments);
        return result;
    }
    
    /**
     * Create a retry-tool action.
     *
     * @param toolName tool name
     * @param reason action reason
     * @param arguments tool arguments
     * @return action payload
     * @throws IllegalArgumentException when tool name is blank
     */
    public static Map<String, Object> retryTool(final String toolName, final String reason, final Map<String, Object> arguments) {
        ShardingSpherePreconditions.checkState(!toolName.isBlank(), () -> new IllegalArgumentException("Tool name is required for a retry action."));
        Map<String, Object> result = createBaseAction("tool_call", "Retry " + toolName, reason);
        result.put("arguments", arguments);
        result.put("tool_name", toolName);
        return result;
    }
    
    /**
     * Create a completion action.
     *
     * @param action completion action
     * @return action payload
     */
    public static Map<String, Object> completeArgument(final MCPCompletionAction action) {
        Map<String, Object> result = createBaseAction("completion", "Complete " + action.getArgumentName(), action.getReason());
        result.put("ref", createCompletionRef(action.getReferenceType(), action.getReference()));
        result.put("argument", Map.of("name", action.getArgumentName(), "value", action.getArgumentPrefix()));
        if (!action.getContextArguments().isEmpty()) {
            result.put("context", Map.of("arguments", action.getContextArguments()));
        }
        result.put("missing_context_arguments", action.getMissingContextArguments());
        if (!action.getResumeTargetType().isEmpty()) {
            result.put("resume_ref", createCompletionRef(action.getResumeTargetType(), action.getResumeTarget()));
        }
        if (!action.getResumeArguments().isEmpty()) {
            result.put("resume_arguments", action.getResumeArguments());
        }
        return result;
    }
    
    private static Map<String, Object> createCompletionRef(final String referenceType, final String reference) {
        String actualReferenceType = toCompletionReferenceType(referenceType);
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("type", actualReferenceType);
        result.put("ref/resource".equals(actualReferenceType) ? "uri" : "name", reference);
        return result;
    }
    
    private static String toCompletionReferenceType(final String referenceType) {
        if ("prompt".equals(referenceType)) {
            return "ref/prompt";
        }
        if ("resource".equals(referenceType)) {
            return "ref/resource";
        }
        return referenceType;
    }
    
    /**
     * Create an ask-user action.
     *
     * @param reason action reason
     * @param requiredInputs required user inputs
     * @return action payload
     */
    public static Map<String, Object> askUser(final String reason, final List<String> requiredInputs) {
        Map<String, Object> result = createBaseAction("ask_user", "Ask user", reason);
        result.put("question", reason);
        result.put("required_inputs", requiredInputs);
        return result;
    }
    
    /**
     * Create a stop action.
     *
     * @param reason action reason
     * @return action payload
     */
    public static Map<String, Object> stop(final String reason) {
        return createBaseAction("terminal", "Stop", reason);
    }
    
    private static Map<String, Object> createBaseAction(final String type, final String title, final String reason) {
        Map<String, Object> result = new LinkedHashMap<>(10, 1F);
        result.put("order", 1);
        result.put("type", type);
        result.put("title", title);
        result.put(MCPPayloadFieldNames.REASON, reason);
        return result;
    }
    
    /**
     * Add 1-based order values to actions.
     *
     * @param actions actions
     * @return ordered actions
     */
    @SafeVarargs
    public static List<Map<String, Object>> ordered(final Map<String, Object>... actions) {
        return ordered(Arrays.asList(actions));
    }
    
    /**
     * Add 1-based order values to actions.
     *
     * @param actions actions
     * @return ordered actions
     */
    public static List<Map<String, Object>> ordered(final Collection<Map<String, Object>> actions) {
        List<Map<String, Object>> result = new ArrayList<>(actions.size());
        int index = 0;
        for (Map<String, Object> each : actions) {
            Map<String, Object> action = new LinkedHashMap<>(each);
            action.put("order", ++index);
            result.add(action);
        }
        return result;
    }
    
    /**
     * Add action dependencies by 1-based order.
     *
     * @param action action
     * @param dependsOn action orders that must complete first
     * @return action payload
     */
    public static Map<String, Object> dependsOn(final Map<String, Object> action, final Integer... dependsOn) {
        Map<String, Object> result = new LinkedHashMap<>(action);
        result.put("depends_on", List.of(dependsOn));
        return result;
    }
}
