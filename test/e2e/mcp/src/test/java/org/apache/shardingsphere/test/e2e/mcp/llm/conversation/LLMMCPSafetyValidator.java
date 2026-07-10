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

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.StatementClassifier;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class LLMMCPSafetyValidator {
    
    private static final String EXECUTION_MODE_PREVIEW = "preview";
    
    private static final String EXECUTION_MODE_MANUAL_ONLY = "manual-only";
    
    private final StatementClassifier statementClassifier = new StatementClassifier();
    
    Optional<LLMMCPToolCallValidationFailure> validate(final String actionName, final Map<String, Object> arguments) {
        if (MCPInteractionActionNames.READ_RESOURCE.equals(actionName) && Objects.toString(arguments.get("uri"), "").trim().isEmpty()) {
            return Optional.of(new LLMMCPToolCallValidationFailure(MCPInteractionActionNames.RESOURCE_READ_KIND, "invalid_tool_arguments", "Model returned an empty resource URI."));
        }
        if (MCPInteractionActionNames.GET_PROMPT.equals(actionName) && Objects.toString(arguments.get("name"), "").trim().isEmpty()) {
            return Optional.of(new LLMMCPToolCallValidationFailure(MCPInteractionActionNames.PROMPT_GET_KIND, "invalid_tool_arguments", "Model returned an empty prompt name."));
        }
        if ("database_gateway_execute_query".equals(actionName) && !isReadOnlyQuery(arguments)) {
            return Optional.of(new LLMMCPToolCallValidationFailure("tool_call", "unsafe_sql_attempted", "Model attempted a non-read-only SQL statement."));
        }
        if ("database_gateway_execute_update".equals(actionName) && isReadOnlyQuery(arguments)) {
            return Optional.of(new LLMMCPToolCallValidationFailure("tool_call", "invalid_tool_arguments", "Model routed a read-only SQL statement to the side-effecting SQL tool."));
        }
        if ("database_gateway_execute_update".equals(actionName) && !EXECUTION_MODE_PREVIEW.equals(Objects.toString(arguments.get("execution_mode"), ""))) {
            return Optional.of(new LLMMCPToolCallValidationFailure("tool_call", "unsafe_sql_execution_attempted",
                    "Model attempted to execute side-effecting SQL in an LLM usability scenario."));
        }
        if ("database_gateway_apply_workflow".equals(actionName) && !isSafeWorkflowExecutionMode(arguments)) {
            return Optional.of(new LLMMCPToolCallValidationFailure("tool_call", "unsafe_workflow_execution_attempted",
                    "Model attempted to execute workflow side effects in an LLM usability scenario."));
        }
        return Optional.empty();
    }
    
    private boolean isReadOnlyQuery(final Map<String, Object> arguments) {
        String sql = Objects.toString(arguments.get("sql"), "").trim();
        try {
            SupportedMCPStatement statementClass = statementClassifier.classify(sql).getStatementClass();
            return SupportedMCPStatement.QUERY == statementClass || SupportedMCPStatement.EXPLAIN == statementClass;
        } catch (final MCPInvalidRequestException | MCPUnsupportedException | IllegalArgumentException ignored) {
            return false;
        }
    }
    
    private boolean isSafeWorkflowExecutionMode(final Map<String, Object> arguments) {
        String executionMode = Objects.toString(arguments.get("execution_mode"), "");
        return EXECUTION_MODE_PREVIEW.equals(executionMode) || EXECUTION_MODE_MANUAL_ONLY.equals(executionMode);
    }
}
