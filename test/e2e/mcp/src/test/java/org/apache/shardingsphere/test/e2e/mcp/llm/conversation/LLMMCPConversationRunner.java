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
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatCompletion;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatMessage;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMToolCall;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LLM MCP conversation runner.
 */
public final class LLMMCPConversationRunner {
    
    private static final Pattern RESOURCE_URI_PATTERN = Pattern.compile("shardingsphere://[^`\\s,]+");
    
    private static final String PLANNING_TOOL_NAME_PREFIX = "database_gateway_plan_";
    
    private final int maxTurns;
    
    private final LLMChatModelClient llmChatClient;
    
    private final MCPInteractionClient mcpInteractionClient;
    
    private final LLMMCPActionExecutor actionExecutor;
    
    private final LLMMCPToolDefinitionFactory toolDefinitionFactory = new LLMMCPToolDefinitionFactory();
    
    private final LLMMCPFinalAnswerValidator finalAnswerValidator = new LLMMCPFinalAnswerValidator();
    
    private final LLMMCPSafetyValidator safetyValidator = new LLMMCPSafetyValidator();
    
    private final String modelProvider;
    
    private final String modelName;
    
    private final boolean recordCapabilityFingerprints;
    
    public LLMMCPConversationRunner(final int maxTurns, final LLMChatModelClient llmChatClient, final MCPInteractionClient mcpInteractionClient) {
        this(maxTurns, llmChatClient, mcpInteractionClient, "unknown", "unknown", false);
    }
    
    public LLMMCPConversationRunner(final int maxTurns, final LLMChatModelClient llmChatClient, final MCPInteractionClient mcpInteractionClient,
                                    final String modelProvider, final String modelName) {
        this(maxTurns, llmChatClient, mcpInteractionClient, modelProvider, modelName, true);
    }
    
    private LLMMCPConversationRunner(final int maxTurns, final LLMChatModelClient llmChatClient, final MCPInteractionClient mcpInteractionClient,
                                     final String modelProvider, final String modelName, final boolean recordCapabilityFingerprints) {
        this.maxTurns = maxTurns;
        this.llmChatClient = llmChatClient;
        this.mcpInteractionClient = mcpInteractionClient;
        actionExecutor = new LLMMCPActionExecutor(mcpInteractionClient);
        this.modelProvider = modelProvider;
        this.modelName = modelName;
        this.recordCapabilityFingerprints = recordCapabilityFingerprints;
    }
    
    /**
     * Run.
     *
     * @param scenario scenario
     * @return LLM E2E artifact bundle
     */
    public LLME2EArtifactBundle run(final LLME2EScenario scenario) {
        final List<LLMChatMessage> messages = createInitialMessages(scenario);
        final LLMMCPConversationArtifacts artifacts = new LLMMCPConversationArtifacts(modelProvider, modelName);
        try {
            llmChatClient.waitUntilReady();
            openInteractionClient();
            artifacts.setCapabilityFingerprints(readCapabilityFingerprintsSafely());
            boolean finalAnswerRequested = false;
            int finalAnswerAttempts = 0;
            for (int turnIndex = 0; turnIndex < maxTurns; turnIndex++) {
                finalAnswerRequested = requestFinalAnswerIfReady(scenario, messages, artifacts, finalAnswerRequested);
                final List<String> turnToolNames = finalAnswerRequested ? List.of() : createTurnToolNames(scenario, artifacts.getInteractionTrace());
                final LLMChatCompletion completion = completeTurn(scenario, messages, artifacts, finalAnswerRequested, turnToolNames);
                final LLME2EArtifactBundle toolCallFailure = processToolCallCompletion(scenario, completion, messages, artifacts, turnToolNames);
                if (null != toolCallFailure) {
                    return toolCallFailure;
                }
                if (!completion.getToolCalls().isEmpty()) {
                    continue;
                }
                if (!finalAnswerRequested) {
                    if (!LLMMCPInteractionCoverage.hasRequiredInteractionCoverage(scenario.getRequiredToolNames(), artifacts.getInteractionTrace())) {
                        messages.add(LLMChatMessage.user(createRequiredToolCallInstruction(scenario, artifacts)));
                    }
                    continue;
                }
                final FinalAnswerHandlingResult finalAnswerHandlingResult = handleFinalAnswerCompletion(scenario, completion, messages, artifacts, finalAnswerAttempts);
                finalAnswerAttempts = finalAnswerHandlingResult.finalAnswerAttempts();
                if (finalAnswerHandlingResult.retryRequested()) {
                    continue;
                }
                return finalAnswerHandlingResult.artifactBundle();
            }
            return createFailureBundle(scenario, artifacts, "missing_required_tool_coverage",
                    "Conversation exhausted turns before reaching the required tool coverage.");
        } catch (final IOException ex) {
            return createFailureBundle(scenario, artifacts, "model_service_unavailable", "Model service request failed: " + ex.getMessage());
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            return createFailureBundle(scenario, artifacts, "model_service_unavailable", "Conversation was interrupted.");
        } catch (final IllegalStateException ex) {
            final String failureType = ex.getMessage().startsWith("MCP") || ex.getMessage().startsWith("Failed to initialize MCP")
                    ? "mcp_runtime_unavailable"
                    : "model_service_unavailable";
            return createFailureBundle(scenario, artifacts, failureType, ex.getMessage());
        } finally {
            closeInteractionClient();
        }
    }
    
    private List<LLMChatMessage> createInitialMessages(final LLME2EScenario scenario) {
        List<LLMChatMessage> result = new LinkedList<>();
        result.add(LLMChatMessage.system(scenario.getSystemPrompt()));
        result.add(LLMChatMessage.user(scenario.getUserPrompt()));
        return result;
    }
    
    private boolean requestFinalAnswerIfReady(final LLME2EScenario scenario, final List<LLMChatMessage> messages, final LLMMCPConversationArtifacts artifacts,
                                              final boolean finalAnswerRequested) {
        if (finalAnswerRequested || !LLMMCPInteractionCoverage.hasRequiredInteractionCoverage(scenario.getRequiredToolNames(), artifacts.getInteractionTrace())) {
            return finalAnswerRequested;
        }
        if (hasPendingImmediateNextAction(artifacts.getInteractionTrace())) {
            return false;
        }
        if (scenario.getRequiredToolNames().contains("database_gateway_execute_query") && !hasExpectedExecuteQuery(scenario.getExpectedAnswer(), artifacts.getInteractionTrace())) {
            messages.add(LLMChatMessage.user(createExpectedQueryInstruction(scenario.getExpectedAnswer())));
            return false;
        }
        messages.add(LLMChatMessage.user(createFinalAnswerInstruction(scenario, artifacts.getInteractionTrace())));
        return true;
    }
    
    private boolean hasExpectedExecuteQuery(final LLMStructuredAnswer expectedAnswer, final List<MCPInteractionTraceRecord> interactionTrace) {
        for (int index = interactionTrace.size() - 1; index >= 0; index--) {
            MCPInteractionTraceRecord each = interactionTrace.get(index);
            if (!"database_gateway_execute_query".equals(each.getTargetName())) {
                continue;
            }
            return each.isValid()
                    && expectedAnswer.getDatabase().equals(Objects.toString(each.getArguments().get("database"), ""))
                    && isExpectedSchema(expectedAnswer, each.getArguments())
                    && normalizeComparableQuery(expectedAnswer, expectedAnswer.getQuery()).equals(
                            normalizeComparableQuery(expectedAnswer, Objects.toString(each.getArguments().get("sql"), "")));
        }
        return false;
    }
    
    private boolean isExpectedSchema(final LLMStructuredAnswer expectedAnswer, final Map<String, Object> arguments) {
        final String schema = Objects.toString(arguments.get("schema"), "").trim();
        return expectedAnswer.getSchema().equals(schema) || schema.isEmpty();
    }
    
    private String createExpectedQueryInstruction(final LLMStructuredAnswer expectedAnswer) {
        return String.format(Locale.ENGLISH,
                "Required MCP tool coverage is present, but the latest successful database_gateway_execute_query did not use database `%s`, schema `%s`, and query `%s`. "
                        + "Call database_gateway_execute_query now with exactly those arguments before returning the final JSON.",
                expectedAnswer.getDatabase(), expectedAnswer.getSchema(), expectedAnswer.getQuery());
    }
    
    private LLMChatCompletion completeTurn(final LLME2EScenario scenario, final List<LLMChatMessage> messages, final LLMMCPConversationArtifacts artifacts,
                                           final boolean finalAnswerRequested, final List<String> turnToolNames) throws IOException, InterruptedException {
        final String toolChoice = createToolChoice(scenario, artifacts, finalAnswerRequested);
        final LLMChatCompletion result = llmChatClient.complete(finalAnswerRequested ? createFinalAnswerMessages(scenario, messages, artifacts.getInteractionTrace()) : messages,
                finalAnswerRequested ? List.of() : toolDefinitionFactory.create(turnToolNames),
                toolChoice, finalAnswerRequested);
        artifacts.addRawModelOutput(result.getRawResponse());
        return result;
    }
    
    private List<String> createTurnToolNames(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!interactionTrace.isEmpty()) {
            final String immediateActionName = findImmediateNextActionName(interactionTrace.getLast());
            if (!immediateActionName.isEmpty() && scenario.getAllowedToolNames().contains(immediateActionName)) {
                return List.of(immediateActionName);
            }
        }
        if (hasSideEffectExecutionNextAction(interactionTrace)) {
            final List<String> readOnlyToolNames = findMissingReadOnlyToolNames(scenario, interactionTrace);
            if (!readOnlyToolNames.isEmpty()) {
                return List.of(readOnlyToolNames.get(0));
            }
        }
        final List<String> missingToolNames = findMissingAllowedToolNames(scenario, interactionTrace);
        if (!missingToolNames.isEmpty()) {
            return List.of(missingToolNames.get(0));
        }
        return scenario.getAllowedToolNames();
    }
    
    private List<String> findMissingAllowedToolNames(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        final List<String> result = new LinkedList<>();
        for (String each : LLMMCPInteractionCoverage.findMissingRequiredInteractionNames(scenario.getRequiredToolNames(), interactionTrace)) {
            if (scenario.getAllowedToolNames().contains(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private List<String> findMissingReadOnlyToolNames(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        final List<String> result = new LinkedList<>();
        for (String each : LLMMCPInteractionCoverage.findMissingRequiredInteractionNames(scenario.getRequiredToolNames(), interactionTrace)) {
            if (scenario.getAllowedToolNames().contains(each) && isReadOnlyToolName(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean isReadOnlyToolName(final String toolName) {
        return MCPInteractionActionNames.LIST_RESOURCES.equals(toolName)
                || MCPInteractionActionNames.READ_RESOURCE.equals(toolName)
                || MCPInteractionActionNames.LIST_PROMPTS.equals(toolName)
                || MCPInteractionActionNames.GET_PROMPT.equals(toolName)
                || MCPInteractionActionNames.COMPLETE.equals(toolName)
                || "database_gateway_search_metadata".equals(toolName)
                || "database_gateway_execute_query".equals(toolName);
    }
    
    private String createToolChoice(final LLME2EScenario scenario, final LLMMCPConversationArtifacts artifacts, final boolean finalAnswerRequested) {
        if (finalAnswerRequested) {
            return "none";
        }
        return LLMMCPInteractionCoverage.hasRequiredInteractionCoverage(scenario.getRequiredToolNames(), artifacts.getInteractionTrace()) ? "auto" : "required";
    }
    
    private LLME2EArtifactBundle processToolCallCompletion(final LLME2EScenario scenario, final LLMChatCompletion completion,
                                                           final List<LLMChatMessage> messages, final LLMMCPConversationArtifacts artifacts,
                                                           final List<String> turnToolNames) throws InterruptedException {
        if (completion.getToolCalls().isEmpty()) {
            return null;
        }
        messages.add(LLMChatMessage.assistant(completion.getContent(), completion.getToolCalls()));
        for (LLMToolCall each : completion.getToolCalls()) {
            final LLME2EArtifactBundle result = processToolCall(scenario, each, messages, artifacts, turnToolNames);
            if (null != result) {
                return result;
            }
        }
        addTraceDrivenInstruction(scenario, messages, artifacts.getInteractionTrace());
        return null;
    }
    
    private LLME2EArtifactBundle processToolCall(final LLME2EScenario scenario, final LLMToolCall toolCall, final List<LLMChatMessage> messages,
                                                 final LLMMCPConversationArtifacts artifacts, final List<String> turnToolNames) throws InterruptedException {
        if (!scenario.getAllowedToolNames().contains(toolCall.getName())) {
            artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", toolCall.getName(),
                    Map.of("rawArgumentsJson", toolCall.getArgumentsJson()), "unexpected_tool_requested"));
            return createFailureBundle(scenario, artifacts, "unexpected_tool_requested", "Model requested an unsupported tool.");
        }
        Map<String, Object> rawArguments;
        Map<String, Object> arguments;
        try {
            rawArguments = LLMMCPJsonValues.parseToolArguments(toolCall.getArgumentsJson());
            final List<String> currentToolNames = artifacts.getInteractionTrace().isEmpty()
                    ? turnToolNames
                    : createTurnToolNames(scenario, artifacts.getInteractionTrace());
            final String toolName = normalizeToolName(toolCall.getName(), rawArguments, currentToolNames);
            arguments = normalizeToolArguments(scenario, toolName, normalizeToolNameArguments(toolCall.getName(), toolName, rawArguments), artifacts.getInteractionTrace());
            return processToolCall(scenario, toolCall, messages, artifacts, rawArguments, arguments, toolName);
        } catch (final IllegalArgumentException ex) {
            artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", toolCall.getName(),
                    Map.of("rawArgumentsJson", toolCall.getArgumentsJson()), "invalid_tool_arguments"));
            return createFailureBundle(scenario, artifacts, "invalid_tool_arguments", "Model returned invalid tool arguments JSON.");
        }
    }
    
    private LLME2EArtifactBundle processToolCall(final LLME2EScenario scenario, final LLMToolCall toolCall, final List<LLMChatMessage> messages,
                                                 final LLMMCPConversationArtifacts artifacts, final Map<String, Object> rawArguments,
                                                 final Map<String, Object> arguments, final String toolName) throws InterruptedException {
        final LLME2EArtifactBundle validationFailure = validateToolCall(scenario, toolName, arguments, artifacts);
        if (null != validationFailure) {
            return validationFailure;
        }
        final long startTime = System.currentTimeMillis();
        final Map<String, Object> response = actionExecutor.executeSafely(toolName, arguments);
        final long latencyMillis = System.currentTimeMillis() - startTime;
        artifacts.addRuntimeLogLine("action=" + toolName + " args=" + JsonUtils.toJsonString(arguments));
        artifacts.addRuntimeLogLine("response=" + JsonUtils.toJsonString(response));
        String actionOrigin = toolCall.getName().equals(toolName) && rawArguments.equals(arguments)
                ? MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN
                : MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN;
        artifacts.addInteractionTrace(createTraceRecord(artifacts.nextSequence(), toolName, actionOrigin, arguments, response, latencyMillis));
        messages.add(LLMChatMessage.tool(toolCall.getId(), createModelFacingToolResponse(response)));
        return null;
    }
    
    private String normalizeToolName(final String toolName, final Map<String, Object> rawArguments, final List<String> turnToolNames) {
        return shouldRouteReadOnlySqlToOfferedQueryTool(toolName, rawArguments, turnToolNames)
                ? "database_gateway_execute_query"
                : toolName;
    }
    
    private boolean shouldRouteReadOnlySqlToOfferedQueryTool(final String toolName, final Map<String, Object> rawArguments, final List<String> turnToolNames) {
        return !turnToolNames.contains(toolName)
                && turnToolNames.equals(List.of("database_gateway_execute_query"))
                && isReadOnlySql(Objects.toString(rawArguments.get("sql"), ""));
    }
    
    private boolean isReadOnlySql(final String sql) {
        return sql.trim().toUpperCase(Locale.ENGLISH).startsWith("SELECT ");
    }
    
    private Map<String, Object> normalizeToolNameArguments(final String rawToolName, final String toolName, final Map<String, Object> rawArguments) {
        if (rawToolName.equals(toolName) || !"database_gateway_execute_query".equals(toolName)) {
            return rawArguments;
        }
        final Map<String, Object> result = new LinkedHashMap<>(rawArguments);
        result.remove("execution_mode");
        return result;
    }
    
    private String createModelFacingToolResponse(final Map<String, Object> response) {
        final Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        copyIfPresent(response, result, "response_mode");
        copyIfPresent(response, result, "error_code");
        copyIfPresent(response, result, "message");
        copyIfPresent(response, result, "recovery_category");
        copyIfPresent(response, result, "result_kind");
        copyIfPresent(response, result, "status");
        copyIfPresent(response, result, "statement_type");
        copyIfPresent(response, result, "normalized_sql");
        copyIfPresent(response, result, "rows");
        copyIfPresent(response, result, "row_objects");
        copyIfPresent(response, result, "returned_row_count");
        copyIfPresent(response, result, "plan_id");
        copyIfPresent(response, result, "workflow_resource");
        copyIfPresent(response, result, "manual_artifact_summary");
        copyIfPresent(response, result, "manual_follow_up");
        copyCompactArtifactList(response, result, "manual_artifacts");
        copyCompactArtifactList(response, result, "exported_artifacts");
        copyIfPresent(response, result, "resources_to_read");
        copyModelFacingNextActions(response, result);
        copyIfPresent(response, result, "completion");
        copyIfPresent(response, result, "count");
        copyIfPresent(response, result, "has_more");
        copyIfPresent(response, result, "total_match_count");
        copyIfPresent(response, result, "search_context");
        copyIfPresent(response, result, "ambiguity_state");
        copyPromptList(response, result);
        copyPromptMessages(response, result);
        copyCompactItems(response, result);
        copyCompactRecovery(response, result);
        return JsonUtils.toJsonString(result.isEmpty() ? response : result);
    }
    
    private void copyIfPresent(final Map<String, Object> source, final Map<String, Object> target, final String fieldName) {
        if (source.containsKey(fieldName)) {
            target.put(fieldName, source.get(fieldName));
        }
    }
    
    private void copyCompactItems(final Map<String, Object> source, final Map<String, Object> target) {
        final List<Map<String, Object>> items = LLMMCPJsonValues.castToList(source.get("items"));
        if (items.isEmpty()) {
            return;
        }
        final List<Map<String, Object>> compactItems = new LinkedList<>();
        for (Map<String, Object> each : items.subList(0, Math.min(items.size(), 5))) {
            final Map<String, Object> compactItem = new LinkedHashMap<>(8, 1F);
            copyIfPresent(each, compactItem, "database");
            copyIfPresent(each, compactItem, "schema");
            copyIfPresent(each, compactItem, "objectType");
            copyIfPresent(each, compactItem, "table");
            copyIfPresent(each, compactItem, "view");
            copyIfPresent(each, compactItem, "column");
            copyIfPresent(each, compactItem, "name");
            copyIfPresent(each, compactItem, "resource");
            compactItems.add(compactItem.isEmpty() ? each : compactItem);
        }
        target.put("items", compactItems);
    }
    
    private void copyPromptList(final Map<String, Object> source, final Map<String, Object> target) {
        final List<Map<String, Object>> prompts = LLMMCPJsonValues.castToList(source.get("prompts"));
        if (prompts.isEmpty()) {
            return;
        }
        final List<String> names = new LinkedList<>();
        for (Map<String, Object> each : prompts) {
            final String name = Objects.toString(each.get("name"), "").trim();
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        target.put("prompts", names);
    }
    
    private void copyPromptMessages(final Map<String, Object> source, final Map<String, Object> target) {
        copyIfPresent(source, target, "description");
        final List<Object> messages = LLMMCPJsonValues.castToList(source.get("messages"));
        if (!messages.isEmpty()) {
            target.put("message_count", messages.size());
        }
    }
    
    private void copyCompactArtifactList(final Map<String, Object> source, final Map<String, Object> target, final String fieldName) {
        final List<Map<String, Object>> artifacts = LLMMCPJsonValues.castToList(source.get(fieldName));
        if (artifacts.isEmpty()) {
            return;
        }
        final List<Map<String, Object>> summaries = new LinkedList<>();
        for (Map<String, Object> each : artifacts.subList(0, Math.min(artifacts.size(), 3))) {
            final Map<String, Object> summary = new LinkedHashMap<>(4, 1F);
            putArtifactCount(each, summary, "ddl_artifacts", "ddl_artifact_count");
            putArtifactCount(each, summary, "index_plan", "index_plan_count");
            putArtifactCount(each, summary, "distsql_artifacts", "distsql_artifact_count");
            if (!summary.isEmpty()) {
                summaries.add(summary);
            }
        }
        if (!summaries.isEmpty()) {
            target.put(fieldName, summaries);
        }
    }
    
    private void putArtifactCount(final Map<String, Object> source, final Map<String, Object> target, final String sourceFieldName, final String targetFieldName) {
        final List<Object> artifacts = LLMMCPJsonValues.castToList(source.get(sourceFieldName));
        if (!artifacts.isEmpty()) {
            target.put(targetFieldName, artifacts.size());
        }
    }
    
    private void copyCompactRecovery(final Map<String, Object> source, final Map<String, Object> target) {
        if (!source.containsKey("recovery")) {
            return;
        }
        final Map<String, Object> recovery = LLMMCPJsonValues.castToMap(source.get("recovery"));
        if (recovery.isEmpty()) {
            return;
        }
        final Map<String, Object> compactRecovery = new LinkedHashMap<>(8, 1F);
        copyIfPresent(recovery, compactRecovery, "recovery_category");
        copyIfPresent(recovery, compactRecovery, "category");
        copyIfPresent(recovery, compactRecovery, "model_action");
        copyIfPresent(recovery, compactRecovery, "plan_id");
        copyIfPresent(recovery, compactRecovery, "completion_first");
        copyIfPresent(recovery, compactRecovery, "suggested_arguments");
        copyIfPresent(recovery, compactRecovery, "resources_to_read");
        copyIfPresent(recovery, compactRecovery, "next_actions");
        target.put("recovery", compactRecovery);
    }
    
    private void copyModelFacingNextActions(final Map<String, Object> source, final Map<String, Object> target) {
        final List<Map<String, Object>> nextActions = LLMMCPJsonValues.castToList(source.get("next_actions"));
        if (nextActions.isEmpty()) {
            return;
        }
        final List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : nextActions) {
            result.add(isSideEffectExecutionAction(each) ? createSideEffectExecutionNextActionSummary(each) : each);
        }
        target.put("next_actions", result);
    }
    
    private Map<String, Object> createSideEffectExecutionNextActionSummary(final Map<String, Object> action) {
        final Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        copyIfPresent(action, result, "type");
        copyIfPresent(action, result, "title");
        copyIfPresent(action, result, "reason");
        return result;
    }
    
    private void addTraceDrivenInstruction(final LLME2EScenario scenario, final List<LLMChatMessage> messages, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (interactionTrace.isEmpty()) {
            return;
        }
        final String resourceReadInstruction = createResourceReadInstruction(scenario, interactionTrace);
        if (!resourceReadInstruction.isEmpty()) {
            messages.add(LLMChatMessage.user(resourceReadInstruction));
            return;
        }
        final String immediateNextActionInstruction = createImmediateNextActionInstruction(interactionTrace.getLast());
        if (!immediateNextActionInstruction.isEmpty()) {
            messages.add(LLMChatMessage.user(immediateNextActionInstruction));
            return;
        }
        if (hasSideEffectExecutionNextAction(interactionTrace)) {
            messages.add(LLMChatMessage.user(createSideEffectExecutionNextActionInstruction()));
        }
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
    
    private String findImmediateNextActionName(final MCPInteractionTraceRecord traceRecord) {
        for (Map<?, ?> each : LLMMCPNextActions.getNextActions(traceRecord.getStructuredContent())) {
            String result = findMachineNextActionName(each);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return "";
    }
    
    private String findMachineNextActionName(final Map<?, ?> action) {
        if (isSideEffectExecutionAction(action)) {
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
        if (isSideEffectExecutionAction(action)) {
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
            Object arguments = action.containsKey("arguments") ? action.get("arguments") : action;
            return String.format(Locale.ENGLISH, "The latest MCP response gave an immediate completion next_action. Call mcp_complete now with exactly these arguments: %s.",
                    JsonUtils.toJsonString(arguments));
        }
        return "";
    }
    
    private String createResourceReadInstruction(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!scenario.getRequiredToolNames().contains(MCPInteractionActionNames.READ_RESOURCE)
                || !shouldPromptExactResourceRead(interactionTrace.getLast())) {
            return "";
        }
        final String resourceUri = findExpectedResourceUri(scenario);
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
    
    private String findExpectedResourceUri(final LLME2EScenario scenario) {
        final String expectedTableResourceUri = createExpectedTableResourceUri(scenario.getExpectedAnswer());
        String promptResourceUri = "";
        Matcher matcher = RESOURCE_URI_PATTERN.matcher(scenario.getUserPrompt());
        while (matcher.find()) {
            final String each = trimResourceUri(matcher.group());
            if (each.equals(expectedTableResourceUri)) {
                return each;
            }
            if (promptResourceUri.isEmpty()) {
                promptResourceUri = each;
            }
        }
        return promptResourceUri.isEmpty() ? expectedTableResourceUri : promptResourceUri;
    }
    
    private String trimResourceUri(final String resourceUri) {
        return resourceUri.replaceAll("[.)\\]]+$", "");
    }
    
    private String createExpectedTableResourceUri(final LLMStructuredAnswer expectedAnswer) {
        return expectedAnswer.getDatabase().isEmpty() || expectedAnswer.getSchema().isEmpty() || expectedAnswer.getTable().isEmpty()
                ? ""
                : String.format(Locale.ENGLISH, "shardingsphere://databases/%s/schemas/%s/tables/%s", expectedAnswer.getDatabase(), expectedAnswer.getSchema(), expectedAnswer.getTable());
    }
    
    private boolean hasPendingImmediateNextAction(final List<MCPInteractionTraceRecord> interactionTrace) {
        return !interactionTrace.isEmpty() && !createImmediateNextActionInstruction(interactionTrace.getLast()).isEmpty();
    }
    
    private Map<String, Object> normalizeToolArguments(final LLME2EScenario scenario, final String toolName, final Map<String, Object> args,
                                                       final List<MCPInteractionTraceRecord> interactionTrace) {
        Map<String, Object> result = normalizeResourceUriArgument(scenario, toolName, args);
        result = normalizeSearchMetadataScopeArgument(scenario, toolName, result);
        result = normalizeExpectedQuerySchemaArgument(scenario, toolName, result);
        result = normalizeInitialPlanningPlanIdArgument(toolName, result, interactionTrace);
        result = normalizeWorkflowPlanIdArgument(toolName, result, interactionTrace);
        return normalizeCompletionArguments(toolName, result, interactionTrace);
    }
    
    private Map<String, Object> normalizeResourceUriArgument(final LLME2EScenario scenario, final String toolName, final Map<String, Object> args) {
        String resourceUriArgument = Objects.toString(args.get("uri"), "").trim();
        if (!MCPInteractionActionNames.READ_RESOURCE.equals(toolName) || resourceUriArgument.isEmpty() || resourceUriArgument.startsWith("shardingsphere://")) {
            return args;
        }
        String resourceUri = findExpectedResourceUri(scenario);
        if (resourceUri.isEmpty()) {
            return args;
        }
        Map<String, Object> result = new LinkedHashMap<>(args);
        result.put("uri", resourceUri);
        return result;
    }
    
    private Map<String, Object> normalizeSearchMetadataScopeArgument(final LLME2EScenario scenario, final String toolName, final Map<String, Object> args) {
        if (!"database_gateway_search_metadata".equals(toolName) || !hasExplicitExpectedScopeInstruction(scenario) || !shouldUseExpectedSearchScope(scenario.getExpectedAnswer(), args)) {
            return args;
        }
        Map<String, Object> result = new LinkedHashMap<>(args);
        putIfBlank(result, "database", scenario.getExpectedAnswer().getDatabase());
        putIfBlank(result, "schema", scenario.getExpectedAnswer().getSchema());
        return result;
    }
    
    private boolean hasExplicitExpectedScopeInstruction(final LLME2EScenario scenario) {
        final LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
        return scenario.getUserPrompt().contains(String.format(Locale.ENGLISH, "Use logical database `%s` and schema `%s`", expectedAnswer.getDatabase(), expectedAnswer.getSchema()));
    }
    
    private void putIfBlank(final Map<String, Object> values, final String key, final String value) {
        if (Objects.toString(values.get(key), "").trim().isEmpty()) {
            values.put(key, value);
        }
    }
    
    private boolean shouldUseExpectedSearchScope(final LLMStructuredAnswer expectedAnswer, final Map<String, Object> args) {
        final String query = Objects.toString(args.get("query"), "").trim();
        if (expectedAnswer.getDatabase().isEmpty() || expectedAnswer.getSchema().isEmpty() || !expectedAnswer.getTable().equalsIgnoreCase(query)) {
            return false;
        }
        return Objects.toString(args.get("database"), "").trim().isEmpty() || Objects.toString(args.get("schema"), "").trim().isEmpty();
    }
    
    private Map<String, Object> normalizeExpectedQuerySchemaArgument(final LLME2EScenario scenario, final String toolName, final Map<String, Object> args) {
        if (!"database_gateway_execute_query".equals(toolName) || !Objects.toString(args.get("schema"), "").trim().isEmpty()) {
            return args;
        }
        LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
        if (expectedAnswer.getSchema().isEmpty()
                || !expectedAnswer.getDatabase().equals(Objects.toString(args.get("database"), "").trim())
                || !normalizeComparableQuery(expectedAnswer, expectedAnswer.getQuery()).equals(normalizeComparableQuery(expectedAnswer, Objects.toString(args.get("sql"), "")))) {
            return args;
        }
        Map<String, Object> result = new LinkedHashMap<>(args.size() + 1, 1F);
        result.putAll(args);
        result.put("schema", expectedAnswer.getSchema());
        return result;
    }
    
    private Map<String, Object> normalizeInitialPlanningPlanIdArgument(final String toolName, final Map<String, Object> args, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!toolName.startsWith(PLANNING_TOOL_NAME_PREFIX) || !args.containsKey("plan_id") || !findLatestPlanId(interactionTrace).isEmpty()) {
            return args;
        }
        Map<String, Object> result = new LinkedHashMap<>(args);
        result.remove("plan_id");
        return result;
    }
    
    private Map<String, Object> normalizeWorkflowPlanIdArgument(final String toolName, final Map<String, Object> args, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!("database_gateway_apply_workflow".equals(toolName) || "database_gateway_validate_workflow".equals(toolName)) || !isPlanIdPlaceholder(args.get("plan_id"))) {
            return args;
        }
        String latestPlanId = findLatestPlanId(interactionTrace);
        if (latestPlanId.isEmpty()) {
            return args;
        }
        Map<String, Object> result = new LinkedHashMap<>(args);
        result.put("plan_id", latestPlanId);
        return result;
    }
    
    private boolean isPlanIdPlaceholder(final Object value) {
        String planId = Objects.toString(value, "").trim();
        return "plan_id".equals(planId) || "{plan_id}".equals(planId) || "<plan_id>".equals(planId) || planId.matches("\\d+");
    }
    
    private Map<String, Object> normalizeCompletionArguments(final String toolName, final Map<String, Object> args, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!MCPInteractionActionNames.COMPLETE.equals(toolName) || args.containsKey("reference")) {
            return args;
        }
        Map<String, Object> latestReference = findLatestCompletionReference(interactionTrace);
        if (latestReference.isEmpty()) {
            return args;
        }
        Map<String, Object> result = new LinkedHashMap<>(args.size() + 1, 1F);
        result.put("reference", latestReference);
        result.putAll(args);
        return result;
    }
    
    private Map<String, Object> findLatestCompletionReference(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (int index = interactionTrace.size() - 1; index >= 0; index--) {
            MCPInteractionTraceRecord each = interactionTrace.get(index);
            if (!each.isValid() || each.getStructuredContent().containsKey("error_code")) {
                continue;
            }
            Map<String, Object> result = createCompletionReference(each);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return Map.of();
    }
    
    private Map<String, Object> createCompletionReference(final MCPInteractionTraceRecord traceRecord) {
        if (MCPInteractionActionNames.GET_PROMPT.equals(traceRecord.getTargetName())) {
            String promptName = Objects.toString(traceRecord.getArguments().get("name"), "").trim();
            return promptName.isEmpty() ? Map.of() : Map.of("type", "ref/prompt", "name", promptName);
        }
        if (MCPInteractionActionNames.READ_RESOURCE.equals(traceRecord.getTargetName())) {
            String resourceUri = Objects.toString(traceRecord.getArguments().get("uri"), "").trim();
            return resourceUri.isEmpty() ? Map.of() : Map.of("type", "ref/resource", "uri", resourceUri);
        }
        return Map.of();
    }
    
    private LLME2EArtifactBundle validateToolCall(final LLME2EScenario scenario, final String toolName, final Map<String, Object> arguments,
                                                  final LLMMCPConversationArtifacts artifacts) {
        return safetyValidator.validate(toolName, arguments)
                .map(each -> {
                    artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), each.traceKind(), toolName, arguments,
                            each.failureType()));
                    return createFailureBundle(scenario, artifacts, each.failureType(), each.message());
                })
                .orElse(null);
    }
    
    private FinalAnswerHandlingResult handleFinalAnswerCompletion(final LLME2EScenario scenario, final LLMChatCompletion completion, final List<LLMChatMessage> messages,
                                                                  final LLMMCPConversationArtifacts artifacts, final int finalAnswerAttempts) {
        final int actualAttempts = finalAnswerAttempts + 1;
        artifacts.setFinalAnswerJson(completion.getContent());
        try {
            final LLMStructuredAnswer actualAnswer = LLMStructuredAnswer.fromJson(artifacts.getFinalAnswerJson());
            return FinalAnswerHandlingResult.complete(actualAttempts,
                    artifacts.createArtifactBundle(scenario, finalAnswerValidator.validateSafely(scenario, actualAnswer, artifacts.getInteractionTrace())));
        } catch (final IllegalArgumentException ex) {
            if (1 >= actualAttempts) {
                messages.add(LLMChatMessage.user("Return valid JSON only. Do not include markdown or explanation."));
                return FinalAnswerHandlingResult.retry(actualAttempts);
            }
            return FinalAnswerHandlingResult.complete(actualAttempts,
                    createFailureBundle(scenario, artifacts, "invalid_final_json", "Model did not return a valid final JSON payload."));
        }
    }
    
    private LLME2EArtifactBundle createFailureBundle(final LLME2EScenario scenario, final LLMMCPConversationArtifacts artifacts, final String failureType, final String message) {
        return artifacts.createArtifactBundle(scenario, LLME2EAssertionReport.failure(failureType, message));
    }
    
    private void openInteractionClient() throws InterruptedException {
        try {
            mcpInteractionClient.open();
        } catch (final IOException ex) {
            throw new IllegalStateException("MCP runtime failed to initialize.", ex);
        }
    }
    
    private Map<String, Object> readCapabilityFingerprintsSafely() throws InterruptedException {
        if (!recordCapabilityFingerprints) {
            return Map.of();
        }
        try {
            Map<String, Object> capabilities = mcpInteractionClient.readResource("shardingsphere://capabilities");
            return null == capabilities || !capabilities.containsKey("fingerprints") ? Map.of() : LLMMCPJsonValues.castToMap(capabilities.get("fingerprints"));
        } catch (final IOException | UnsupportedOperationException ex) {
            return Map.of();
        }
    }
    
    private void closeInteractionClient() {
        try {
            mcpInteractionClient.close();
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (final IOException ignored) {
        }
    }
    
    private List<LLMChatMessage> createFinalAnswerMessages(final LLME2EScenario scenario, final List<LLMChatMessage> messages,
                                                           final List<MCPInteractionTraceRecord> interactionTrace) {
        final List<LLMChatMessage> result = new LinkedList<>();
        result.add(LLMChatMessage.system("Return the final MCP assessment answer as valid JSON only."));
        result.add(LLMChatMessage.user(createFinalAnswerInstruction(scenario, interactionTrace)));
        final String retryInstruction = findFinalAnswerRetryInstruction(messages);
        if (!retryInstruction.isEmpty()) {
            result.add(LLMChatMessage.user(retryInstruction));
        }
        return result;
    }
    
    private String findFinalAnswerRetryInstruction(final List<LLMChatMessage> messages) {
        for (int index = messages.size() - 1; index >= 0; index--) {
            final String content = messages.get(index).getContent();
            if (content.contains("Return valid JSON only.")) {
                return content;
            }
            if (content.contains("Return JSON only with keys")) {
                return "";
            }
        }
        return "";
    }
    
    private String createFinalAnswerInstruction(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        final LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
        final String interactionSequence = JsonUtils.toJsonString(createComparableInteractionSequence(interactionTrace));
        final String totalOrders = findLatestTotalOrders(interactionTrace);
        final String prompt = "Return JSON only with keys database, schema, table, query, totalOrders, interactionSequence. "
                + "Use database `%s`, schema `%s`, table `%s`, and query `%s`; set totalOrders to `%s` from the latest successful database_gateway_execute_query result. "
                + "Set interactionSequence exactly to this JSON array: %s. "
                + "Do not add inferred, expected, available, or failed MCP action names. Required tools are `%s`.";
        return String.format(Locale.ENGLISH,
                prompt,
                expectedAnswer.getDatabase(), expectedAnswer.getSchema(), expectedAnswer.getTable(), expectedAnswer.getQuery(), totalOrders, interactionSequence,
                String.join(", ", scenario.getRequiredToolNames()));
    }
    
    private String findLatestTotalOrders(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (int index = interactionTrace.size() - 1; index >= 0; index--) {
            final MCPInteractionTraceRecord each = interactionTrace.get(index);
            if ("database_gateway_execute_query".equals(each.getTargetName()) && each.isValid()) {
                final String result = findTotalOrdersInRowObjects(each.getStructuredContent());
                return result.isEmpty() ? findTotalOrdersInRows(each.getStructuredContent()) : result;
            }
        }
        return "";
    }
    
    private String findTotalOrdersInRowObjects(final Map<String, Object> structuredContent) {
        final List<Map<String, Object>> rowObjects = LLMMCPJsonValues.castToList(structuredContent.get("row_objects"));
        if (rowObjects.isEmpty()) {
            return "";
        }
        return Objects.toString(rowObjects.get(0).get("total_orders"), "").trim();
    }
    
    private String findTotalOrdersInRows(final Map<String, Object> structuredContent) {
        final List<Object> rows = LLMMCPJsonValues.castToList(structuredContent.get("rows"));
        if (rows.isEmpty()) {
            return "";
        }
        final List<Object> row = LLMMCPJsonValues.castToList(rows.get(0));
        return row.isEmpty() ? "" : Objects.toString(row.get(0), "").trim();
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
    
    private String createRequiredToolCallInstruction(final LLME2EScenario scenario, final LLMMCPConversationArtifacts artifacts) {
        final List<String> missingToolNames = LLMMCPInteractionCoverage.findMissingRequiredInteractionNames(scenario.getRequiredToolNames(), artifacts.getInteractionTrace());
        final LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
        final String previewInstruction = missingToolNames.contains("database_gateway_execute_update")
                ? String.format(Locale.ENGLISH,
                        " For database_gateway_execute_update, set database `%s`, schema `%s`, execution_mode=preview, and keep the side-effecting SQL unchanged; do not use execution_mode=execute.",
                        expectedAnswer.getDatabase(), expectedAnswer.getSchema())
                : "";
        final String resourceInstruction = missingToolNames.contains(MCPInteractionActionNames.READ_RESOURCE)
                ? " For mcp_read_resource, use an exact shardingsphere:// URI from the user request or the latest tool response; do not invent abbreviated URI strings."
                : "";
        final String planningInstruction = hasMissingPlanningTool(missingToolNames)
                ? " For a new database_gateway_plan_* call, omit plan_id unless a previous MCP planning response returned an actual plan_id."
                : "";
        final String workflowPlanInstruction = createWorkflowPlanInstruction(missingToolNames, artifacts.getInteractionTrace());
        return String.format(Locale.ENGLISH,
                "Required MCP tool coverage is incomplete. Remaining required MCP tools: %s. "
                        + "Call one remaining tool as an actual MCP tool_call now; do not answer in text, do not write JSON, and do not write <tool_call> markup. "
                        + "If database_gateway_execute_query is remaining, set database `%s`, schema `%s`, and sql `%s`.%s%s%s%s",
                String.join(", ", missingToolNames), expectedAnswer.getDatabase(), expectedAnswer.getSchema(), expectedAnswer.getQuery(), previewInstruction, resourceInstruction,
                planningInstruction, workflowPlanInstruction);
    }
    
    private boolean hasMissingPlanningTool(final List<String> missingToolNames) {
        return missingToolNames.stream().anyMatch(each -> each.startsWith(PLANNING_TOOL_NAME_PREFIX));
    }
    
    private String createSideEffectExecutionNextActionInstruction() {
        return "The latest MCP response contains side-effect execution next_actions; do not execute them in this score lane. Continue only with remaining read-only verification.";
    }
    
    private boolean hasSideEffectExecutionNextAction(final List<MCPInteractionTraceRecord> interactionTrace) {
        return !interactionTrace.isEmpty() && LLMMCPNextActions.getNextActions(interactionTrace.getLast().getStructuredContent()).stream().anyMatch(this::isSideEffectExecutionAction);
    }
    
    private boolean isSideEffectExecutionAction(final Map<?, ?> action) {
        if (!"tool_call".equals(Objects.toString(action.get("type"), "").trim())) {
            return false;
        }
        final String toolName = Objects.toString(action.get("tool_name"), "").trim();
        final Map<String, Object> arguments = LLMMCPJsonValues.castToMap(action.get("arguments"));
        final String executionMode = Objects.toString(arguments.get("execution_mode"), "").trim();
        return "database_gateway_execute_update".equals(toolName) && "execute".equals(executionMode)
                || "database_gateway_apply_workflow".equals(toolName) && "review-then-execute".equals(executionMode);
    }
    
    private String createWorkflowPlanInstruction(final List<String> missingToolNames, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!missingToolNames.contains("database_gateway_apply_workflow") && !missingToolNames.contains("database_gateway_validate_workflow")) {
            return "";
        }
        String latestPlanId = findLatestPlanId(interactionTrace);
        return latestPlanId.isEmpty()
                ? " For database_gateway_apply_workflow or database_gateway_validate_workflow, use an actual plan_id returned by a successful planning tool call; "
                        + "do not use placeholder text `plan_id`."
                : String.format(Locale.ENGLISH,
                        " For database_gateway_apply_workflow or database_gateway_validate_workflow, set plan_id `%s`; do not use placeholder text `plan_id`.", latestPlanId);
    }
    
    private String findLatestPlanId(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (int index = interactionTrace.size() - 1; index >= 0; index--) {
            MCPInteractionTraceRecord each = interactionTrace.get(index);
            String result = Objects.toString(each.getStructuredContent().get("plan_id"), "").trim();
            if (each.isValid() && !result.isEmpty() && !each.getStructuredContent().containsKey("error_code")) {
                return result;
            }
        }
        return "";
    }
    
    private String normalizeComparableQuery(final String query) {
        return query.replaceAll("\\s+", " ").trim().toUpperCase(Locale.ENGLISH);
    }
    
    private String normalizeComparableQuery(final LLMStructuredAnswer expectedAnswer, final String query) {
        final String result = normalizeComparableQuery(query);
        if (expectedAnswer.getSchema().isEmpty() || expectedAnswer.getTable().isEmpty()) {
            return result;
        }
        return result.replaceAll("\\b" + Pattern.quote(expectedAnswer.getSchema().toUpperCase(Locale.ENGLISH)) + "\\."
                + Pattern.quote(expectedAnswer.getTable().toUpperCase(Locale.ENGLISH)) + "\\b", Matcher.quoteReplacement(expectedAnswer.getTable().toUpperCase(Locale.ENGLISH)));
    }
    
    private MCPInteractionTraceRecord createTraceRecord(final int sequence, final String actionName, final String actionOrigin, final Map<String, Object> arguments,
                                                        final Map<String, Object> structuredContent, final long latencyMillis) {
        String bridgeActionOrigin = toBridgeActionOrigin(actionOrigin);
        if (MCPInteractionActionNames.LIST_RESOURCES.equals(actionName)) {
            return new MCPInteractionTraceRecord(sequence, MCPInteractionActionNames.RESOURCE_LIST_KIND, bridgeActionOrigin, MCPInteractionActionNames.LIST_RESOURCES,
                    Map.of(), structuredContent, true, latencyMillis);
        }
        if (MCPInteractionActionNames.READ_RESOURCE.equals(actionName)) {
            return new MCPInteractionTraceRecord(sequence, MCPInteractionActionNames.RESOURCE_READ_KIND, bridgeActionOrigin, MCPInteractionActionNames.READ_RESOURCE,
                    Map.of("uri", Objects.toString(arguments.get("uri"), "").trim()), structuredContent, true, latencyMillis);
        }
        if (MCPInteractionActionNames.LIST_PROMPTS.equals(actionName)) {
            return new MCPInteractionTraceRecord(sequence, MCPInteractionActionNames.PROMPT_LIST_KIND, bridgeActionOrigin, MCPInteractionActionNames.LIST_PROMPTS,
                    Map.of(), structuredContent, true, latencyMillis);
        }
        if (MCPInteractionActionNames.GET_PROMPT.equals(actionName)) {
            return new MCPInteractionTraceRecord(sequence, MCPInteractionActionNames.PROMPT_GET_KIND, bridgeActionOrigin, MCPInteractionActionNames.GET_PROMPT,
                    Map.of("name", Objects.toString(arguments.get("name"), "").trim(), "arguments", LLMMCPJsonValues.castToMap(arguments.getOrDefault("arguments", Map.of()))),
                    structuredContent, true, latencyMillis);
        }
        if (MCPInteractionActionNames.COMPLETE.equals(actionName)) {
            return new MCPInteractionTraceRecord(sequence, MCPInteractionActionNames.COMPLETION_KIND, bridgeActionOrigin, MCPInteractionActionNames.COMPLETE,
                    arguments, structuredContent, true, latencyMillis);
        }
        return new MCPInteractionTraceRecord(sequence, "tool_call", actionOrigin, actionName, arguments, structuredContent, true, latencyMillis);
    }
    
    private String toBridgeActionOrigin(final String actionOrigin) {
        return MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN.equals(actionOrigin)
                ? MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN
                : MCPInteractionTraceRecord.PROTOCOL_BRIDGE_ORIGIN;
    }
    
    private record FinalAnswerHandlingResult(int finalAnswerAttempts, boolean retryRequested, LLME2EArtifactBundle artifactBundle) {

        private static FinalAnswerHandlingResult retry(final int finalAnswerAttempts) {
            return new FinalAnswerHandlingResult(finalAnswerAttempts, true, null);
        }

        private static FinalAnswerHandlingResult complete(final int finalAnswerAttempts, final LLME2EArtifactBundle artifactBundle) {
            return new FinalAnswerHandlingResult(finalAnswerAttempts, false, artifactBundle);
        }
    }
}
