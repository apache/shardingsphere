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
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPExecutionModeRequiredException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidApprovedStepsException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidExecutionModeException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPWorkflowStateException;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArgumentConflictException;

import java.util.List;
import java.util.Map;

/**
 * MCP workflow recovery payload factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPWorkflowRecoveryPayloadFactory {
    
    static Map<String, Object> createMissingExecutionModeRecovery(final MCPExecutionModeRequiredException cause) {
        return "database_gateway_apply_workflow".equals(cause.getToolName()) ? createMissingWorkflowExecutionModeRecovery(cause) : createMissingUpdateExecutionModeRecovery(cause);
    }
    
    static Map<String, Object> createInvalidExecutionModeRecovery(final MCPInvalidExecutionModeException cause) {
        return "database_gateway_apply_workflow".equals(cause.getToolName()) ? createInvalidWorkflowExecutionModeRecovery(cause) : createInvalidUpdateExecutionModeRecovery(cause);
    }
    
    static Map<String, Object> createInvalidApprovedStepsRecovery(final MCPInvalidApprovedStepsException cause) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(
                "invalid_enum_value", "Retry database_gateway_apply_workflow with execution_mode=preview, review the returned preview_artifacts, "
                        + "then pass explicit approved_steps copied from visible preview_artifacts.approval_step values.");
        Map<String, Object> suggestedArguments = MCPRecoveryPayloadSupport.getSuggestedArguments(cause.getSuggestedArguments(), Map.of(WorkflowFieldNames.EXECUTION_MODE, "preview"));
        result.put(MCPPayloadFieldNames.FIELD, "approved_steps");
        result.put(MCPPayloadFieldNames.ALLOWED_VALUES, cause.getAllowedValues());
        result.put("suggested_arguments", suggestedArguments);
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, List.of(MCPNextActionUtils.callTool(
                "database_gateway_apply_workflow", "Preview again, then copy only visible approval_step values into approved_steps.", suggestedArguments)));
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    static Map<String, Object> createWorkflowArgumentConflictRecovery(final WorkflowArgumentConflictException cause) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(
                "workflow_argument_conflict", "Ask the user which public workflow argument value to keep, then retry with only one value.");
        List<String> argumentFields = createWorkflowArgumentConflictFields(cause.getConflictingArguments());
        result.put("conflicting_arguments", cause.getConflictingArguments());
        result.put(MCPPayloadFieldNames.CLARIFICATION_QUESTIONS, createWorkflowArgumentConflictQuestions(cause.getConflictingArguments()));
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, List.of(MCPNextActionUtils.askUser("Resolve conflicting workflow arguments before retrying the planning tool.", argumentFields)));
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    static Map<String, Object> createWorkflowStateRecovery(final MCPWorkflowStateException cause) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(
                "stale_workflow", "Use current-session completion to select an available plan_id, or re-run the matching planning tool.");
        result.put(WorkflowFieldNames.PLAN_ID, cause.getPlanId());
        result.put(MCPPayloadFieldNames.RESOURCES_TO_READ, MCPRecoveryPayloadSupport.createResourceHintList(
                "shardingsphere://capabilities", "capability", "Read workflow-capable MCP tools before re-planning."));
        result.put("completion_first", Map.of("argument", WorkflowFieldNames.PLAN_ID, "scope", "current MCP session"));
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, MCPNextActionUtils.ordered(
                MCPNextActionUtils.completeArgument("resource", "shardingsphere://workflows/{plan_id}", WorkflowFieldNames.PLAN_ID, "", Map.of(), List.of(), "resource",
                        "shardingsphere://workflows/{plan_id}", Map.of(), "Use MCP completion for plan_id to pick an available current-session workflow plan."),
                MCPNextActionUtils.dependsOn(MCPNextActionUtils.readResource(
                        "shardingsphere://capabilities", "Read current workflow tools, then re-run the matching planning tool if completion has no usable plan."), 1)));
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    private static Map<String, Object> createMissingUpdateExecutionModeRecovery(final MCPExecutionModeRequiredException cause) {
        return createExecutionModeRecovery("missing_execution_mode",
                "Retry the same side-effecting tool with execution_mode=preview, then execute only when the requested side effect is still intended.",
                cause.getToolName(), cause.getAllowedValues(), cause.getSuggestedArguments(), "Retry the same side-effecting tool in preview mode.", true, true);
    }
    
    private static Map<String, Object> createMissingWorkflowExecutionModeRecovery(final MCPExecutionModeRequiredException cause) {
        return createExecutionModeRecovery("missing_execution_mode",
                "Retry database_gateway_apply_workflow with execution_mode=preview, then use review-then-execute or manual-only after review.",
                cause.getToolName(), cause.getAllowedValues(), cause.getSuggestedArguments(), "Retry database_gateway_apply_workflow with execution_mode=preview first.", true, true);
    }
    
    private static Map<String, Object> createInvalidUpdateExecutionModeRecovery(final MCPInvalidExecutionModeException cause) {
        return createExecutionModeRecovery("invalid_enum_value", "Retry with execution_mode=preview or execution_mode=execute.", cause.getToolName(), cause.getAllowedValues(),
                cause.getSuggestedArguments(), "Retry database_gateway_execute_update with execution_mode=preview first.", false, false);
    }
    
    private static Map<String, Object> createInvalidWorkflowExecutionModeRecovery(final MCPInvalidExecutionModeException cause) {
        return createExecutionModeRecovery("invalid_enum_value",
                "Retry database_gateway_apply_workflow with execution_mode=preview, then use review-then-execute or manual-only after review.",
                cause.getToolName(), cause.getAllowedValues(), cause.getSuggestedArguments(), "Retry database_gateway_apply_workflow with execution_mode=preview first.", false, false);
    }
    
    private static Map<String, Object> createExecutionModeRecovery(final String category, final String modelAction, final String toolName, final List<String> allowedValues,
                                                                   final Map<String, Object> sourceSuggestedArguments, final String retryReason, final boolean missingField,
                                                                   final boolean retryTool) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(category, modelAction);
        if (missingField) {
            result.put("missing_fields", List.of(WorkflowFieldNames.EXECUTION_MODE));
        }
        result.put(MCPPayloadFieldNames.FIELD, WorkflowFieldNames.EXECUTION_MODE);
        result.put("source_tool", toolName);
        result.put("tool_name", toolName);
        result.put(MCPPayloadFieldNames.ALLOWED_VALUES, allowedValues);
        Map<String, Object> suggestedArguments = MCPRecoveryPayloadSupport.getSuggestedArguments(sourceSuggestedArguments, Map.of(WorkflowFieldNames.EXECUTION_MODE, "preview"));
        result.put("suggested_arguments", suggestedArguments);
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, List.of(retryTool
                ? MCPNextActionUtils.retryTool(toolName, retryReason, suggestedArguments)
                : MCPNextActionUtils.callTool(toolName, retryReason, suggestedArguments)));
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    private static List<String> createWorkflowArgumentConflictFields(final List<String> conflictingArguments) {
        return conflictingArguments.stream().map(MCPWorkflowRecoveryPayloadFactory::getWorkflowArgumentConflictField).distinct().toList();
    }
    
    private static List<Map<String, Object>> createWorkflowArgumentConflictQuestions(final List<String> conflictingArguments) {
        return conflictingArguments.stream().map(MCPWorkflowRecoveryPayloadFactory::createWorkflowArgumentConflictQuestion).toList();
    }
    
    private static Map<String, Object> createWorkflowArgumentConflictQuestion(final String conflict) {
        String field = getWorkflowArgumentConflictField(conflict);
        return Map.of(
                MCPPayloadFieldNames.FIELD, field,
                "conflict", conflict,
                MCPPayloadFieldNames.INPUT_TYPE, "string",
                MCPPayloadFieldNames.DISPLAY_MESSAGE, String.format("Choose one value for `%s`, or remove the duplicate path.", field));
    }
    
    private static String getWorkflowArgumentConflictField(final String conflict) {
        int conflictSeparatorIndex = conflict.indexOf(" conflicts with ");
        return 0 < conflictSeparatorIndex ? conflict.substring(0, conflictSeparatorIndex) : conflict;
    }
}
