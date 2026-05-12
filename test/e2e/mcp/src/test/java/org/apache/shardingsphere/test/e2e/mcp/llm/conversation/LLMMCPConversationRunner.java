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
        if (scenario.getRequiredToolNames().contains("execute_query") && !hasExpectedExecuteQuery(scenario.getExpectedAnswer(), artifacts.getInteractionTrace())) {
            messages.add(LLMChatMessage.user(createExpectedQueryInstruction(scenario.getExpectedAnswer())));
            return false;
        }
        messages.add(LLMChatMessage.user(createFinalAnswerInstruction(scenario, artifacts.getInteractionTrace())));
        return true;
    }
    
    private boolean hasExpectedExecuteQuery(final LLMStructuredAnswer expectedAnswer, final List<MCPInteractionTraceRecord> interactionTrace) {
        for (int index = interactionTrace.size() - 1; index >= 0; index--) {
            MCPInteractionTraceRecord each = interactionTrace.get(index);
            if (!"execute_query".equals(each.getTargetName())) {
                continue;
            }
            return each.isValid()
                    && expectedAnswer.getDatabase().equals(Objects.toString(each.getArguments().get("database"), ""))
                    && expectedAnswer.getSchema().equals(Objects.toString(each.getArguments().get("schema"), ""))
                    && normalizeComparableQuery(expectedAnswer.getQuery()).equals(normalizeComparableQuery(Objects.toString(each.getArguments().get("sql"), "")));
        }
        return false;
    }
    
    private String createExpectedQueryInstruction(final LLMStructuredAnswer expectedAnswer) {
        return String.format(Locale.ENGLISH,
                "Required MCP tool coverage is present, but the latest successful execute_query did not use database `%s`, schema `%s`, and query `%s`. "
                        + "Call execute_query now with exactly those arguments before returning the final JSON.",
                expectedAnswer.getDatabase(), expectedAnswer.getSchema(), expectedAnswer.getQuery());
    }
    
    private LLMChatCompletion completeTurn(final LLME2EScenario scenario, final List<LLMChatMessage> messages, final LLMMCPConversationArtifacts artifacts,
                                           final boolean finalAnswerRequested) throws IOException, InterruptedException {
        final String toolChoice = createToolChoice(scenario, artifacts, finalAnswerRequested);
        final LLMChatCompletion result = llmChatClient.complete(messages,
                finalAnswerRequested ? List.of() : toolDefinitionFactory.create(scenario.getAllowedToolNames()),
                toolChoice, finalAnswerRequested);
        artifacts.addRawModelOutput(result.getRawResponse());
        return result;
    }
    
    private String createToolChoice(final LLME2EScenario scenario, final LLMMCPConversationArtifacts artifacts, final boolean finalAnswerRequested) {
        if (finalAnswerRequested) {
            return "none";
        }
        return LLMMCPInteractionCoverage.hasRequiredInteractionCoverage(scenario.getRequiredToolNames(), artifacts.getInteractionTrace()) ? "auto" : "required";
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
        addTraceDrivenInstruction(scenario, messages, artifacts.getInteractionTrace());
        return null;
    }
    
    private LLME2EArtifactBundle processToolCall(final LLME2EScenario scenario, final LLMToolCall toolCall, final List<LLMChatMessage> messages,
                                                 final LLMMCPConversationArtifacts artifacts) throws InterruptedException {
        if (!scenario.getAllowedToolNames().contains(toolCall.getName())) {
            artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", toolCall.getName(),
                    Map.of("rawArgumentsJson", toolCall.getArgumentsJson()), "unexpected_tool_requested"));
            return createFailureBundle(scenario, artifacts, "unexpected_tool_requested", "Model requested an unsupported tool.");
        }
        Map<String, Object> rawArguments;
        Map<String, Object> arguments;
        try {
            rawArguments = LLMMCPJsonValues.parseToolArguments(toolCall.getArgumentsJson());
            arguments = normalizeToolArguments(scenario, toolCall.getName(), rawArguments, artifacts.getInteractionTrace());
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
        String actionOrigin = rawArguments.equals(arguments) ? MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN : MCPInteractionTraceRecord.HARNESS_ARGUMENT_NORMALIZATION_ORIGIN;
        artifacts.addInteractionTrace(createTraceRecord(artifacts.nextSequence(), toolCall.getName(), actionOrigin, arguments, response, latencyMillis));
        messages.add(LLMChatMessage.tool(toolCall.getId(), JsonUtils.toJsonString(response)));
        return null;
    }
    
    private void addTraceDrivenInstruction(final LLME2EScenario scenario, final List<LLMChatMessage> messages, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (interactionTrace.isEmpty()) {
            return;
        }
        final String immediateNextActionInstruction = createImmediateNextActionInstruction(interactionTrace.getLast());
        if (!immediateNextActionInstruction.isEmpty()) {
            messages.add(LLMChatMessage.user(immediateNextActionInstruction));
            return;
        }
        final String resourceReadInstruction = createResourceReadInstruction(scenario, interactionTrace);
        if (!resourceReadInstruction.isEmpty()) {
            messages.add(LLMChatMessage.user(resourceReadInstruction));
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
    
    private String createMachineNextActionInstruction(final Map<?, ?> action) {
        if (Boolean.TRUE.equals(action.get("requires_user_approval"))) {
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
                || LLMMCPInteractionCoverage.hasRequiredInteractionCoverage(List.of(MCPInteractionActionNames.READ_RESOURCE), interactionTrace)
                || !shouldPromptExactResourceRead(interactionTrace.getLast())) {
            return "";
        }
        String resourceUri = findPromptResourceUri(scenario);
        if (resourceUri.isEmpty()) {
            resourceUri = createExpectedTableResourceUri(scenario.getExpectedAnswer());
        }
        return resourceUri.isEmpty()
                ? ""
                : String.format(Locale.ENGLISH,
                        "The remaining required resource action is mcp_read_resource. Use exactly `%s` as uri; do not copy parameter schema or placeholder text as uri.", resourceUri);
    }
    
    private boolean shouldPromptExactResourceRead(final MCPInteractionTraceRecord traceRecord) {
        return MCPInteractionActionNames.LIST_RESOURCES.equals(traceRecord.getTargetName())
                || MCPInteractionActionNames.READ_RESOURCE.equals(traceRecord.getTargetName()) && traceRecord.getStructuredContent().containsKey("error_code");
    }
    
    private String findPromptResourceUri(final LLME2EScenario scenario) {
        Matcher matcher = RESOURCE_URI_PATTERN.matcher(scenario.getUserPrompt());
        return matcher.find() ? trimResourceUri(matcher.group()) : "";
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
        result = normalizeWorkflowPlanIdArgument(toolName, result, interactionTrace);
        return normalizeCompletionArguments(toolName, result, interactionTrace);
    }
    
    private Map<String, Object> normalizeResourceUriArgument(final LLME2EScenario scenario, final String toolName, final Map<String, Object> args) {
        String resourceUriArgument = Objects.toString(args.get("uri"), "").trim();
        if (!MCPInteractionActionNames.READ_RESOURCE.equals(toolName) || resourceUriArgument.isEmpty() || resourceUriArgument.startsWith("shardingsphere://")) {
            return args;
        }
        String resourceUri = findPromptResourceUri(scenario);
        if (resourceUri.isEmpty()) {
            resourceUri = createExpectedTableResourceUri(scenario.getExpectedAnswer());
        }
        if (resourceUri.isEmpty()) {
            return args;
        }
        Map<String, Object> result = new LinkedHashMap<>(args);
        result.put("uri", resourceUri);
        return result;
    }
    
    private Map<String, Object> normalizeWorkflowPlanIdArgument(final String toolName, final Map<String, Object> args, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!("apply_workflow".equals(toolName) || "validate_workflow".equals(toolName)) || !isPlanIdPlaceholder(args.get("plan_id"))) {
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
        return "plan_id".equals(planId) || "{plan_id}".equals(planId) || "<plan_id>".equals(planId);
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
    
    private String createFinalAnswerInstruction(final LLME2EScenario scenario, final List<MCPInteractionTraceRecord> interactionTrace) {
        final LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
        final String interactionSequence = JsonUtils.toJsonString(createComparableInteractionSequence(interactionTrace));
        final String prompt = "Return JSON only with keys database, schema, table, query, totalOrders, interactionSequence. "
                + "Use database `%s`, schema `%s`, table `%s`, and query `%s`; set totalOrders from the latest successful execute_query result. "
                + "Set interactionSequence exactly to this JSON array: %s. "
                + "Do not add inferred, expected, available, or failed MCP action names. Required tools are `%s`.";
        return String.format(Locale.ENGLISH,
                prompt,
                expectedAnswer.getDatabase(), expectedAnswer.getSchema(), expectedAnswer.getTable(), expectedAnswer.getQuery(), interactionSequence,
                String.join(", ", scenario.getRequiredToolNames()));
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
        final String previewInstruction = missingToolNames.contains("execute_update")
                ? String.format(Locale.ENGLISH,
                        " For execute_update, set database `%s`, schema `%s`, execution_mode=preview, and keep the side-effecting SQL unchanged; do not use execution_mode=execute.",
                        expectedAnswer.getDatabase(), expectedAnswer.getSchema())
                : "";
        final String resourceInstruction = missingToolNames.contains(MCPInteractionActionNames.READ_RESOURCE)
                ? " For mcp_read_resource, use an exact shardingsphere:// URI from the user request or the latest tool response; do not invent abbreviated URI strings."
                : "";
        final String planningInstruction = hasMissingPlanningTool(missingToolNames)
                ? " For a new plan_* call, omit plan_id unless a previous MCP planning response returned an actual plan_id."
                : "";
        final String workflowPlanInstruction = createWorkflowPlanInstruction(missingToolNames, artifacts.getInteractionTrace());
        return String.format(Locale.ENGLISH,
                "Required MCP tool coverage is incomplete. Remaining required MCP tools: %s. "
                        + "Call one remaining tool as an actual MCP tool_call now; do not answer in text, do not write JSON, and do not write <tool_call> markup. "
                        + "If execute_query is remaining, set database `%s`, schema `%s`, and sql `%s`.%s%s%s%s",
                String.join(", ", missingToolNames), expectedAnswer.getDatabase(), expectedAnswer.getSchema(), expectedAnswer.getQuery(), previewInstruction, resourceInstruction,
                planningInstruction, workflowPlanInstruction);
    }
    
    private boolean hasMissingPlanningTool(final List<String> missingToolNames) {
        return missingToolNames.stream().anyMatch(each -> each.startsWith("plan_"));
    }
    
    private String createWorkflowPlanInstruction(final List<String> missingToolNames, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!missingToolNames.contains("apply_workflow") && !missingToolNames.contains("validate_workflow")) {
            return "";
        }
        String latestPlanId = findLatestPlanId(interactionTrace);
        return latestPlanId.isEmpty()
                ? " For apply_workflow or validate_workflow, use an actual plan_id returned by a successful planning tool call; do not use placeholder text `plan_id`."
                : String.format(Locale.ENGLISH,
                        " For apply_workflow or validate_workflow, set plan_id `%s`; do not use placeholder text `plan_id`.", latestPlanId);
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
