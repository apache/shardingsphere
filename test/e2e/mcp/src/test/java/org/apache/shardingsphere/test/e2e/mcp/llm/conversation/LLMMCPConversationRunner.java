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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * LLM MCP conversation runner.
 */
public final class LLMMCPConversationRunner {
    
    private final int maxTurns;
    
    private final LLMChatModelClient llmChatClient;
    
    private final MCPInteractionClient mcpInteractionClient;
    
    private final LLMMCPActionExecutor actionExecutor;
    
    private final LLMMCPToolDefinitionFactory toolDefinitionFactory = new LLMMCPToolDefinitionFactory();
    
    private final LLMMCPFinalAnswerValidator finalAnswerValidator = new LLMMCPFinalAnswerValidator();
    
    private final LLMMCPSafetyValidator safetyValidator = new LLMMCPSafetyValidator();
    
    private final String modelProvider;
    
    private final String modelName;
    
    public LLMMCPConversationRunner(final int maxTurns, final LLMChatModelClient llmChatClient, final MCPInteractionClient mcpInteractionClient) {
        this(maxTurns, llmChatClient, mcpInteractionClient, "unknown", "unknown");
    }
    
    public LLMMCPConversationRunner(final int maxTurns, final LLMChatModelClient llmChatClient, final MCPInteractionClient mcpInteractionClient,
                                    final String modelProvider, final String modelName) {
        this.maxTurns = maxTurns;
        this.llmChatClient = llmChatClient;
        this.mcpInteractionClient = mcpInteractionClient;
        actionExecutor = new LLMMCPActionExecutor(mcpInteractionClient);
        this.modelProvider = modelProvider;
        this.modelName = modelName;
    }
    
    /**
     * Run.
     *
     * @param scenario scenario
     * @return LLM E2E artifact bundle
     */
    public LLME2EArtifactBundle run(final LLME2EScenario scenario) {
        List<LLMChatMessage> messages = createInitialMessages(scenario);
        LLMMCPConversationArtifacts artifacts = new LLMMCPConversationArtifacts(modelProvider, modelName);
        try {
            llmChatClient.waitUntilReady();
            openInteractionClient();
            boolean finalAnswerRequested = false;
            int finalAnswerAttempts = 0;
            for (int turnIndex = 0; turnIndex < maxTurns; turnIndex++) {
                finalAnswerRequested = requestFinalAnswerIfReady(scenario, messages, artifacts, finalAnswerRequested);
                List<String> turnToolNames = finalAnswerRequested ? List.of() : createTurnToolNames(scenario, artifacts.getInteractionTrace());
                LLMChatCompletion completion = completeTurn(scenario, messages, artifacts, finalAnswerRequested, turnToolNames);
                Optional<LLME2EArtifactBundle> toolCallFailure = processToolCallCompletion(scenario, completion, messages, artifacts, turnToolNames);
                if (toolCallFailure.isPresent()) {
                    return toolCallFailure.get();
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
                FinalAnswerHandlingResult finalAnswerHandlingResult = handleFinalAnswerCompletion(scenario, completion, messages, artifacts, finalAnswerAttempts);
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
            String failureType = ex.getMessage().startsWith("MCP") || ex.getMessage().startsWith("Failed to initialize MCP")
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
                    && LLMMCPScenarioInference.normalizeComparableQuery(expectedAnswer, expectedAnswer.getQuery()).equals(
                            LLMMCPScenarioInference.normalizeComparableQuery(expectedAnswer, Objects.toString(each.getArguments().get("sql"), "")));
        }
        return false;
    }
    
    private boolean isExpectedSchema(final LLMStructuredAnswer expectedAnswer, final Map<String, Object> arguments) {
        String schema = Objects.toString(arguments.get("schema"), "").trim();
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
        String toolChoice = createToolChoice(scenario, artifacts, finalAnswerRequested);
        LLMChatCompletion result = llmChatClient.complete(finalAnswerRequested ? createFinalAnswerMessages(scenario, messages, artifacts.getInteractionTrace()) : messages,
                finalAnswerRequested ? List.of() : toolDefinitionFactory.create(turnToolNames),
                toolChoice, finalAnswerRequested);
        artifacts.addRawModelOutput(result.getRawResponse());
        return result;
    }
    
    private List<String> createTurnToolNames(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!interactionTrace.isEmpty()) {
            String immediateActionName = findImmediateNextActionName(interactionTrace.getLast());
            if (!immediateActionName.isEmpty() && scenario.getAllowedToolNames().contains(immediateActionName)) {
                return List.of(immediateActionName);
            }
        }
        if (hasSideEffectExecutionNextAction(interactionTrace)) {
            List<String> readOnlyToolNames = findMissingReadOnlyToolNames(scenario, interactionTrace);
            if (!readOnlyToolNames.isEmpty()) {
                return List.of(readOnlyToolNames.getFirst());
            }
        }
        List<String> missingToolNames = findMissingAllowedToolNames(scenario, interactionTrace);
        if (!missingToolNames.isEmpty()) {
            return List.of(missingToolNames.getFirst());
        }
        return scenario.getAllowedToolNames();
    }
    
    private List<String> findMissingAllowedToolNames(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        List<String> result = new LinkedList<>();
        for (String each : LLMMCPInteractionCoverage.findMissingRequiredInteractionNames(scenario.getRequiredToolNames(), interactionTrace)) {
            if (scenario.getAllowedToolNames().contains(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private List<String> findMissingReadOnlyToolNames(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        List<String> result = new LinkedList<>();
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
    
    private Optional<LLME2EArtifactBundle> processToolCallCompletion(final LLME2EScenario scenario, final LLMChatCompletion completion,
                                                                     final List<LLMChatMessage> messages, final LLMMCPConversationArtifacts artifacts,
                                                                     final List<String> turnToolNames) throws InterruptedException {
        if (completion.getToolCalls().isEmpty()) {
            return Optional.empty();
        }
        messages.add(LLMChatMessage.assistant(completion.getContent(), completion.getToolCalls()));
        for (LLMToolCall each : completion.getToolCalls()) {
            Optional<LLME2EArtifactBundle> result = processToolCall(scenario, each, messages, artifacts, turnToolNames);
            if (result.isPresent()) {
                return result;
            }
        }
        addTraceDrivenInstruction(scenario, messages, artifacts.getInteractionTrace());
        return Optional.empty();
    }
    
    private Optional<LLME2EArtifactBundle> processToolCall(final LLME2EScenario scenario, final LLMToolCall toolCall, final List<LLMChatMessage> messages,
                                                           final LLMMCPConversationArtifacts artifacts, final List<String> turnToolNames) throws InterruptedException {
        if (!scenario.getAllowedToolNames().contains(toolCall.getName())) {
            artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", toolCall.getName(),
                    Map.of("rawArgumentsJson", toolCall.getArgumentsJson()), "unexpected_tool_requested"));
            return Optional.of(createFailureBundle(scenario, artifacts, "unexpected_tool_requested", "Model requested an unsupported tool."));
        }
        try {
            Map<String, Object> rawArguments = LLMMCPJsonValues.parseToolArguments(toolCall.getArgumentsJson());
            List<String> currentToolNames = artifacts.getInteractionTrace().isEmpty()
                    ? turnToolNames
                    : createTurnToolNames(scenario, artifacts.getInteractionTrace());
            LLMMCPToolCallNormalizer.NormalizedToolCall normalizedToolCall = LLMMCPToolCallNormalizer.normalize(scenario, toolCall.getName(), rawArguments, currentToolNames,
                    artifacts.getInteractionTrace());
            return processToolCall(scenario, toolCall, messages, artifacts, rawArguments, normalizedToolCall.arguments(), normalizedToolCall.name());
        } catch (final IllegalArgumentException ex) {
            artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", toolCall.getName(),
                    Map.of("rawArgumentsJson", toolCall.getArgumentsJson()), "invalid_tool_arguments"));
            return Optional.of(createFailureBundle(scenario, artifacts, "invalid_tool_arguments", "Model returned invalid tool arguments JSON."));
        }
    }
    
    private Optional<LLME2EArtifactBundle> processToolCall(final LLME2EScenario scenario, final LLMToolCall toolCall, final List<LLMChatMessage> messages,
                                                           final LLMMCPConversationArtifacts artifacts, final Map<String, Object> rawArguments,
                                                           final Map<String, Object> arguments, final String toolName) throws InterruptedException {
        Optional<LLME2EArtifactBundle> validationFailure = validateToolCall(scenario, toolName, arguments, artifacts);
        if (validationFailure.isPresent()) {
            return validationFailure;
        }
        long startTime = System.currentTimeMillis();
        Map<String, Object> response = actionExecutor.executeSafely(toolName, arguments);
        long latencyMillis = System.currentTimeMillis() - startTime;
        artifacts.addRuntimeLogLine("action=" + toolName + " args=" + JsonUtils.toJsonString(arguments));
        artifacts.addRuntimeLogLine("response=" + JsonUtils.toJsonString(response));
        String actionOrigin = toolCall.getName().equals(toolName) && rawArguments.equals(arguments)
                ? MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN
                : MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN;
        artifacts.addInteractionTrace(createTraceRecord(artifacts.nextSequence(), toolName, actionOrigin, arguments, response, latencyMillis));
        messages.add(LLMChatMessage.tool(toolCall.getId(), LLMMCPModelFacingToolResponseFormatter.format(response)));
        return Optional.empty();
    }
    
    private void addTraceDrivenInstruction(final LLME2EScenario scenario, final List<LLMChatMessage> messages, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (interactionTrace.isEmpty()) {
            return;
        }
        String resourceReadInstruction = createResourceReadInstruction(scenario, interactionTrace);
        if (!resourceReadInstruction.isEmpty()) {
            messages.add(LLMChatMessage.user(resourceReadInstruction));
            return;
        }
        String immediateNextActionInstruction = createImmediateNextActionInstruction(interactionTrace.getLast());
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
    
    private boolean hasPendingImmediateNextAction(final List<MCPInteractionTraceRecord> interactionTrace) {
        return !interactionTrace.isEmpty() && !createImmediateNextActionInstruction(interactionTrace.getLast()).isEmpty();
    }
    
    private Optional<LLME2EArtifactBundle> validateToolCall(final LLME2EScenario scenario, final String toolName, final Map<String, Object> arguments,
                                                            final LLMMCPConversationArtifacts artifacts) {
        return safetyValidator.validate(toolName, arguments)
                .map(optional -> {
                    artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), optional.traceKind(), toolName, arguments,
                            optional.failureType()));
                    return createFailureBundle(scenario, artifacts, optional.failureType(), optional.message());
                });
    }
    
    private FinalAnswerHandlingResult handleFinalAnswerCompletion(final LLME2EScenario scenario, final LLMChatCompletion completion, final List<LLMChatMessage> messages,
                                                                  final LLMMCPConversationArtifacts artifacts, final int finalAnswerAttempts) {
        int actualAttempts = finalAnswerAttempts + 1;
        artifacts.setFinalAnswerJson(completion.getContent());
        try {
            LLMStructuredAnswer actualAnswer = LLMStructuredAnswer.fromJson(artifacts.getFinalAnswerJson());
            return new FinalAnswerHandlingResult(actualAttempts, false,
                    artifacts.createArtifactBundle(scenario, finalAnswerValidator.validateSafely(scenario, actualAnswer, artifacts.getInteractionTrace())));
        } catch (final IllegalArgumentException ex) {
            if (1 >= actualAttempts) {
                messages.add(LLMChatMessage.user("Return valid JSON only. Do not include markdown or explanation."));
                return new FinalAnswerHandlingResult(actualAttempts, true, null);
            }
            return new FinalAnswerHandlingResult(actualAttempts, false,
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
        List<LLMChatMessage> result = new LinkedList<>();
        result.add(LLMChatMessage.system("Return the final MCP assessment answer as valid JSON only."));
        result.add(LLMChatMessage.user(createFinalAnswerInstruction(scenario, interactionTrace)));
        String retryInstruction = findFinalAnswerRetryInstruction(messages);
        if (!retryInstruction.isEmpty()) {
            result.add(LLMChatMessage.user(retryInstruction));
        }
        return result;
    }
    
    private String findFinalAnswerRetryInstruction(final List<LLMChatMessage> messages) {
        for (int index = messages.size() - 1; index >= 0; index--) {
            String content = messages.get(index).getContent();
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
    
    private String createRequiredToolCallInstruction(final LLME2EScenario scenario, final LLMMCPConversationArtifacts artifacts) {
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
    
    private boolean hasMissingPlanningTool(final List<String> missingToolNames) {
        return missingToolNames.stream().anyMatch(each -> each.startsWith(LLMMCPScenarioInference.PLANNING_TOOL_NAME_PREFIX));
    }
    
    private String createSideEffectExecutionNextActionInstruction() {
        return "The latest MCP response contains side-effect execution next_actions; do not execute them in this score lane. Continue only with remaining read-only verification.";
    }
    
    private boolean hasSideEffectExecutionNextAction(final List<MCPInteractionTraceRecord> interactionTrace) {
        return !interactionTrace.isEmpty() && LLMMCPNextActions.getNextActions(interactionTrace.getLast().getStructuredContent()).stream().anyMatch(LLMMCPSideEffectNextAction::isExecutionAction);
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
    }
}
