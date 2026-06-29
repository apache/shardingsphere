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

import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatMessage;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

final class LLMMCPConversationInstructionFactory {
    
    String createExpectedQueryInstruction(final LLMStructuredAnswer expectedAnswer) {
        return String.format(Locale.ENGLISH,
                "Required MCP tool coverage is present, but the latest successful database_gateway_execute_query did not use database `%s`, schema `%s`, and query `%s`. "
                        + "Call database_gateway_execute_query now with exactly those arguments before returning the final JSON.",
                expectedAnswer.getDatabase(), expectedAnswer.getSchema(), expectedAnswer.getQuery());
    }
    
    String createTraceDrivenInstruction(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (interactionTrace.isEmpty()) {
            return "";
        }
        String resourceReadInstruction = createResourceReadInstruction(scenario, interactionTrace);
        if (!resourceReadInstruction.isEmpty()) {
            return resourceReadInstruction;
        }
        String immediateNextActionInstruction = createImmediateNextActionInstruction(interactionTrace.getLast());
        if (!immediateNextActionInstruction.isEmpty()) {
            return immediateNextActionInstruction;
        }
        return hasSideEffectExecutionNextAction(interactionTrace) ? createSideEffectExecutionNextActionInstruction(scenario.getExpectedAnswer()) : "";
    }
    
    boolean hasPendingImmediateNextAction(final List<MCPInteractionTraceRecord> interactionTrace) {
        return !interactionTrace.isEmpty() && !findImmediateNextActionName(interactionTrace.getLast()).isEmpty();
    }
    
    String findImmediateNextActionName(final MCPInteractionTraceRecord traceRecord) {
        for (Map<?, ?> each : LLMMCPNextActions.getNextActions(traceRecord.getStructuredContent())) {
            String result = findMachineNextActionName(each);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return "";
    }
    
    boolean hasSideEffectExecutionNextAction(final List<MCPInteractionTraceRecord> interactionTrace) {
        return !interactionTrace.isEmpty() && LLMMCPNextActions.getNextActions(interactionTrace.getLast().getStructuredContent()).stream().anyMatch(LLMMCPSideEffectNextAction::isExecutionAction);
    }
    
    List<LLMChatMessage> createFinalAnswerMessages(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        List<LLMChatMessage> result = new LinkedList<>();
        result.add(LLMChatMessage.system("Return the final MCP assessment answer as valid JSON only."));
        result.add(LLMChatMessage.user(createFinalAnswerInstruction(scenario, interactionTrace)));
        return result;
    }
    
    String createFinalAnswerInstruction(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
        String interactionSequence = JsonUtils.toJsonString(createComparableInteractionSequence(interactionTrace));
        String totalOrders = findLatestTotalOrders(interactionTrace);
        String prompt = "Return JSON only with keys database, schema, table, query, totalOrders, interactionSequence. "
                + "Use database `%s`, schema `%s`, table `%s`, and query `%s`; set totalOrders to `%s` from the latest successful database_gateway_execute_query result. "
                + "Set interactionSequence exactly to this JSON array: %s. "
                + "Do not add inferred, expected, available, or failed MCP action names. Required tools are `%s`.";
        return String.format(Locale.ENGLISH,
                prompt,
                expectedAnswer.getDatabase(), expectedAnswer.getSchema(), expectedAnswer.getTable(), expectedAnswer.getQuery(), totalOrders, interactionSequence,
                String.join(", ", scenario.getRequiredToolNames()));
    }
    
    String createRequiredToolCallInstruction(final LLME2EScenario scenario, final LLMMCPConversationArtifacts artifacts) {
        List<String> missingToolNames = LLMMCPInteractionCoverage.findMissingRequiredInteractionNames(scenario.getRequiredToolNames(), artifacts.getInteractionTrace());
        LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
        String previewInstruction = missingToolNames.contains("database_gateway_execute_update")
                ? String.format(Locale.ENGLISH,
                        " For database_gateway_execute_update, set database `%s`, schema `%s`, execution_mode=preview, and keep the side-effecting SQL unchanged; do not use execution_mode=execute.",
                        expectedAnswer.getDatabase(), expectedAnswer.getSchema())
                : "";
        String resourceInstruction = missingToolNames.contains(MCPInteractionActionNames.READ_RESOURCE)
                ? " For mcp_read_resource, use an exact shardingsphere:// URI from the user request or the latest tool response; do not invent abbreviated URI strings."
                : "";
        String planningInstruction = hasMissingPlanningTool(missingToolNames)
                ? " For a new database_gateway_plan_* call, omit plan_id unless a previous MCP planning response returned an actual plan_id."
                : "";
        String workflowPlanInstruction = createWorkflowPlanInstruction(missingToolNames, artifacts.getInteractionTrace());
        return String.format(Locale.ENGLISH,
                "Required MCP tool coverage is incomplete. Remaining required MCP tools: %s. "
                        + "Call one remaining tool as an actual MCP tool_call now; do not answer in text, do not write JSON, and do not write <tool_call> markup. "
                        + "If database_gateway_execute_query is remaining, set database `%s`, schema `%s`, and sql `%s`.%s%s%s%s",
                String.join(", ", missingToolNames), expectedAnswer.getDatabase(), expectedAnswer.getSchema(), expectedAnswer.getQuery(), previewInstruction, resourceInstruction,
                planningInstruction, workflowPlanInstruction);
    }
    
    private String createImmediateNextActionInstruction(final MCPInteractionTraceRecord traceRecord) {
        for (Map<?, ?> each : LLMMCPNextActions.getNextActions(traceRecord.getStructuredContent())) {
            String result = createMachineNextActionInstruction(each);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return "";
    }
    
    private String findMachineNextActionName(final Map<?, ?> action) {
        if (LLMMCPSideEffectNextAction.isExecutionAction(action)) {
            return "";
        }
        String actionType = Objects.toString(action.get("type"), "").trim();
        if ("resource_read".equals(actionType) && !Objects.toString(action.get("resource_uri"), "").trim().isEmpty()) {
            return MCPInteractionActionNames.READ_RESOURCE;
        }
        if ("tool_call".equals(actionType)) {
            return Objects.toString(action.get("tool_name"), "").trim();
        }
        return "completion".equals(actionType) ? MCPInteractionActionNames.COMPLETE : "";
    }
    
    private String createMachineNextActionInstruction(final Map<?, ?> action) {
        if (LLMMCPSideEffectNextAction.isExecutionAction(action)) {
            return "";
        }
        String actionType = Objects.toString(action.get("type"), "").trim();
        if ("resource_read".equals(actionType)) {
            String resourceUri = Objects.toString(action.get("resource_uri"), "").trim();
            return resourceUri.isEmpty()
                    ? ""
                    : String.format(Locale.ENGLISH,
                            "The latest MCP response gave a read-only next_action. Call mcp_read_resource with uri `%s` now before any other MCP action or final answer.", resourceUri);
        }
        if ("tool_call".equals(actionType)) {
            String toolName = Objects.toString(action.get("tool_name"), "").trim();
            return toolName.isEmpty()
                    ? ""
                    : String.format(Locale.ENGLISH, "The latest MCP response gave an immediate next_action. Call `%s` now with exactly these arguments: %s. Do not replace values with placeholders.",
                            toolName, JsonUtils.toJsonString(action.containsKey("arguments") ? action.get("arguments") : Map.of()));
        }
        if ("completion".equals(actionType)) {
            Object arguments = action.containsKey("arguments") ? action.get("arguments") : createCompletionArguments(action);
            return String.format(Locale.ENGLISH, "The latest MCP response gave an immediate completion next_action. Call mcp_complete now with exactly these arguments: %s.",
                    JsonUtils.toJsonString(arguments));
        }
        return "";
    }
    
    private Map<String, Object> createCompletionArguments(final Map<?, ?> action) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("ref", action.get("ref"));
        result.put("argument", action.get("argument"));
        if (action.containsKey("context")) {
            result.put("context", action.get("context"));
        }
        return result;
    }
    
    private String createResourceReadInstruction(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!scenario.getRequiredToolNames().contains(MCPInteractionActionNames.READ_RESOURCE)
                || !shouldPromptExactResourceRead(interactionTrace.getLast())) {
            return "";
        }
        String resourceUri = LLMMCPScenarioInference.findExpectedResourceUri(scenario);
        return resourceUri.isEmpty() || hasReadResource(resourceUri, interactionTrace)
                ? ""
                : String.format(Locale.ENGLISH,
                        "The remaining required resource action is mcp_read_resource. Use exactly `%s` as uri; do not copy parameter schema or placeholder text as uri.", resourceUri);
    }
    
    private boolean shouldPromptExactResourceRead(final MCPInteractionTraceRecord traceRecord) {
        return MCPInteractionActionNames.LIST_RESOURCES.equals(traceRecord.getTargetName())
                || MCPInteractionActionNames.READ_RESOURCE.equals(traceRecord.getTargetName())
                        && (traceRecord.getStructuredContent().containsKey("error_code") || Boolean.FALSE.equals(traceRecord.getStructuredContent().get("found")));
    }
    
    private boolean hasReadResource(final String resourceUri, final List<MCPInteractionTraceRecord> interactionTrace) {
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (MCPInteractionActionNames.RESOURCE_READ_KIND.equals(each.getActionKind()) && resourceUri.equals(each.getArguments().get("uri"))) {
                return true;
            }
        }
        return false;
    }
    
    private String findLatestTotalOrders(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (int index = interactionTrace.size() - 1; index >= 0; index--) {
            MCPInteractionTraceRecord each = interactionTrace.get(index);
            if ("database_gateway_execute_query".equals(each.getTargetName()) && each.isValid()) {
                String result = findTotalOrdersInRowObjects(each.getStructuredContent());
                return result.isEmpty() ? findTotalOrdersInRows(each.getStructuredContent()) : result;
            }
        }
        return "";
    }
    
    private String findTotalOrdersInRowObjects(final Map<String, Object> structuredContent) {
        List<Map<String, Object>> rowObjects = LLMMCPJsonValues.castToList(structuredContent.get("row_objects"));
        if (rowObjects.isEmpty()) {
            return "";
        }
        return Objects.toString(rowObjects.getFirst().get("total_orders"), "").trim();
    }
    
    private String findTotalOrdersInRows(final Map<String, Object> structuredContent) {
        List<Object> rows = LLMMCPJsonValues.castToList(structuredContent.get("rows"));
        if (rows.isEmpty()) {
            return "";
        }
        List<Object> row = LLMMCPJsonValues.castToList(rows.getFirst());
        return row.isEmpty() ? "" : Objects.toString(row.getFirst(), "").trim();
    }
    
    private List<String> createComparableInteractionSequence(final List<MCPInteractionTraceRecord> interactionTrace) {
        List<String> result = new LinkedList<>();
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (result.isEmpty() || !result.getLast().equals(each.getTargetName())) {
                result.add(each.getTargetName());
            }
        }
        return result;
    }
    
    private boolean hasMissingPlanningTool(final List<String> missingToolNames) {
        return missingToolNames.stream().anyMatch(each -> each.startsWith(LLMMCPScenarioInference.PLANNING_TOOL_NAME_PREFIX));
    }
    
    private String createSideEffectExecutionNextActionInstruction(final LLMStructuredAnswer expectedAnswer) {
        return String.format(Locale.ENGLISH,
                "The latest MCP response contains side-effect execution next_actions; do not execute them in this score lane. "
                        + "Call database_gateway_execute_query now with database `%s`, schema `%s`, and sql `%s`. "
                        + "Do not call database_gateway_execute_update for SELECT or row-count verification.",
                expectedAnswer.getDatabase(), expectedAnswer.getSchema(), expectedAnswer.getQuery());
    }
    
    private String createWorkflowPlanInstruction(final List<String> missingToolNames, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!missingToolNames.contains("database_gateway_apply_workflow") && !missingToolNames.contains("database_gateway_validate_workflow")) {
            return "";
        }
        String latestPlanId = LLMMCPScenarioInference.findLatestPlanId(interactionTrace);
        return latestPlanId.isEmpty()
                ? " For database_gateway_apply_workflow or database_gateway_validate_workflow, use an actual plan_id returned by a successful planning tool call; "
                        + "do not use placeholder text `plan_id`."
                : String.format(Locale.ENGLISH,
                        " For database_gateway_apply_workflow or database_gateway_validate_workflow, set plan_id `%s`; do not use placeholder text `plan_id`.", latestPlanId);
    }
}
