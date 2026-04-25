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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandlerRegistry;
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * LLM MCP conversation runner.
 */
@RequiredArgsConstructor
public final class LLMMCPConversationRunner {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private final int maxTurns;
    
    private final LLMChatModelClient llmChatClient;
    
    private final MCPInteractionClient mcpInteractionClient;
    
    /**
     * Run.
     *
     * @param scenario scenario
     * @return LLM E2E artifact bundle
     */
    public LLME2EArtifactBundle run(final LLME2EScenario scenario) {
        final List<LLMChatMessage> messages = createInitialMessages(scenario);
        final ConversationArtifacts artifacts = new ConversationArtifacts();
        try {
            llmChatClient.waitUntilReady();
            openInteractionClient();
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
    
    private boolean requestFinalAnswerIfReady(final LLME2EScenario scenario, final List<LLMChatMessage> messages, final ConversationArtifacts artifacts,
                                              final boolean finalAnswerRequested) {
        if (finalAnswerRequested || !hasRequiredInteractionCoverage(scenario.getRequiredToolNames(), artifacts.getInteractionTrace())) {
            return finalAnswerRequested;
        }
        messages.add(LLMChatMessage.user(createFinalAnswerInstruction(scenario)));
        return true;
    }
    
    private LLMChatCompletion completeTurn(final LLME2EScenario scenario, final List<LLMChatMessage> messages, final ConversationArtifacts artifacts,
                                           final boolean finalAnswerRequested) throws IOException, InterruptedException {
        final String toolChoice = finalAnswerRequested ? "none" : artifacts.getInteractionTrace().isEmpty() ? "required" : "auto";
        final LLMChatCompletion result = llmChatClient.complete(messages,
                finalAnswerRequested ? List.of() : createToolDefinitions(scenario.getAllowedToolNames()),
                toolChoice, finalAnswerRequested);
        artifacts.addRawModelOutput(result.getRawResponse());
        return result;
    }
    
    private LLME2EArtifactBundle processToolCallCompletion(final LLME2EScenario scenario, final LLMChatCompletion completion, final List<LLMChatMessage> messages,
                                                           final ConversationArtifacts artifacts) throws InterruptedException {
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
                                                 final ConversationArtifacts artifacts) throws InterruptedException {
        if (!scenario.getAllowedToolNames().contains(toolCall.getName())) {
            artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", toolCall.getName(),
                    Map.of("rawArgumentsJson", toolCall.getArgumentsJson()), "unexpected_tool_requested"));
            return createFailureBundle(scenario, artifacts, "unexpected_tool_requested", "Model requested an unsupported tool.");
        }
        final Map<String, Object> arguments;
        try {
            arguments = parseToolArguments(toolCall.getArgumentsJson());
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
        final Map<String, Object> response = executeActionSafely(toolCall.getName(), arguments);
        final long latencyMillis = System.currentTimeMillis() - startTime;
        artifacts.addRuntimeLogLine("action=" + toolCall.getName() + " args=" + JsonUtils.toJsonString(arguments));
        artifacts.addRuntimeLogLine("response=" + JsonUtils.toJsonString(response));
        artifacts.addInteractionTrace(createTraceRecord(artifacts.nextSequence(), toolCall.getName(), arguments, response, latencyMillis));
        messages.add(LLMChatMessage.tool(toolCall.getId(), JsonUtils.toJsonString(response)));
        return null;
    }
    
    private LLME2EArtifactBundle validateToolCall(final LLME2EScenario scenario, final LLMToolCall toolCall, final Map<String, Object> arguments,
                                                  final ConversationArtifacts artifacts) {
        if (MCPInteractionActionNames.READ_RESOURCE.equals(toolCall.getName()) && Objects.toString(arguments.get("uri"), "").trim().isEmpty()) {
            artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(),
                    MCPInteractionActionNames.RESOURCE_READ_KIND, toolCall.getName(), arguments, "invalid_tool_arguments"));
            return createFailureBundle(scenario, artifacts, "invalid_tool_arguments", "Model returned an empty resource URI.");
        }
        if ("execute_query".equals(toolCall.getName()) && !isReadOnlyQuery(arguments)) {
            artifacts.addInteractionTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", toolCall.getName(), arguments, "unsafe_sql_attempted"));
            return createFailureBundle(scenario, artifacts, "unsafe_sql_attempted", "Model attempted a non-read-only SQL statement.");
        }
        return null;
    }
    
    private FinalAnswerHandlingResult handleFinalAnswerCompletion(final LLME2EScenario scenario, final LLMChatCompletion completion, final List<LLMChatMessage> messages,
                                                                  final ConversationArtifacts artifacts, final int finalAnswerAttempts) {
        final int actualAttempts = finalAnswerAttempts + 1;
        artifacts.setFinalAnswerJson(completion.getContent());
        try {
            final LLMStructuredAnswer actualAnswer = LLMStructuredAnswer.fromJson(artifacts.getFinalAnswerJson());
            return FinalAnswerHandlingResult.complete(actualAttempts,
                    artifacts.createArtifactBundle(scenario, validateFinalAnswerSafely(scenario, actualAnswer, artifacts.getInteractionTrace())));
        } catch (final IllegalArgumentException ex) {
            if (1 >= actualAttempts) {
                messages.add(LLMChatMessage.user("Return valid JSON only. Do not include markdown or explanation."));
                return FinalAnswerHandlingResult.retry(actualAttempts);
            }
            return FinalAnswerHandlingResult.complete(actualAttempts,
                    createFailureBundle(scenario, artifacts, "invalid_final_json", "Model did not return a valid final JSON payload."));
        }
    }
    
    private LLME2EArtifactBundle createFailureBundle(final LLME2EScenario scenario, final ConversationArtifacts artifacts, final String failureType, final String message) {
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
            // Ignore tool-client close failures in test cleanup.
        }
    }
    
    private boolean hasRequiredInteractionCoverage(final Collection<String> requiredActionNames, final Collection<MCPInteractionTraceRecord> interactionTrace) {
        Set<String> result = new LinkedHashSet<>();
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (!isSuccessfulInteraction(each)) {
                continue;
            }
            result.add(each.getTargetName());
        }
        return result.containsAll(requiredActionNames);
    }
    
    private boolean isSuccessfulInteraction(final MCPInteractionTraceRecord interactionTraceRecord) {
        return interactionTraceRecord.isValid() && !interactionTraceRecord.getStructuredContent().containsKey("error_code");
    }
    
    private boolean isReadOnlyQuery(final Map<String, Object> arguments) {
        String sql = Objects.toString(arguments.get("sql"), "").trim();
        String normalizedSql = sql.toUpperCase(Locale.ENGLISH);
        return normalizedSql.startsWith("SELECT") && (6 == normalizedSql.length() || Character.isWhitespace(normalizedSql.charAt(6))) && !normalizedSql.contains(";");
    }
    
    private String createFinalAnswerInstruction(final LLME2EScenario scenario) {
        final LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
        final String prompt = "Return JSON only with keys database, schema, table, query, totalOrders, interactionSequence. "
                + "The target table is `%s`, the final interactionSequence must match the observed interaction trace exactly, "
                + "and the required tools are `%s`.";
        return String.format(Locale.ENGLISH,
                prompt,
                expectedAnswer.getTable(), String.join(", ", scenario.getRequiredToolNames()));
    }
    
    private LLME2EAssertionReport validateFinalAnswer(final LLME2EScenario scenario, final LLMStructuredAnswer actualAnswer,
                                                      final List<MCPInteractionTraceRecord> interactionTrace) {
        final LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
        if (!hasRequiredInteractionCoverage(scenario.getRequiredToolNames(), interactionTrace)) {
            return LLME2EAssertionReport.failure("missing_required_tool_coverage", "Tool trace does not contain the required tools.");
        }
        if (!expectedAnswer.getDatabase().equals(actualAnswer.getDatabase())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer database does not match expected database.");
        }
        if (!expectedAnswer.getSchema().equals(actualAnswer.getSchema())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer schema does not match expected schema.");
        }
        if (!expectedAnswer.getTable().equals(actualAnswer.getTable())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer table does not match expected table.");
        }
        if (!expectedAnswer.getNormalizedQuery().equals(actualAnswer.getNormalizedQuery())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer query does not match expected query.");
        }
        int actualTotalOrders = getActualTotalOrders(interactionTrace);
        if (actualTotalOrders != actualAnswer.getTotalOrders() || expectedAnswer.getTotalOrders() != actualAnswer.getTotalOrders()) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer totalOrders does not match the execute_query result.");
        }
        List<String> actualInteractionSequence = new LinkedList<>();
        for (MCPInteractionTraceRecord each : interactionTrace) {
            actualInteractionSequence.add(each.getTargetName());
        }
        if (!actualInteractionSequence.equals(actualAnswer.getInteractionSequence())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer interactionSequence does not match the observed interaction trace.");
        }
        return LLME2EAssertionReport.isSuccess("LLM MCP smoke passed.");
    }
    
    private LLME2EAssertionReport validateFinalAnswerSafely(final LLME2EScenario scenario, final LLMStructuredAnswer actualAnswer,
                                                            final List<MCPInteractionTraceRecord> interactionTrace) {
        try {
            return validateFinalAnswer(scenario, actualAnswer, interactionTrace);
        } catch (final IllegalArgumentException ex) {
            return LLME2EAssertionReport.failure("unexpected_query_result", ex.getMessage());
        }
    }
    
    private int getActualTotalOrders(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (int index = interactionTrace.size() - 1; index >= 0; index--) {
            final MCPInteractionTraceRecord each = interactionTrace.get(index);
            if (!"execute_query".equals(each.getTargetName())) {
                continue;
            }
            final Object resultKind = each.getStructuredContent().get("result_kind");
            if (!"result_set".equals(Objects.toString(resultKind, ""))) {
                break;
            }
            final List<List<Object>> rows = castToRows(each.getStructuredContent().get("rows"));
            if (!rows.isEmpty() && !rows.get(0).isEmpty()) {
                final Object value = rows.get(0).get(0);
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
                try {
                    return Integer.parseInt(Objects.toString(value, "").trim());
                } catch (final NumberFormatException ex) {
                    throw new IllegalArgumentException("The execute_query trace does not contain a numeric result set.", ex);
                }
            }
        }
        throw new IllegalArgumentException("The execute_query trace does not contain a numeric result set.");
    }
    
    private List<List<Object>> castToRows(final Object value) {
        return JsonUtils.fromJsonString(JsonUtils.toJsonString(value), new TypeReference<>() {
        });
    }
    
    private Map<String, Object> parseToolArguments(final String argumentsJson) {
        try {
            return OBJECT_MAPPER.readValue(argumentsJson, new TypeReference<>() {
            });
        } catch (final JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid tool arguments JSON.", ex);
        }
    }
    
    private List<Map<String, Object>> createToolDefinitions(final Collection<String> allowedToolNames) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (String each : allowedToolNames) {
            if (MCPInteractionActionNames.LIST_RESOURCES.equals(each)) {
                result.add(Map.of("type", "function", "function", Map.of(
                        "name", MCPInteractionActionNames.LIST_RESOURCES,
                        "description", "Bridge to MCP resources/list for application-driven context discovery.",
                        "parameters", createEmptyObjectSchema())));
                continue;
            }
            if (MCPInteractionActionNames.READ_RESOURCE.equals(each)) {
                result.add(Map.of("type", "function", "function", Map.of(
                        "name", MCPInteractionActionNames.READ_RESOURCE,
                        "description", "Bridge to MCP resources/read for application-driven context retrieval.",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of("uri", Map.of("type", "string", "description", "Resource URI to read.")),
                                "required", List.of("uri"),
                                "additionalProperties", false))));
                continue;
            }
            MCPToolDescriptor toolDescriptor = getToolDescriptor(each);
            result.add(Map.of("type", "function", "function", Map.of(
                    "name", toolDescriptor.getName(),
                    "description", toolDescriptor.getDescription(),
                    "parameters", createParameterSchema(toolDescriptor.getFields()))));
        }
        return result;
    }
    
    private MCPToolDescriptor getToolDescriptor(final String toolName) {
        for (MCPToolDescriptor each : ToolHandlerRegistry.getSupportedToolDescriptors()) {
            if (toolName.equals(each.getName())) {
                return each;
            }
        }
        throw new IllegalArgumentException("Unsupported tool descriptor: " + toolName);
    }
    
    private Map<String, Object> executeActionSafely(final String actionName, final Map<String, Object> arguments) throws InterruptedException {
        try {
            return executeAction(actionName, arguments);
        } catch (final IOException | IllegalStateException ex) {
            throw new IllegalStateException(String.format("MCP action `%s` failed: %s", actionName, ex.getMessage()), ex);
        }
    }
    
    private Map<String, Object> executeAction(final String actionName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        if (MCPInteractionActionNames.LIST_RESOURCES.equals(actionName)) {
            return mcpInteractionClient.listResources();
        }
        if (MCPInteractionActionNames.READ_RESOURCE.equals(actionName)) {
            String resourceUri = Objects.toString(arguments.get("uri"), "").trim();
            if (resourceUri.isEmpty()) {
                throw new IllegalArgumentException("Resource URI is required.");
            }
            return mcpInteractionClient.readResource(resourceUri);
        }
        return mcpInteractionClient.call(actionName, arguments);
    }
    
    private MCPInteractionTraceRecord createTraceRecord(final int sequence, final String actionName, final Map<String, Object> arguments,
                                                        final Map<String, Object> structuredContent, final long latencyMillis) {
        if (MCPInteractionActionNames.LIST_RESOURCES.equals(actionName)) {
            return MCPInteractionTraceRecord.createResourceList(sequence, structuredContent, latencyMillis);
        }
        if (MCPInteractionActionNames.READ_RESOURCE.equals(actionName)) {
            return MCPInteractionTraceRecord.createResourceRead(sequence, Objects.toString(arguments.get("uri"), "").trim(), structuredContent, latencyMillis);
        }
        return new MCPInteractionTraceRecord(sequence, "tool_call", actionName, arguments, structuredContent, true, latencyMillis);
    }
    
    private Map<String, Object> createEmptyObjectSchema() {
        return Map.of("type", "object", "properties", Map.of(), "additionalProperties", false);
    }
    
    private Map<String, Object> createParameterSchema(final List<MCPToolFieldDefinition> fields) {
        Map<String, Object> properties = new LinkedHashMap<>(fields.size(), 1F);
        List<String> required = new LinkedList<>();
        for (MCPToolFieldDefinition each : fields) {
            properties.put(each.getName(), createValueSchema(each.getValueDefinition()));
            if (each.isRequired()) {
                required.add(each.getName());
            }
        }
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", "object");
        result.put("properties", properties);
        result.put("additionalProperties", true);
        if (!required.isEmpty()) {
            result.put("required", required);
        }
        return result;
    }
    
    private Map<String, Object> createValueSchema(final MCPToolValueDefinition valueDefinition) {
        return valueDefinition.toSchemaFragment();
    }
    
    private record FinalAnswerHandlingResult(int finalAnswerAttempts, boolean retryRequested, LLME2EArtifactBundle artifactBundle) {
        
        private static FinalAnswerHandlingResult retry(final int finalAnswerAttempts) {
            return new FinalAnswerHandlingResult(finalAnswerAttempts, true, null);
        }
        
        private static FinalAnswerHandlingResult complete(final int finalAnswerAttempts, final LLME2EArtifactBundle artifactBundle) {
            return new FinalAnswerHandlingResult(finalAnswerAttempts, false, artifactBundle);
        }
    }
    
    private static final class ConversationArtifacts {
        
        private final List<String> rawModelOutputs = new LinkedList<>();
        
        private final List<MCPInteractionTraceRecord> interactionTrace = new LinkedList<>();
        
        private final List<String> mcpRuntimeLogLines = new LinkedList<>();
        
        private String finalAnswerJson = "";
        
        private void addRawModelOutput(final String rawModelOutput) {
            rawModelOutputs.add(rawModelOutput);
        }
        
        private void addInteractionTrace(final MCPInteractionTraceRecord traceRecord) {
            interactionTrace.add(traceRecord);
        }
        
        private void addRuntimeLogLine(final String runtimeLogLine) {
            mcpRuntimeLogLines.add(runtimeLogLine);
        }
        
        private List<MCPInteractionTraceRecord> getInteractionTrace() {
            return interactionTrace;
        }
        
        private int nextSequence() {
            return interactionTrace.size() + 1;
        }
        
        private String getFinalAnswerJson() {
            return finalAnswerJson;
        }
        
        private void setFinalAnswerJson(final String finalAnswerJson) {
            this.finalAnswerJson = finalAnswerJson;
        }
        
        private LLME2EArtifactBundle createArtifactBundle(final LLME2EScenario scenario, final LLME2EAssertionReport assertionReport) {
            return new LLME2EArtifactBundle(scenario.getScenarioId(), scenario.getSystemPrompt(), scenario.getUserPrompt(), finalAnswerJson,
                    List.copyOf(rawModelOutputs), List.copyOf(interactionTrace), List.copyOf(mcpRuntimeLogLines), assertionReport);
        }
    }
}
