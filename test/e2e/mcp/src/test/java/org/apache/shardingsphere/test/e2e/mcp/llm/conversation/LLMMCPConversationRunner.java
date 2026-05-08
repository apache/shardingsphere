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
                final LLMChatCompletion completion = completeTurn(scenario, messages, artifacts, finalAnswerRequested);
                final LLME2EArtifactBundle toolCallFailure = processToolCallCompletion(scenario, completion, messages, artifacts);
                if (null != toolCallFailure) {
                    return toolCallFailure;
                }
                if (!completion.getToolCalls().isEmpty()) {
                    continue;
                }
                if (!finalAnswerRequested) {
                    messages.add(LLMChatMessage.user("You must call the required MCP tools before answering. Do not guess. Use the tools now."));
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
        messages.add(LLMChatMessage.user(createFinalAnswerInstruction(scenario)));
        return true;
    }

    private LLMChatCompletion completeTurn(final LLME2EScenario scenario, final List<LLMChatMessage> messages, final LLMMCPConversationArtifacts artifacts,
                                           final boolean finalAnswerRequested) throws IOException, InterruptedException {
        final String toolChoice = finalAnswerRequested ? "none" : artifacts.getInteractionTrace().isEmpty() ? "required" : "auto";
        final LLMChatCompletion result = llmChatClient.complete(messages,
                finalAnswerRequested ? List.of() : toolDefinitionFactory.create(scenario.getAllowedToolNames()),
                toolChoice, finalAnswerRequested);
        artifacts.addRawModelOutput(result.getRawResponse());
        return result;
    }

    private LLME2EArtifactBundle processToolCallCompletion(final LLME2EScenario scenario, final LLMChatCompletion completion,
                                                           final List<LLMChatMessage> messages, final LLMMCPConversationArtifacts artifacts) throws InterruptedException {
        if (completion.getToolCalls().isEmpty()) {
            return null;
        }
        messages.add(LLMChatMessage.assistant(completion.getContent(), completion.getToolCalls()));
        for (LLMToolCall each : completion.getToolCalls()) {
            final LLME2EArtifactBundle result = processToolCall(scenario, each, messages, artifacts);
            if (null != result) {
                return result;
            }
        }
        return null;
    }

    private LLME2EArtifactBundle processToolCall(final LLME2EScenario scenario, final LLMToolCall toolCall, final List<LLMChatMessage> messages,
                                                 final LLMMCPConversationArtifacts artifacts) throws InterruptedException {
        if (!scenario.getAllowedToolNames().contains(toolCall.getName())) {
            artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", toolCall.getName(),
                    Map.of("rawArgumentsJson", toolCall.getArgumentsJson()), "unexpected_tool_requested"));
            return createFailureBundle(scenario, artifacts, "unexpected_tool_requested", "Model requested an unsupported tool.");
        }
        final Map<String, Object> arguments;
        try {
            arguments = LLMMCPJsonValues.parseToolArguments(toolCall.getArgumentsJson());
        } catch (final IllegalArgumentException ex) {
            artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", toolCall.getName(),
                    Map.of("rawArgumentsJson", toolCall.getArgumentsJson()), "invalid_tool_arguments"));
            return createFailureBundle(scenario, artifacts, "invalid_tool_arguments", "Model returned invalid tool arguments JSON.");
        }
        final LLME2EArtifactBundle validationFailure = validateToolCall(scenario, toolCall, arguments, artifacts);
        if (null != validationFailure) {
            return validationFailure;
        }
        final long startTime = System.currentTimeMillis();
        final Map<String, Object> response = actionExecutor.executeSafely(toolCall.getName(), arguments);
        final long latencyMillis = System.currentTimeMillis() - startTime;
        artifacts.addRuntimeLogLine("action=" + toolCall.getName() + " args=" + JsonUtils.toJsonString(arguments));
        artifacts.addRuntimeLogLine("response=" + JsonUtils.toJsonString(response));
        artifacts.addInteractionTrace(createTraceRecord(artifacts.nextSequence(), toolCall.getName(), arguments, response, latencyMillis));
        messages.add(LLMChatMessage.tool(toolCall.getId(), JsonUtils.toJsonString(response)));
        return null;
    }

    private LLME2EArtifactBundle validateToolCall(final LLME2EScenario scenario, final LLMToolCall toolCall, final Map<String, Object> arguments,
                                                  final LLMMCPConversationArtifacts artifacts) {
        return safetyValidator.validate(toolCall.getName(), arguments)
                .map(each -> {
                    artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), each.traceKind(), toolCall.getName(), arguments,
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

    private String createFinalAnswerInstruction(final LLME2EScenario scenario) {
        final LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
        final String prompt = "Return JSON only with keys database, schema, table, query, totalOrders, interactionSequence. "
                + "Use database `%s`, schema `%s`, table `%s`, and query `%s`; set totalOrders from the latest successful execute_query result. "
                + "Set interactionSequence to a JSON array of observed MCP action names only, not objects; collapse consecutive repeated action names. Required tools are `%s`.";
        return String.format(Locale.ENGLISH,
                prompt,
                expectedAnswer.getDatabase(), expectedAnswer.getSchema(), expectedAnswer.getTable(), expectedAnswer.getQuery(),
                String.join(", ", scenario.getRequiredToolNames()));
    }

    private MCPInteractionTraceRecord createTraceRecord(final int sequence, final String actionName, final Map<String, Object> arguments,
                                                        final Map<String, Object> structuredContent, final long latencyMillis) {
        if (MCPInteractionActionNames.LIST_RESOURCES.equals(actionName)) {
            return MCPInteractionTraceRecord.createResourceList(sequence, structuredContent, latencyMillis);
        }
        if (MCPInteractionActionNames.READ_RESOURCE.equals(actionName)) {
            return MCPInteractionTraceRecord.createResourceRead(sequence, Objects.toString(arguments.get("uri"), "").trim(), structuredContent, latencyMillis);
        }
        if (MCPInteractionActionNames.LIST_PROMPTS.equals(actionName)) {
            return MCPInteractionTraceRecord.createPromptList(sequence, structuredContent, latencyMillis);
        }
        if (MCPInteractionActionNames.GET_PROMPT.equals(actionName)) {
            return MCPInteractionTraceRecord.createPromptGet(sequence, Objects.toString(arguments.get("name"), "").trim(),
                    LLMMCPJsonValues.castToMap(arguments.getOrDefault("arguments", Map.of())), structuredContent, latencyMillis);
        }
        if (MCPInteractionActionNames.COMPLETE.equals(actionName)) {
            return MCPInteractionTraceRecord.createCompletion(sequence, arguments, structuredContent, latencyMillis);
        }
        return new MCPInteractionTraceRecord(sequence, "tool_call", actionName, arguments, structuredContent, true, latencyMillis);
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
