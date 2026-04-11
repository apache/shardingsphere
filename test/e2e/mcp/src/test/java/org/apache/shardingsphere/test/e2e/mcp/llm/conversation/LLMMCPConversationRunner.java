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
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandlerRegistry;
import org.apache.shardingsphere.test.e2e.mcp.llm.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.artifact.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.chat.LLMChatClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.chat.LLMChatCompletion;
import org.apache.shardingsphere.test.e2e.mcp.llm.chat.LLMChatMessage;
import org.apache.shardingsphere.test.e2e.mcp.llm.chat.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.chat.LLMToolCall;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.runtime.transport.MCPInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.runtime.transport.MCPInteractionResponse;
import org.apache.shardingsphere.test.e2e.mcp.runtime.transport.MCPInteractionTraceRecord;

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
public final class LLMMCPConversationRunner {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private static final String RESOURCE_LIST_BRIDGE_NAME = "mcp_list_resources";
    
    private static final String RESOURCE_READ_BRIDGE_NAME = "mcp_read_resource";
    
    private final int maxTurns;
    
    private final LLMChatClient llmChatClient;
    
    private final MCPInteractionClient mcpInteractionClient;
    
    public LLMMCPConversationRunner(final int maxTurns, final LLMChatModelClient llmChatClient, final MCPInteractionClient mcpInteractionClient) {
        this(maxTurns, (LLMChatClient) llmChatClient, mcpInteractionClient);
    }
    
    LLMMCPConversationRunner(final int maxTurns, final LLMChatClient llmChatClient, final MCPInteractionClient mcpInteractionClient) {
        this.maxTurns = maxTurns;
        this.llmChatClient = llmChatClient;
        this.mcpInteractionClient = mcpInteractionClient;
    }
    
    public LLME2EArtifactBundle run(final LLME2EScenario scenario) {
        final List<LLMChatMessage> messages = new LinkedList<>();
        final List<String> rawModelOutputs = new LinkedList<>();
        final List<MCPInteractionTraceRecord> interactionTrace = new LinkedList<>();
        final List<String> mcpRuntimeLogLines = new LinkedList<>();
        String finalAnswerJson = "";
        messages.add(LLMChatMessage.system(scenario.systemPrompt()));
        messages.add(LLMChatMessage.user(scenario.userPrompt()));
        try {
            llmChatClient.waitUntilReady();
            openInteractionClient();
            boolean finalAnswerRequested = false;
            int finalAnswerAttempts = 0;
            for (int turnIndex = 0; turnIndex < maxTurns; turnIndex++) {
                if (!finalAnswerRequested && hasRequiredInteractionCoverage(scenario.requiredToolNames(), interactionTrace)) {
                    messages.add(LLMChatMessage.user(createFinalAnswerInstruction(scenario)));
                    finalAnswerRequested = true;
                }
                final String toolChoice = finalAnswerRequested ? "none" : interactionTrace.isEmpty() ? "required" : "auto";
                final LLMChatCompletion completion = llmChatClient.complete(messages,
                        finalAnswerRequested ? List.of() : createToolDefinitions(scenario.allowedToolNames()),
                        toolChoice, finalAnswerRequested);
                rawModelOutputs.add(completion.rawResponse());
                if (!completion.toolCalls().isEmpty()) {
                    messages.add(LLMChatMessage.assistant(completion.content(), completion.toolCalls()));
                    for (LLMToolCall each : completion.toolCalls()) {
                        if (!scenario.allowedToolNames().contains(each.name())) {
                            interactionTrace.add(MCPInteractionTraceRecord.createInvalidAction(interactionTrace.size() + 1, "tool_call", each.name(),
                                    Map.of("rawArgumentsJson", each.argumentsJson()), "unexpected_tool_requested"));
                            return createArtifactBundle(scenario, rawModelOutputs, interactionTrace, mcpRuntimeLogLines, finalAnswerJson,
                                    LLME2EAssertionReport.failure("unexpected_tool_requested", "Model requested an unsupported tool."));
                        }
                        final Map<String, Object> arguments;
                        try {
                            arguments = parseToolArguments(each.argumentsJson());
                        } catch (final IllegalArgumentException ex) {
                            interactionTrace.add(MCPInteractionTraceRecord.createInvalidAction(interactionTrace.size() + 1, "tool_call", each.name(),
                                    Map.of("rawArgumentsJson", each.argumentsJson()), "invalid_tool_arguments"));
                            return createArtifactBundle(scenario, rawModelOutputs, interactionTrace, mcpRuntimeLogLines, finalAnswerJson,
                                    LLME2EAssertionReport.failure("invalid_tool_arguments", "Model returned invalid tool arguments JSON."));
                        }
                        if (RESOURCE_READ_BRIDGE_NAME.equals(each.name()) && Objects.toString(arguments.get("uri"), "").trim().isEmpty()) {
                            interactionTrace.add(MCPInteractionTraceRecord.createInvalidAction(interactionTrace.size() + 1, "resource_read", each.name(), arguments, "invalid_tool_arguments"));
                            return createArtifactBundle(scenario, rawModelOutputs, interactionTrace, mcpRuntimeLogLines, finalAnswerJson,
                                    LLME2EAssertionReport.failure("invalid_tool_arguments", "Model returned an empty resource URI."));
                        }
                        if ("execute_query".equals(each.name()) && !isReadOnlyQuery(arguments)) {
                            interactionTrace.add(MCPInteractionTraceRecord.createInvalidAction(interactionTrace.size() + 1, "tool_call", each.name(), arguments, "unsafe_sql_attempted"));
                            return createArtifactBundle(scenario, rawModelOutputs, interactionTrace, mcpRuntimeLogLines, finalAnswerJson,
                                    LLME2EAssertionReport.failure("unsafe_sql_attempted", "Model attempted a non-read-only SQL statement."));
                        }
                        long startTime = System.currentTimeMillis();
                        MCPInteractionResponse response = executeActionSafely(each.name(), arguments);
                        long latencyMillis = System.currentTimeMillis() - startTime;
                        mcpRuntimeLogLines.add("action=" + each.name() + " args=" + JsonUtils.toJsonString(arguments));
                        mcpRuntimeLogLines.add("response=" + JsonUtils.toJsonString(response.structuredContent()));
                        interactionTrace.add(createTraceRecord(interactionTrace.size() + 1, each.name(), arguments, response.structuredContent(), latencyMillis));
                        messages.add(LLMChatMessage.tool(each.id(), JsonUtils.toJsonString(response.structuredContent())));
                    }
                    continue;
                }
                if (!finalAnswerRequested) {
                    messages.add(LLMChatMessage.user("You must call the required MCP tools before answering. Do not guess. Use the tools now."));
                    continue;
                }
                finalAnswerJson = completion.content();
                finalAnswerAttempts++;
                try {
                    final LLMStructuredAnswer actualAnswer = LLMStructuredAnswer.fromJson(finalAnswerJson);
                    return createArtifactBundle(scenario, rawModelOutputs, interactionTrace, mcpRuntimeLogLines, finalAnswerJson,
                            validateFinalAnswerSafely(scenario, actualAnswer, interactionTrace));
                } catch (final IllegalArgumentException ex) {
                    if (1 >= finalAnswerAttempts) {
                        messages.add(LLMChatMessage.user("Return valid JSON only. Do not include markdown or explanation."));
                        continue;
                    }
                    return createArtifactBundle(scenario, rawModelOutputs, interactionTrace, mcpRuntimeLogLines, finalAnswerJson,
                            LLME2EAssertionReport.failure("invalid_final_json", "Model did not return a valid final JSON payload."));
                }
            }
            return createArtifactBundle(scenario, rawModelOutputs, interactionTrace, mcpRuntimeLogLines, finalAnswerJson,
                    LLME2EAssertionReport.failure("missing_required_tool_coverage", "Conversation exhausted turns before reaching the required tool coverage."));
        } catch (final IOException ex) {
            return createArtifactBundle(scenario, rawModelOutputs, interactionTrace, mcpRuntimeLogLines, finalAnswerJson,
                    LLME2EAssertionReport.failure("model_service_unavailable", "Model service request failed: " + ex.getMessage()));
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            return createArtifactBundle(scenario, rawModelOutputs, interactionTrace, mcpRuntimeLogLines, finalAnswerJson,
                    LLME2EAssertionReport.failure("model_service_unavailable", "Conversation was interrupted."));
        } catch (final IllegalStateException ex) {
            final String failureType = ex.getMessage().startsWith("MCP") || ex.getMessage().startsWith("Failed to initialize MCP")
                    ? "mcp_runtime_unavailable"
                    : "model_service_unavailable";
            return createArtifactBundle(scenario, rawModelOutputs, interactionTrace, mcpRuntimeLogLines, finalAnswerJson,
                    LLME2EAssertionReport.failure(failureType, ex.getMessage()));
        } finally {
            closeInteractionClient();
        }
    }
    
    private LLME2EArtifactBundle createArtifactBundle(final LLME2EScenario scenario, final List<String> rawModelOutputs, final List<MCPInteractionTraceRecord> interactionTrace,
                                                      final List<String> mcpRuntimeLogLines, final String finalAnswerJson, final LLME2EAssertionReport assertionReport) {
        return new LLME2EArtifactBundle(scenario.scenarioId(), scenario.systemPrompt(), scenario.userPrompt(), finalAnswerJson,
                List.copyOf(rawModelOutputs), List.copyOf(interactionTrace), List.copyOf(mcpRuntimeLogLines), assertionReport);
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
            result.add(each.targetName());
        }
        return result.containsAll(requiredActionNames);
    }
    
    private boolean isSuccessfulInteraction(final MCPInteractionTraceRecord interactionTraceRecord) {
        return interactionTraceRecord.valid() && !interactionTraceRecord.structuredContent().containsKey("error_code");
    }
    
    private boolean isReadOnlyQuery(final Map<String, Object> arguments) {
        String sql = Objects.toString(arguments.get("sql"), "").trim();
        String normalizedSql = sql.toUpperCase(Locale.ENGLISH);
        return normalizedSql.startsWith("SELECT") && (6 == normalizedSql.length() || Character.isWhitespace(normalizedSql.charAt(6))) && !normalizedSql.contains(";");
    }
    
    private String createFinalAnswerInstruction(final LLME2EScenario scenario) {
        final LLMStructuredAnswer expectedAnswer = scenario.expectedAnswer();
        final String prompt = "Return JSON only with keys database, schema, table, query, totalOrders, interactionSequence. "
                + "The target table is `%s`, the final interactionSequence must match the observed interaction trace exactly, "
                + "and the required tools are `%s`.";
        return String.format(Locale.ENGLISH,
                prompt,
                expectedAnswer.table(), String.join(", ", scenario.requiredToolNames()));
    }
    
    private LLME2EAssertionReport validateFinalAnswer(final LLME2EScenario scenario, final LLMStructuredAnswer actualAnswer,
                                                      final List<MCPInteractionTraceRecord> interactionTrace) {
        final LLMStructuredAnswer expectedAnswer = scenario.expectedAnswer();
        if (!hasRequiredInteractionCoverage(scenario.requiredToolNames(), interactionTrace)) {
            return LLME2EAssertionReport.failure("missing_required_tool_coverage", "Tool trace does not contain the required tools.");
        }
        if (!expectedAnswer.database().equals(actualAnswer.database())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer database does not match expected database.");
        }
        if (!expectedAnswer.schema().equals(actualAnswer.schema())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer schema does not match expected schema.");
        }
        if (!expectedAnswer.table().equals(actualAnswer.table())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer table does not match expected table.");
        }
        if (!expectedAnswer.getNormalizedQuery().equals(actualAnswer.getNormalizedQuery())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer query does not match expected query.");
        }
        int actualTotalOrders = getActualTotalOrders(interactionTrace);
        if (actualTotalOrders != actualAnswer.totalOrders() || expectedAnswer.totalOrders() != actualAnswer.totalOrders()) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer totalOrders does not match the execute_query result.");
        }
        List<String> actualInteractionSequence = new LinkedList<>();
        for (MCPInteractionTraceRecord each : interactionTrace) {
            actualInteractionSequence.add(each.targetName());
        }
        if (!actualInteractionSequence.equals(actualAnswer.interactionSequence())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer interactionSequence does not match the observed interaction trace.");
        }
        return LLME2EAssertionReport.success("LLM MCP smoke passed.");
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
            if (!"execute_query".equals(each.targetName())) {
                continue;
            }
            final Object resultKind = each.structuredContent().get("result_kind");
            if (!"result_set".equals(Objects.toString(resultKind, ""))) {
                break;
            }
            final List<List<Object>> rows = castToRows(each.structuredContent().get("rows"));
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
            if (RESOURCE_LIST_BRIDGE_NAME.equals(each)) {
                result.add(Map.of("type", "function", "function", Map.of(
                        "name", RESOURCE_LIST_BRIDGE_NAME,
                        "description", "Bridge to MCP resources/list for application-driven context discovery.",
                        "parameters", createEmptyObjectSchema())));
                continue;
            }
            if (RESOURCE_READ_BRIDGE_NAME.equals(each)) {
                result.add(Map.of("type", "function", "function", Map.of(
                        "name", RESOURCE_READ_BRIDGE_NAME,
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
    
    private MCPInteractionResponse executeActionSafely(final String actionName, final Map<String, Object> arguments) throws InterruptedException {
        try {
            return executeAction(actionName, arguments);
        } catch (final IOException | IllegalStateException ex) {
            throw new IllegalStateException(String.format("MCP action `%s` failed: %s", actionName, ex.getMessage()), ex);
        }
    }
    
    private MCPInteractionResponse executeAction(final String actionName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        if (RESOURCE_LIST_BRIDGE_NAME.equals(actionName)) {
            return mcpInteractionClient.listResources();
        }
        if (RESOURCE_READ_BRIDGE_NAME.equals(actionName)) {
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
        if (RESOURCE_LIST_BRIDGE_NAME.equals(actionName)) {
            return MCPInteractionTraceRecord.createResourceList(sequence, structuredContent, latencyMillis);
        }
        if (RESOURCE_READ_BRIDGE_NAME.equals(actionName)) {
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
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", switch (valueDefinition.getType()) {
            case STRING -> "string";
            case INTEGER -> "integer";
            case ARRAY -> "array";
        });
        result.put("description", valueDefinition.getDescription());
        if (MCPToolValueDefinition.Type.ARRAY == valueDefinition.getType()) {
            result.put("items", createValueSchema(valueDefinition.getItemDefinition()));
        }
        return result;
    }
}
