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

package org.apache.shardingsphere.mcp.core.protocol.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidMetadataObjectTypesException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidToolArgumentException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPToolArgumentContractViolationException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPToolCallLimitExceededException;
import org.apache.shardingsphere.mcp.core.resource.handler.ResourceHandlerRegistry;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolHandlerRegistry;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * MCP basic recovery payload factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPBasicRecoveryPayloadFactory {
    
    static Map<String, Object> createUnsupportedToolRecovery(final String toolName) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery("unsupported_tool", "Call one of the supported tools returned by tools/list.");
        result.put("tool_name", toolName);
        result.put("supported_tools", ToolHandlerRegistry.getSupportedTools());
        result.put("resources_to_read", MCPRecoveryPayloadSupport.createResourceHintList(
                "shardingsphere://capabilities", "capability", "Read ShardingSphere catalog details after checking tools/list."));
        result.put("next_actions", List.of(MCPNextActionUtils.readResource("shardingsphere://capabilities",
                "Read the ShardingSphere domain catalog only when tools/list does not provide enough context.")));
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    static Map<String, Object> createUnsupportedResourceRecovery(final String resourceUri) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(
                "unsupported_resource", "Read one of the supported resources or templates returned by resources/list and resources/templates/list.");
        result.put("resource", MCPResourceHintUtils.create(resourceUri, "resource", "inspect_detail", "Unsupported resource URI requested.", "recovery"));
        result.put("matching_resource_templates", ResourceHandlerRegistry.getSupportedResources());
        result.put("resources_to_read", MCPRecoveryPayloadSupport.createResourceHintList(
                "shardingsphere://capabilities", "capability", "Read ShardingSphere catalog details after checking resources/list and resources/templates/list."));
        result.put("next_actions", List.of(MCPNextActionUtils.readResource("shardingsphere://capabilities",
                "Read the ShardingSphere domain catalog only when official resource discovery does not provide enough context.")));
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    static Map<String, Object> createRuntimeDatabaseConnectionRecovery(final RuntimeDatabaseConnectionException cause) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(cause.getCategory(), createRuntimeDatabaseConnectionModelAction(cause));
        result.put("database", cause.getDatabaseName());
        result.put("resources_to_read", MCPRecoveryPayloadSupport.createResourceHintList(
                "shardingsphere://capabilities", "capability", "Read configured MCP runtime capabilities before retrying."));
        result.put("next_actions", MCPNextActionUtils.ordered(
                MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read current MCP runtime capabilities and configured database names."),
                MCPNextActionUtils.dependsOn(MCPNextActionUtils.askUser(
                        "Ask the operator to fix the MCP runtime database configuration before retrying.", List.of("runtime_database_configuration"), false), 1)));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    static Map<String, Object> createToolCallLimitRecovery(final MCPToolCallLimitExceededException cause) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(
                "tool_call_limit_exceeded", "Stop the current loop, summarize progress, or start a new MCP session before retrying.");
        result.put("identity_scope", "mcp_session");
        result.put("session_id", cause.getSessionId());
        result.put("tool_name", cause.getToolName());
        result.put("max_tool_calls_per_session", cause.getMaxToolCallsPerSession());
        result.put("resources_to_read", MCPRecoveryPayloadSupport.createResourceHintList(
                "shardingsphere://capabilities", "capability", "Read the current MCP safety policy before retrying."));
        result.put("next_actions", List.of(MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read current MCP safety policy and tool call limits.")));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    static Map<String, Object> createInvalidToolArgumentRecovery(final MCPInvalidToolArgumentException cause) {
        return createInvalidIntegerArgumentRecovery(cause.getArgumentPath(), cause.getSuggestedValue(), cause.getSourceTool(), cause.getTargetTool(), cause.getMinimumValue(),
                cause.getMaximumValue());
    }
    
    static Map<String, Object> createToolArgumentContractViolationRecovery(final MCPToolArgumentContractViolationException cause) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(cause.getCategory(), createToolArgumentContractModelAction(cause));
        result.put("field", cause.getArgumentPath());
        result.put("argument_path", cause.getArgumentPath());
        result.put("tool_name", cause.getToolName());
        if (!cause.getExpectedType().isEmpty()) {
            result.put("expected_type", cause.getExpectedType());
        }
        if (!cause.getAllowedValues().isEmpty()) {
            result.put("allowed_values", cause.getAllowedValues());
        }
        if (!cause.getSuggestedArguments().isEmpty()) {
            result.put("suggested_arguments", cause.getSuggestedArguments());
        }
        result.put("next_actions", createToolArgumentContractNextActions(cause));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", cause.getSuggestedArguments().isEmpty());
        return result;
    }
    
    private static String createToolArgumentContractModelAction(final MCPToolArgumentContractViolationException cause) {
        if ("unknown_argument".equals(cause.getCategory())) {
            return "Remove arguments that are not declared by the tool inputSchema before retrying.";
        }
        if ("invalid_enum_value".equals(cause.getCategory())) {
            return "Retry with one of the allowed enum values, or omit the optional argument when safe.";
        }
        return "Retry with a value that matches the declared inputSchema type.";
    }
    
    private static List<Map<String, Object>> createToolArgumentContractNextActions(final MCPToolArgumentContractViolationException cause) {
        if (cause.getSuggestedArguments().isEmpty()) {
            return List.of(MCPNextActionUtils.askUser("Ask the user for a value that matches the tool inputSchema.", List.of(cause.getArgumentPath()), false));
        }
        return List.of(MCPNextActionUtils.retryTool(cause.getToolName(), "Retry with arguments that match the tool inputSchema.", cause.getSuggestedArguments(), false));
    }
    
    static Map<String, Object> createMissingArgumentRecovery(final String argumentName) {
        boolean missingDatabase = "database".equals(argumentName);
        String category = missingDatabase ? "missing_database" : "missing_argument";
        String modelAction = missingDatabase
                ? "Read shardingsphere://databases, infer a single safe database when possible, or ask the user."
                : "Provide the missing argument, infer it from resources when safe, or ask the user.";
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(category, modelAction);
        result.put("missing_fields", List.of(argumentName));
        result.put("resources_to_read", createMissingArgumentResourcesToRead(argumentName));
        result.put("next_actions", createMissingArgumentNextActions(argumentName));
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    static Map<String, Object> createInvalidObjectTypesRecovery(final MCPInvalidMetadataObjectTypesException cause) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery("invalid_enum_value", "Retry with one or more allowed object_types values.");
        result.put("field", "object_types");
        result.put("allowed_values", cause.getAllowedValues());
        result.put("suggested_arguments", Map.of("object_types", cause.getAllowedValues()));
        result.put("next_actions", List.of(MCPNextActionUtils.retryTool("database_gateway_search_metadata",
                "Retry database_gateway_search_metadata with allowed object_types values, or omit object_types to search every supported metadata type.",
                Map.of("object_types", cause.getAllowedValues()), false)));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    static Map<String, Object> createInvalidPageTokenRecovery() {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(
                "invalid_page_token", "Retry without page_token, or use the next_page_token returned by the previous search page.");
        result.put("field", "page_token");
        result.put("argument_path", "page_token");
        result.put("source_tool", "database_gateway_search_metadata");
        result.put("tool_name", "database_gateway_search_metadata");
        result.put("minimum_value", 0);
        result.put("suggested_value", "");
        result.put("suggested_arguments", Map.of("page_token", ""));
        result.put("next_actions", List.of(MCPNextActionUtils.retryTool("database_gateway_search_metadata",
                "Retry the same search without page_token, or use a next_page_token returned by the previous page.", Map.of("page_token", ""), false)));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    private static Map<String, Object> createInvalidIntegerArgumentRecovery(final String fieldName, final int suggestedValue, final String sourceTool, final String targetTool,
                                                                            final int minimumValue, final int maximumValue) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery("invalid_integer_argument", "Retry with an integer value inside the documented bounds.");
        result.put("field", fieldName);
        result.put("argument_path", fieldName);
        if (!sourceTool.isEmpty()) {
            result.put("source_tool", sourceTool);
        }
        if (!targetTool.isEmpty()) {
            result.put("tool_name", targetTool);
        }
        result.put("minimum_value", minimumValue);
        result.put("maximum_value", maximumValue);
        result.put("suggested_value", suggestedValue);
        Map<String, Object> suggestedArguments = Map.of(fieldName, suggestedValue);
        result.put("suggested_arguments", suggestedArguments);
        result.put("next_actions", createInvalidIntegerArgumentNextActions(fieldName, targetTool, suggestedArguments));
        result.put("requires_user_approval", false);
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    private static List<Map<String, Object>> createInvalidIntegerArgumentNextActions(final String fieldName, final String targetTool, final Map<String, Object> suggestedArguments) {
        if (targetTool.isEmpty()) {
            return List.of(MCPNextActionUtils.askUser("Ask the user for a bounded integer value before retrying.", List.of(fieldName), false));
        }
        return List.of(MCPNextActionUtils.retryTool(targetTool, "Retry with a bounded integer argument.", suggestedArguments, false));
    }
    
    private static List<Map<String, Object>> createMissingArgumentResourcesToRead(final String argumentName) {
        if ("database".equals(argumentName)) {
            return MCPRecoveryPayloadSupport.createResourceHintList(
                    "shardingsphere://databases", "logical-database", "Read logical databases before retrying with a database argument.");
        }
        if ("plan_id".equals(argumentName)) {
            return MCPRecoveryPayloadSupport.createResourceHintList(
                    "shardingsphere://capabilities", "capability", "Read workflow-capable MCP tools before retrying with a plan_id argument.");
        }
        return List.of();
    }
    
    private static List<Map<String, Object>> createMissingArgumentNextActions(final String argumentName) {
        List<Map<String, Object>> resources = createMissingArgumentResourcesToRead(argumentName);
        if (resources.isEmpty()) {
            return List.of(MCPNextActionUtils.askUser("Ask the user for the missing argument.", List.of(argumentName), false));
        }
        return List.of(MCPNextActionUtils.readResource(Objects.toString(resources.iterator().next().get("uri"), ""), "Read a safe resource before retrying with the missing argument."));
    }
    
    private static String createRuntimeDatabaseConnectionModelAction(final RuntimeDatabaseConnectionException cause) {
        if (RuntimeDatabaseConnectionException.CATEGORY_MISSING_JDBC_DRIVER.equals(cause.getCategory())) {
            return "Install or configure the JDBC driver for the MCP runtime database, then retry.";
        }
        if (RuntimeDatabaseConnectionException.CATEGORY_AUTHENTICATION_FAILED.equals(cause.getCategory())) {
            return "Check the runtime database credentials outside MCP, then retry.";
        }
        if (RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_TIMEOUT.equals(cause.getCategory())) {
            return "Check database reachability and timeout settings, then retry.";
        }
        if (RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION.equals(cause.getCategory())) {
            return "Fix the MCP runtime database configuration outside MCP, then retry.";
        }
        return "Check the runtime database availability and configuration, then retry.";
    }
}
