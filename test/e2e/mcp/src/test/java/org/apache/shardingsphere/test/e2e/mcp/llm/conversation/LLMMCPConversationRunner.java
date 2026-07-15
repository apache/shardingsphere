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
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;

import java.io.IOException;
import java.util.LinkedHashMap;
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
    
    private final LLMMCPTraceRecordFactory traceRecordFactory = new LLMMCPTraceRecordFactory();
    
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
            openInteractionClient();
            LLMMCPConversationInstructionFactory instructionFactory = new LLMMCPConversationInstructionFactory();
            LLMMCPConversationTurnPlanner turnPlanner = new LLMMCPConversationTurnPlanner();
            boolean finalAnswerRequested = false;
            for (int turnIndex = 0; turnIndex < maxTurns; turnIndex++) {
                finalAnswerRequested = requestFinalAnswerIfReady(scenario, messages, artifacts, finalAnswerRequested, instructionFactory);
                List<String> turnToolNames = finalAnswerRequested ? List.of() : turnPlanner.createTurnToolNames(scenario, artifacts.getInteractionTrace());
                LLMChatCompletion completion = completeTurn(scenario, messages, artifacts, finalAnswerRequested, turnToolNames, instructionFactory, turnPlanner);
                Optional<LLME2EArtifactBundle> toolCallFailure = processToolCallCompletion(scenario, completion, messages, artifacts, instructionFactory, turnPlanner);
                if (toolCallFailure.isPresent()) {
                    return toolCallFailure.get();
                }
                if (!completion.getToolCalls().isEmpty()) {
                    continue;
                }
                if (!finalAnswerRequested) {
                    if (!LLMMCPInteractionCoverage.hasRequiredInteractionCoverage(scenario.getRequiredToolNames(), artifacts.getInteractionTrace())) {
                        messages.add(LLMChatMessage.user(instructionFactory.createRequiredToolCallInstruction(scenario, artifacts)));
                    }
                    continue;
                }
                return handleFinalAnswerCompletion(scenario, completion, artifacts);
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
                                              final boolean finalAnswerRequested, final LLMMCPConversationInstructionFactory instructionFactory) {
        if (finalAnswerRequested || !LLMMCPInteractionCoverage.hasRequiredInteractionCoverage(scenario.getRequiredToolNames(), artifacts.getInteractionTrace())) {
            return finalAnswerRequested;
        }
        if (LLMMCPNextActions.hasPendingImmediateNextAction(artifacts.getInteractionTrace())) {
            return false;
        }
        if (scenario.getRequiredToolNames().contains("database_gateway_execute_query") && !hasExpectedExecuteQuery(scenario.getExpectedAnswer(), artifacts.getInteractionTrace())) {
            messages.add(LLMChatMessage.user(instructionFactory.createExpectedQueryInstruction(scenario.getExpectedAnswer())));
            return false;
        }
        messages.add(LLMChatMessage.user(instructionFactory.createFinalAnswerInstruction(scenario, artifacts.getInteractionTrace())));
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
    
    private LLMChatCompletion completeTurn(final LLME2EScenario scenario, final List<LLMChatMessage> messages, final LLMMCPConversationArtifacts artifacts,
                                           final boolean finalAnswerRequested, final List<String> turnToolNames, final LLMMCPConversationInstructionFactory instructionFactory,
                                           final LLMMCPConversationTurnPlanner turnPlanner) throws IOException, InterruptedException {
        String toolChoice = turnPlanner.createToolChoice(scenario, artifacts.getInteractionTrace(), finalAnswerRequested);
        LLMChatCompletion result = llmChatClient.complete(finalAnswerRequested ? instructionFactory.createFinalAnswerMessages(scenario, artifacts.getInteractionTrace()) : messages,
                finalAnswerRequested ? List.of() : toolDefinitionFactory.create(turnToolNames),
                toolChoice, finalAnswerRequested);
        artifacts.addRawModelOutput(result.getRawResponse());
        return result;
    }
    
    private Optional<LLME2EArtifactBundle> processToolCallCompletion(final LLME2EScenario scenario, final LLMChatCompletion completion,
                                                                     final List<LLMChatMessage> messages, final LLMMCPConversationArtifacts artifacts,
                                                                     final LLMMCPConversationInstructionFactory instructionFactory,
                                                                     final LLMMCPConversationTurnPlanner turnPlanner) throws InterruptedException {
        if (completion.getToolCalls().isEmpty()) {
            return Optional.empty();
        }
        messages.add(LLMChatMessage.assistant(completion.getContent(), completion.getToolCalls()));
        for (LLMToolCall each : completion.getToolCalls()) {
            List<String> availableToolNames = resolveAvailableToolNames(scenario, artifacts, turnPlanner);
            if (!availableToolNames.isEmpty() && !availableToolNames.contains(each.getName())) {
                addUnavailableToolCorrection(scenario, each, messages, availableToolNames);
                return Optional.empty();
            }
            Optional<LLME2EArtifactBundle> result = processToolCall(scenario, each, messages, artifacts, availableToolNames);
            if (result.isPresent()) {
                return result;
            }
        }
        String traceDrivenInstruction = instructionFactory.createTraceDrivenInstruction(scenario, artifacts.getInteractionTrace());
        if (!traceDrivenInstruction.isEmpty()) {
            messages.add(LLMChatMessage.user(traceDrivenInstruction));
        }
        return Optional.empty();
    }
    
    private List<String> resolveAvailableToolNames(final LLME2EScenario scenario, final LLMMCPConversationArtifacts artifacts,
                                                   final LLMMCPConversationTurnPlanner turnPlanner) {
        List<String> result = turnPlanner.createImmediateNextActionToolNames(artifacts.getInteractionTrace());
        return result.isEmpty() && LLMMCPNextActions.hasSideEffectExecutionNextAction(artifacts.getInteractionTrace())
                ? turnPlanner.createTurnToolNames(scenario, artifacts.getInteractionTrace())
                : result;
    }
    
    private void addUnavailableToolCorrection(final LLME2EScenario scenario, final LLMToolCall toolCall, final List<LLMChatMessage> messages,
                                              final List<String> availableToolNames) {
        messages.add(LLMChatMessage.tool(toolCall.getId(), JsonUtils.toJsonString(createUnavailableToolResponse(scenario, availableToolNames))));
        messages.add(LLMChatMessage.user(String.format(Locale.ENGLISH,
                "The previous response requested `%s`, but that MCP tool is not available in this turn. Available MCP tools for this turn: %s. "
                        + "Do not call tools outside this list.%s",
                toolCall.getName(), String.join(", ", availableToolNames), createExpectedQueryInstruction(scenario, availableToolNames))));
    }
    
    private Map<String, Object> createUnavailableToolResponse(final LLME2EScenario scenario, final List<String> availableToolNames) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("response_mode", "tool_call_rejected");
        result.put("reason", "tool_not_available_in_current_turn");
        result.put("available_tools", availableToolNames);
        if (availableToolNames.contains("database_gateway_execute_query")) {
            LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
            result.put("next_action", Map.of("tool_name", "database_gateway_execute_query", "arguments",
                    Map.of("database", expectedAnswer.getDatabase(), "schema", expectedAnswer.getSchema(), "sql", expectedAnswer.getQuery())));
        }
        return result;
    }
    
    private String createExpectedQueryInstruction(final LLME2EScenario scenario, final List<String> availableToolNames) {
        if (!availableToolNames.contains("database_gateway_execute_query")) {
            return "";
        }
        LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
        return String.format(Locale.ENGLISH, " Call database_gateway_execute_query now with database `%s`, schema `%s`, and sql `%s`.",
                expectedAnswer.getDatabase(), expectedAnswer.getSchema(), expectedAnswer.getQuery());
    }
    
    private Optional<LLME2EArtifactBundle> processToolCall(final LLME2EScenario scenario, final LLMToolCall toolCall, final List<LLMChatMessage> messages,
                                                           final LLMMCPConversationArtifacts artifacts, final List<String> extraAllowedToolNames) throws InterruptedException {
        if (!scenario.getAllowedToolNames().contains(toolCall.getName()) && !extraAllowedToolNames.contains(toolCall.getName())) {
            artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", toolCall.getName(),
                    Map.of("rawArgumentsJson", toolCall.getArgumentsJson()), "unexpected_tool_requested"));
            return Optional.of(createFailureBundle(scenario, artifacts, "unexpected_tool_requested", "Model requested an unsupported tool."));
        }
        try {
            Map<String, Object> rawArguments = LLMMCPJsonValues.parseToolArguments(toolCall.getArgumentsJson());
            return processToolCall(scenario, toolCall, messages, artifacts, rawArguments);
        } catch (final IllegalArgumentException ex) {
            artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", toolCall.getName(),
                    Map.of("rawArgumentsJson", toolCall.getArgumentsJson()), "invalid_tool_arguments"));
            return Optional.of(createFailureBundle(scenario, artifacts, "invalid_tool_arguments", "Model returned invalid tool arguments JSON."));
        }
    }
    
    private Optional<LLME2EArtifactBundle> processToolCall(final LLME2EScenario scenario, final LLMToolCall toolCall, final List<LLMChatMessage> messages,
                                                           final LLMMCPConversationArtifacts artifacts, final Map<String, Object> arguments) throws InterruptedException {
        String toolName = toolCall.getName();
        Optional<LLME2EArtifactBundle> validationFailure = validateToolCall(scenario, toolName, arguments, artifacts);
        if (validationFailure.isPresent()) {
            return validationFailure;
        }
        long startTime = System.currentTimeMillis();
        Map<String, Object> response;
        try {
            response = actionExecutor.executeSafely(toolName, arguments);
        } catch (final IllegalArgumentException ex) {
            artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", toolName, arguments, "invalid_tool_arguments"));
            return Optional.of(createFailureBundle(scenario, artifacts, "invalid_tool_arguments", "Model provided invalid tool arguments."));
        }
        long latencyMillis = System.currentTimeMillis() - startTime;
        artifacts.addRuntimeLogLine("action=" + toolName + " args=" + JsonUtils.toJsonString(arguments));
        artifacts.addRuntimeLogLine("response=" + JsonUtils.toJsonString(response));
        String actionOrigin = MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN;
        artifacts.addInteractionTrace(traceRecordFactory.createTraceRecord(artifacts.nextSequence(), toolName, actionOrigin, arguments, response, latencyMillis));
        messages.add(LLMChatMessage.tool(toolCall.getId(), LLMMCPModelFacingToolResponseFormatter.format(response)));
        return Optional.empty();
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
    
    private LLME2EArtifactBundle handleFinalAnswerCompletion(final LLME2EScenario scenario, final LLMChatCompletion completion, final LLMMCPConversationArtifacts artifacts) {
        artifacts.setFinalAnswerJson(completion.getContent());
        try {
            LLMStructuredAnswer actualAnswer = LLMStructuredAnswer.fromJson(artifacts.getFinalAnswerJson());
            return artifacts.createArtifactBundle(scenario, finalAnswerValidator.validateSafely(scenario, actualAnswer, artifacts.getInteractionTrace()));
        } catch (final IllegalArgumentException ex) {
            return createFailureBundle(scenario, artifacts, "invalid_final_json", "Model did not return a valid final JSON payload.");
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
    
}
